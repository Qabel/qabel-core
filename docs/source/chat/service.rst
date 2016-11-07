Service Package
===============

.. toctree::
    :maxdepth: 2


The service represents the whole chat and chat sharing.
It uses the Box(for sharing) and Drop(for messages):

        https://qabel.github.io/docs/Qabel-Protocol-Box/


Basic Features
**************

    - Send and receive messages
        - Prerequisites: (each object has its own local repository)
            - Identity and Contact
            - ChatDropMessage (type = MESSAGE)
            - ChatService to send and receive
    - Send and receive shares to an contact
        - Prerequisites:
            - Identity and Contact
            - BoxFile
            - BoxVolume to upload and navigate the BoxFile
            - SharingService to send and receive
