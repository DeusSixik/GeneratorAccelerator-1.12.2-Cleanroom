package dev.sixik.generator_accelerator.mixin.patch;

import dev.sixik.generator_accelerator.GeneratorAccelerator;
import dev.sixik.generator_accelerator.patches.ChunkProviderServerExtern;
import dev.sixik.generator_accelerator.patches.MinecraftServerExtern;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer implements ChunkProviderServerExtern, IChunkProvider {

    @Shadow
    @Final
    public WorldServer world;

    @Shadow
    @Nullable
    public abstract Chunk loadChunk(int x, int z);

    @Shadow
    @Final
    public IChunkGenerator chunkGenerator;

    @Shadow
    @Final
    public Long2ObjectMap<Chunk> loadedChunks;

    @Redirect(method = "provideChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;loadChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    public Chunk ga$provideChunk(ChunkProviderServer instance, int x, int z) {
        return ga$safeLoadChunk(x, z);
    }

    @Override
    public CompletableFuture<Chunk> gar$provideChunk(int x, int z) {
        final long pos = ChunkPos.asLong(x, z);
        return CompletableFuture.supplyAsync(() -> {
            Chunk chunk = ga$safeLoadChunk(x, z);
            if (chunk != null) {
                try {
                    chunk = this.chunkGenerator.generateChunk(x, z);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return chunk;
        }, GeneratorAccelerator.EXECUTOR_SERVICE).whenComplete((chunk, thr) -> {
            MinecraftServerExtern.get(world.getMinecraftServer()).ga$execute(() -> {
                if (thr != null) {
                    CrashReport crashreport = CrashReport.makeCrashReport(thr, "Exception generating new chunk");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Chunk to be generated");
                    crashreportcategory.addCrashSection("Location", String.format("%d,%d", x, z));
                    crashreportcategory.addCrashSection("Position hash", pos);
                    crashreportcategory.addCrashSection("Generator", this.chunkGenerator);
                    throw new ReportedException(crashreport);
                }
                if (chunk != null) {
                    this.loadedChunks.put(pos, chunk);
                    chunk.onLoad();
                    chunk.populate(this, this.chunkGenerator);
                }
            });
        });
    }

    @Unique
    private Chunk ga$safeLoadChunk(int x, int z) {
        var server = MinecraftServerExtern.get(world.getMinecraftServer());

        if (server.ga$isMainThread())
            return loadChunk(x, z);

        return server.ga$execute(() -> loadChunk(x, z)).join();
    }
}
