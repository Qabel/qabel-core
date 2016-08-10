package de.qabel.core.config

import java.util.*

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#accounts
 */
class Accounts {

    private val accounts = HashMap<String, Account>()

    /**
     * Returns unmodifiable set of contained accounts

     * @return Set
     */
    fun getAccounts(): Set<Account> {
        return Collections.unmodifiableSet(HashSet(accounts.values))
    }

    /**
     * Put an account

     * @param account Account to put
     * *
     * @return True if newly added, false if updated
     */
    fun put(account: Account): Boolean {
        return accounts.put(account.persistenceID, account) == null
    }

    /**
     * Removes account from list of accounts

     * @param account Account to be removed.
     * *
     * @return true if account was contained in list, false if not
     */
    fun remove(account: Account): Boolean {
        return accounts.remove(account.persistenceID) != null
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (accounts == null) 0 else accounts.hashCode()
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
        if (accounts == null) {
            if (other!!.accounts != null) {
                return false
            }
        } else if (accounts != other!!.accounts) {
            return false
        }
        return true
    }
}
