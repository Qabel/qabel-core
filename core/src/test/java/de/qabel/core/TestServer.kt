package de.qabel.core

val testserver = System.getenv("TESTSERVER")?: "localhost"
object TestServer {
    @JvmField val DROP = "http://$testserver:5000"
    @JvmField val ACCOUNTING = "http://$testserver:9696"
    @JvmField val BLOCK = "http://$testserver:9697"
    @JvmField val INDEX = "http://$testserver:9698"
}
