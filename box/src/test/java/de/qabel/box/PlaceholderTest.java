package de.qabel.box;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PlaceholderTest {
    @Test
    public void compileCheckTest() {
        assertTrue(new Placeholder() instanceof Placeholder);
    }
}
