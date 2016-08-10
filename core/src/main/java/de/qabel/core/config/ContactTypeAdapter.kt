package de.qabel.core.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import de.qabel.core.crypto.QblEcPublicKeyTypeAdapter
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidURL

import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList

class ContactTypeAdapter : TypeAdapter<Contact>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Contact) {
        val builder = GsonBuilder()
        out.beginObject()
        out.name("keys")
        builder.registerTypeAdapter(QblECPublicKey::class.java, QblEcPublicKeyTypeAdapter())
        val gson = builder.create()
        val primaryKeyAdapter = gson.getAdapter(QblECPublicKey::class.java)
        primaryKeyAdapter.write(out, value.ecPublicKey)

        out.name("alias")
        out.value(value.alias)

        out.name("email").value(value.email)

        out.name("phone").value(value.phone)

        out.name("drop_urls")
        out.beginArray()
        val dropUrls = value.dropUrls
        val urlAdapter = gson.getAdapter(URI::class.java)
        for (dropUrl in dropUrls) {
            urlAdapter.write(out, dropUrl.uri)
        }
        out.endArray()

        out.name("module_data")
        out.beginObject()
        //TODO: write module data
        out.endObject()

        // SyncSettingItem properties
        out.name("id").value(value.id.toLong())
        out.name("created").value(value.created)
        out.name("updated").value(value.updated)
        out.name("deleted").value(value.deleted)

        out.endObject()

        return
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Contact? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val contact: Contact
        var alias: String? = null
        var email: String? = null
        var phone: String? = null
        var ecPublicKey: QblECPublicKey? = null
        var dropURLs: MutableCollection<DropURL>? = null
        val syncItem = SyncSettingItem()
        `in`.beginObject()
        while (`in`.hasNext()) {
            when (`in`.nextName()) {
                "keys" -> {
                    val publicKeyTypeAdapter = QblEcPublicKeyTypeAdapter()
                    ecPublicKey = publicKeyTypeAdapter.read(`in`)
                }
                "alias" -> alias = `in`.nextString()
                "email" -> email = `in`.nextString()
                "phone" -> phone = `in`.nextString()
                "drop_urls" -> {
                    `in`.beginArray()
                    dropURLs = ArrayList<DropURL>()
                    while (`in`.hasNext()) {
                        try {
                            dropURLs.add(DropURL(`in`.nextString()))
                        } catch (e: QblDropInvalidURL) {
                            // TODO Auto-generated catch block
                            e.printStackTrace()
                        } catch (e: URISyntaxException) {
                            e.printStackTrace()
                        }

                    }
                    `in`.endArray()
                }
                "module_data" -> {
                    `in`.beginObject()
                    //TODO: read module data
                    `in`.endObject()
                }
            // SyncSettingItem properties
                "id" -> syncItem.id = `in`.nextInt()
                "created" -> syncItem.created = `in`.nextLong()
                "updated" -> syncItem.updated = `in`.nextLong()
                "deleted" -> syncItem.deleted = `in`.nextLong()
            }
        }
        `in`.endObject()

        if (ecPublicKey == null || dropURLs == null) {
            return null
        }

        contact = Contact(alias, dropURLs, ecPublicKey)

        contact.email = email
        contact.phone = phone

        // copy all sync item properties
        contact.id = syncItem.id
        contact.created = syncItem.created
        contact.updated = syncItem.updated
        contact.deleted = syncItem.deleted

        return contact
    }
}
