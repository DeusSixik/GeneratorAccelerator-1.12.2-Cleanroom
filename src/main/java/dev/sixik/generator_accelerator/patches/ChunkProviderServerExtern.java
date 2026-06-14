package dev.sixik.generator_accelerator.patches;

import dev.sixik.generator_accelerator.patches.exception.NotImplementedException;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.concurrent.CompletableFuture;

public interface ChunkProviderServerExtern {

    static ChunkProviderServerExtern get(ChunkProviderServer providerServer) {
        if(providerServer instanceof ChunkProviderServerExtern extern)
            return extern;
        throw new NotImplementedException(providerServer.getClass(), ChunkProviderServerExtern.class);
    }

    CompletableFuture<Chunk> gar$provideChunk(int x, int z);
}
