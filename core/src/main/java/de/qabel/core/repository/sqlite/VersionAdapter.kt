package de.qabel.core.repository.sqlite

import kotlin.properties.ReadWriteProperty

interface VersionAdapter: ReadWriteProperty<Any, Long>
