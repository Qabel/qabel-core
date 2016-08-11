package de.qabel.core.config.factory

import de.qabel.core.config.DropServer
import de.qabel.core.drop.DropURL

import java.net.URI
import java.net.URISyntaxException

class DropUrlGenerator(dropServerUrl: URI) {
    private val dropServer: DropServer

    @Throws(URISyntaxException::class)
    constructor(dropServerUrl: String) : this(URI(dropServerUrl)) {
    }

    init {
        dropServer = DropServer(dropServerUrl, null, true)
    }

    fun generateUrl(): DropURL {
        try {
            return DropURL(dropServer)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to generate dropUrl:" + e.message, e)
        }

    }
}
