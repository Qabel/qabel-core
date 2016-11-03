Accounting
==========

.. toctree::
    :maxdepth: 2

The accounting packages contains Information about a users accounting profile and storage quota.

The BoxClient is the collaboration of Account related use-cases.
It needs an HTTP client to open the connection to the AccountingServer.

Following Use-Cases are provided by the accounting package:

BoxClient:
    - Register(create) an Account
    - Update account information (such as resetting password)
    - Login / Authentication
    - List of Identitys
    - retrieve and create various information related to the Box Storage
        - QuotaState, storage information
        - prefixes for connecting into the Box Volumes (for further information about Box see: "INSERT LINK HERE"


Usage Requirements
******************

- valid instance of AccountingServer
- valid instance of AccountingProfile
- *only for tests: instance of CloseableHttpClient*



