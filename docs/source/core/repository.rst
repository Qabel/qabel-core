Repository
==========

.. toctree::
    :maxdepth: 2

The Repository package is one of our biggest packages.

All database communication is stored here. It has an implementation with a sqlite database.

Subpackages
***********

Entities
^^^^^^^^
Currently only the Dropstate. Probably be modified soon.

Exception
^^^^^^^^^
Various repository related exceptions are located in here.
Some examples:

Used when persisting failed

.. code-block:: java

    public class PersistenceException extends Exception {
        public PersistenceException(String message) {
            super(message);
        }

        public PersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

Entity duplication found? throw this:

.. code-block:: java

    class EntityExistsException(val msg: String = "Entity already exists") : PersistenceException(msg) {

    }

If u search for a given entity and its not there you can thorw this:

.. code-block:: java

    public class EntityNotFoundException extends Exception {
        public EntityNotFoundException(String message) {
            super(message);
        }

        public EntityNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

Transaction failures? throw this:

.. code-block:: java

    public class TransactionException extends PersistenceException {
        public TransactionException(String message) {
            super(message);
        }

        public TransactionException(String message, Throwable cause) {
            super(message, cause);
        }
    }




Framework
^^^^^^^^^
Here you find our small database framework.
Usually no changes in here. If u intend to modify some data schema then go to the SQLite_ package.


SQLite
^^^^^^

This is the the biggest subpackage. Here u find the implementation of the database abstraction layer, each schemas, migrations and hydrator.
The explicit SQLite repositorys are located in here.

Some examples of Repositorys:
    SqliteAccountRepository - which is used to store account and authentication information
    SqllieContactRepository - which obviously is the repository for contacts

.. note::

    If u change something on the database structure - for example adding an new column than u need to create a migration.
    Samples can be found in sqlite/migration package.



