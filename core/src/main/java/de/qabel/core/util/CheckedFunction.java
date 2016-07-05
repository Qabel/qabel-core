package de.qabel.core.util;

public interface CheckedFunction<T, S> {
    S apply(T t) throws Exception;
}
