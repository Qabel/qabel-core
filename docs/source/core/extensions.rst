Extensions
==========

.. toctree::
    :maxdepth: 2

The Extensions package is some kind of Utils package but contains only Kotlin extension methods to extend
some some util functionality without the need of changing or modifying some Interfaces to apply some class methods.

such as for example:

.. code-block:: java

    fun <T  : Entity> Set<T>.findById(id : Int) = find { it.id == id }

So every Instance of an 'Entity' can use this findById method.
