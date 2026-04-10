package com.bank.app;

import com.bank.Configuration;
import com.bank.random.ExponentialDistribution;
import com.bank.random.LCG;
import com.bank.random.PoissonDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SimulationUI extends JFrame {
    // --- Single Run Components ---
    private JTextField lambdaField, timeField, seedField;
    private JCheckBox singleQueueCheck;
    private JTextField[] probFields = new JTextField[5];
    private JTextField[] durFields = new JTextField[5];
    private JLabel avgWaitLabel, avgBusyLabel, avgSpentLabel;
    private JPanel chartsPanel;

    // --- Research Components ---
    private JComboBox<String> researchParamCombo;
    private JTextField researchStartField, researchEndField, researchStepField, researchRunsField;
    private JButton researchButton;
    private JTextArea researchLogArea;
    private DefaultTableModel researchTableModel;
    private JPanel researchChartPanel;

    // Хранилище результатов для графика
    private final List<ResearchPoint> researchResults = new ArrayList<>();

    private final DecimalFormat df = new DecimalFormat("#.##");
    private final Random seedRandom = new Random();

    public SimulationUI() {
        setTitle("Bank Simulation UI + Research");
        setSize(1300, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Одиночный прогон", createSingleRunPanel());
        tabbedPane.addTab("Исследование (ПЗ №6)", createResearchPanel());
        tabbedPane.addTab("Графики распределений", createDistributionsPanel());
        add(tabbedPane);
    }

    // ================= SINGLE RUN TAB =================

    private JPanel createSingleRunPanel() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createConfigPanel());
        splitPane.setRightComponent(createResultsPanel());
        splitPane.setDividerLocation(350);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Настройки"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        gbc.gridy = 0;
        panel.add(new JLabel("Lambda (клиентов/час):"), gbc);
        gbc.gridx = 1;
        lambdaField = new JTextField("200", 8);
        panel.add(lambdaField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Время симуляции (сек):"), gbc);
        gbc.gridx = 1;
        timeField = new JTextField("3600", 8);
        panel.add(timeField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Seed:"), gbc);
        gbc.gridx = 1;
        seedField = new JTextField(String.valueOf(42), 8);
        panel.add(seedField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        singleQueueCheck = new JCheckBox("Единая очередь");
        panel.add(singleQueueCheck, gbc);
        gbc.gridwidth = 1;

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Вероятности:"), gbc);
        gbc.gridx = 1;
        JPanel probPanel = new JPanel(new GridLayout(5, 1, 0, 2));
        double[] defaultProbs = {0.1, 0.19, 0.32, 0.24, 0.15};
        for (int i = 0; i < 5; i++) {
            probFields[i] = new JTextField(String.valueOf(defaultProbs[i]));
            probPanel.add(probFields[i]);
        }
        panel.add(probPanel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Длительности (сек):"), gbc);
        gbc.gridx = 1;
        JPanel durPanel = new JPanel(new GridLayout(5, 1, 0, 2));
        int[] defaultDurs = {45, 75, 100, 150, 300};
        for (int i = 0; i < 5; i++) {
            durFields[i] = new JTextField(String.valueOf(defaultDurs[i]));
            durPanel.add(durFields[i]);
        }
        panel.add(durPanel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton runButton = new JButton("Запустить симуляцию");
        runButton.addActionListener(this::runSingleSimulation);
        panel.add(runButton, gbc);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Результаты"));

        JPanel metricsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Метрики"));
        metricsPanel.add(new JLabel("Среднее время ожидания:"));
        avgWaitLabel = new JLabel("-");
        metricsPanel.add(avgWaitLabel);
        metricsPanel.add(new JLabel("Средняя загрузка операторов:"));
        avgBusyLabel = new JLabel("-");
        metricsPanel.add(avgBusyLabel);
        metricsPanel.add(new JLabel("Среднее время в банке:"));
        avgSpentLabel = new JLabel("-");
        metricsPanel.add(avgSpentLabel);

        panel.add(metricsPanel, BorderLayout.NORTH);

        chartsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        panel.add(chartsPanel, BorderLayout.CENTER);

        return panel;
    }

    private void runSingleSimulation(ActionEvent e) {
        try {
            Configuration config = parseConfig();
            Simulation sim = new Simulation(config);
            sim.run();

            avgWaitLabel.setText(df.format(sim.getAvgWaitTime()) + " сек");
            double avgUtilization = Arrays.stream(sim.getOperatorUtilization()).average().orElse(0.0);
            avgBusyLabel.setText(df.format(avgUtilization) + " %");
            avgSpentLabel.setText(df.format(sim.getAvgTimeSpent()) + " сек");

            updateCharts(sim);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private Configuration parseConfig() {
        int lambda = Integer.parseInt(lambdaField.getText());
        int time = Integer.parseInt(timeField.getText());
        int seed = Integer.parseInt(seedField.getText());
        boolean isSingle = singleQueueCheck.isSelected();

        double[] probs = new double[5];
        int[] durs = new int[5];
        for (int i = 0; i < 5; i++) {
            probs[i] = Double.parseDouble(probFields[i].getText());
            durs[i] = Integer.parseInt(durFields[i].getText());
        }
        return new Configuration(probs, durs, seed, lambda, isSingle, time);
    }

    private void updateCharts(Simulation sim) {
        chartsPanel.removeAll();

        DefaultCategoryDataset utilDataset = new DefaultCategoryDataset();
        double[] utils = sim.getOperatorUtilization();
        for (int i = 0; i < utils.length; i++) {
            utilDataset.addValue(utils[i], "Загрузка (%)", "Оп. " + (i + 1));
        }
        JFreeChart utilChart = ChartFactory.createBarChart(
            "Загрузка операторов", "Оператор", "%", utilDataset,
            PlotOrientation.VERTICAL, true, true, false);
        chartsPanel.add(new ChartPanel(utilChart));

        try {
            DefaultCategoryDataset waitDataset = new DefaultCategoryDataset();
            java.lang.reflect.Method method = Simulation.class.getMethod("getAvgWaitTimeByType");
            double[] avgWaits = (double[]) method.invoke(sim);
            for (int i = 0; i < avgWaits.length; i++) {
                waitDataset.addValue(avgWaits[i], "Ожидание", "Тип " + (i + 1));
            }
            JFreeChart waitChart = ChartFactory.createBarChart(
                "Ожидание по типам", "Тип", "Сек", waitDataset,
                PlotOrientation.VERTICAL, true, true, false);
            chartsPanel.add(new ChartPanel(waitChart));
        } catch (Exception ignored) {
            chartsPanel.add(new JLabel("Добавьте getAvgWaitTimeByType() в Simulation"));
        }

        chartsPanel.revalidate();
        chartsPanel.repaint();
    }

    // ================= DISTRIBUTIONS TAB =================

    private JPanel createDistributionsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        
        // Панель настроек
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Настройки генерации"));
        
        JLabel sampleSizeLabel = new JLabel("Размер выборки:");
        JTextField sampleSizeField = new JTextField("10000", 6);
        JLabel seedLabel = new JLabel("Seed:");
        JTextField seedFieldDist = new JTextField("42", 6);
        JLabel lambdaLabel = new JLabel("Lambda (для эксп. и Пуассона):");
        JTextField lambdaFieldDist = new JTextField("5.0", 6);
        JButton generateButton = new JButton("Обновить графики");
        
        settingsPanel.add(sampleSizeLabel);
        settingsPanel.add(sampleSizeField);
        settingsPanel.add(seedLabel);
        settingsPanel.add(seedFieldDist);
        settingsPanel.add(lambdaLabel);
        settingsPanel.add(lambdaFieldDist);
        settingsPanel.add(generateButton);
        
        mainPanel.add(settingsPanel, BorderLayout.NORTH);
        
        // Контейнер для графиков
        JPanel chartsContainer = new JPanel(new GridLayout(2, 2, 10, 10));
        chartsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Панели для графиков (будут заполнены при нажатии кнопки)
        JPanel lcgChartPanel = new JPanel(new BorderLayout());
        lcgChartPanel.setBorder(BorderFactory.createTitledBorder("LCG - Равномерное распределение [0, 1]"));
        lcgChartPanel.setName("lcgPanel");
        lcgChartPanel.add(new JLabel("Нажмите 'Обновить графики'", SwingConstants.CENTER), BorderLayout.CENTER);
        chartsContainer.add(lcgChartPanel);
        
        JPanel expChartPanel = new JPanel(new BorderLayout());
        expChartPanel.setBorder(BorderFactory.createTitledBorder("Экспоненциальное распределение"));
        expChartPanel.setName("expPanel");
        expChartPanel.add(new JLabel("Нажмите 'Обновить графики'", SwingConstants.CENTER), BorderLayout.CENTER);
        chartsContainer.add(expChartPanel);
        
        JPanel poisChartPanel = new JPanel(new BorderLayout());
        poisChartPanel.setBorder(BorderFactory.createTitledBorder("Пуассоновское распределение"));
        poisChartPanel.setName("poisPanel");
        poisChartPanel.add(new JLabel("Нажмите 'Обновить графики'", SwingConstants.CENTER), BorderLayout.CENTER);
        chartsContainer.add(poisChartPanel);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация"));
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setText("LCG - генератор псевдослучайных чисел\n" +
                        "Экспоненциальное - время между событиями\n" +
                        "Пуассоновское - количество событий за интервал");
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        chartsContainer.add(infoPanel);
        
        mainPanel.add(chartsContainer, BorderLayout.CENTER);
        
        // Обработчик кнопки
        generateButton.addActionListener(e -> {
            try {
                int sampleSize = Integer.parseInt(sampleSizeField.getText());
                long seed = Long.parseLong(seedFieldDist.getText());
                double lambda = Double.parseDouble(lambdaFieldDist.getText());
                
                if (sampleSize <= 0) throw new IllegalArgumentException("Размер выборки должен быть > 0");
                if (lambda <= 0) throw new IllegalArgumentException("Lambda должна быть > 0");
                
                updateDistributionCharts(chartsContainer, sampleSize, seed, lambda);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        return mainPanel;
    }
    
    private void updateDistributionCharts(JPanel container, int sampleSize, long seed, double lambda) {
        container.removeAll();
        
        // 1. LCG - Равномерное распределение
        LCG lcg = new LCG(seed);
        float[] lcgSamples = lcg.generateSequence(sampleSize);
        
        DefaultCategoryDataset lcgDataset = new DefaultCategoryDataset();
        int[] lcgHistogram = new int[10];
        for (float val : lcgSamples) {
            int bin = Math.min((int)(val * 10), 9);
            lcgHistogram[bin]++;
        }
        for (int i = 0; i < 10; i++) {
            lcgDataset.addValue(lcgHistogram[i], "Частота", String.valueOf(i / 10.0));
        }
        
        JFreeChart lcgChart = ChartFactory.createBarChart(
            "Гистограмма LCG", "Интервал", "Частота", lcgDataset,
            PlotOrientation.VERTICAL, false, true, false);
        JPanel lcgPanel = new JPanel(new BorderLayout());
        lcgPanel.setBorder(BorderFactory.createTitledBorder("LCG - Равномерное распределение [0, 1]"));
        lcgPanel.add(new ChartPanel(lcgChart), BorderLayout.CENTER);
        container.add(lcgPanel);
        
        // 2. Экспоненциальное распределение
        LCG lcgExp = new LCG(seed);
        double[] expSamples = new double[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            expSamples[i] = ExponentialDistribution.get(lambda, lcgExp);
        }
        
        DefaultCategoryDataset expDataset = new DefaultCategoryDataset();
        int[] expHistogram = new int[15];
        double maxExp = 5.0 / lambda; // Ограничим для наглядности
        for (double val : expSamples) {
            if (val < maxExp) {
                int bin = Math.min((int)(val * 15 / maxExp), 14);
                expHistogram[bin]++;
            }
        }
        for (int i = 0; i < 15; i++) {
            expDataset.addValue(expHistogram[i], "Частота", String.format("%.2f", i * maxExp / 15));
        }
        
        JFreeChart expChart = ChartFactory.createBarChart(
            "Гистограмма экспоненциального распределения", "Значение", "Частота", expDataset,
            PlotOrientation.VERTICAL, false, true, false);
        JPanel expPanel = new JPanel(new BorderLayout());
        expPanel.setBorder(BorderFactory.createTitledBorder("Экспоненциальное распределение (λ=" + lambda + ")"));
        expPanel.add(new ChartPanel(expChart), BorderLayout.CENTER);
        container.add(expPanel);
        
        // 3. Пуассоновское распределение
        LCG lcgPois = new LCG(seed);
        int[] poisSamples = new int[sampleSize];
        int maxPois = 0;
        for (int i = 0; i < sampleSize; i++) {
            poisSamples[i] = PoissonDistribution.get(lambda, lcgPois);
            if (poisSamples[i] > maxPois) maxPois = poisSamples[i];
        }
        
        DefaultCategoryDataset poisDataset = new DefaultCategoryDataset();
        int histogramSize = Math.min(maxPois + 1, 20);
        int[] poisHistogram = new int[histogramSize];
        for (int val : poisSamples) {
            if (val < histogramSize) {
                poisHistogram[val]++;
            } else {
                poisHistogram[histogramSize - 1]++;
            }
        }
        for (int i = 0; i < histogramSize; i++) {
            poisDataset.addValue(poisHistogram[i], "Частота", String.valueOf(i));
        }
        
        JFreeChart poisChart = ChartFactory.createBarChart(
            "Гистограмма пуассоновского распределения", "k (кол-во событий)", "Частота", poisDataset,
            PlotOrientation.VERTICAL, false, true, false);
        JPanel poisPanel = new JPanel(new BorderLayout());
        poisPanel.setBorder(BorderFactory.createTitledBorder("Пуассоновское распределение (λ=" + lambda + ")"));
        poisPanel.add(new ChartPanel(poisChart), BorderLayout.CENTER);
        container.add(poisPanel);
        
        // 4. Информационная панель
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Статистика"));
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        
        double lcgMean = Arrays.stream(lcgSamples).average().orElse(0);
        double expMean = Arrays.stream(expSamples).average().orElse(0);
        double poisMean = Arrays.stream(poisSamples).mapToDouble(i -> i).average().orElse(0);
        
        infoArea.setText(String.format(
            "LCG:\n  Среднее: %.4f (ожидается ~0.5)\n  Мин: %.4f\n  Макс: %.4f\n\n" +
            "Экспоненциальное:\n  Среднее: %.4f (ожидается ~%.4f)\n  Мин: %.4f\n  Макс: %.4f\n\n" +
            "Пуассоновское:\n  Среднее: %.4f (ожидается ~%.4f)\n  Мин: %d\n  Макс: %d",
            lcgMean, Arrays.stream(lcgSamples).min().orElse(0), Arrays.stream(lcgSamples).max().orElse(0),
            expMean, 1.0/lambda, Arrays.stream(expSamples).min().orElse(0), Arrays.stream(expSamples).max().orElse(0),
            poisMean, lambda, Arrays.stream(poisSamples).min().orElse(0), Arrays.stream(poisSamples).max().orElse(0)
        ));
        infoPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        container.add(infoPanel);
        
        container.revalidate();
        container.repaint();
    }

    // ================= RESEARCH TAB =================
    
    private JPanel createResearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Параметры исследования"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Параметр:"), gbc);
        gbc.gridx = 1;
        researchParamCombo = new JComboBox<>(new String[]{"Lambda", "Simulation Time"});
        controlPanel.add(researchParamCombo, gbc);

        gbc.gridx = 2;
        controlPanel.add(new JLabel("Start:"), gbc);
        gbc.gridx = 3;
        researchStartField = new JTextField("100", 6);
        controlPanel.add(researchStartField, gbc);

        gbc.gridx = 4;
        controlPanel.add(new JLabel("End:"), gbc);
        gbc.gridx = 5;
        researchEndField = new JTextField("300", 6);
        controlPanel.add(researchEndField, gbc);

        gbc.gridx = 6;
        controlPanel.add(new JLabel("Step:"), gbc);
        gbc.gridx = 7;
        researchStepField = new JTextField("50", 6);
        controlPanel.add(researchStepField, gbc);

        gbc.gridx = 8;
        controlPanel.add(new JLabel("Прогонов:"), gbc);
        gbc.gridx = 9;
        researchRunsField = new JTextField("100", 6);
        controlPanel.add(researchRunsField, gbc);

        gbc.gridx = 10;
        researchButton = new JButton("Запустить исследование");
        researchButton.addActionListener(this::startResearch);
        controlPanel.add(researchButton, gbc);

        panel.add(controlPanel, BorderLayout.NORTH);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        researchChartPanel = new JPanel(new BorderLayout());
        researchChartPanel.setBorder(BorderFactory.createTitledBorder("График зависимости"));
        // Добавляем заглушку
        researchChartPanel.add(new JLabel("График появится после исследования", SwingConstants.CENTER), BorderLayout.CENTER);
        centerSplit.setTopComponent(researchChartPanel);

        researchTableModel = new DefaultTableModel(new String[]{"Значение", "Ср. Ожидание", "Ср. Загрузка", "Ср. Время в банке"}, 0);
        JTable table = new JTable(researchTableModel);
        centerSplit.setBottomComponent(new JScrollPane(table));
        centerSplit.setDividerLocation(350);

        panel.add(centerSplit, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        researchLogArea = new JTextArea(5, 20);
        researchLogArea.setEditable(false);
        bottomPanel.add(new JScrollPane(researchLogArea), BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void startResearch(ActionEvent e) {
        try {
            researchButton.setEnabled(false);
            researchTableModel.setRowCount(0);
            researchResults.clear(); // Очищаем результаты
            researchChartPanel.removeAll();
            researchChartPanel.add(new JLabel("Вычисление...", SwingConstants.CENTER), BorderLayout.CENTER);
            researchChartPanel.revalidate();
            researchLogArea.setText("");

            String param = (String) researchParamCombo.getSelectedItem();
            double start = Double.parseDouble(researchStartField.getText());
            double end = Double.parseDouble(researchEndField.getText());
            double step = Double.parseDouble(researchStepField.getText());
            int runs = Integer.parseInt(researchRunsField.getText());

            if (step <= 0 || start > end) throw new IllegalArgumentException("Проверьте диапазон и шаг");
            if (runs < 1) throw new IllegalArgumentException("Прогонов должно быть >= 1");

            ResearchWorker worker = new ResearchWorker(param, start, end, step, runs);
            worker.execute();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка ввода: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            researchButton.setEnabled(true);
        }
    }

    class ResearchWorker extends SwingWorker<Void, ResearchPoint> {
        private final String param;
        private final double start, end, step;
        private final int runs;

        public ResearchWorker(String param, double start, double end, double step, int runs) {
            this.param = param;
            this.start = start;
            this.end = end;
            this.step = step;
            this.runs = runs;
        }

        @Override
        protected Void doInBackground() {
            int totalSteps = (int) Math.ceil((end - start) / step) + 1;
            int totalOps = totalSteps * runs;
            int currentOp = 0;

            for (double val = start; val <= end + 1e-9; val += step) {
                double sumWait = 0, sumBusy = 0, sumSpent = 0;

                log("Шаг: " + param + " = " + df.format(val) + " | Прогонов: " + runs);

                for (int i = 0; i < runs; i++) {
                    if (isCancelled()) return null;

                    Configuration baseConfig = parseConfig();
                    Configuration researchConfig = modifyConfig(baseConfig, param, val);
                    researchConfig.setSeed(seedRandom.nextInt());

                    Simulation sim = new Simulation(researchConfig);
                    sim.run();

                    sumWait += sim.getAvgWaitTime();
                    sumBusy += sim.getAvgOperatorBusyTime();
                    sumSpent += sim.getAvgTimeSpent();

                    currentOp++;
                    int progress = (int) (100.0 * currentOp / totalOps);
                    setProgress(progress);
                }

                ResearchPoint point = new ResearchPoint(
                    val,
                    sumWait / runs,
                    sumBusy / runs,
                    sumSpent / runs
                );
                publish(point);
            }
            return null;
        }

        @Override
        protected void process(List<ResearchPoint> chunks) {
            for (ResearchPoint p : chunks) {
                // Добавляем в список результатов
                researchResults.add(p);

                // Добавляем в таблицу
                researchTableModel.addRow(new Object[]{
                    df.format(p.value),
                    df.format(p.avgWait),
                    df.format(p.avgBusy),
                    df.format(p.avgSpent)
                });
            }
        }

        @Override
        protected void done() {
            researchButton.setEnabled(true);
            try {
                get();
                updateResearchChart();
                log("Исследование завершено успешно.");
            } catch (Exception ex) {
                log("Ошибка: " + ex.getCause().getMessage());
                ex.printStackTrace();
                researchChartPanel.removeAll();
                researchChartPanel.add(new JLabel("Ошибка построения графика", SwingConstants.CENTER), BorderLayout.CENTER);
                researchChartPanel.revalidate();
            }
        }

        private Configuration modifyConfig(Configuration base, String param, double val) {
            if (param.equals("Lambda")) {
                return new Configuration(
                    base.getProbabilities(), base.getDurations(),
                    base.getSeed(), (int) val, base.isSingleQueue(), base.getSimulationTimeSeconds()
                );
            } else if (param.equals("Simulation Time")) {
                return new Configuration(
                    base.getProbabilities(), base.getDurations(),
                    base.getSeed(), base.getLambda(), base.isSingleQueue(), (int) val
                );
            }
            return base;
        }

        private void log(String msg) {
            SwingUtilities.invokeLater(() -> {
                researchLogArea.append(msg + "\n");
                researchLogArea.setCaretPosition(researchLogArea.getDocument().getLength());
            });
        }
    }

    private void updateResearchChart() {
        try {
            if (researchResults.isEmpty()) {
                researchChartPanel.removeAll();
                researchChartPanel.add(new JLabel("Нет данных для графика", SwingConstants.CENTER), BorderLayout.CENTER);
                researchChartPanel.revalidate();
                return;
            }

            XYSeries waitSeries = new XYSeries("Ср. время ожидания");
            for (ResearchPoint p : researchResults) {
                waitSeries.add(p.value, p.avgWait);
            }

            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(waitSeries);

            String xAxisLabel = (String) researchParamCombo.getSelectedItem();
            JFreeChart chart = ChartFactory.createXYLineChart(
                "Зависимость времени ожидания от " + xAxisLabel,
                xAxisLabel,
                "Среднее время ожидания (сек)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
            );

            researchChartPanel.removeAll();
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(600, 300));
            researchChartPanel.add(chartPanel, BorderLayout.CENTER);

            researchChartPanel.revalidate();
            researchChartPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка при построении графика: " + e.getMessage(), "Ошибка графика", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class ResearchPoint {
        double value, avgWait, avgBusy, avgSpent;
        ResearchPoint(double v, double w, double b, double s) {
            value = v; avgWait = w; avgBusy = b; avgSpent = s;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulationUI().setVisible(true));
    }
}
