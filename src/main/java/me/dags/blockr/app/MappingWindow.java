package me.dags.blockr.app;

import me.dags.blockr.Config;
import me.dags.blockr.block.BlockInfo;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MappingWindow extends JPanel {

    final SetupWindow setup;
    final JFrame parent;
    final JPanel content = new JPanel();
    final Config config;
    final String[] fromBlockNames;
    final String[] toBlockNames;
    final List<Mapping> mappings = new ArrayList<>();
    final JScrollPane scrollPane = new JScrollPane(content);

    public MappingWindow(SetupWindow setup, JFrame parent) {
        this.parent = parent;
        this.setup = setup;
        this.config = setup.config;
        this.fromBlockNames = setup.fromWorld.blockRegistry.blockNames();
        this.toBlockNames = setup.toWorld.blockRegistry.blockNames();

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        scrollPane.setAutoscrolls(true);
        scrollPane.setPreferredSize(new Dimension(900, 600));
        scrollPane.getVerticalScrollBar().setUnitIncrement(100);

        JButton create = new JButton("New");
        create.setAlignmentX(Component.CENTER_ALIGNMENT);
        create.addActionListener(e -> create());

        JButton done = new JButton("Done");
        done.setAlignmentX(Component.CENTER_ALIGNMENT);
        done.addActionListener(e -> done());

        JPanel buttons = new JPanel();
        buttons.setPreferredSize(new Dimension(90, 50));
        buttons.add(create);
        buttons.add(done);

        JPanel all = new JPanel();
        all.setLayout(new BoxLayout(all, BoxLayout.Y_AXIS));
        all.setPreferredSize(new Dimension(900, 600));
        all.add(scrollPane);
        all.add(buttons);

        this.add(all);

        for (BlockInfo info : config.blocks) {
            if (info.to == null) {
                continue;
            }
            if (setup.fromWorld.blockRegistry.has(info.name) && setup.toWorld.blockRegistry.has(info.to.name)) {
                Mapping mapping = new Mapping(this, info, fromBlockNames, toBlockNames);
                mappings.add(mapping);
                content.add(mapping);
            }
        }
    }

    private void create() {
        BlockInfo to = new BlockInfo("minecraft:air", -1, 0, 0, BlockInfo.EMPTY);
        BlockInfo from = new BlockInfo(to.name, -1, 0, 0, BlockInfo.EMPTY);
        Mapping mapping = new Mapping(this, from, fromBlockNames, toBlockNames);
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
        Mapping newMapping = new Mapping(this, mapping.blockInfo.copy(), fromBlockNames, toBlockNames);
        mappings.add(mappings.indexOf(mapping) + 1, newMapping);
        refresh();
    }

    public void removeMapping(Mapping mapping) {
        mappings.remove(mapping);
        refresh();
    }
}
