package me.dags.massblockr.client.gui;

import me.dags.massblockr.App;
import me.dags.massblockr.client.Client;
import me.dags.massblockr.client.Options;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author dags <dags@dags.me>
 */
public class GuiApp implements App {

    private static final JLabel SPACER = new JLabel("  ");
    private static final File DIR = new File("").getAbsoluteFile();

    private final JFrame frame = new JFrame();

    @Override
    public Client newClient(Options options) {
        return new GuiClient(options.threads);
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, writer, t.getMessage(), JOptionPane.ERROR_MESSAGE));
    }

    @Override
    public void launch(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Slider.focus", UIManager.get("Slider.background"));
        } catch (Throwable e) {
            e.printStackTrace();
        }

        frame.setResizable(false);
        frame.setTitle("World Converter");
        frame.setMinimumSize(new Dimension(300, 100));

        selectWorld(frame, new Options());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void selectWorld(JFrame frame, Options options) {
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

    private void selectLevel(JFrame frame, Options options) {
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
            selectOptions(frame, options);
        };
        choose.addActionListener(e -> chooser(root, title, error, JFileChooser.FILES_ONLY, predicate, action));

        JButton skip = new JButton("Skip");
        skip.addActionListener(e -> SwingUtilities.invokeLater(() -> selectOptions(frame, options)));

        root.add(new JLabel("Choose a custom level.dat file"));
        root.add(SPACER);
        root.add(choose);
        root.add(skip);

        frame.setContentPane(root);
        frame.pack();
    }

    private void selectOptions(JFrame frame, Options options) {
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

        JCheckBox remap = new JCheckBox("Auto-Remap");
        remap.setToolTipText(tooltips("Attempt to remap blocks with mismatching block ids."));
        remap.setSelected(true);
        autoRemap.add(remap);

        JCheckBox onlyRemap = new JCheckBox("Remap Only");
        onlyRemap.setToolTipText(tooltips("Only remap mis-matching block ids."));
        onlyRemap.setSelected(false);
        onlyRemap.setEnabled(true);
        autoRemap.add(onlyRemap);

        remap.addActionListener(actionEvent -> {
            onlyRemap.setEnabled(remap.isSelected());
            if (!onlyRemap.isEnabled()) {
                onlyRemap.setSelected(false);
            }
        });

        JCheckBox onlySchems = new JCheckBox("Schematics Only");
        onlySchems.setToolTipText(tooltips("Only convert schematics ignoring any volume files."));
        onlyRemap.setSelected(false);
        autoRemap.add(onlySchems);

        JPanel buttons = root();
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> System.exit(0));

        JButton start = new JButton("Start");
        start.addActionListener(e -> {
            options.threads = threadSlider.getValue();
            options.remap = remap.isSelected();
            options.remapOnly = options.remap && onlyRemap.isSelected();
            options.schemsOnly = onlySchems.isSelected();
            start.setEnabled(false);
            submit(options);
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

    private static JPanel root() {
        JPanel root = new JPanel();
        root.setLayout(new GridBagLayout());
        root.setAlignmentY(Component.CENTER_ALIGNMENT);
        return root;
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
}