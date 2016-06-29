package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFile

class CreateFileChange(newFile: BoxFile) : UpdateFileChange(null, newFile)
