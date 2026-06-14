package dev.sixik.generator_accelerator.mixin.chunk_generation;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkMap.class)
public abstract class MixinPlayerChunkMap {

    @Shadow
    public abstract WorldServer getWorldServer();

    @Inject(method = "updateMovingPlayer", at = @At("HEAD"))
    public void ga$updateMove(EntityPlayerMP player, CallbackInfo ci) {


    }
}