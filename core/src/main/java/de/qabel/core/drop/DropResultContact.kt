package de.qabel.core.drop

import de.qabel.core.config.Contact

import java.util.Arrays

/**
 * Class DropResultContact: Save the return value of a contact
 */
class DropResultContact
/**
 * Constructor

 * @param contact Contact object
 */
internal constructor(
        /**
         * Get contact

         * @return Contact object
         */
        val contact: Contact) {
    /**
     * Return error code

     * @return Error code
     */
    var errorCode = IntArray(0)
        private set
    /**
     * Get the return state of the shipping

     * @return Return state
     */
    var isSuccess: Boolean = false
        private set

    /**
     * Add an received error code to the error code list

     * @param errorCode Received error code
     * *
     * @return true when you have received error code 200 one time
     */
    internal fun addErrorCode(errorCode: Int): Boolean {
        if (errorCode == 200) {
            isSuccess = true
        }

        this.errorCode = addElement(this.errorCode, errorCode)

        return isSuccess
    }

    private fun addElement(array: IntArray, element: Int): IntArray {
        var array = array
        array = Arrays.copyOf(array, array.size + 1)
        array[array.size - 1] = element
        return array
    }
}
