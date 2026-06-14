package dev.sixik.generator_accelerator.chunk_generation;

public final class ChunkGenerationTypes {

    /**
     * Тип задачи для чанка.
     */
    public enum Task implements TypeGetter {
        /**
         * Чанк требует загрузки с диска
         */
        LOAD,

        /**
         * Чанк требует чтобы его сгенерировали. (То есть его не существует)
         */
        GENERATE;

        @Override
        public byte getTypeByte() {
            return (byte) ordinal();
        }
    }

    /**
     * Этапы генерации
     * <p>
     *     По сколку у нас нет ChunkPyramid как в 1.20+ мы сделаем её сами и немного проще
     * </p>
     */
    public enum Step implements TypeGetter {
        /**
         * Этап подготовки.
         * <p>
         *      Здесь мы подоготавливаем чанк, то есть он может в этот момент как уже лежать в очереди так и собирать
         *      нужные ему данные
         * </p>
         */
        PRE_GENERATING,

        /**
         * Этап генерации
         * <p>
         *     На этом этапе чанк уже начал генерацию.
         * </p>
         */
        GENERATING,

        /**
         * Этап окончания генерация
         * <p>
         *     Здесь чанк уже полностью сгенерирован и ждёт когда его отправят на сервер
         * </p>
         */
        GENERATED;

        @Override
        public byte getTypeByte() {
            return (byte) ordinal();
        }
    }

    /**
     * Вспомогательный интерфейс для получения {@link Enum#ordinal()} в виде {@code byte}, а не в {@code int}
     */
    protected interface TypeGetter {

        byte getTypeByte();
    }
}
