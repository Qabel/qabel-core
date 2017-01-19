package de.qabel.client.box.interactor

import de.qabel.box.storage.dto.BoxPath
import rx.Observable
import java.io.OutputStream

interface OperationFileBrowser : ReadFileBrowser {

    fun upload(path: BoxPath.File, source: UploadSource): Pair<FileOperationState, Observable<FileOperationState>>
    fun download(path: BoxPath.File, targetStream: OutputStream): Pair<FileOperationState, Observable<FileOperationState>>

    fun createFolder(path: BoxPath.FolderLike): Observable<Unit>
    fun delete(path: BoxPath): Observable<Unit>

}
