package org.example.ui;

import org.example.engine.SimulationStats;
import org.example.model.RgbColor;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Pannello grafico che traccia in tempo reale il grafico dell'andamento
 * delle popolazioni per ciascuna specie turno dopo turno.
 */
public class PopulationChartPanel extends JPanel {

    private static final int MAX_HISTORY = 200;

    private final Map<String, List<Integer>> historyBySpecies = new LinkedHashMap<>();
    private final Map<String, Color> speciesColors = new HashMap<>();
    private final List<Integer> turns = new ArrayList<>();

    public PopulationChartPanel() {
        setPreferredSize(new Dimension(600, 180));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Andamento Popolazioni in Tempo Reale"));
    }

    /**
     * Registra i colori associati a ciascuna specie per le linee del grafico.
     *
     * @param colors mappa Nome Specie -> RgbColor
     */
    public void setSpeciesColors(Map<String, RgbColor> colors) {
        speciesColors.clear();
        for (Map.Entry<String, RgbColor> entry : colors.entrySet()) {
            RgbColor c = entry.getValue();
            // Se il colore è quasi bianco (es. Coniglio 255,255,255), lo scuriamo leggermente per renderlo visibile su sfondo bianco
            int r = (c.r() > 235 && c.g() > 235 && c.b() > 235) ? 100 : c.r();
            int g = (c.r() > 235 && c.g() > 235 && c.b() > 235) ? 140 : c.g();
            int b = (c.r() > 235 && c.g() > 235 && c.b() > 235) ? 200 : c.b();
            speciesColors.put(entry.getKey(), new Color(r, g, b));
        }
    }

    /**
     * Azzera lo storico del grafico e registra il punto iniziale.
     *
     * @param initialStats statistiche iniziali
     */
    public void resetChart(SimulationStats initialStats) {
        historyBySpecies.clear();
        turns.clear();
        recordStats(initialStats);
    }

    /**
     * Aggiunge i dati del turno corrente al grafico e richiede il ridisegno.
     *
     * @param stats statistiche del turno appena concluso
     */
    public void recordStats(SimulationStats stats) {
        turns.add(stats.turn());
        Set<String> allKnownSpecies = new LinkedHashSet<>(historyBySpecies.keySet());
        allKnownSpecies.addAll(stats.populationBySpecies().keySet());
        allKnownSpecies.addAll(speciesColors.keySet());

        for (String species : allKnownSpecies) {
            historyBySpecies.computeIfAbsent(species, k -> {
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < turns.size() - 1; i++) {
                    list.add(0);
                }
                return list;
            }).add(stats.populationBySpecies().getOrDefault(species, 0));
        }

        if (turns.size() > MAX_HISTORY) {
            turns.remove(0);
            for (List<Integer> series : historyBySpecies.values()) {
                if (!series.isEmpty()) {
                    series.remove(0);
                }
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padLeft = 45;
        int padRight = 20;
        int padTop = 28;
        int padBottom = 25;
        int w = getWidth() - padLeft - padRight;
        int h = getHeight() - padTop - padBottom;

        if (w <= 10 || h <= 10) {
            return;
        }

        // Assi cartesiani
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(padLeft, padTop, w, h);

        int maxPop = 10;
        for (List<Integer> series : historyBySpecies.values()) {
            for (int v : series) {
                if (v > maxPop) {
                    maxPop = v;
                }
            }
        }

        // Etichette asse Y
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString(String.valueOf(maxPop), 8, padTop + 10);
        g2.drawString(String.valueOf(maxPop / 2), 8, padTop + h / 2 + 4);
        g2.drawString("0", 25, padTop + h);

        if (turns.size() < 2) {
            return;
        }

        // Disegno delle curve per ogni specie
        g2.setStroke(new BasicStroke(2.2f));
        for (Map.Entry<String, List<Integer>> entry : historyBySpecies.entrySet()) {
            String species = entry.getKey();
            List<Integer> values = entry.getValue();
            g2.setColor(speciesColors.getOrDefault(species, Color.BLUE));

            for (int i = 0; i < values.size() - 1; i++) {
                int x1 = padLeft + (i * w) / (values.size() - 1);
                int y1 = padTop + h - (int) ((values.get(i) / (double) maxPop) * h);
                int x2 = padLeft + ((i + 1) * w) / (values.size() - 1);
                int y2 = padTop + h - (int) ((values.get(i + 1) / (double) maxPop) * h);
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        // Etichette turno iniziale e finale sull'asse X
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("T:" + turns.get(0), padLeft, padTop + h + 16);
        String endTurn = "T:" + turns.get(turns.size() - 1);
        g2.drawString(endTurn, padLeft + w - 35, padTop + h + 16);
    }
}