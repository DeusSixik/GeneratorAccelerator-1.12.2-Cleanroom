package dev.sixik.generator_accelerator.mixin.patch;

import dev.sixik.generator_accelerator.patches.MinecraftServerExtern;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements MinecraftServerExtern {

    @Unique
    private static final Logger LOGGER = LogManager.getLogger("GeneratorAccelerator-Server");

    @Shadow
    private Thread serverThread;

    @Unique
    private final ConcurrentLinkedQueue<Runnable> ga$tasks = new ConcurrentLinkedQueue<>();

    @Override
    public boolean ga$isMainThread() {
        return Thread.currentThread() == serverThread;
    }

    @Override
    public void ga$execute(Runnable task) {
        if(ga$isMainThread()) {
            task.run();
        } else ga$tasks.add(task);
    }

    @Override
    public <T> CompletableFuture<T> ga$execute(Supplier<T> task) {
        if (ga$isMainThread()) {
            try {
                return CompletableFuture.completedFuture(task.get());
            } catch (Exception e) {
                CompletableFuture<T> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            }
        } else {
            final CompletableFuture<T> future = new CompletableFuture<>();
            ga$tasks.add(() -> {
                try {
                    future.complete(task.get());
                } catch (Exception e) {
                    future.completeExceptionally(e);
                    LOGGER.error("Exception in async task scheduled for main thread", e);
                }
            });
            return future;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void ga$drainTasks(CallbackInfo ci) {
        Runnable task;
        while ((task = ga$tasks.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.error("Error executing queued main-thread task", e);
            } catch (Throwable t) {
                LOGGER.fatal("Fatal error in queued task", t);
            }
        }
    }
}
