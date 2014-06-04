package com.jakubkaleta.checklist.DataAccess.beans.test;

import java.util.List;

import com.jakubkaleta.checklist.DataAccess.CategorySortOrder;
import com.jakubkaleta.checklist.DataAccess.beans.CategoryBean;
import com.jakubkaleta.checklist.DataAccess.beans.EntryBean;

import junit.framework.TestCase;

/**
 * @author Jakub Kaleta Contains tests for methods in the CategoryBean class.
 */
public class CategoryBeanTests extends TestCase {
	/**
	 * For testing the For testing creation of the bean and properties
	 */
	public void testCreationAndGetters() {
		// Arrange
		int id = 1;
		String name = "sampleName";
		CategorySortOrder sortOrder = CategorySortOrder.Custom;

		// Act
		CategoryBean bean = new CategoryBean(id, name, sortOrder);

		// Assert
		assertEquals(id, bean.getId());
		assertEquals(name, bean.getName());
		assertEquals(sortOrder, bean.getSortOrder());
		assertNull(bean.getEntry("Does not Exist"));
	}
	
	/**
	 * For testing the addEntry method - scenario when a duplicate
	 * entry is being inserted, and has a non-0 database id set.
	 */
	public void testAddEntry_DoNotInsertItemsWithDuplicateIds() {
		// Arrange
		EntryBean entry1 = new EntryBean(2,"e1", false);
		EntryBean entry2 = new EntryBean(2,"e2", false);
		CategoryBean bean = new CategoryBean(1, "test", CategorySortOrder.AlphabeticallyAsc);
		
		// Act
		bean.addEntry(entry1);
		bean.addEntry(entry2);
		List<EntryBean> entries = bean.getEntries();

		// Assert
		assertEquals(1, entries.size());
		assertEquals(entry1, entries.get(0));
	}
	
	/**
	 * For testing the addEntry method - scenario when a duplicate
	 * entry is being inserted, and has no valid database id set.
	 */
	public void testAddEntry_DoNotInsertItemsWithDuplicateNames() {
		// Arrange
		EntryBean entry1 = new EntryBean(2,"e1", false);
		EntryBean entry2 = new EntryBean(0,"e1", false);
		EntryBean entry3 = new EntryBean(0,"E1", false);
		CategoryBean bean = new CategoryBean(1, "test", CategorySortOrder.AlphabeticallyAsc);
		
		// Act
		bean.addEntry(entry1);
		bean.addEntry(entry2);
		bean.addEntry(entry3);
		List<EntryBean> entries = bean.getEntries();

		// Assert
		assertEquals(1, entries.size());
		assertEquals(entry1, entries.get(0));
	}
	
	/**
	 * For testing the addEntry method - scenario when valid entries are added
	 * that have unique names, but some of them have database ids, and some don't
	 */
	public void testAddEntry_DoNotInsertValidItemsNoDuplicates() {
		// Arrange
		EntryBean entry1 = new EntryBean(0,"e1", false);
		EntryBean entry2 = new EntryBean(1,"e2", false);
		EntryBean entry3 = new EntryBean(0,"e3", false);
		EntryBean entry4 = new EntryBean(2,"e4", false);
		CategoryBean bean = new CategoryBean(1, "test", CategorySortOrder.AlphabeticallyAsc);
		
		// Act
		bean.addEntry(entry1);
		bean.addEntry(entry2);
		bean.addEntry(entry3);
		bean.addEntry(entry4);
		List<EntryBean> entries = bean.getEntries();

		// Assert
		assertEquals(4, entries.size());
		assertTrue(entries.contains(entry1));
		assertTrue(entries.contains(entry2));
		assertTrue(entries.contains(entry3));
		assertTrue(entries.contains(entry4));
	}
	
	/**
	 * For testing the addEntry method - scenario when a duplicate
	 * entry is being inserted, and both entries have no database ids
	 */
	public void testAddEntry_DoNotInsertItemsWithDuplicateNames2() {
		// Arrange
		EntryBean entry1 = new EntryBean(0,"e1", false);
		EntryBean entry2 = new EntryBean(0,"e1", false);
		CategoryBean bean = new CategoryBean(1, "test", CategorySortOrder.AlphabeticallyAsc);
		
		// Act
		bean.addEntry(entry1);
		bean.addEntry(entry2);
		List<EntryBean> entries = bean.getEntries();

		// Assert
		assertEquals(1, entries.size());
		assertEquals(entry1, entries.get(0));
	}

}
