package de.qabel.core

import org.meanbean.bean.info.BeanInformationFactory
import org.meanbean.bean.info.JavaBeanInformationFactory
import org.meanbean.factories.FactoryCollection
import org.meanbean.factories.FactoryRepository
import org.meanbean.factories.util.BasicFactoryLookupStrategy
import org.meanbean.factories.util.FactoryLookupStrategy
import org.meanbean.lang.EquivalentFactory
import org.meanbean.lang.Factory
import org.meanbean.test.Configuration
import org.meanbean.test.ConfigurationBuilder
import org.meanbean.test.HashCodeMethodTester
import org.meanbean.util.AssertionUtils
import org.meanbean.util.RandomValueGenerator
import org.meanbean.util.SimpleRandomValueGenerator

import java.lang.reflect.Field

/**
 * ExtendedHashCodeMethodTester
 * This class extends the hashCode() method testing functionality of the MeanBean library.
 * By now the MeanBean library does not provide functionality to test the significance of
 * fields in the hashCode() method so it is implemented here.
 */
class ExtendedHashCodeMethodTester : HashCodeMethodTester() {
    /**
     * Random number generator used by factories to randomly generate values.
     */
    private val randomValueGenerator = SimpleRandomValueGenerator()

    /**
     * The collection of test data Factories.
     */
    private val factoryCollection = FactoryRepository(randomValueGenerator)

    /**
     * Provides a means of acquiring a suitable Factory.
     */
    private val factoryLookupStrategy = BasicFactoryLookupStrategy(factoryCollection,
            randomValueGenerator)

    /**
     * Factory used to gather information about a given bean and store it in a BeanInformation object.
     */
    private val beanInformationFactory = JavaBeanInformationFactory()

    override fun testHashCodeMethod(factory: EquivalentFactory<*>) {
        val config = ConfigurationBuilder().build()
        testHashCodeMethod(factory, config)
    }

    /**
     * @param factory An EquivalentFactory that creates non-null logically equivalent objects that will be used to test whether the equals logic implemented by the type is correct. The factory must create logically equivalent but different actual instances of the type upon each invocation of create() in order for the test to be meaningful and correct.
     * *
     * @param config  A custom Configuration to be used when testing to ignore the testing of named properties or use a custom test data Factory when testing a named property. This Configuration is only used for this individual test and will not be retained for future testing of this or any other type. If no custom Configuration is required, use testHashCodeMethod(Factory) instead.
     */
    @Throws(IllegalArgumentException::class, AssertionError::class)
    fun testHashCodeMethod(factory: EquivalentFactory<*>, config: Configuration) {
        super.testHashCodeMethod(factory)

        val testObj = factory.create()

        var oldHashCode = testObj.hashCode()
        var hashCode: Int

        val fields = testObj.javaClass.declaredFields
        for (field in fields) {

            if (config.isIgnoredProperty(field.name)) {
                continue
            }

            field.isAccessible = true

            val propertyFactory = factoryLookupStrategy.getFactory(beanInformationFactory.create(testObj.javaClass), field.name, field.type, config)

            var oldValue: Any? = null
            var newValue: Any? = null

            try {
                oldValue = field.get(testObj)
            } catch (e1: IllegalAccessException) {
                AssertionUtils.fail("Field \"" + field.name + "\" of class \"" + testObj.javaClass.name + "\" cannot be accessed")
            }

            do {
                newValue = propertyFactory.create()
            } while (newValue == oldValue)

            try {
                field.set(testObj, newValue)
            } catch (e: IllegalAccessException) {
                AssertionUtils.fail("Field \"" + field.name + "\" of class \"" + testObj.javaClass.name + "\" cannot be accessed")
                e.printStackTrace()
            }

            hashCode = testObj.hashCode()
            if (hashCode == oldHashCode) {
                AssertionUtils.fail("Property \"" + field.name + "\" is not significant!")
            }

            oldHashCode = hashCode
        }
        return
    }
}
