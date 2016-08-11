package de.qabel.core.config

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

import java.io.IOException

class AccountsTypeAdapter : TypeAdapter<Accounts>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Accounts) {
        out.beginArray()
        val gson = Gson()
        val set = value.getAccounts()
        val adapter = gson.getAdapter(Account::class.java)
        for (account in set) {
            adapter.write(out, account)
        }
        out.endArray()
        return
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Accounts? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }

        val gson = Gson()
        val accounts = Accounts()
        val adapter = gson.getAdapter(Account::class.java)
        var account: Account? = null

        `in`.beginArray()
        while (`in`.hasNext()) {
            account = adapter.read(`in`)
            accounts.put(account)
        }
        `in`.endArray()

        return accounts
    }

}
