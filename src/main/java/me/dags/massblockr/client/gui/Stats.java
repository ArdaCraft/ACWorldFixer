package me.dags.massblockr.client.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author dags <dags@dags.me>
 */
public class Stats {

    public static void displayResults() {
        JFrame frame = new JFrame();
        frame.add(getStats());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private static JPanel getStats() {
        int dimensions = me.dags.massblockr.util.Stats.dimVisits.get();
        long extents = Math.abs(me.dags.massblockr.util.Stats.extentVisits.get());
        long totalBlocks = Math.abs(me.dags.massblockr.util.Stats.blockVisits.get());
        long blockChanged = Math.abs(me.dags.massblockr.util.Stats.blockChanges.get());
        long entities = Math.abs(me.dags.massblockr.util.Stats.entityChanges.get());
        long tileEntities = Math.abs(me.dags.massblockr.util.Stats.tileEntityChanges.get());

        Double time = (me.dags.massblockr.util.Stats.finish - me.dags.massblockr.util.Stats.start) / 1000D;
        Double bps = totalBlocks / time;
        Double tps = me.dags.massblockr.util.Stats.globalTasksComplete.get() / time;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(14, 1));

        JLabel dp = new JLabel(" Dimensions processed: " + me.dags.massblockr.util.Stats.numFormat(dimensions));
        dp.setPreferredSize(new Dimension(250, 25));

        JLabel rp = new JLabel(" Regions processed: " + me.dags.massblockr.util.Stats.numFormat(me.dags.massblockr.util.Stats.globalTasksComplete.get()));
        rp.setPreferredSize(new Dimension(250, 25));

        JLabel cp = new JLabel(" Extents processed: " + me.dags.massblockr.util.Stats.numFormat(extents));
        cp.setPreferredSize(new Dimension(250, 25));

        JLabel bp = new JLabel(" Blocks processed: " + me.dags.massblockr.util.Stats.numFormat(totalBlocks));
        bp.setPreferredSize(new Dimension(250, 25));

        JLabel bc = new JLabel(" Blocks changed: " + me.dags.massblockr.util.Stats.numFormat(blockChanged));
        bc.setPreferredSize(new Dimension(250, 25));

        JLabel er = new JLabel(" Entities changed: " + me.dags.massblockr.util.Stats.numFormat(entities));
        er.setPreferredSize(new Dimension(250, 25));

        JLabel tr = new JLabel(" TileEntities removed: " + me.dags.massblockr.util.Stats.numFormat(tileEntities));
        tr.setPreferredSize(new Dimension(250, 25));

        JLabel tt = new JLabel(" Time taken: " + me.dags.massblockr.util.Stats.numFormat(time) + "s");
        tt.setPreferredSize(new Dimension(250, 25));

        JLabel bPS = new JLabel(" Blocks per second: " + me.dags.massblockr.util.Stats.numFormat(bps));
        bPS.setPreferredSize(new Dimension(250, 25));

        JLabel rPS = new JLabel(" Tasks per second: " + me.dags.massblockr.util.Stats.numFormat(tps));
        rPS.setPreferredSize(new Dimension(250, 25));

        panel.add(dp);
        panel.add(rp);
        panel.add(cp);
        panel.add(bp);
        panel.add(bc);
        panel.add(er);
        panel.add(tr);
        panel.add(new JLabel());
        panel.add(tt);
        panel.add(bPS);
        panel.add(rPS);

        return panel;
    }
}
