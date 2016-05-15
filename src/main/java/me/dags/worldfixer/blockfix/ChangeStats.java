package me.dags.worldfixer.blockfix;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dags <dags@dags.me>
 */
public class ChangeStats {

    private static final AtomicInteger blockChanges = new AtomicInteger(0);
    private static final AtomicInteger entitiesRemoved = new AtomicInteger(0);
    private static final AtomicInteger tileEntitiesRemoved = new AtomicInteger(0);

    private static long start = 0L;
    private static long finish = 0L;

    static void punchIn() {
        start = System.currentTimeMillis();
    }

    static void punchOut() {
        finish = System.currentTimeMillis();
    }

    static void incBlockCount() {
        blockChanges.getAndAdd(1);
    }

    static void incEntityCount() {
        entitiesRemoved.getAndAdd(1);
    }

    static void incTileEntityCount() {
        tileEntitiesRemoved.getAndAdd(1);
    }

    static void displayResults(int regionCount, int cores) {
        JFrame frame = new JFrame();
        frame.add(getStats(regionCount, cores));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private static JPanel getStats(int regionCount, int cores) {
        int blocks = blockChanges.get();
        int entities = entitiesRemoved.get();
        int tileEntities = tileEntitiesRemoved.get();
        int totalBlocks = regionCount * 32 * 16 * 16 * 256;

        double time = (finish - start) / 1000D;
        double bps = totalBlocks / time;
        double rps = regionCount / time;

        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(12, 1));
        JLabel rp = new JLabel("Regions processed: " + numFormat(regionCount));
        rp.setPreferredSize(new Dimension(250, 25));

        JLabel bp = new JLabel("Blocks processed: " + numFormat(totalBlocks));
        bp.setPreferredSize(new Dimension(250, 25));

        JLabel bc = new JLabel("Blocks changed: " + numFormat(blocks));
        bc.setPreferredSize(new Dimension(250, 25));

        JLabel er = new JLabel("Entities removed: " + numFormat(entities));
        er.setPreferredSize(new Dimension(250, 25));

        JLabel tr = new JLabel("TileEntities removed: " + numFormat(tileEntities));
        tr.setPreferredSize(new Dimension(250, 25));

        JLabel tt = new JLabel("Time taken: " + numFormat(time) + "s");
        tt.setPreferredSize(new Dimension(250, 25));

        JLabel bPS = new JLabel("Blocks per second: " + numFormat(bps));
        bPS.setPreferredSize(new Dimension(250, 25));

        JLabel rPS = new JLabel("Regions per second: " + numFormat(rps));
        rPS.setPreferredSize(new Dimension(250, 25));

        JLabel cU = new JLabel("Cores used: " + cores);
        cU.setPreferredSize(new Dimension(250, 25));

        panel.add(rp);
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
        return s.length() > 15 ? s.substring(0, 15) : s;
    }
}
