package me.dags.blockr.task;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dags <dags@dags.me>
 */
public class ChangeStats {

    private static final AtomicInteger dimVisits = new AtomicInteger(0);
    private static final AtomicLong extentVisits = new AtomicLong(0);
    private static final AtomicLong blockVisits = new AtomicLong(0);
    private static final AtomicLong blockChanges = new AtomicLong(0);
    private static final AtomicLong entityChanges = new AtomicLong(0);
    private static final AtomicLong tileEntityChanges = new AtomicLong(0);

    public static final AtomicBoolean running = new AtomicBoolean(true);
    public static final AtomicInteger dimTaskCount = new AtomicInteger(0);
    public static final AtomicInteger dimTaskProgress = new AtomicInteger(0);
    public static final AtomicInteger overallTaskCount = new AtomicInteger(0);
    public static final AtomicInteger overallTaskProgress = new AtomicInteger(0);

    private static long start = 0L;
    private static long finish = 0L;

    public static void punchIn() {
        start = System.currentTimeMillis();
    }

    public static void punchOut() {
        finish = System.currentTimeMillis();
    }

    public static void incDimensionCount() {
        dimVisits.getAndAdd(1);
    }

    public static void incExtentCount() {
        extentVisits.addAndGet(1);
    }

    public static void incBlockVisits(long count) {
        blockVisits.getAndAdd(count);
    }

    public static void incBlockChanges() {
        blockChanges.getAndAdd(1);
    }

    public static void incEntityChanges() {
        entityChanges.getAndAdd(1);
    }

    public static void incTileEntityChanges() {
        tileEntityChanges.getAndAdd(1);
    }

    public static void displayResults(int cores) {
        JFrame frame = new JFrame();
        frame.add(getStats(cores));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private static JPanel getStats(int cores) {
        int dimensions = dimVisits.get();
        long extents = Math.abs(extentVisits.get());
        long totalBlocks = Math.abs(blockVisits.get());
        long blockChanged = Math.abs(blockChanges.get());
        long entities = Math.abs(entityChanges.get());
        long tileEntities = Math.abs(tileEntityChanges.get());

        Double time = (finish - start) / 1000D;
        Double bps = totalBlocks / time;
        Double tps = overallTaskProgress.get() / time;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(14, 1));

        JLabel dp = new JLabel(" Dimensions processed: " + numFormat(dimensions));
        dp.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel rp = new JLabel(" Regions processed: " + numFormat(overallTaskProgress.get()));
        rp.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel cp = new JLabel(" Extents processed: " + numFormat(extents));
        cp.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel bp = new JLabel(" Blocks processed: " + numFormat(totalBlocks));
        bp.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel bc = new JLabel(" Blocks changed: " + numFormat(blockChanged));
        bc.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel er = new JLabel(" Entities changed: " + numFormat(entities));
        er.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel tr = new JLabel(" TileEntities removed: " + numFormat(tileEntities));
        tr.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel tt = new JLabel(" Time taken: " + numFormat(time) + "s");
        tt.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel bPS = new JLabel(" Blocks per second: " + numFormat(bps));
        bPS.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel rPS = new JLabel(" Tasks per second: " + numFormat(tps));
        rPS.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel cU = new JLabel(" Threads used: " + cores);
        cU.setPreferredSize(new Dimension(250, 25));

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
        panel.add(new JLabel());
        panel.add(cU);

        return panel;
    }

    private static String numFormat(Number in) {
        String s = NumberFormat.getInstance().format(in);
        if (in instanceof Double) {
            return s.length() > 15 ? s.substring(0, 15) : s;
        }
        return s;
    }
}
