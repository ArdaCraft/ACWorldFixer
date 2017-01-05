package me.dags.blockr.app;

import me.dags.blockr.block.BlockInfo;

import javax.swing.*;
import java.awt.*;

public class Mapping extends JPanel {

    BlockInfo blockInfo;
    final String[] fromBlocks;
    final String[] toBlocks;
    private final MappingWindow parent;

    public Mapping(MappingWindow parent, BlockInfo blockInfo, String[] from, String[] to) {
        this.parent = parent;
        this.blockInfo = blockInfo;
        this.fromBlocks = from;
        this.toBlocks = to;
        this.update();
        this.setPreferredSize(new Dimension(850, 35));
        this.setMaximumSize(new Dimension(850, 40));
    }

    public void update() {
        this.removeAll();

        BlockInfo info = blockInfo;

        int fromId = parent.setup.fromWorld.blockRegistry.getId(info.name);
        int toId = parent.setup.toWorld.blockRegistry.getId(info.to.present() ? info.to.name : info.name);

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JPanel from = new JPanel();
        from.add(from(fromId, info));
        from.setPreferredSize(new Dimension(300, 35));
        from.setMinimumSize(new Dimension(300, 35));
        from.setAlignmentX(LEFT_ALIGNMENT);
        this.add(from);

        JPanel arrow = new JPanel();
        arrow.add(new JLabel(" -> "));
        arrow.setPreferredSize(new Dimension(10, 35));
        arrow.setMaximumSize(new Dimension(10, 35));
        this.add(arrow);

        JPanel to = new JPanel();
        to.add(to(toId, info.to.present() ? info.to : info));
        to.setPreferredSize(new Dimension(300, 35));
        to.setMinimumSize(new Dimension(300, 35));
        to.setAlignmentX(LEFT_ALIGNMENT);
        this.add(to);

        JButton edit = new JButton("Edit");
        edit.addActionListener(e -> edit(this));
        JButton copy = new JButton("Copy");
        copy.addActionListener(e -> parent.copyMapping(this));
        JButton remove = new JButton("Remove");
        remove.addActionListener(e -> parent.removeMapping(this));

        JPanel buttons = new JPanel();
        buttons.setPreferredSize(new Dimension(200, 35));
        buttons.setMaximumSize(new Dimension(200, 35));
        buttons.add(edit);
        buttons.add(copy);
        buttons.add(remove);

        this.add(buttons);

        parent.refresh();
    }

    private JLabel from(int id, BlockInfo info) {
        String biome = info.biome < 0 ? "-" : "" + info.biome;
        return blockLabel("(%s) %s[%s:%s] biome:%s", id, info.name, info.min, info.max, biome);
    }

    private JLabel to(int id, BlockInfo info) {
        return blockLabel("(%s) %s[%s:%s]", id, info.name, info.min, info.max);
    }

    private JLabel blockLabel(String fmt, Object... args) {
        return new JLabel(String.format(fmt, args));
    }

    private static void edit(Mapping mapping) {
        JFrame frame = new JFrame();
        frame.add(new MappingPopup(frame, mapping));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setTitle("Mapping Editor");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
}
