package de.qabel.core.config

import org.apache.commons.lang3.builder.HashCodeBuilder

class Prefix @JvmOverloads constructor(val prefix: String, val type: TYPE = Prefix.TYPE.USER) {
    enum class TYPE {
        USER, CLIENT;
    }

    /**
     * username of the account
     * if this is thy primary prefix of the given type for that account
     */
    var account: String? = null

    override fun toString() = prefix

    override fun equals(other: Any?)
        = other is Prefix
        && other.prefix == prefix
        && other.type == type
        && other.account == account

    override fun hashCode(): Int = HashCodeBuilder().append(prefix).append(type).toHashCode()
}
