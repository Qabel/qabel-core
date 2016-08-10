package de.qabel.core.exceptions

abstract class QblException : Exception {


    constructor() {
    }

    constructor(msg: String) : super(msg) {
    }

    constructor(e: Throwable) : super(e) {
    }

    constructor(msg: String, e: Throwable) : super(msg, e) {
    }

    companion object {

        /**

         */
        private val serialVersionUID = 4261199805687953191L
    }
}
