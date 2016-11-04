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

SQLite
^^^^^^
