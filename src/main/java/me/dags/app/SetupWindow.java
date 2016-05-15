package me.dags.app;

import me.dags.blockinfo.Config;
import me.dags.data.json.JsonSerializer;
import me.dags.worldfixer.WorldData;
import me.dags.worldfixer.blockfix.BlockFixer;
import me.dags.worldfixer.levelfix.LevelFixer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author dags <dags@dags.me>
 */
class SetupWindow extends JPanel {

    private static final File WORKING_DIR = new File(new File("").getAbsolutePath());
    private final JTextField targetDir = new JTextField();
    private final JTextField targetConfig = new JTextField();
    private final JSlider cores = new JSlider(SwingConstants.HORIZONTAL);

    SetupWindow() {
        int fullWidth = 425;
        int buttonWidth = 100;
        int labelWidth = 65;
        int lineHeight = 25;

        targetDir.setPreferredSize(new Dimension(fullWidth - buttonWidth, lineHeight));
        targetDir.setText(WORKING_DIR.getAbsolutePath());

        JButton chooseDir = new JButton("Choose");
        chooseDir.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        chooseDir.addActionListener(choose(targetDir));

        targetConfig.setPreferredSize(new Dimension(fullWidth, lineHeight));
        targetConfig.setText("https://raw.githubusercontent.com/ArdaCraft/ACWorldFixer/master/block_data.json");

        cores.setPreferredSize(new Dimension(fullWidth, 35));
        cores.setMinimum(1);
        cores.setMaximum(Runtime.getRuntime().availableProcessors());
        cores.setMajorTickSpacing(1);
        cores.setPaintLabels(true);
        cores.setPaintTicks(true);
        cores.setPaintTrack(true);

        JButton ok = new JButton("Ok");
        ok.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        ok.addActionListener(ok());

        JLabel worldLabel = new JLabel("World Dir:");
        worldLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
        JLabel configLabel = new JLabel("Config URL:");
        configLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
        JLabel coresLabel = new JLabel("CPU Cores:");
        coresLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));

        this.setLayout(new GridLayout(4, 1));
        this.add(toPanel(worldLabel, targetDir, chooseDir));
        this.add(toPanel(configLabel, targetConfig));
        this.add(toPanel(coresLabel, cores));
        this.add(toPanel(ok));
    }

    private JPanel toPanel(JComponent... component) {
        JPanel panel = new JPanel();
        for (JComponent c : component) {
            panel.add(c);
        }
        return panel;
    }

    private ActionListener choose(JTextField updateField) {
        return e -> {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(WORKING_DIR);
            dirChooser.setSelectedFile(WORKING_DIR);
            int response = dirChooser.showOpenDialog(SetupWindow.this);
            switch (response) {
                case JFileChooser.APPROVE_OPTION:
                    File target = dirChooser.getSelectedFile();
                    updateField.setText(target.getAbsolutePath());
                    break;
                default:
                    break;
            }
        };
    }

    private ActionListener ok() {
        return e -> {
            URL url = null;
            try {
                url = new URL(targetConfig.getText());
            } catch (IOException ex) {
                errorWindow("Invalid config URL provided!", ex.getMessage());
                return;
            }

            WorldData worldData = new WorldData(new File(targetDir.getText()));

            if (!worldData.validate()) {
                errorWindow("Invalid world directory selected!", worldData.error());
                return;
            }

            Config config = null;
            try (InputStream inputStream = url.openConnection().getInputStream()) {
                config = JsonSerializer.pretty().deserialize(inputStream, Config.class);
            } catch (Exception ex) {
                errorWindow("Error reading config!", ex.getMessage());
                return;
            }

            try {
                LevelFixer levelFixer = new LevelFixer(config, worldData);
                levelFixer.fix();
            } catch (Exception ex) {
                errorWindow("Error occurred whilst fixing level data!", ex.getMessage());
                return;
            }

            try {
                BlockFixer blockFixer = new BlockFixer(config, worldData, cores.getValue());

                JProgressBar progressBar = new JProgressBar();
                progressBar.setPreferredSize(new Dimension(250, 30));
                progressBar.setVisible(true);
                progressBar.setStringPainted(true);

                JFrame frame = new JFrame();
                frame.setLayout(new GridBagLayout());
                frame.add(progressBar);

                progressBar.setPreferredSize(new Dimension(250, 30));

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.setResizable(false);
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                new Thread(() -> blockFixer.run(frame, progressBar)).start();
            } catch (Exception ex) {
                errorWindow("Error occurred whilst processing region files", ex.getMessage());
            }
        };
    }

    private static void errorWindow(String label, String error) {
        JFrame frame = new JFrame();

        JTextArea log = new JTextArea();
        log.setText(label + "\n\n" + error);
        log.setEditable(false);

        JScrollPane pane = new JScrollPane();
        pane.setPreferredSize(new Dimension(300, 300));
        pane.getViewport().add(log);

        frame.add(pane);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
