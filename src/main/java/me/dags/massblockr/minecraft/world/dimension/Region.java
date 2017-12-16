package me.dags.massblockr.minecraft.world.dimension;

import me.dags.massblockr.minecraft.world.chunk.Chunk;

import java.util.stream.Stream;

/**
 * @author dags <dags@dags.me>
 */
public interface Region {

    String getName();

    Stream<Chunk> getChunks();

    void writeChunks(Stream<Chunk> chunks);
}
