package dev.sixik.generator_accelerator.mixin.chunk_generation;

import com.google.common.base.Predicate;
import dev.sixik.generator_accelerator.chunk_generation.ChunkGenerationTask;
import dev.sixik.generator_accelerator.patches.MinecraftServerExtern;
import dev.sixik.generator_accelerator.patches.PlayerChunkMapEntryExtern;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerChunkMap.class)
public abstract class MixinPlayerChunkMap {

    private Long2ObjectMap<ChunkGenerationTask> nowGenerating = new Long2ObjectOpenHashMap<>();

    @Mutable
    @Shadow
    @Final
    private List<PlayerChunkMapEntry> entriesWithoutChunks;

    @Shadow
    @Final
    private static Predicate<EntityPlayerMP> CAN_GENERATE_CHUNKS;

    @Shadow
    public abstract WorldServer getWorldServer();

    @Shadow
    @Final
    private List<PlayerChunkMapEntry> pendingSendToPlayers;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void ga$init(WorldServer serverWorld, CallbackInfo ci) {
        this.entriesWithoutChunks = new ObjectArrayList<>(entriesWithoutChunks);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    public boolean ga$tick(List<PlayerChunkMapEntry> instance) {
        if (instance != this.entriesWithoutChunks) {
            return instance.isEmpty();
        }

        // Если список реально пуст, просто скипаем блок
        if (instance.isEmpty()) {
            return true;
        }

        MinecraftServerExtern server = MinecraftServerExtern.get(getWorldServer().getMinecraftServer());

        // Копируем список, чтобы избежать ConcurrentModificationException
        List<PlayerChunkMapEntry> copy = new ObjectArrayList<>(instance);

        for (PlayerChunkMapEntry chunkMap : copy) {
            // НАМ НУЖНЫ именно пустые чанки!
            if (chunkMap.getChunk() != null) continue;

            PlayerChunkMapEntryExtern externMap = PlayerChunkMapEntryExtern.get(chunkMap);

            boolean canGenerate = chunkMap.hasPlayerMatching(CAN_GENERATE_CHUNKS); // Замените на вашу логику chunkMap.hasPlayerMatching(CAN_GENERATE_CHUNKS)

            ChunkGenerationTask task = externMap.ga$runTask(canGenerate);


            final ChunkPos pos = externMap.ga$getPos();
            final var longPos = ga$asLong(pos.x, pos.z);
            if (task.inNull() || nowGenerating.containsKey(longPos))
                continue;

            nowGenerating.put(longPos, task);
            task.execute(getWorldServer().getChunkProvider())
                    .whenComplete((chunk, throwable) -> {
                        if (throwable != null) {
                            throwable.printStackTrace();
                            return;
                        }

                        // ВСЕ изменения коллекций и отправка пакетов строго в главном потоке!
                        server.ga$execute(() -> {
                            externMap.ga$setChunk(chunk);

                            // Удаляем из списка ожидающих ТОЛЬКО когда чанк реально готов
                            this.entriesWithoutChunks.remove(chunkMap);
                            this.nowGenerating.remove(longPos);

                            try {
                                if (chunk != null && chunkMap.sendToPlayers()) {
                                    this.pendingSendToPlayers.remove(chunkMap);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    });
        }

        // Возвращаем true, чтобы ванильный if (!this.entriesWithoutChunks.isEmpty()) не сработал
        return true;
    }

    @Unique
    private static long ga$asLong(int x, int z) {
        return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
    }

    @Unique
    private static int ga$getX(long packed) {
        return (int) (packed & 0xFFFFFFFFL);
    }

    @Unique
    private static int ga$getZ(long packed) {
        return (int) (packed >>> 32);
    }
}