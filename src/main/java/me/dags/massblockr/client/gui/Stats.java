package me.dags.massblockr.client.gui;

import me.dags.massblockr.util.StatCounters;

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
        int dimensions = StatCounters.dimVisits.get();
        long extents = Math.abs(StatCounters.chunkVisits.get());
        long totalBlocks = Math.abs(StatCounters.blockVisits.get());
        long blockChanged = Math.abs(StatCounters.blockChanges.get());
        long entities = Math.abs(StatCounters.entityChanges.get());
        long tileEntities = Math.abs(StatCounters.tileEntityChanges.get());

        Double time = (StatCounters.finish - StatCounters.start) / 1000D;
        Double bps = totalBlocks / time;
        Double tps = StatCounters.globalTasksComplete.get() / time;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(14, 1));

        JLabel dp = new JLabel(" Dimensions processed: " + StatCounters.numFormat(dimensions));
        dp.setPreferredSize(new Dimension(250, 25));

        JLabel rp = new JLabel(" Regions processed: " + StatCounters.numFormat(StatCounters.globalTasksComplete.get()));
        rp.setPreferredSize(new Dimension(250, 25));

        JLabel cp = new JLabel(" Extents processed: " + StatCounters.numFormat(extents));
        cp.setPreferredSize(new Dimension(250, 25));

        JLabel bp = new JLabel(" Blocks processed: " + StatCounters.numFormat(totalBlocks));
        bp.setPreferredSize(new Dimension(250, 25));

        JLabel bc = new JLabel(" Blocks changed: " + StatCounters.numFormat(blockChanged));
        bc.setPreferredSize(new Dimension(250, 25));

        JLabel er = new JLabel(" Entities changed: " + StatCounters.numFormat(entities));
        er.setPreferredSize(new Dimension(250, 25));

        JLabel tr = new JLabel(" TileEntities removed: " + StatCounters.numFormat(tileEntities));
        tr.setPreferredSize(new Dimension(250, 25));

        JLabel tt = new JLabel(" Time taken: " + StatCounters.numFormat(time) + "s");
        tt.setPreferredSize(new Dimension(250, 25));

        JLabel bPS = new JLabel(" Blocks per second: " + StatCounters.numFormat(bps));
        bPS.setPreferredSize(new Dimension(250, 25));

        JLabel rPS = new JLabel(" Tasks per second: " + StatCounters.numFormat(tps));
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
