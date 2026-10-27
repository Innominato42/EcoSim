package org.example.ui;

import org.example.engine.SimulationEngine;
import org.example.engine.SimulationObserver;
import org.example.engine.SimulationStats;
import org.example.model.*;
import org.example.parser.ConfigParser;
import org.example.parser.ConfigurationException;
import org.example.parser.SimulationConfig;
import org.example.parser.StateSerializer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Interfaccia grafica Swing completa per il livello Implementazione Avanzata (30 e Lode).
 */
public class SwingGuiView extends JFrame implements SimulationObserver {

    private SimulationConfig currentConfig;
    private SimulationEngine engine;
    private final Timer simulationTimer;

    private final GridCanvas gridCanvas;
    private final PopulationChartPanel chartPanel;
    private final JPanel legendPanel;
    private final JTextArea inspectorArea;
    private final JLabel turnLabel;
    private final JLabel eventBanner;
    private final JButton startButton;
    private final JButton pauseButton;
    private final JButton stepButton;
    private Position inspectedPosition;

    /**
     * Costruisce e inizializza la finestra grafica con una configurazione iniziale.
     *
     * @param initialConfig configurazione iniziale della simulazione
     */
    public SwingGuiView(SimulationConfig initialConfig) {
        super("EcoSim - Simulatore di Ecosistema 2D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        this.gridCanvas = new GridCanvas();
        this.chartPanel = new PopulationChartPanel();
        this.legendPanel = new JPanel();
        this.legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        this.inspectorArea = new JTextArea(9, 22);
        this.inspectorArea.setEditable(false);
        this.inspectorArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        this.turnLabel = new JLabel("Turno: 0");
        this.turnLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

        this.eventBanner = new JLabel("Nessun Evento Globale Attivo", SwingConstants.CENTER);
        this.eventBanner.setOpaque(true);
        this.eventBanner.setFont(new Font("SansSerif", Font.BOLD, 14));
        this.eventBanner.setBorder(new EmptyBorder(6, 10, 6, 10));

        this.startButton = new JButton("Avvia");
        this.pauseButton = new JButton("Pausa");
        this.stepButton = new JButton("Passo-Passo");

        this.simulationTimer = new Timer(300, e -> engine.step());

        buildMenuAndToolbar();
        buildMainLayout();

        loadSimulationConfig(initialConfig);
    }

    private void buildMenuAndToolbar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem loadConfigItem = new JMenuItem("Carica Configurazione...");
        loadConfigItem.addActionListener(e -> onLoadConfigurationDialog());

        JMenuItem loadStateItem = new JMenuItem("Carica Stato...");
        loadStateItem.addActionListener(e -> onLoadStateDialog());

        JMenuItem saveStateItem = new JMenuItem("Salva Stato...");
        saveStateItem.addActionListener(e -> onSaveStateDialog());

        fileMenu.add(loadConfigItem);
        fileMenu.add(loadStateItem);
        fileMenu.addSeparator();
        fileMenu.add(saveStateItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JPanel topContainer = new JPanel(new BorderLayout());
        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));

        startButton.addActionListener(e -> startSimulation());
        pauseButton.addActionListener(e -> pauseSimulation());
        pauseButton.setEnabled(false);
        stepButton.addActionListener(e -> {
            pauseSimulation();
            engine.step();
        });

        JButton loadCfgBtn = new JButton("Carica Config");
        loadCfgBtn.addActionListener(e -> onLoadConfigurationDialog());
        JButton loadStateBtn = new JButton("Carica Stato");
        loadStateBtn.addActionListener(e -> onLoadStateDialog());
        JButton saveStateBtn = new JButton("Salva Stato");
        saveStateBtn.addActionListener(e -> onSaveStateDialog());

        JSlider speedSlider = new JSlider(1, 20, 4);
        speedSlider.setPreferredSize(new Dimension(130, 36));
        speedSlider.setToolTipText("Velocità simulazione (turni al secondo)");
        speedSlider.addChangeListener(e -> {
            int turnsPerSec = speedSlider.getValue();
            simulationTimer.setDelay(1000 / turnsPerSec);
        });

        controlBar.add(turnLabel);
        controlBar.add(new JSeparator(SwingConstants.VERTICAL));
        controlBar.add(startButton);
        controlBar.add(pauseButton);
        controlBar.add(stepButton);
        controlBar.add(new JLabel("Velocità:"));
        controlBar.add(speedSlider);
        controlBar.add(loadCfgBtn);
        controlBar.add(loadStateBtn);
        controlBar.add(saveStateBtn);

