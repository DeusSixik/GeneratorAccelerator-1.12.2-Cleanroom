package dev.sixik.generator_accelerator.mixin.chunk_generation;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mixin(PlayerChunkMap.class)
public abstract class MixinPlayerChunkMap {

    @Shadow
    private int playerViewRadius;

    @Shadow
    protected abstract boolean overlaps(int x1, int z1, int x2, int z2, int radius);

    @Shadow
    protected abstract PlayerChunkMapEntry getOrCreateEntry(int chunkX, int chunkZ);

    @Shadow
    @Nullable
    public abstract PlayerChunkMapEntry getEntry(int x, int z);

    @Shadow
    protected abstract void markSortPending();

    @Mutable
    @Shadow
    @Final
    private Long2ObjectMap<PlayerChunkMapEntry> entryMap;

    @Mutable
    @Shadow
    @Final
    private List<PlayerChunkMapEntry> entries;

    @Mutable
    @Shadow
    @Final
    private List<PlayerChunkMapEntry> entriesWithoutChunks;

    @Mutable
    @Shadow
    @Final
    private List<PlayerChunkMapEntry> pendingSendToPlayers;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void bts$init(WorldServer serverWorld, CallbackInfo ci) {
        this.entryMap = Long2ObjectMaps.synchronize(entryMap);
        this.entries = Collections.synchronizedList(entries);
        this.entriesWithoutChunks = Collections.synchronizedList(entriesWithoutChunks);
        this.pendingSendToPlayers = Collections.synchronizedList(pendingSendToPlayers);
    }

    /**
     * @author Sixik
     * @reason Multithreaded chunk update experiment
     */
    @Overwrite
    public void updateMovingPlayer(EntityPlayerMP player) {
        int i = (int) player.posX >> 4;
        int j = (int) player.posZ >> 4;
        double d0 = player.managedPosX - player.posX;
        double d1 = player.managedPosZ - player.posZ;
        double d2 = d0 * d0 + d1 * d1;

        if (!(d2 < 64.0)) {
            int k = (int) player.managedPosX >> 4;
            int l = (int) player.managedPosZ >> 4;
            int i1 = this.playerViewRadius;
            int j1 = i - k;
            int k1 = j - l;

            // ИСПРАВЛЕНИЕ 1: Размер массива должен быть равен количеству итераций по X,
            // а не зависеть от абсолютных координат игрока.
            int radiusDiameter = (i1 * 2) + 1;
            CompletableFuture<Void>[] cfTasks = new CompletableFuture[radiusDiameter];

            // Переименовано для ясности: это очередь на удаление, а не основные задачи
            Queue<Runnable> removeTasks = new ConcurrentLinkedQueue<>();

            if (j1 != 0 || k1 != 0) {
                int startX = i - i1;

                for (int l1 = startX; l1 <= i + i1; l1++) {
                    int finalL = l1;

                    // ИСПРАВЛЕНИЕ 2: Правильный индекс для массива (от 0 до radiusDiameter - 1)
                    int taskIndex = l1 - startX;

                    cfTasks[taskIndex] = CompletableFuture.runAsync(() -> {
                        for (int i2 = j - i1; i2 <= j + i1; i2++) {
                            if (!this.overlaps(finalL, i2, k, l, i1)) {
                                this.getOrCreateEntry(finalL, i2).addPlayer(player);
                            }

                            if (!this.overlaps(finalL - j1, i2 - k1, i, j, i1)) {
                                int finalI = i2;
                                removeTasks.add(() -> {
                                    PlayerChunkMapEntry playerchunkmapentry = this.getEntry(finalL - j1, finalI - k1);

                                    if (playerchunkmapentry != null) {
                                        playerchunkmapentry.removePlayer(player);
                                    }
                                });
                            }
                        }
                    });
                }

                // Теперь массив заполнен полностью, null-элементов нет, NPE не будет
                CompletableFuture.allOf(cfTasks).join();

                Runnable ru;
                while ((ru = removeTasks.poll()) != null) {
                    ru.run();
                }

                player.managedPosX = player.posX;
                player.managedPosZ = player.posZ;
                this.markSortPending();
            }
        }
    }
}