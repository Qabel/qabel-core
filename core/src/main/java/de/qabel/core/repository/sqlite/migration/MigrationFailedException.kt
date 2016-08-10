package de.qabel.core.repository.sqlite.migration

import de.qabel.core.repository.sqlite.MigrationException

class MigrationFailedException : MigrationException {
    var failedMigration: AbstractMigration? = null
        private set

    constructor(failedMigration: AbstractMigration, message: String) : super(message) {
        this.failedMigration = failedMigration
    }

    constructor(failedMigration: AbstractMigration, message: String, cause: Throwable) : super(message, cause) {
        this.failedMigration = failedMigration
    }
}
