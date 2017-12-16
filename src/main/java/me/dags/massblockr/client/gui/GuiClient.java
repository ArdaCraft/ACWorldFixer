package me.dags.massblockr.client.gui;

import me.dags.massblockr.client.Client;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dags <dags@dags.me>
 */
public class GuiClient implements Client {

    private final JFrame frame = new JFrame();
    private final JLabel overallLabel = new JLabel();
    private final JLabel dimensionLabel = new JLabel();
    private final JProgressBar overallProgress = new JProgressBar();
    private final JProgressBar currentProgress = new JProgressBar();

    private final int coreCount;
    private final ExecutorService executorService;
    private final AtomicReference<String> progress = new AtomicReference<>("0%");

    public GuiClient(int coreCount) {
        this.coreCount = coreCount;
        this.executorService = Executors.newFixedThreadPool(coreCount);
    }

    @Override
    public ExecutorService getExecutor() {
        return executorService;
    }

    @Override
    public void setup() {
        overallLabel.setText("Overall:");

        dimensionLabel.setText("World:");
        dimensionLabel.setPreferredSize(new Dimension(100, 30));

        overallProgress.setPreferredSize(new Dimension(200, 30));
        overallProgress.setValue(0);
        overallProgress.setMinimum(0);
        overallProgress.setMaximum(1);

        currentProgress.setPreferredSize(new Dimension(200, 30));
        currentProgress.setValue(0);
        currentProgress.setMinimum(0);
        currentProgress.setMaximum(1);

        JPanel panel = new JPanel();
        panel.add(overallLabel);
        panel.add(overallProgress);
        panel.add(dimensionLabel);
        panel.add(currentProgress);

        frame.add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowStateListener(e -> {
            if (!e.getWindow().isVisible()) {
                System.out.println("Shutting down...");
                getExecutor().shutdownNow();
            }
        });
    }

    @Override
    public void update() {
        currentProgress.repaint();
        overallProgress.repaint();
        frame.setTitle(progress.get());
    }

    @Override
    public void finish(File outputDirRoot) {
        executorService.shutdown();

        Stats.displayResults();
        JOptionPane.showMessageDialog(null, "Conversion Complete!");
        frame.dispose();

        try {
            Desktop.getDesktop().open(outputDirRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDimension(String dimension) {
        dimensionLabel.setText(String.format("Dim: %s", dimension));
    }

    @Override
    public void setDimTaskProgress(int tasksComplete, int taskTotal) {
        currentProgress.setMaximum(taskTotal);
        currentProgress.setValue(tasksComplete);
    }

    @Override
    public void setGlobalTaskProgress(int tasksComplete, int taskTotal) {
        overallProgress.setMaximum(taskTotal);
        overallProgress.setValue(tasksComplete);

        float percentage = (tasksComplete / (float) taskTotal) * 100;
        progress.set(String.format("Converting: %.2f%%", percentage));
    }
}
