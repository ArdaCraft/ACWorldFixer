package me.dags.blockr.app;

import me.dags.blockr.Config;
import me.dags.blockr.WorldData;
import me.dags.blockr.world.World;
import me.dags.data.NodeAdapter;
import me.dags.data.node.Node;
import me.dags.data.node.NodeTypeAdapters;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class SetupWindow extends JPanel {

    private static final File WORKING_DIR = new File(new File("").getAbsolutePath());
    public static final JButton ok = new JButton("Ok");
    private final JSlider cores = new JSlider(SwingConstants.HORIZONTAL);

    private File worldDir = null;
    private File outputDir = null;

    Config config = null;
    WorldData fromWorld = null;
    WorldData toWorld = null;

    SetupWindow() {
        int fullWidth = 425;
        int buttonWidth = 100;
        int labelWidth = 75;
        int lineHeight = 25;

        // level.dat
        JTextField targetLevel = new JTextField();
        targetLevel.setPreferredSize(new Dimension(fullWidth - buttonWidth, lineHeight));
        // targetLevel.setText(WORKING_DIR.getAbsolutePath());
        JButton chooseLevel = new JButton("Choose");
        chooseLevel.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        chooseLevel.addActionListener(choose(targetLevel, JFileChooser.FILES_ONLY, f -> f.getName().endsWith(".dat"), f -> {
            Consumer<WorldData> consumer = worldData -> this.toWorld = worldData;
            this.loadLevelData(f, consumer);
        }));

        // world dir
        JTextField worldDir = new JTextField();
        worldDir.setPreferredSize(new Dimension(fullWidth - buttonWidth, lineHeight));
        // worldDir.setText(WORKING_DIR.getAbsolutePath());

        JButton chooseDir = new JButton("Choose");
        chooseDir.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        chooseDir.addActionListener(choose(worldDir, JFileChooser.DIRECTORIES_ONLY, f -> true, f -> {
            Consumer<WorldData> consumer = worldData -> this.fromWorld = worldData;
            this.loadLevelData(new File(f, "level.dat"), consumer);
            this.worldDir = f;
        }));

        // output dir
        JTextField outputDir = new JTextField();
        outputDir.setPreferredSize(new Dimension(fullWidth - buttonWidth, lineHeight));
        // outputDir.setText(WORKING_DIR.getAbsolutePath());

        JButton chooseOutput = new JButton("Choose");
        chooseOutput.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        chooseOutput.addActionListener(choose(outputDir, JFileChooser.DIRECTORIES_ONLY, File::isDirectory, f -> this.outputDir = f));

        // load config
        JButton chooseConfig = new JButton("Load Config");
        chooseConfig.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        chooseConfig.addActionListener(choose(new JTextField(), JFileChooser.FILES_ONLY, f -> f.getName().endsWith(".json"), this::loadConfig));

        // edit config
        JButton editConfig = new JButton("Edit Config");
        editConfig.addActionListener(editConfig());
        editConfig.setPreferredSize(new Dimension(buttonWidth, lineHeight));

        // save config
        JButton saveConfig = new JButton("Save Config");
        saveConfig.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        saveConfig.addActionListener(saveConfig());

        cores.setPreferredSize(new Dimension(fullWidth, 35));
        cores.setMinimum(1);
        cores.setMaximum(Runtime.getRuntime().availableProcessors() * 2);
        cores.setValue(Runtime.getRuntime().availableProcessors());
        cores.setMajorTickSpacing(1);
        cores.setPaintLabels(true);
        cores.setPaintTicks(true);
        cores.setPaintTrack(true);

        JCheckBox remap = new JCheckBox("Auto-Remap");
        remap.setSelected(false);
        remap.setToolTipText("Detect and remap blocks if they exist in the world and the level.dat but have different IDs");
        remap.addActionListener(e -> Config.setAutoRemap(remap.isSelected()));

        ok.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        ok.addActionListener(ok());

        JLabel levelLabel = new JLabel("Level File:");
        levelLabel.setToolTipText("The level.dat file to be used in the final converted world");
        levelLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
        JLabel worldLabel = new JLabel("World Dir:");
        worldLabel.setToolTipText("The world to be converted");
        worldLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
        JLabel outputLabel = new JLabel("Output Dir:");
        outputLabel.setToolTipText("The directory where the converted world will be exported");
        outputLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));
        JLabel coresLabel = new JLabel("Threads:");
        coresLabel.setToolTipText("The number of Threads the converter should use");
        coresLabel.setPreferredSize(new Dimension(labelWidth, lineHeight));

        this.setLayout(new GridLayout(6, 1));
        this.add(toPanel(levelLabel, targetLevel, chooseLevel));
        this.add(toPanel(worldLabel, worldDir, chooseDir));
        this.add(toPanel(outputLabel, outputDir, chooseOutput));
        this.add(toPanel(chooseConfig, editConfig, saveConfig));
        this.add(toPanel(coresLabel, cores));
        this.add(toPanel(remap, ok));
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

    private void loadLevelData(File target, Consumer<WorldData> consumer) {
        WorldData worldData = new WorldData(target);
        if (!worldData.validate()) {
            errorWindow("Invalid level.dat selected!", worldData.error());
            return;
        }
        try {
            worldData.loadRegistry();
            consumer.accept(worldData);
        } catch (Exception e) {
            errorWindow("An error occurred whilst reading the level data", worldData.error());
        }
    }

    private void loadConfig(File targetConfig) {
        Config config = null;
        try {
            config = NodeTypeAdapters.of(Config.class).fromNode(NodeAdapter.json().from(targetConfig));
        } catch (Exception ex) {
        }
        if (config == null) {
            config = new Config();
        }
        this.config = config;
    }

    private ActionListener choose(JTextField updateField, int mode, Predicate<File> fileFilter, Consumer<File> action) {
        return e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(mode);
            chooser.setFileHidingEnabled(false);
            chooser.ensureFileIsVisible(WORKING_DIR);
            chooser.setCurrentDirectory(WORKING_DIR);
            int response = chooser.showOpenDialog(SetupWindow.this);
            if (response == JFileChooser.APPROVE_OPTION) {
                File target = chooser.getSelectedFile();
                if (fileFilter.test(target)) {
                    updateField.setText(target.getAbsolutePath());
                    action.accept(target);
                }
            }
        };
    }

    private ActionListener editConfig() {
        return e -> {
            if (this.fromWorld == null) {
                errorWindow("World directory not selected!", "");
                return;
            }
            if (this.toWorld == null) {
                errorWindow("level.dat not selected!", "");
                return;
            }
            if (config == null) {
                config = new Config();
            }
            JFrame frame = new JFrame();
            frame.setTitle("Remapper");
            frame.setLayout(new GridBagLayout());
            frame.add(new MappingWindow(this, frame));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
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
                    Node node = NodeTypeAdapters.of(Config.class).toNode(config != null ? config : new Config());
                    NodeAdapter.json().to(node, out);
                    break;
                default:
                    break;
            }
        };
    }

    private ActionListener ok() {
        return e -> {
            if (this.fromWorld == null) {
                errorWindow("'From' world data not loaded correctly!", "");
                return;
            }

            if (this.toWorld == null) {
                errorWindow("'To' world data not loaded correctly!", "");
                return;
            }

            if (this.config == null) {
                errorWindow("No config has been loaded or set up!", "");
                return;
            }

            if (this.worldDir == null) {
                errorWindow("No world directory selected!", "");
                return;
            }

            if (this.outputDir == null) {
                errorWindow("No output directory selected!", "");
                return;
            }

            try {
                final World world = new World(worldDir, outputDir, fromWorld, toWorld, config, cores.getValue());

                new Thread() {
                    public void run() {
                        world.convert();
                    }
                }.start();

                ok.setEnabled(false);
            } catch (Exception ex) {
                errorWindow("Error occurred whilst processing region files", ex.getMessage());
            }
        };
    }

    private static void errorWindow(String label, String error) {
        JOptionPane.showMessageDialog(null, label, error, JOptionPane.ERROR_MESSAGE);
    }
}
