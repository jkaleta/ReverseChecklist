package com.jakubkaleta.checklist.DataAccess.beans.test;

import junit.framework.TestCase;

import com.jakubkaleta.checklist.DataAccess.beans.EntryBean;

/**
 * @author Jakub Kaleta Contains tests for methods in the EntryBean class.
 */
public class EntryBeanTests extends TestCase {
	/**
	 * For testing the For testing creation of the bean and properties
	 */
	public void testCreationAndGetters() {
		// Arrange
		int id = 1;
		String name = "sampleName";

		// Act
		EntryBean bean = new EntryBean(id, name, true);

		// Assert
		assertEquals(id, bean.getId());
		assertEquals(name, bean.getName());
		assertTrue(bean.getIsSelected());
	}
	
	/**
	 * Tests if the isDirty property gets correctly updated
	 * after the bean is modified.
	 */
	public void testGetIsDirty()
	{
		// Arrange
		int id = 1;
		String name = "sampleName";

		// Act
		EntryBean bean = new EntryBean(id, name, true);

		// Assert
		assertFalse(bean.getIsDirty());
		bean.setIsSelected(false);
		assertFalse(bean.getIsSelected());
		assertTrue(bean.getIsDirty());
	}
	
}
