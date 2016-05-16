package me.dags.app;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import me.dags.blockinfo.Config;
import me.dags.data.json.JsonSerializer;
import me.dags.worldfixer.BlockFixer;
import me.dags.worldfixer.LevelFixer;
import me.dags.worldfixer.WorldData;

/**
 * @author dags <dags@dags.me>
 */
public class SetupWindow extends JPanel {

    private static final File WORKING_DIR = new File(new File("").getAbsolutePath());
    private final JTextField targetDir = new JTextField();
    private final JTextField targetConfig = new JTextField();
    private final JSlider cores = new JSlider(SwingConstants.HORIZONTAL);

    Config config = null;
    LevelFixer levelFixer = null;
    BlockFixer blockFixer = null;

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

        targetConfig.setPreferredSize(new Dimension(fullWidth - buttonWidth, lineHeight));
        targetConfig.setText("https://raw.githubusercontent.com/ArdaCraft/ACWorldFixer/master/block_data.json");

        JButton loadConfig = new JButton("Modify");
        loadConfig.setPreferredSize(new Dimension(buttonWidth, lineHeight));
        loadConfig.addActionListener(loadConfig(targetConfig));

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
        this.add(toPanel(configLabel, targetConfig, loadConfig));
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
            errorWindow("Invalid world directory selected!", worldData.error());
            return;
        }
        this.levelFixer = new LevelFixer(worldData);
        this.levelFixer.loadRegistry();
    }

    private void loadConfig() {
        Config config = null;
        try (InputStream inputStream = new URL(targetConfig.getText()).openConnection().getInputStream()) {
            config = JsonSerializer.pretty().deserialize(inputStream, Config.class);
        } catch (Exception ex) {
        }
        if (config == null) {
            config = new Config();
        }
        this.config = config;
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
                    loadLevelData(target);
                    break;
                default:
                    break;
            }
        };
    }

    private ActionListener loadConfig(JTextField configField) {
        return e -> {
            if (this.levelFixer == null) {
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

    private ActionListener ok() {
        return e -> {
            if (this.levelFixer == null) {
                errorWindow("World directory not loaded correctly!", "");
                return;
            }
            if (this.config == null) {
                loadConfig();
            }

            this.levelFixer.removeBlocks(config.removeBlocks.keySet());
            this.levelFixer.writeChanges();

            try {
                BlockFixer blockFixer = new BlockFixer(config, levelFixer.worldData, cores.getValue());

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
