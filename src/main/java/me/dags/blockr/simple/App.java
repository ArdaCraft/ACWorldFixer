package me.dags.blockr.simple;

import me.dags.blockr.Config;
import me.dags.blockr.block.BlockInfo;
import me.dags.blockr.world.World;
import me.dags.blockr.world.WorldData;
import me.dags.blockr.world.WorldDataFile;
import me.dags.blockr.world.WorldDataResource;
import me.dags.data.NodeAdapter;
import me.dags.data.node.NodeTypeAdapters;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class App {

    private static final JLabel SPACER = new JLabel("  ");
    private static final File DIR = new File("").getAbsoluteFile();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Slider.focus", UIManager.get("Slider.background"));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        NodeTypeAdapters.register(BlockInfo.class, new BlockInfo.Adapter());
        NodeTypeAdapters.register(Config.class, new Config.Adapter());

        JFrame frame = new JFrame();
        frame.setResizable(false);
        frame.setTitle("World Converter");
        frame.setMinimumSize(new Dimension(300, 100));

        App.selectWorld(frame, new Options());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private static JPanel root() {
        JPanel root = new JPanel();
        root.setLayout(new GridBagLayout());
        root.setAlignmentY(Component.CENTER_ALIGNMENT);
        return root;
    }

    private static void selectWorld(JFrame frame, Options options) {
        JPanel root = root();

        JButton choose = new JButton("Choose");
        String title = "Select a World";
        String error = "%s does not appear to be a valid world folder";
        Predicate<File> predicate = file -> {
            File level = new File(file, "level.dat");
            return level.exists();
        };
        Consumer<File> action = file -> {
            options.world = file;
            selectLevel(frame, options);
        };
        choose.addActionListener(e -> chooser(root, title, error, JFileChooser.DIRECTORIES_ONLY, predicate, action));

        root.add(new JLabel("Choose a world folder"));
        root.add(SPACER);
        root.add(choose);
        frame.setContentPane(root);
        frame.pack();
    }

    private static void selectLevel(JFrame frame, Options options) {
        JPanel root = root();
        root.setToolTipText(tooltips(
                "Choose a custom level.dat file to be used in the converted world"
        ));

        JButton choose = new JButton("Choose");
        String title = "Select a level.dat";
        String error = "%s does not appear to be a valid level.dat file";
        Predicate<File> predicate = file -> file.getName().equals("level.dat");
        Consumer<File> action = file -> {
            options.level = file;
            start(frame, options);
        };
        choose.addActionListener(e -> chooser(root, title, error, JFileChooser.FILES_ONLY, predicate, action));

        JButton skip = new JButton("Skip");
        skip.addActionListener(e -> SwingUtilities.invokeLater(() -> start(frame, options)));

        root.add(new JLabel("Choose a custom level.dat file"));
        root.add(SPACER);
        root.add(choose);
        root.add(skip);

        frame.setContentPane(root);
        frame.pack();
    }

    private static void start(JFrame frame, Options options) {
        JPanel threads = root();
        threads.setToolTipText(tooltips(
                "Set the maximum number of threads the converter can use.",
                "Higher values may slow your computer during the conversion."
        ));

        JSlider threadSlider = new JSlider();
        threadSlider.setMinimum(1);
        threadSlider.setMaximum(Runtime.getRuntime().availableProcessors() * 2);
        threadSlider.setValue(Runtime.getRuntime().availableProcessors());
        threadSlider.setMajorTickSpacing(1);
        threadSlider.setPaintLabels(true);
        threadSlider.setPaintTicks(true);
        threadSlider.setPaintTrack(true);
        threads.add(new JLabel("Threads"));
        threads.add(SPACER);
        threads.add(threadSlider);

        JPanel autoRemap = root();
        autoRemap.setToolTipText(tooltips("Attempt to remap blocks with mismatching block ids."));

        JCheckBox remap = new JCheckBox("Auto-Remap");
        remap.setSelected(true);
        autoRemap.add(remap);

        JPanel buttons = root();
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> System.exit(0));

        JButton start = new JButton("Start");
        start.addActionListener(e -> {
            options.threads = threadSlider.getValue();
            options.remap = remap.isSelected();
            start.setEnabled(false);
            start(options);
        });
        buttons.add(cancel);
        buttons.add(start);

        JPanel root = new JPanel();
        root.setLayout(new GridLayout(3, 1));
        root.add(threads);
        root.add(autoRemap);
        root.add(buttons);

        frame.setContentPane(root);
        frame.pack();
    }

    private static void start(Options options) {
        try (InputStream in = App.class.getResourceAsStream("/mappings.json")) {
            File source = options.world;
            File output = new File(source.getParent());
            WorldData from = new WorldDataFile(new File(source, "level.dat"));
            WorldData to = options.level == null ? new WorldDataResource("/level.dat") : new WorldDataFile(options.level);
            Config config = NodeTypeAdapters.of(Config.class).fromNode(NodeAdapter.json().from(in));
            Config.do_entities = false;
            Config.auto_remap = options.remap;
            int threads = options.threads;
            World world = new World(source, output, from, to, config, threads);
            new Thread(world::convert).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void chooser(JPanel parent, String title, String error, int mode, Predicate<File> predicate, Consumer<File> consumer) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(DIR);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(mode);
        chooser.setFileHidingEnabled(false);

        int option = chooser.showOpenDialog(parent);

        if (option == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (predicate.test(selected)) {
                SwingUtilities.invokeLater(() -> consumer.accept(selected));
            } else {
                JOptionPane.showMessageDialog(parent, String.format(error, selected));
            }
        }
    }

    private static String tooltips(String... lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        {
            for (String line : lines) {
                sb.append(line).append("<br>");
            }
        }
        sb.append("</html>");
        return sb.toString();
    }

    private static class Options {

        private File world = null;
        private File level = null;
        private int threads = 1;
        private boolean remap = true;
    }
}