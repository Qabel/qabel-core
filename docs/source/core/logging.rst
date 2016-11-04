Logging
=======

.. toctree::
    :maxdepth: 2

The logging package is self explaining.

Here is the Inteface of the logger for an short overview:

.. code-block:: java

    fun trace(msg: Any, vararg args: Any)
    fun debug(msg: Any, vararg args: Any)
    fun info(msg: Any, vararg args: Any)
    fun warn(msg: Any, vararg args: Any)
    fun error(msg: Any?, exception: Throwable?)
