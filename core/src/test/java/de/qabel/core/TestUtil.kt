package de.qabel.core

import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.config.factory.IdentityBuilderFactory

val dropUrlGenerator = DropUrlGenerator("http://localhost:5000")
val identityBuilderFactory = IdentityBuilderFactory(dropUrlGenerator)
fun testIdentity() = identityBuilderFactory.factory().withAlias("tester").build()!!
