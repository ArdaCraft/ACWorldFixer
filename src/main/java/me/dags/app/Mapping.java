package me.dags.app;

import me.dags.blockinfo.BlockInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Mapping extends JPanel {

    private static final Dimension NAME = new Dimension(200, 25);
    private static final Dimension DATA = new Dimension(40, 25);

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

        JLabel min = new JLabel("min: " + info.min);
        min.setPreferredSize(DATA);

        JLabel max = new JLabel("max: " + info.max);
        max.setPreferredSize(DATA);

        JLabel to = new JLabel(info.to.name);
        to.setPreferredSize(NAME);

        JLabel data = new JLabel("data: " + info.to.min);
        data.setPreferredSize(DATA);

        JButton edit = new JButton("Edit");
        edit.addActionListener(e -> edit(this));

        JButton copy = new JButton("Copy");
        copy.addActionListener(e -> parent.copyMapping(this));

        JButton remove = new JButton("Remove");
        remove.addActionListener(e -> parent.removeMapping(this));

        row.add(from);
        row.add(min);
        row.add(max);
        row.add(new JLabel("---->  "));
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
