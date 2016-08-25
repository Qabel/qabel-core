package de.qabel.core.ui


class MockDataView : DataView<String> {

    private var data = listOf<String>()

    override fun getCount(): Int = data.size

    override fun appendData(models: List<String>) {
        data = data.plus(models)
    }

    override fun prependData(models: List<String>) {
        data = models.plus(data)
    }

    override fun reset() {
        data = emptyList()
    }

    override fun handleLoadError(throwable: Throwable) = TODO()

}
