package com.jakubkaleta.checklist.DataAccess.beans.test;

import java.util.List;

import com.jakubkaleta.checklist.DataAccess.CategorySortOrder;
import com.jakubkaleta.checklist.DataAccess.beans.ActivityBean;
import com.jakubkaleta.checklist.DataAccess.beans.CategoryBean;

import junit.framework.TestCase;

/**
 * @author Jakub Kaleta Contains tests for methods in the ActivityBean class.
 */
public class ActivityBeanTests extends TestCase {
	/**
	 * For testing creation of the bean and properties
	 */
	public void testCreationAndGetters() {
		// Arrange
		int id = 1;
		String name = "sampleName";

		// Act
		ActivityBean bean = new ActivityBean(id, name);

		// Assert
		assertEquals(id, bean.getId());
		assertEquals(name, bean.getName());
	}

	/**
	 * For testing the addCategory method - scenario when a duplicate category
	 * is being inserted, and has a non-0 database id set.
	 */
	public void testAddCategory_DoNotInsertItemsWithDuplicateIds() {
		// Arrange
		CategoryBean category1 = new CategoryBean(2, "e1", CategorySortOrder.AlphabeticallyAsc);
		CategoryBean category2 = new CategoryBean(2, "e2", CategorySortOrder.AlphabeticallyAsc);
		ActivityBean bean = new ActivityBean(1, "test");

		// Act
		bean.addCategory(category1);
		bean.addCategory(category2);
		List<CategoryBean> entries = bean.getCategories();

		// Assert
		assertEquals(1, entries.size());
		assertEquals(category1, entries.get(0));
	}

	/**
	 * For testing the addCategory method - scenario when a duplicate category
	 * is being inserted, and has no valid database id set.
	 */
	public void testAddCategory_DoNotInsertItemsWithDuplicateNames() {
		// Arrange
		CategoryBean category1 = new CategoryBean(2, "e1", CategorySortOrder.AlphabeticallyAsc);
		CategoryBean category2 = new CategoryBean(0, "e1", CategorySortOrder.AlphabeticallyAsc);
		ActivityBean bean = new ActivityBean(1, "test");

		// Act
		bean.addCategory(category1);
		bean.addCategory(category2);
		List<CategoryBean> entries = bean.getCategories();

		// Assert
		assertEquals(1, entries.size());
		assertEquals(category1, entries.get(0));
	}

	/**
	 * For testing the addCategory method - scenario when valid enties are added
	 * that have unique names, but some of them have database ids, and some
	 * don't
	 */
	public void testAddCategory_DoNotInsertValidItemsNoDuplicates() {
		// Arrange
		CategoryBean category1 = new CategoryBean(0, "e1", CategorySortOrder.AlphabeticallyAsc);
		CategoryBean category2 = new CategoryBean(1, "e2", CategorySortOrder.AlphabeticallyAsc);
		CategoryBean category3 = new CategoryBean(0, "e3", CategorySortOrder.AlphabeticallyAsc);
		CategoryBean category4 = new CategoryBean(2, "e4", CategorySortOrder.AlphabeticallyAsc);
		ActivityBean bean = new ActivityBean(1, "test");

		// Act
		bean.addCategory(category1);
		bean.addCategory(category2);
		bean.addCategory(category3);
		bean.addCategory(category4);
		List<CategoryBean> entries = bean.getCategories();

		// Assert
		assertEquals(4, entries.size());
		assertTrue(entries.contains(category1));
		assertTrue(entries.contains(category2));
		assertTrue(entries.contains(category3));
		assertTrue(entries.contains(category4));
	}

	/**
	 * For testing the addCategory method - scenario when a duplicate category
	 * is being inserted, and both entries have no database ids
	 */
	public void testAddCategory_DoNotInsertItemsWithDuplicateNames2() {
		// Arrange
		CategoryBean category1 = new CategoryBean(0, "e1", CategorySortOrder.AlphabeticallyAsc);
		CategoryBean category2 = new CategoryBean(0, "e1", CategorySortOrder.AlphabeticallyAsc);
		ActivityBean bean = new ActivityBean(1, "test");

		// Act
		bean.addCategory(category1);
		bean.addCategory(category2);
		List<CategoryBean> entries = bean.getCategories();

		// Assert
		assertEquals(1, entries.size());
		assertEquals(category1, entries.get(0));
	}

	/**
	 * For testing the getCategory by id method
	 */
	public void testGetCategory_byId() {
		// Arrange
		CategoryBean category1 = new CategoryBean(0, "e1", CategorySortOrder.AlphabeticallyAsc);
		CategoryBean category2 = new CategoryBean(1, "e2", CategorySortOrder.AlphabeticallyAsc);
		ActivityBean bean = new ActivityBean(1, "test");

		// Act
		bean.addCategory(category1);
		bean.addCategory(category2);

		// Scenario 1
		CategoryBean category = bean.getCategory(0);
		assertNull(category);

		// Scenario 2
		category = bean.getCategory(1);
		assertEquals(category2, category);
	}
	
	/**
	 * For testing the getCategory by name method
	 */
	public void testGetCategory_byName() {
		// Arrange
		CategoryBean category1 = new CategoryBean(0, "e1", CategorySortOrder.AlphabeticallyAsc);
		CategoryBean category2 = new CategoryBean(1, "e2", CategorySortOrder.AlphabeticallyAsc);
		ActivityBean bean = new ActivityBean(1, "test");

		// Act
		bean.addCategory(category1);
		bean.addCategory(category2);

		// Scenario 1
		CategoryBean category = bean.getCategory("e1");
		assertEquals(category1, category);

		// Scenario 2
		category = bean.getCategory("E2");
		assertEquals(category2, category);
	}

}
