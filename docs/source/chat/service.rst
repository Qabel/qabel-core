Service Package
===============

.. toctree::
    :maxdepth: 2


The service represents the whole chat and its contents.
It does simply read and send messages through DropMessages and can send BoxShares as well.
(for example Foo want to send Bar a file: the service will send the DropMessage and a BoxMetaFile)
For further information see:

        https://qabel.github.io/docs/Qabel-Protocol-Box/


Basic Features
**************

    - Send and receive messages
        - Prerequisites: (each object has its own local repository)
            - Identity and Contact
            - ChatDropMessage (type = MESSAGE)
    - Send and receive shares to an contact
        - Prerequisites:
            - Identity and Contact
            - BoxFile
            - BoxVolume to upload the BoxFile
            - SharingService
