package com.jakubkaleta.checklist.test;

import com.jakubkaleta.checklist.Utilities;

import junit.framework.TestCase;

/**
 * Contains tests for static methods in the Utilities class.
 * 
 * @author Jakub Kaleta
 */
public class UtilitiesTests extends TestCase {
	/**
	 * For testing the join method in Utilities with an empty array
	 */
	public void testJoin_emptyArray() {

		// Arrange
		String[] strings = new String[] {};

		// Act
		String result = Utilities.join(",", strings);

		// Assert
		assertTrue(result.equalsIgnoreCase(""));
	}

	/**
	 * For testing the join method in Utilities with a null array
	 */
	public void testJoin_nullArray() {

		// Act
		String result = Utilities.join(",", null);

		// Assert
		assertTrue(result.equalsIgnoreCase(""));
	}

	/**
	 * For testing the join method in Utilities with an array of strings
	 */
	public void testJoin_stringArray() {
		// Arrange
		String[] strings = new String[] { "A", "B", "C" };

		// Act
		String result = Utilities.join(",", strings);

		// Assert
		assertTrue(result.equalsIgnoreCase("A, B, C"));
	}

	/**
	 * For testing the join method in Utilities with an array of longs
	 */
	public void testJoin_longsArray() {
		// Arrange
		Long[] longs = new Long[] { 1l, 23l, 45l };

		// Act
		String result = Utilities.join(",", longs);

		// Assert
		assertTrue(result.equalsIgnoreCase("1, 23, 45"));
	}	
}
