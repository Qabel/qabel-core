package de.qabel.core.repository;

import java.util.function.Function;

public class LambdaEntityManager<I, H> extends GenericEntityManager<I, H> {
    private Function<H, I> transformer;

    public LambdaEntityManager(Function<H, I> transformer) {
        this.transformer = transformer;
    }

    @Override
    protected I getId(H entity) {
        return transformer.apply(entity);
    }
}
