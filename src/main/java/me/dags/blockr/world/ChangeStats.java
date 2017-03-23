package me.dags.blockr.world;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.*;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dags <dags@dags.me>
 */
public class ChangeStats {

    private static final AtomicInteger dimensionsComplete = new AtomicInteger(0);
    private static final AtomicInteger overallRegionProgress = new AtomicInteger(0);
    private static final AtomicLong chunksComplete = new AtomicLong(0);
    private static final AtomicLong blockChanges = new AtomicLong(0);
    private static final AtomicLong entitiesChanged = new AtomicLong(0);
    private static final AtomicLong tileEntitiesRemoved = new AtomicLong(0);

    static final AtomicBoolean running = new AtomicBoolean(true);
    static final AtomicInteger regionCount = new AtomicInteger(0);
    static final AtomicInteger regionProgress = new AtomicInteger(0);
    static final AtomicInteger overallRegionCount = new AtomicInteger(0);

    private static long start = 0L;
    private static long finish = 0L;

    public static int getProgress() {
        return overallRegionProgress.get();
    }

    public static void punchIn() {
        start = System.currentTimeMillis();
    }

    public static void punchOut() {
        finish = System.currentTimeMillis();
    }

    static void incDimensionCount() {
        dimensionsComplete.getAndAdd(1);
    }

    static void incRegionsCount() {
        overallRegionProgress.addAndGet(1);
    }

    static void incChunkCount() {
        chunksComplete.addAndGet(1);
    }

    static void incBlockCount() {
        blockChanges.getAndAdd(1);
    }

    static void incEntityCount() {
        entitiesChanged.getAndAdd(1);
    }

    static void incTileEntityCount() {
        tileEntitiesRemoved.getAndAdd(1);
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
        int dimensions = dimensionsComplete.get();
        long chunks = Math.abs(chunksComplete.get());
        long blocks = Math.abs(blockChanges.get());
        long entities = Math.abs(entitiesChanged.get());
        long tileEntities = Math.abs(tileEntitiesRemoved.get());
        long totalBlocks = Math.abs(chunks * 16 * 16 * 256);

        Double time = (finish - start) / 1000D;
        Double bps = totalBlocks / time;
        Double rps = getProgress() / time;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(14, 1));

        JLabel dp = new JLabel(" Dimensions processed: " + numFormat(dimensions));
        dp.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel rp = new JLabel(" Regions processed: " + numFormat(getProgress()));
        rp.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel cp = new JLabel(" Chunks processed: " + numFormat(chunks));
        cp.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel bp = new JLabel(" Blocks processed: " + numFormat(totalBlocks));
        bp.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel bc = new JLabel(" Blocks changed: " + numFormat(blocks));
        bc.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel er = new JLabel(" Entities changed: " + numFormat(entities));
        er.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel tr = new JLabel(" TileEntities removed: " + numFormat(tileEntities));
        tr.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel tt = new JLabel(" Time taken: " + numFormat(time) + "s");
        tt.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel bPS = new JLabel(" Blocks per second: " + numFormat(bps));
        bPS.setPreferredSize(new java.awt.Dimension(250, 25));

        JLabel rPS = new JLabel(" Regions per second: " + numFormat(rps));
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
