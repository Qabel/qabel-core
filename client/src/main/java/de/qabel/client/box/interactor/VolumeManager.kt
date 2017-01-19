package de.qabel.client.box.interactor

interface VolumeManager {

    val roots: List<VolumeRoot>

    fun readFileBrowser(rootID: String): ReadFileBrowser
    fun operationFileBrowser(rootID: String): OperationFileBrowser

}

