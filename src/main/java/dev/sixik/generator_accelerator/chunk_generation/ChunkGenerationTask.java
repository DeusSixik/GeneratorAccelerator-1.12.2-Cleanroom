package dev.sixik.generator_accelerator.chunk_generation;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.util.concurrent.CompletableFuture;

public final class ChunkGenerationTask {

    private static final CompletableFuture<Chunk> NULL = new CompletableFuture<>();

    private final ChunkPos chunkPos;
    private final ChunkGenerationTypes.Task task;

    private CompletableFuture<Chunk> chunk = NULL;

    public static ChunkGenerationTask loading(ChunkPos chunkPos) {
        return new ChunkGenerationTask(chunkPos, ChunkGenerationTypes.Task.LOAD);
    }

    public static ChunkGenerationTask generation(ChunkPos chunkPos) {
        return new ChunkGenerationTask(chunkPos, ChunkGenerationTypes.Task.GENERATE);
    }

    private ChunkGenerationTask(ChunkPos chunkPos, ChunkGenerationTypes.Task task) {
        this.chunkPos = chunkPos;
        this.task = task;
    }

    public void setChunk(CompletableFuture<Chunk> chunk) {
        this.chunk = chunk;
    }

    public CompletableFuture<Chunk> getChunk() {
        return chunk;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public ChunkGenerationTypes.Task getTask() {
        return task;
    }

    public boolean inNull() {
        return chunk == NULL;
    }
}
