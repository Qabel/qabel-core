package de.qabel.core.drop;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DropIdGenerationTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testAdjustable() {
		DropIdGenerator generator = new AdjustableDropIdGenerator();
		String id = generator.generateDropId();
		Assert.assertEquals(DropIdGenerator.DROP_ID_LENGTH, id.length());
	}

	@Test
	public void testAdjustableTooFewBits() {
		exception.expect(IllegalArgumentException.class);
		DropIdGenerator generator = new AdjustableDropIdGenerator(0);
		String id = generator.generateDropId();
		Assert.assertEquals(DropIdGenerator.DROP_ID_LENGTH, id.length());
	}

	@Test
	public void testAdjustableTooManyBits() {
		exception.expect(IllegalArgumentException.class);
		DropIdGenerator generator = new AdjustableDropIdGenerator(
				DropIdGenerator.DROP_ID_LENGTH_BYTE * 8 + 1);
		String id = generator.generateDropId();
		Assert.assertEquals(DropIdGenerator.DROP_ID_LENGTH, id.length());
	}

	@Test
	public void testAdjustableJustRightLow() {
		DropIdGenerator generator = new AdjustableDropIdGenerator(1);
		String id = generator.generateDropId();
		Assert.assertEquals(DropIdGenerator.DROP_ID_LENGTH, id.length());
	}

	@Test
	public void testAdjustableJustRightHigh() {
		DropIdGenerator generator = new AdjustableDropIdGenerator(
				DropIdGenerator.DROP_ID_LENGTH_BYTE * 8);
		String id = generator.generateDropId();
		Assert.assertEquals(DropIdGenerator.DROP_ID_LENGTH, id.length());
	}
}