        topContainer.add(controlBar, BorderLayout.NORTH);
        topContainer.add(eventBanner, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);
    }

    private void buildMainLayout() {
        add(gridCanvas, BorderLayout.CENTER);

        JPanel sidePanel = new JPanel(new BorderLayout(8, 8));
        sidePanel.setPreferredSize(new Dimension(290, 0));
        sidePanel.setBorder(new EmptyBorder(0, 4, 0, 8));

        JScrollPane legendScroll = new JScrollPane(legendPanel);
        legendScroll.setBorder(BorderFactory.createTitledBorder("Legenda Specie e Popolazione"));
        legendScroll.setPreferredSize(new Dimension(280, 230));

        JScrollPane inspectorScroll = new JScrollPane(inspectorArea);
        inspectorScroll.setBorder(BorderFactory.createTitledBorder("Ispezione Cella (Click sulla Griglia)"));

        sidePanel.add(legendScroll, BorderLayout.NORTH);
        sidePanel.add(inspectorScroll, BorderLayout.CENTER);

        add(sidePanel, BorderLayout.EAST);
        add(chartPanel, BorderLayout.SOUTH);
    }

    private void loadSimulationConfig(SimulationConfig config) {
        pauseSimulation();
        this.currentConfig = config;
        this.engine = new SimulationEngine(config.getGrid(), config.getEvents());
        this.engine.addObserver(this);
        this.inspectedPosition = null;

        Map<String, RgbColor> colors = new LinkedHashMap<>();
        for (Map.Entry<String, Entity> entry : config.getSpeciesPrototypes().entrySet()) {
            colors.put(entry.getKey(), entry.getValue().getColor());
        }
        chartPanel.setSpeciesColors(colors);
        chartPanel.resetChart(engine.getLatestStats());

        refreshView(engine.getGrid(), engine.getLatestStats());
    }

    private void startSimulation() {
        simulationTimer.start();
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
    }

    private void pauseSimulation() {
        simulationTimer.stop();
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
    }

    @Override
    public void onTurnCompleted(Grid grid, SimulationStats stats) {
        SwingUtilities.invokeLater(() -> {
            chartPanel.recordStats(stats);
            refreshView(grid, stats);
        });
    }

    private void refreshView(Grid grid, SimulationStats stats) {
        turnLabel.setText("Turno: " + stats.turn());

        if (stats.activeEvents().isEmpty()) {
            eventBanner.setText("Nessun Evento Globale Attivo");
            eventBanner.setBackground(new Color(225, 235, 225));
            eventBanner.setForeground(Color.DARK_GRAY);
            gridCanvas.setActiveEventHighlight(false);
        } else {
            String text = stats.activeEvents().stream()
                    .map(ev -> "EVENTO ATTIVO: " + ev.name().toUpperCase()
                            + " (Turni rimanenti: " + ev.remainingTurns() + ")")
                    .collect(Collectors.joining("  |  "));
            eventBanner.setText(text);
            eventBanner.setBackground(new Color(220, 53, 69));
            eventBanner.setForeground(Color.WHITE);
            gridCanvas.setActiveEventHighlight(true);
        }

        updateLegend(stats);
        updateInspector(grid);
        gridCanvas.repaint();
    }

    private void updateLegend(SimulationStats stats) {
        legendPanel.removeAll();
        Map<String, Entity> prototypes = currentConfig.getSpeciesPrototypes();

        for (Map.Entry<String, Entity> entry : prototypes.entrySet()) {
            String species = entry.getKey();
            Entity proto = entry.getValue();
            int count = stats.populationBySpecies().getOrDefault(species, 0);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            JPanel colorBox = new JPanel();
            colorBox.setPreferredSize(new Dimension(18, 18));
            RgbColor c = proto.getColor();
            colorBox.setBackground(new Color(c.r(), c.g(), c.b()));
            colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            JLabel label = new JLabel(species + " [" + proto.getSymbol() + "] : " + count);
            label.setFont(new Font("SansSerif", Font.PLAIN, 13));

            row.add(colorBox);
            row.add(label);
            legendPanel.add(row);
        }
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    private void updateInspector(Grid grid) {
        if (inspectedPosition == null) {
            inspectorArea.setText("Clicca su una cella della griglia\nper ispezionare i dettagli\ndell'entità presente.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Cella selezionata: (").append(inspectedPosition.x())
                .append(", ").append(inspectedPosition.y()).append(")\n");
        sb.append("------------------------------\n");

        Optional<Entity> entityOpt = grid.getEntityAt(inspectedPosition);
        if (entityOpt.isEmpty()) {
            sb.append("Stato: Cella vuota (Bioma)");
        } else {
            Entity entity = entityOpt.get();
            sb.append("Specie: ").append(entity.getSpeciesName()).append("\n");
            sb.append("Categoria: ").append(entity.getType()).append("\n");
            sb.append("Simbolo: ").append(entity.getSymbol()).append("\n");

            if (entity instanceof Animal animal) {
                sb.append("Energia: ").append(animal.getEnergy())
                        .append(" / ").append(animal.getMaxEnergy()).append("\n");
                sb.append("Età: ").append(animal.getAge())
                        .append(" / ").append(animal.getMaxAge()).append("\n");
                sb.append("Età Riprod.: [").append(animal.getMinReproductiveAge())
                        .append(" - ").append(animal.getMaxReproductiveAge()).append("]\n");
                sb.append("Dieta: ").append(animal.getDiet()).append("\n");
                sb.append("Raggio visivo: ").append(animal.getSightRange()).append("\n");
                sb.append("Velocità: ").append(animal.getSpeed()).append("\n");
            } else if (entity instanceof Plant plant) {
                sb.append("Energia fornita: ").append(plant.getEnergy()).append("\n");
                sb.append(String.format("Tasso base: %.2f%n", plant.getBaseReproductionRate()));
                sb.append(String.format("Tasso effettivo: %.2f%n", plant.getEffectiveReproductionRate()));
            }
        }
        inspectorArea.setText(sb.toString());
    }

    private void onLoadConfigurationDialog() {
        pauseSimulation();
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Seleziona File di Configurazione");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                SimulationConfig cfg = new ConfigParser().parseFile(chooser.getSelectedFile().toPath());
                loadSimulationConfig(cfg);
            } catch (ConfigurationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore di Configurazione", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onLoadStateDialog() {
        pauseSimulation();
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Seleziona File di Stato da Caricare");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                StateSerializer serializer = new StateSerializer();
                this.engine = serializer.loadFromFile(chooser.getSelectedFile().toPath(), this.currentConfig);
                this.engine.addObserver(this);
                this.chartPanel.resetChart(engine.getLatestStats());
                refreshView(engine.getGrid(), engine.getLatestStats());
            } catch (ConfigurationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore Caricamento Stato", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onSaveStateDialog() {
        pauseSimulation();
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Salva File di Stato");
        chooser.setSelectedFile(new File("stato_simulazione.txt"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                new StateSerializer().saveToFile(
                        engine.getGrid(),
                        engine.getCurrentTurn(),
                        chooser.getSelectedFile().toPath()
                );
                JOptionPane.showMessageDialog(this, "Stato salvato correttamente!", "Salvataggio Completato", JOptionPane.INFORMATION_MESSAGE);
            } catch (ConfigurationException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore di Salvataggio", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Pannello interno responsabile del rendering 2D della griglia e della gestione del click di ispezione.
     */
    private class GridCanvas extends JPanel {

        private boolean activeEventHighlight = false;

        GridCanvas() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleCellClick(e.getX(), e.getY());
                }
            });
        }

        void setActiveEventHighlight(boolean highlight) {
            this.activeEventHighlight = highlight;
        }

        private void handleCellClick(int mouseX, int mouseY) {
            Grid grid = engine.getGrid();
            int cellW = Math.max(1, getWidth() / grid.getWidth());
            int cellH = Math.max(1, getHeight() / grid.getHeight());

            int gridX = mouseX / cellW;
            // Conversione coordinata Y: (0,0) nel modello è in basso a sinistra
            int rowFromTop = mouseY / cellH;
            int gridY = grid.getHeight() - 1 - rowFromTop;

            if (gridX >= 0 && gridX < grid.getWidth() && gridY >= 0 && gridY < grid.getHeight()) {
                inspectedPosition = new Position(gridX, gridY);
                updateInspector(grid);
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (engine == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g;
            Grid grid = engine.getGrid();

            int cols = grid.getWidth();
            int rows = grid.getHeight();
            int cellW = Math.max(1, getWidth() / cols);
            int cellH = Math.max(1, getHeight() / rows);

            RgbColor bg = grid.getBackgroundColor();
            Color biomeColor = new Color(bg.r(), bg.g(), bg.b());

            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    int screenX = x * cellW;
                    // (0, 0) si trova in basso a sinistra
                    int screenY = (rows - 1 - y) * cellH;

                    Position pos = new Position(x, y);
                    Optional<Entity> entityOpt = grid.getEntityAt(pos);

                    if (entityOpt.isPresent()) {
                        RgbColor c = entityOpt.get().getColor();
                        g2.setColor(new Color(c.r(), c.g(), c.b()));
                    } else {
                        g2.setColor(biomeColor);
                    }
                    g2.fillRect(screenX, screenY, cellW, cellH);

                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.drawRect(screenX, screenY, cellW, cellH);

                    if (pos.equals(inspectedPosition)) {
                        g2.setColor(Color.YELLOW);
                        g2.setStroke(new BasicStroke(2.5f));
                        g2.drawRect(screenX + 1, screenY + 1, cellW - 2, cellH - 2);
                        g2.setStroke(new BasicStroke(1.0f));
                    }
                }
            }

            // Bordo colorato di avviso sulla griglia quando un evento globale è in corso
            if (activeEventHighlight) {
                g2.setColor(new Color(220, 53, 69));
                g2.setStroke(new BasicStroke(5.0f));
                g2.drawRect(2, 2, cols * cellW - 4, rows * cellH - 4);
            }
        }
    }
}
