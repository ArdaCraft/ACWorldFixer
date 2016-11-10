package me.dags.app;

import me.dags.data.NodeAdapter;
import me.dags.data.node.Node;
import me.dags.data.node.NodeTypeAdapters;
import me.dags.worldfixer.Config;
import me.dags.worldfixer.WorldModifier;
import me.dags.worldfixer.WorldData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class SetupWindow extends JPanel {

    private static final File WORKING_DIR = new File(new File("").getAbsolutePath());
    private final JTextField targetLevel = new JTextField();
    private final JTextField targetDir = new JTextField();
    private final JTextField targetConfig = new JTextField();
    private final JSlider cores = new JSlider(SwingConstants.HORIZONTAL);

    Config config = null;
    File worldDir = null;
    WorldData worldData = null;

    SetupWindow() {
        int fullWidth = 425;
        int buttonWidth = 100;
        int labelWidth = 65;
        int lineHeight = 25;

        targetLevel.setPreferredSize(new Dimension(fullWidth - buttonWidth, lineHeight));
        targetLevel.setText(WORKING_DIR.getAbsolutePath());
        JButton chooseLevel = new JButton("Choose");
        chooseLevel.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        chooseLevel.addActionListener(choose(targetLevel, JFileChooser.FILES_ONLY, f -> f.getName().endsWith(".dat"), this::loadLevelData));

        targetDir.setPreferredSize(new Dimension(fullWidth - buttonWidth, lineHeight));
        targetDir.setText(WORKING_DIR.getAbsolutePath());

        JButton chooseDir = new JButton("Choose");
        chooseDir.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        chooseDir.addActionListener(choose(targetDir, JFileChooser.DIRECTORIES_ONLY, f -> true, f -> worldDir = f));

        targetConfig.setPreferredSize(new Dimension(fullWidth - buttonWidth - buttonWidth, lineHeight));
        targetConfig.setText("");

        JButton loadConfig = new JButton("Modify");
        loadConfig.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        loadConfig.addActionListener(loadConfig(targetConfig));

        JButton saveConfig = new JButton("Save");
        saveConfig.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        saveConfig.addActionListener(saveConfig());

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

        JLabel levelLabel = new JLabel("Level File:");
        levelLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
        JLabel worldLabel = new JLabel("World Dir:");
        worldLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
        JLabel configLabel = new JLabel("Config URL:");
        configLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
        JLabel coresLabel = new JLabel("CPU Cores:");
        coresLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));

        this.setLayout(new GridLayout(5, 1));
        this.add(toPanel(levelLabel, targetLevel, chooseLevel));
        this.add(toPanel(worldLabel, targetDir, chooseDir));
        this.add(toPanel(configLabel, targetConfig, loadConfig, saveConfig));
        this.add(toPanel(coresLabel, cores));
        this.add(toPanel(ok));
    }

    public void updateConfig(Config config) {
        this.config = config;
    }

    private JPanel toPanel(JComponent... component) {
        JPanel panel = new JPanel();
        for (JComponent c : component) {
            panel.add(c);
        }
        return panel;
    }

    private void loadLevelData(File target) {
        WorldData worldData = new WorldData(target);
        if (!worldData.validate()) {
            errorWindow("Invalid level.dat selected!", worldData.error());
            return;
        }
        this.worldData = worldData;
        this.worldData.loadRegistry();
    }

    private void loadConfig() {
        Config config = null;
        try {
            config = NodeAdapter.json().from(new URL(targetConfig.getText()), Config.class);
        } catch (Exception ex) {
        }
        if (config == null) {
            config = new Config();
        }
        this.config = config;
    }

    private ActionListener choose(JTextField updateField, int mode, Predicate<File> fileFilter, Consumer<File> action) {
        return e -> {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setFileSelectionMode(mode);
            dirChooser.setFileHidingEnabled(false);
            dirChooser.ensureFileIsVisible(WORKING_DIR);
            dirChooser.setSelectedFile(WORKING_DIR);
            int response = dirChooser.showOpenDialog(SetupWindow.this);
            switch (response) {
                case JFileChooser.APPROVE_OPTION:
                    File target = dirChooser.getSelectedFile();
                    if (fileFilter.test(target)) {
                        updateField.setText(target.getAbsolutePath());
                        action.accept(target);
                    }
                    break;
                default:
                    break;
            }
        };
    }

    private ActionListener loadConfig(JTextField configField) {
        return e -> {
            if (this.worldData == null) {
                errorWindow("Invalid world directory selected!", "");
                return;
            }
            loadConfig();
            JFrame frame = new JFrame();
            frame.setTitle("Remapper");
            frame.setLayout(new GridBagLayout());
            frame.add(new MappingWindow(this, frame));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        };
    }

    private ActionListener saveConfig() {
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
                    File out = new File(target, "config.json");
                    Node node = NodeTypeAdapters.of(Config.class).toNode(config);
                    NodeAdapter.json().to(node, out);
                    break;
                default:
                    break;
            }
        };
    }

    private ActionListener ok() {
        return e -> {
            if (this.worldData == null) {
                errorWindow("World data not loaded correctly!", "");
                return;
            }

            if (this.worldDir == null) {
                errorWindow("No world directory selected!", "");
                return;
            }

            if (this.config == null) {
                loadConfig();
            }

            try {
                WorldModifier blockFixer = new WorldModifier(config, worldData, worldDir, cores.getValue());

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
