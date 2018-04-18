package me.dags.blockr.task;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;
import me.dags.blockr.replacer.Replacer;
import org.jnbt.CompoundTag;
import org.jnbt.StringTag;
import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.Extent;

/**
 * @author dags <dags@dags.me>
 */
public abstract class ExtentTask<T extends Extent> implements Runnable, Callable<Object> {

    private final File inputFile;
    private final File outputFile;
    private final Replacer[][] replacers;

    public ExtentTask(File inputFile, File outputFile, Replacer[][] replacers) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.replacers = replacers;
    }

    public abstract void process(File file) throws IOException;

    @Override
    public Object call() throws Exception {
        run();
        return true;
    }

    @Override
    public void run() {
        try (RandomAccessFile from = new RandomAccessFile(inputFile, "rw")) {
            File parent = outputFile.getParentFile();
            if (!parent.exists() && parent.mkdirs()) {
                System.out.println("Creating dir: " + parent);
            }

            RandomAccessFile to = new RandomAccessFile(outputFile, "rw");
            from.getChannel().transferTo(0, Long.MAX_VALUE, to.getChannel());
            to.close();

            process(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ChangeStats.dimTasksComplete.addAndGet(1);
            ChangeStats.globalTasksComplete.addAndGet(1);
        }
    }

    public void processExtent(T extent) {
        if (replacers.length > 0) {
            processBlocks(extent);
        }

        processEntities(extent);

        ChangeStats.incExtentCount();
    }

    public void processBlocks(T extent) {
        for (int y = 0; y < extent.getHeight(); y++) {
            for (int z = 0; z < extent.getLength(); z++) {
                for (int x = 0; x < extent.getWidth(); x++) {
                    int id = extent.getBlockType(x, y, z);
                    if (id >= 0 && id < this.replacers.length) {
                        Replacer[] replacers = this.replacers[id];

                        if (replacers == null) {
                            continue;
                        }

                        for (Replacer replacer : replacers) {
                            if (replacer.apply(extent, id, x, y, z)) {
                                ChangeStats.incBlockChanges();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void processEntities(T extent) {
        for (Entity entity : extent.getEntities()) {
            String id = entity.getId();

            if (!id.startsWith("conquest.painting")) {
                continue;
            }

            CompoundTag tag = (CompoundTag) entity.toNBT();
            String type = id.substring("conquest.".length());

            // TODO check that 1.12.2 entities are id'd as follows
            tag.setTag("id", new StringTag("id", "conquest.painting"));
            tag.setTag("ArtType", new StringTag("ArtType", type));

            // ArtID has not changed between 1.10.2 and 1.12.2
//            tag.setTag("ArtID", new IntTag("ArtID", art.index()));

            ChangeStats.incEntityChanges();
        }
    }
}
