package me.dags.app;

import me.dags.blockinfo.BlockInfo;

import javax.swing.*;
import java.awt.*;

public class MappingPopup extends JPanel {

    private final Frame parent;
    private final Mapping mapping;
    private final JComboBox<String> from;
    private final JSpinner min;
    private final JSpinner max;
    private final JComboBox<String> to;
    private final JSpinner data;

    public MappingPopup(Frame parent, Mapping mapping) {
        this.parent = parent;
        this.mapping = mapping;
        from = new JComboBox<>(mapping.blocks);
        from.setSelectedItem(mapping.blockInfo.name);

        SpinnerModel minModel = new SpinnerNumberModel(mapping.blockInfo.min, 0, 16, 1);
        min = new JSpinner(minModel);

        SpinnerModel maxModel = new SpinnerNumberModel(mapping.blockInfo.max, 0, 16, 1);
        max = new JSpinner(maxModel);

        to = new JComboBox<>(mapping.blocks);
        to.setSelectedItem(mapping.blockInfo.to.name);

        SpinnerModel dataModel = new SpinnerNumberModel(mapping.blockInfo.to.min, 0, 16, 1);
        data = new JSpinner(dataModel);

        JButton done = new JButton("Done");
        done.addActionListener(e -> updateMapping());

        this.add(from);
        this.add(min);
        this.add(max);
        this.add(new JLabel("to"));
        this.add(to);
        this.add(data);
        this.add(done);
    }

    public void updateMapping() {
        String fromName = from.getSelectedItem().toString();
        int minData = (int) min.getValue();
        int maxData = (int) max.getValue();
        String toName = to.getSelectedItem().toString();
        int toData = (int) data.getValue();
        BlockInfo to = new BlockInfo(toName, toData, false);
        mapping.blockInfo = new BlockInfo(fromName, minData, maxData, to);
        mapping.update();
        parent.dispose();
    }
}
