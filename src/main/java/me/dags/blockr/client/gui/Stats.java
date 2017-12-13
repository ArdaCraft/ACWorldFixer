package me.dags.blockr.client.gui;

import me.dags.blockr.task.ChangeStats;

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
        int dimensions = ChangeStats.dimVisits.get();
        long extents = Math.abs(ChangeStats.extentVisits.get());
        long totalBlocks = Math.abs(ChangeStats.blockVisits.get());
        long blockChanged = Math.abs(ChangeStats.blockChanges.get());
        long entities = Math.abs(ChangeStats.entityChanges.get());
        long tileEntities = Math.abs(ChangeStats.tileEntityChanges.get());

        Double time = (ChangeStats.finish - ChangeStats.start) / 1000D;
        Double bps = totalBlocks / time;
        Double tps = ChangeStats.globalTasksComplete.get() / time;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(14, 1));

        JLabel dp = new JLabel(" Dimensions processed: " + ChangeStats.numFormat(dimensions));
        dp.setPreferredSize(new Dimension(250, 25));

        JLabel rp = new JLabel(" Regions processed: " + ChangeStats.numFormat(ChangeStats.globalTasksComplete.get()));
        rp.setPreferredSize(new Dimension(250, 25));

        JLabel cp = new JLabel(" Extents processed: " + ChangeStats.numFormat(extents));
        cp.setPreferredSize(new Dimension(250, 25));

        JLabel bp = new JLabel(" Blocks processed: " + ChangeStats.numFormat(totalBlocks));
        bp.setPreferredSize(new Dimension(250, 25));

        JLabel bc = new JLabel(" Blocks changed: " + ChangeStats.numFormat(blockChanged));
        bc.setPreferredSize(new Dimension(250, 25));

        JLabel er = new JLabel(" Entities changed: " + ChangeStats.numFormat(entities));
        er.setPreferredSize(new Dimension(250, 25));

        JLabel tr = new JLabel(" TileEntities removed: " + ChangeStats.numFormat(tileEntities));
        tr.setPreferredSize(new Dimension(250, 25));

        JLabel tt = new JLabel(" Time taken: " + ChangeStats.numFormat(time) + "s");
        tt.setPreferredSize(new Dimension(250, 25));

        JLabel bPS = new JLabel(" Blocks per second: " + ChangeStats.numFormat(bps));
        bPS.setPreferredSize(new Dimension(250, 25));

        JLabel rPS = new JLabel(" Tasks per second: " + ChangeStats.numFormat(tps));
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
