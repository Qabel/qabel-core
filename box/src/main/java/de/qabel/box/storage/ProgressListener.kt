package de.qabel.box.storage

import de.qabel.core.util.Consumer

abstract class ProgressListener : Consumer<Long> {
    override fun accept(progress: Long) {
        setProgress(progress)
    }

    abstract fun setProgress(progress: Long)

    abstract fun setSize(size: Long)
}
