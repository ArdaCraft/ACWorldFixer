package me.dags.blockr.app;

import me.dags.blockr.block.BlockInfo;
import me.dags.blockr.Config;
import me.dags.data.node.NodeTypeAdapters;

import javax.swing.*;
import java.awt.*;

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

        NodeTypeAdapters.register(BlockInfo.class, new BlockInfo.Adapter());
        NodeTypeAdapters.register(Config.class, new Config.Adapter());

        JFrame frame = new JFrame();
        frame.setTitle("MassBlockr");
        frame.setLayout(new GridBagLayout());
        frame.add(new SetupWindow());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
