package dev.sixik.generator_accelerator.patches;

import dev.sixik.generator_accelerator.patches.exception.NotImplementedException;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface MinecraftServerExtern {

    static MinecraftServerExtern get(MinecraftServer server) {
        if(server instanceof MinecraftServerExtern extern)
            return extern;
        throw new NotImplementedException(server.getClass(), MinecraftServerExtern.class);
    }

    /**
     * Проверяет находимся ли мы на главном потоке сервера
     * @return {@code true} если мы на {@link MinecraftServer#serverThread}
     */
    boolean ga$isMainThread();

    /**
     * Отправляет задачу для выпалнение в главном потоке
     */
    void ga$execute(Runnable task);

    /**
     * Отправляет задачу для выпалнение в главном потоке
     */
    <T> CompletableFuture<T> ga$execute(Supplier<T> task);
}
