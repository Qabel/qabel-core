package de.qabel.core.config.factory


class IdentityBuilderFactory(private val dropUrlGenerator: DropUrlGenerator) {

    fun factory(): IdentityBuilder {
        return IdentityBuilder(dropUrlGenerator)
    }
}
