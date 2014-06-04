package com.jakubkaleta.checklist.DataAccess.beans.test;

import java.util.List;

import com.jakubkaleta.checklist.DataAccess.beans.ActivityBean;
import com.jakubkaleta.checklist.DataAccess.beans.ActivitiesDataSource;

import junit.framework.TestCase;

/**
 * @author Jakub Kaleta Contains tests for methods in the ActivityBean class.
 */
public class AllActivitiesDataSourceTests extends TestCase {
	
	/**
	 * For testing the addActivity method - scenario when a duplicate activity
	 * is being inserted, and has a non-0 database id set.
	 */
	public void testAddActivity_DoNotInsertItemsWithDuplicateIds() {
		// Arrange
		ActivityBean activity1 = new ActivityBean(2, "e1");
		ActivityBean activity2 = new ActivityBean(2, "e2");
		ActivitiesDataSource bean = new ActivitiesDataSource();

		// Act
		bean.addActivity(activity1);
		bean.addActivity(activity2);
		List<ActivityBean> entries = bean.getActivities();

		// Assert
		assertEquals(1, entries.size());
		assertEquals(activity1, entries.get(0));
	}

	/**
	 * For testing the addActivity method - scenario when a duplicate activity
	 * is being inserted, and has no valid database id set.
	 */
	public void testAddActivity_DoNotInsertItemsWithDuplicateNames() {
		// Arrange
		ActivityBean activity1 = new ActivityBean(2, "e1");
		ActivityBean activity2 = new ActivityBean(0, "e1");
		ActivitiesDataSource bean = new ActivitiesDataSource();

		// Act
		bean.addActivity(activity1);
		bean.addActivity(activity2);
		List<ActivityBean> entries = bean.getActivities();

		// Assert
		assertEquals(1, entries.size());
		assertEquals(activity1, entries.get(0));
	}

	/**
	 * For testing the addActivity method - scenario when valid entries are added
	 * that have unique names, but some of them have database ids, and some
	 * don't
	 */
	public void testAddActivity_DoNotInsertValidItemsNoDuplicates() {
		// Arrange
		ActivityBean activity1 = new ActivityBean(0, "e1");
		ActivityBean activity2 = new ActivityBean(1, "e2");
		ActivityBean activity3 = new ActivityBean(0, "e3");
		ActivityBean activity4 = new ActivityBean(2, "e4");
		ActivitiesDataSource bean = new ActivitiesDataSource();

		// Act
		bean.addActivity(activity1);
		bean.addActivity(activity2);
		bean.addActivity(activity3);
		bean.addActivity(activity4);
		List<ActivityBean> entries = bean.getActivities();

		// Assert
		assertEquals(4, entries.size());
		assertTrue(entries.contains(activity1));
		assertTrue(entries.contains(activity2));
		assertTrue(entries.contains(activity3));
		assertTrue(entries.contains(activity4));
	}

	/**
	 * For testing the addActivity method - scenario when a duplicate activity
	 * is being inserted, and both entries have no database ids
	 */
	public void testAddActivity_DoNotInsertItemsWithDuplicateNames2() {
		// Arrange
		ActivityBean activity1 = new ActivityBean(0, "e1");
		ActivityBean activity2 = new ActivityBean(0, "e1");
		ActivitiesDataSource bean = new ActivitiesDataSource();

		// Act
		bean.addActivity(activity1);
		bean.addActivity(activity2);
		List<ActivityBean> entries = bean.getActivities();

		// Assert
		assertEquals(1, entries.size());
		assertEquals(activity1, entries.get(0));
	}

	/**
	 * For testing the getActivity by id method
	 */
	public void testGetActivity_byId() {
		// Arrange
		ActivityBean activity1 = new ActivityBean(0, "e1");
		ActivityBean activity2 = new ActivityBean(1, "e2");
		ActivitiesDataSource bean = new ActivitiesDataSource();

		// Act
		bean.addActivity(activity1);
		bean.addActivity(activity2);

		// Scenario 1
		ActivityBean activity = bean.getActivity(0);
		assertNull(activity);

		// Scenario 2
		activity = bean.getActivity(1);
		assertEquals(activity2, activity);
	}
	
	/**
	 * For testing the getActivity by name method
	 */
	public void testGetActivity_byName() {
		// Arrange
		ActivityBean activity1 = new ActivityBean(0, "e1");
		ActivityBean activity2 = new ActivityBean(1, "e2");
		ActivitiesDataSource bean = new ActivitiesDataSource();

		// Act
		bean.addActivity(activity1);
		bean.addActivity(activity2);

		// Scenario 1
		ActivityBean activity = bean.getActivity("e1");
		assertEquals(activity1, activity);

		// Scenario 2
		activity = bean.getActivity("E2");
		assertEquals(activity2, activity);
	}
	
	/**
	 * For testing the getActivity by name method
	 */
	public void testGetNewActivities() {
		// Arrange
		ActivityBean activity1 = new ActivityBean(0, "e1");
		ActivityBean activity2 = new ActivityBean(1, "e2");
		ActivityBean activity3 = new ActivityBean(0, "e3");
		ActivitiesDataSource bean = new ActivitiesDataSource();

		// Act
		bean.addActivity(activity1);
		bean.addActivity(activity2);
		bean.addActivity(activity3);

		List<ActivityBean> newActivities = bean.getNewActivities();
		
		// Assert
		
		assertEquals(2, newActivities.size());
	}

	
}
