package dev.sixik.generator_accelerator.chunk_generation;

import net.minecraft.world.chunk.Chunk;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChunkGenerationManager {

    private final ConcurrentLinkedQueue<ChunkGenerationTask> pendingChunks =
            new ConcurrentLinkedQueue<>();

    public Chunk getChunk(int x, int z) {
        return null;
    }

    public CompletableFuture<Chunk> getChunkTask(int x, int z) {
        return null;
    }
}
