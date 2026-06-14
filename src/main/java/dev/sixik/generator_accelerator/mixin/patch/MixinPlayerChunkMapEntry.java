package dev.sixik.generator_accelerator.mixin.patch;

import dev.sixik.generator_accelerator.chunk_generation.ChunkGenerationTask;
import dev.sixik.generator_accelerator.chunk_generation.ChunkGenerationTypes;
import dev.sixik.generator_accelerator.patches.PlayerChunkMapEntryExtern;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(PlayerChunkMapEntry.class)
public class MixinPlayerChunkMapEntry implements PlayerChunkMapEntryExtern {

    @Shadow
    private boolean loading;

    @Shadow
    @Nullable
    private Chunk chunk;

    @Shadow
    @Final
    private ChunkPos pos;

    @Override
    public ChunkGenerationTask ga$runTask(boolean canGenerate) {
        if (this.loading)
            return ChunkGenerationTask.NULL_TASK;
        if (this.chunk != null)
            return ChunkGenerationTask.create(
                    pos,
                    ChunkGenerationTypes.Task.NONE,
                    chunk
            );

        if(canGenerate) {
            return ChunkGenerationTask.generation(pos);
        }

        return ChunkGenerationTask.loading(pos);
    }

    @Override
    public void ga$setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public @Nullable Chunk ga$getChunk() {
        return chunk;
    }

    @Override
    public ChunkPos ga$getPos() {
        return pos;
    }
}
