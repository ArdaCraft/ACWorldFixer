package me.dags.blockr.app;

import me.dags.blockr.BlockInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Mapping extends JPanel {

    BlockInfo blockInfo;
    final String[] fromBlocks;
    final String[] toBlocks;
    private final MappingWindow parent;
    private JPanel row = new JPanel();

    public Mapping(MappingWindow parent, BlockInfo blockInfo, String[] from, String[] to) {
        this.parent = parent;
        this.blockInfo = blockInfo;
        this.fromBlocks = from;
        this.toBlocks = to;
        add(row = row(blockInfo));
        this.setAlignmentY(TOP_ALIGNMENT);
    }

    public void update() {
        remove(row);
        add(row = row(blockInfo));
        parent.refresh();
    }

    private JPanel row(BlockInfo info) {
        int fromId = parent.setup.fromWorld.blockRegistry.getId(info.name);
        JLabel from = from(fromId, info);
        from.setMaximumSize(new Dimension(350, 20));

        int toId = parent.setup.toWorld.blockRegistry.getId(info.to.present() ? info.to.name : info.name);
        JLabel to = to(toId, info.to.present() ? info.to : info);
        to.setMaximumSize(new Dimension(325, 20));

        JPanel description = new JPanel();
        description.setLayout(new GridLayout(1, 2));
        description.setPreferredSize(new Dimension(675, 35));
        description.add(from);
        description.add(to);

        JButton edit = new JButton("Edit");
        edit.addActionListener(e -> edit(this));
        JButton copy = new JButton("Copy");
        copy.addActionListener(e -> parent.copyMapping(this));
        JButton remove = new JButton("Remove");
        remove.addActionListener(e -> parent.removeMapping(this));

        JPanel buttons = new JPanel();
        buttons.setPreferredSize(new Dimension(200, 35));
        buttons.add(edit);
        buttons.add(copy);
        buttons.add(remove);

        GridBagLayout layout = new GridBagLayout();

        JPanel row = new JPanel();
        row.setLayout(layout);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        row.add(description);
        row.add(buttons);

        return row;
    }

    private JLabel from(int id, BlockInfo info) {
        String biome = info.biome < 0 ? "-" : "" + info.biome;
        return blockLabel("(%s) %s[%s:%s] biome:%s", id, info.name, info.min, info.max, biome);
    }

    private JLabel to(int id, BlockInfo info) {
        return blockLabel(" --->  (%s) %s[%s:%s]", id, info.name, info.min, info.max);
    }

    private JLabel blockLabel(String fmt, Object... args) {
        return new JLabel(String.format(fmt, args));
    }

    private static void edit(Mapping mapping) {
        JFrame frame = new JFrame();
        frame.setLayout(new GridBagLayout());
        frame.add(new MappingPopup(frame, mapping));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setTitle("Mapping Editor");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
