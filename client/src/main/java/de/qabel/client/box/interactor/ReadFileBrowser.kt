package de.qabel.client.box.interactor

import de.qabel.box.storage.dto.BoxPath
import de.qabel.client.box.documentId.DocumentId
import rx.Observable

interface ReadFileBrowser {
    fun list(path: BoxPath.FolderLike, fast: Boolean = false): Observable<List<BrowserEntry>>
    fun query(path: BoxPath): Observable<BrowserEntry>
    fun asDocumentId(path: BoxPath): Observable<DocumentId>
}

