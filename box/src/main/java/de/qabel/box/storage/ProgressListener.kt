package de.qabel.box.storage

import java.util.function.Consumer

abstract class ProgressListener : Consumer<Long> {
    override fun accept(progress: Long?) {
        setProgress(progress!!)
    }

    override fun andThen(after: Consumer<in Long>): Consumer<Long> {
        return Consumer { this.setProgress(it) }
    }

    abstract fun setProgress(progress: Long)

    abstract fun setSize(size: Long)
}
