package de.qabel.core.config

import java.util.*

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#internalAccounts
 */
class Accounts {

    private val internalAccounts = HashMap<String, Account>()

    /**
     * Returns unmodifiable set of contained internalAccounts

     * @return Set
     */
    fun getAccounts(): Set<Account> {
        return Collections.unmodifiableSet(HashSet(internalAccounts.values))
    }

    /**
     * Put an account

     * @param account Account to put
     * *
     * @return True if newly added, false if updated
     */
    fun put(account: Account): Boolean {
        return internalAccounts.put(account.persistenceID, account) == null
    }

    /**
     * Removes account from list of internalAccounts

     * @param account Account to be removed.
     * *
     * @return true if account was contained in list, false if not
     */
    fun remove(account: Account): Boolean {
        return internalAccounts.remove(account.persistenceID) != null
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (internalAccounts == null) 0 else internalAccounts.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as Accounts?
        if (internalAccounts == null) {
            if (other!!.internalAccounts != null) {
                return false
            }
        } else if (internalAccounts != other!!.internalAccounts) {
            return false
        }
        return true
    }
}
