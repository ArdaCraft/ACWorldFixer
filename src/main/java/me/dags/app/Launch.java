package me.dags.app;

import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import me.dags.blockinfo.BlockInfo;
import me.dags.blockinfo.Config;
import me.dags.data.node.NodeAdapters;

/**
 * @author dags <dags@dags.me>
 */
public class Launch {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        NodeAdapters.register(BlockInfo.class, new BlockInfo.Adapter());
        NodeAdapters.register(Config.class, new Config.Adapter());

        JFrame frame = new JFrame();
        frame.setTitle("ACWorldFixer");
        frame.setLayout(new GridBagLayout());
        frame.add(new SetupWindow());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
