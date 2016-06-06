package de.qabel.box.storage;

import java.util.function.Consumer;

public abstract class ProgressListener implements Consumer<Long> {
    @Override
    public void accept(Long progress) {
        setProgress(progress);
    }

    @Override
    public Consumer<Long> andThen(Consumer<? super Long> after) {
        return this::setProgress;
    }

    public abstract void setProgress(long progress);

    public abstract void setSize(long size);
}
