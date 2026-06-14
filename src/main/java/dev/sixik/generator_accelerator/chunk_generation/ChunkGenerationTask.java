package dev.sixik.generator_accelerator.chunk_generation;

import dev.sixik.generator_accelerator.patches.ChunkProviderServerExtern;
import dev.sixik.generator_accelerator.patches.MinecraftServerExtern;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.concurrent.CompletableFuture;

public final class ChunkGenerationTask {


    public static final ChunkGenerationTask NULL_TASK = new ChunkGenerationTask(null, null);

    private final ChunkPos chunkPos;
    private final ChunkGenerationTypes.Task task;

    private Chunk chunk = null;

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

    public static ChunkGenerationTask create(ChunkPos pos, ChunkGenerationTypes.Task task) {
        return new ChunkGenerationTask(pos, task);
    }

    public static ChunkGenerationTask create(ChunkPos pos, ChunkGenerationTypes.Task task, Chunk chunk) {
        ChunkGenerationTask generationTask = new ChunkGenerationTask(pos, task);
        generationTask.setChunk(chunk);
        return generationTask;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public ChunkGenerationTypes.Task getTask() {
        return task;
    }

    public boolean inNull() {
        return this == NULL_TASK;
    }

    public CompletableFuture<Chunk> execute(ChunkProviderServer providerServer) {
        switch (task) {
            case NONE -> {
                return CompletableFuture.completedFuture(chunk);
            }
            case LOAD -> {
                return MinecraftServerExtern.get(providerServer.world.getMinecraftServer()).ga$execute(() -> providerServer.loadChunk(chunkPos.x, chunkPos.z));
            }
            case GENERATE -> {
                return ChunkProviderServerExtern.get(providerServer).gar$provideChunk(chunkPos.x, chunkPos.z);
            }
        }

        throw new RuntimeException();
    }
}
