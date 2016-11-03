Config
======

.. toctree::
    :maxdepth: 2

The Config folder/package contains many configuration classes and some few general entity's.


Here u sould find things like:
    Entities:
        - Account related Entity stuff, and even TypeAdapter for import or exporting those
        - Identity,
        - AccountingServer, DropServer definitions
        - Import / Export related stuff for sync settings
        - The Observer implementation for some entities such as for the Identity

You shouldn't find just raw entity classes but also some factory which are probably the most used in here.
