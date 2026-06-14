package dev.sixik.generator_accelerator.patches.exception;

public class NotImplementedException extends org.apache.commons.lang3.NotImplementedException {

    public NotImplementedException(Class<?> cur, Class<?> need) {
        super(cur + " must be implemented '" + need + "'");
    }
}
