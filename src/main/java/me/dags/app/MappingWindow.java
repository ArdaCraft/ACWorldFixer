package me.dags.app;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import me.dags.blockinfo.BlockInfo;
import me.dags.blockinfo.Config;

public class MappingWindow extends JPanel {

    final SetupWindow setup;
    final JFrame parent;
    final JPanel content = new JPanel();
    final Config config;
    final String[] blockNames;
    final List<Mapping> mappings = new ArrayList<>();
    final JScrollPane scrollPane = new JScrollPane(content);

    public MappingWindow(SetupWindow setup, JFrame parent) {
        this.parent = parent;
        this.setup = setup;
        this.config = setup.config;
        this.blockNames = setup.levelFixer.worldData.blockRegistry.blockNames();

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        scrollPane.setAutoscrolls(true);
        scrollPane.setPreferredSize(new Dimension(850, 600));
        scrollPane.getVerticalScrollBar().setUnitIncrement(100);

        JPanel container = new JPanel();
        container.setPreferredSize(new Dimension(90, 50));

        JButton create = new JButton("New");
        container.add(create);
        create.setAlignmentX(Component.CENTER_ALIGNMENT);
        create.addActionListener(e -> create());

        JButton done = new JButton("Done");
        container.add(done);
        done.setAlignmentX(Component.CENTER_ALIGNMENT);
        done.addActionListener(e -> done());

        JPanel all = new JPanel();
        all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
        all.setPreferredSize(new Dimension(850, 600));
        all.add(scrollPane);
        all.add(container);

        this.add(all);

        for (BlockInfo info : config.blocks) {
            if (setup.levelFixer.worldData.blockRegistry.getId(info.name) != null) {
                Mapping mapping = new Mapping(this, info, blockNames);
                mappings.add(mapping);
                content.add(mapping);
            }
        }
    }

    private void create() {
        BlockInfo to = new BlockInfo("minecraft:air", 0, false);
        BlockInfo from = new BlockInfo(to.name, 0, 0, to);
        Mapping mapping = new Mapping(this, from, blockNames);
        mappings.add(mapping);
        refresh();
    }

    private void done() {
        setup.updateConfig(toConfig());
        parent.dispose();
    }

    public Config toConfig() {
        Config config = new Config();
        mappings.forEach(m -> config.blocks.add(m.blockInfo));
        config.removeBlocks.putAll(this.config.removeBlocks);
        config.entities.addAll(this.config.entities);
        config.tileEntities.addAll(this.config.tileEntities);
        return config;
    }

    public void refresh() {
        content.removeAll();
        for (Mapping mapping : mappings) {
            content.add(mapping);
        }
        content.revalidate();
        content.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    public void copyMapping(Mapping mapping) {
        Mapping newMapping = new Mapping(this, mapping.blockInfo.copy(), blockNames);
        mappings.add(mappings.indexOf(mapping) + 1, newMapping);
        refresh();
    }

    public void removeMapping(Mapping mapping) {
        mappings.remove(mapping);
        refresh();
    }
}
