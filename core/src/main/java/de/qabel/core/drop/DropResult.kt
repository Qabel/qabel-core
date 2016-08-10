package de.qabel.core.drop

import java.util.ArrayList
import java.util.Collections

/**
 * Class DropResult: Save the return value of every contact and the overview
 * whether everything is okay
 */
class DropResult {
    private var checked: Boolean = false
    private var success = true
    private var results: MutableList<DropResultContact>? = null

    constructor() {
        results = ArrayList<DropResultContact>()
    }

    /**
     * Constructor

     * @param results List of all results
     */
    constructor(results: MutableList<DropResultContact>) {
        val iterator: Iterator<DropResultContact>

        this.results = results

        iterator = this.results!!.iterator()
        while (iterator.hasNext()) {
            val item: DropResultContact

            checked = true

            item = iterator.next()
            if (item.isSuccess == false) {
                success = false
            }
        }
    }

    /**
     * Add one result

     * @param result Object of DropResultContact
     */
    fun addContactResult(result: DropResultContact) {
        results!!.add(result)

        checked = true

        if (result.isSuccess == false) {
            success = false
        }
    }

    /**
     * Return the overview state of the complete shipping

     * @return State of the shipping
     */
    val isSuccess: Boolean
        get() {
            if (checked == false) {
                return false
            }

            return success
        }

    /**
     * Return the list of all results of the contacts

     * @return List of all results
     */
    val list: List<DropResultContact>
        get() = Collections.unmodifiableList(results!!)

}
