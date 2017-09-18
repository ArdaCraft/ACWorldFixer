package me.dags.blockr.task;

import me.dags.blockr.Schematic;
import me.dags.blockr.replacer.Replacer;

import java.io.File;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class SchematicTask extends ExtentTask<Schematic> {

    public SchematicTask(File inputFile, File outputFile, Replacer[][] replacers) {
        super(inputFile, outputFile, replacers);
    }

    @Override
    public void process(File file) throws IOException {
        try {
            Schematic schematic = new Schematic(file);
            processExtent(schematic);
            schematic.write();
            ChangeStats.incBlockVisits(schematic.getSize());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
