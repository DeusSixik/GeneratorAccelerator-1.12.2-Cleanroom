package dev.sixik.generator_accelerator.patches;

import dev.sixik.generator_accelerator.chunk_generation.ChunkGenerationTask;
import dev.sixik.generator_accelerator.patches.exception.NotImplementedException;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface PlayerChunkMapEntryExtern {

    static PlayerChunkMapEntryExtern get(PlayerChunkMapEntry entry) {
        if(entry instanceof PlayerChunkMapEntryExtern extern)
            return extern;
        throw new NotImplementedException(entry.getClass(), PlayerChunkMapEntryExtern.class);
    }

    /**
     * Если задача не может быть выполнена она будет помечана
     * @param canGenerate
     * @return
     */
    ChunkGenerationTask ga$runTask(boolean canGenerate);

    void ga$setChunk(final Chunk chunk);

    @Nullable
    Chunk ga$getChunk();

    ChunkPos ga$getPos();
}
