package dev.sixik.generator_accelerator.mixin.patch;

import dev.sixik.generator_accelerator.patches.MinecraftServerExtern;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements MinecraftServerExtern {

    @Shadow
    private Thread serverThread;

    @Override
    public boolean ga$isMainThread() {
        return Thread.currentThread() == serverThread;
    }
}
