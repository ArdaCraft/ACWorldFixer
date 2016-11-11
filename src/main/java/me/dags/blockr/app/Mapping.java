package me.dags.blockr.app;

import me.dags.blockr.BlockInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Mapping extends JPanel {

    private static final Dimension NAME = new Dimension(185, 25);
    private static final Dimension DATA = new Dimension(50, 25);

    BlockInfo blockInfo;
    final String[] blocks;
    private final MappingWindow parent;
    private JPanel row = new JPanel();

    public Mapping(MappingWindow parent, BlockInfo blockInfo, String[] blocks) {
        this.parent = parent;
        this.blockInfo = blockInfo;
        this.blocks = blocks;
        add(row = row(blockInfo));
        this.setAlignmentY(TOP_ALIGNMENT);
        this.setMinimumSize(new Dimension(800, 45));
        this.setMaximumSize(new Dimension(900, 45));
    }

    public void update() {
        remove(row);
        add(row = row(blockInfo));
        parent.refresh();
    }

    private JPanel row(BlockInfo info) {
        JPanel row = new JPanel();
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

        JLabel from = new JLabel(info.name);
        from.setPreferredSize(NAME);

        JLabel biome = new JLabel("biome: " + (info.biome < 0 ? "-" : info.biome));
        biome.setPreferredSize(DATA);

        JLabel meta = new JLabel("meta(s): " + info.min + " to " + info.max);
        meta.setPreferredSize(new Dimension(85, 25));

        JLabel to = new JLabel(info.to.present() ? info.to.name : info.name);
        to.setPreferredSize(NAME);

        JLabel data = new JLabel("meta: " + (info.to.min < 0 ? "-" : info.to.min));
        data.setPreferredSize(DATA);

        JButton edit = new JButton("Edit");
        edit.addActionListener(e -> edit(this));

        JButton copy = new JButton("Copy");
        copy.addActionListener(e -> parent.copyMapping(this));

        JButton remove = new JButton("Remove");
        remove.addActionListener(e -> parent.removeMapping(this));

        row.add(from);
        row.add(biome);
        row.add(meta);
        row.add(new JLabel("---->"));
        row.add(to);
        row.add(data);
        row.add(edit);
        row.add(copy);
        row.add(remove);
        return row;
    }

    private ActionListener copy() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                parent.copyMapping(Mapping.this);
            }

        };
    }

    private void copy(Mapping mapping) {
        parent.copyMapping(mapping);
    }

    private void remove(Mapping mapping) {
        parent.removeMapping(mapping);
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
