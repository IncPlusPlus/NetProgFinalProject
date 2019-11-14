package io.github.incplusplus.peerprocessing.linear;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;

class BigDecimalMatrixTest {
	private final BigDecimalMatrix zero2x2 = new BigDecimalMatrix(new BigDecimal[][]{
			{ZERO, ZERO},
			{ZERO, ZERO}
	});
	
	private final BigDecimalMatrix identity2x2 = new BigDecimalMatrix(new BigDecimal[][]{
			{ONE, ZERO},
			{ZERO, ONE}
	});
	
	private final BigDecimalMatrix identity3x3 = new BigDecimalMatrix(new BigDecimal[][]{
			{ONE, ZERO, ZERO},
			{ZERO, ONE, ZERO},
			{ZERO, ZERO, ONE}
	});
	
	private final BigDecimalMatrix dumb4x3 = new BigDecimalMatrix(new BigDecimal[][]{
			{ONE, ZERO, ZERO},
			{ZERO, ONE, ZERO},
			{ZERO, ZERO, ONE},
			{ZERO, ZERO, ZERO}
	});
	
	@Test
	void isSquare() {
		assertTrue(identity2x2.isSquare());
		assertTrue(identity3x3.isSquare());
		assertFalse(dumb4x3.isSquare());
	}
	
	@Test
	void random() {
	}
	
	@Test
	void identity() {
		iterateAndAssertEquals(new BigDecimalMatrix().identity(2).getContents(), identity2x2.getContents());
		iterateAndAssertEquals(new BigDecimalMatrix().identity(3).getContents(), identity3x3.getContents());
	}
	
	@Test
	void multiply() {
		// (2-by-2 identity) * (2-by-2 identity) = (2-by-2 identity)
		iterateAndAssertEquals(identity2x2.multiply(new BigDecimalMatrix(new BigDecimal[][]{
				{ONE, ZERO},
				{ZERO, ONE}
		})).getContents(),identity2x2.getContents());
		
		//(2-by-2 identity) * (2-by-2 zero matrix) = (2-by-2 zero matrix)
		iterateAndAssertEquals(identity2x2.multiply(new BigDecimalMatrix(new BigDecimal[][]{
				{ZERO, ZERO},
				{ZERO, ZERO}
		})).getContents(),zero2x2.getContents());
	}
	
	@Test
	void testMultiply() {
	}
	
	@Test
	void getCol() {
	}
	
	@Test
	void getRow() {
	}
	
	@Test
	void getEntry() {
	}
	
	@Test
	void dot() {
	}
	
	@Test
	void getNumRows() {
	}
	
	@Test
	void getNumCols() {
	}
	
	
	private static void iterateAndAssertEquals(BigDecimal[][] firstMatrix, BigDecimal[][] secondMatrix) {
		iterateAndDoSomething(firstMatrix, secondMatrix, true);
	}
	
	private static void iterateAndAssertCompareToZero(BigDecimal[][] firstMatrix, BigDecimal[][] secondMatrix) {
		iterateAndDoSomething(firstMatrix, secondMatrix, false);
	}
	
	/**
	 * Test helper method. Iterates over each entry and compare the entries using
	 * {@link Object#equals(Object)} if useEquals is true or {@link Comparable#compareTo(Object)} if false.
	 * For any exception that may occur during iteration (like an NPE), this is
	 * just as bad as an assertion failing. Therefore, it is not caught or otherwise handled.
	 *
	 * @param firstMatrix  the first matrix to compare
	 * @param secondMatrix the second matrix to compare
	 * @param useEquals    use {@link Object#equals(Object)} if true; else {@link Comparable#compareTo(Object)}
	 */
	private static void iterateAndDoSomething(BigDecimal[][] firstMatrix, BigDecimal[][] secondMatrix,
	                                          boolean useEquals) {
		assertEquals(firstMatrix.length, secondMatrix.length);
		for (int i = 0; i < firstMatrix.length; i++) {
			assertEquals(firstMatrix[i].length, secondMatrix[i].length);
			for (int j = 0; j < firstMatrix[0].length; j++) {
				if (useEquals) {
					assertEquals(firstMatrix[i][j], secondMatrix[i][j]);
				}
				else {
					assertEquals(firstMatrix[i][j].compareTo(secondMatrix[i][j]), 0);
				}
			}
		}
	}
}