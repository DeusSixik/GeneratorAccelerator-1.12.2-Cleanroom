package dev.sixik.generator_accelerator.patches;

import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.NotImplementedException;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface MinecraftServerExtern {

    static MinecraftServerExtern get(MinecraftServer server) {
        if(server instanceof MinecraftServerExtern extern)
            return extern;
        throw new NotImplementedException(server.getClass() + " must be implement 'dev.sixik.generator_accelerator.patches.MinecraftServerExtern'");
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
