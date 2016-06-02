package de.qabel.core;

import org.meanbean.bean.info.BeanInformationFactory;
import org.meanbean.bean.info.JavaBeanInformationFactory;
import org.meanbean.factories.FactoryCollection;
import org.meanbean.factories.FactoryRepository;
import org.meanbean.factories.util.BasicFactoryLookupStrategy;
import org.meanbean.factories.util.FactoryLookupStrategy;
import org.meanbean.lang.EquivalentFactory;
import org.meanbean.lang.Factory;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;
import org.meanbean.test.HashCodeMethodTester;
import org.meanbean.util.AssertionUtils;
import org.meanbean.util.RandomValueGenerator;
import org.meanbean.util.SimpleRandomValueGenerator;

import java.lang.reflect.Field;

/**
 * ExtendedHashCodeMethodTester
 * This class extends the hashCode() method testing functionality of the MeanBean library.
 * By now the MeanBean library does not provide functionality to test the significance of
 * fields in the hashCode() method so it is implemented here.
 */
public class ExtendedHashCodeMethodTester extends HashCodeMethodTester {
    /**
     * Random number generator used by factories to randomly generate values.
     */
    private final RandomValueGenerator randomValueGenerator = new SimpleRandomValueGenerator();

    /**
     * The collection of test data Factories.
     */
    private final FactoryCollection factoryCollection = new FactoryRepository(randomValueGenerator);

    /**
     * Provides a means of acquiring a suitable Factory.
     */
    private final FactoryLookupStrategy factoryLookupStrategy = new BasicFactoryLookupStrategy(factoryCollection,
        randomValueGenerator);

    /**
     * Factory used to gather information about a given bean and store it in a BeanInformation object.
     */
    private final BeanInformationFactory beanInformationFactory = new JavaBeanInformationFactory();

    @Override
    public void testHashCodeMethod(EquivalentFactory<?> factory) {
        Configuration config = new ConfigurationBuilder().build();
        testHashCodeMethod(factory, config);
    }

    /**
     * @param factory An EquivalentFactory that creates non-null logically equivalent objects that will be used to test whether the equals logic implemented by the type is correct. The factory must create logically equivalent but different actual instances of the type upon each invocation of create() in order for the test to be meaningful and correct.
     * @param config  A custom Configuration to be used when testing to ignore the testing of named properties or use a custom test data Factory when testing a named property. This Configuration is only used for this individual test and will not be retained for future testing of this or any other type. If no custom Configuration is required, use testHashCodeMethod(Factory) instead.
     */
    public void testHashCodeMethod(EquivalentFactory<?> factory, Configuration config) throws IllegalArgumentException, AssertionError {
        super.testHashCodeMethod(factory);

        Object testObj = factory.create();

        int oldHashCode = testObj.hashCode();
        int hashCode;

        Field[] fields = testObj.getClass().getDeclaredFields();
        for (Field field : fields) {

            if (config.isIgnoredProperty(field.getName())) {
                continue;
            }

            field.setAccessible(true);

            Factory<?> propertyFactory = factoryLookupStrategy
                .getFactory(beanInformationFactory
                    .create(testObj.getClass()), field.getName(), field.getType(), config);

            Object oldValue = null;
            Object newValue = null;

            try {
                oldValue = field.get(testObj);
            } catch (IllegalAccessException e1) {
                AssertionUtils.fail("Field \"" + field.getName() + "\" of class \"" + testObj.getClass().getName() + "\" cannot be accessed");
            }

            do {
                newValue = propertyFactory.create();
            } while (newValue.equals(oldValue));

            try {
                field.set(testObj, newValue);
            } catch (IllegalAccessException e) {
                AssertionUtils.fail("Field \"" + field.getName() + "\" of class \"" + testObj.getClass().getName() + "\" cannot be accessed");
                e.printStackTrace();
            }

            hashCode = testObj.hashCode();
            if (hashCode == oldHashCode) {
                AssertionUtils.fail("Property \"" + field.getName() + "\" is not significant!");
            }

            oldHashCode = hashCode;
        }
        return;
    }
}
