package com.jakubkaleta.checklist.DataAccess.beans.test;

import junit.framework.TestCase;

import com.jakubkaleta.checklist.DataAccess.ActivityListSortOrder;
import com.jakubkaleta.checklist.DataAccess.beans.ApplicationStateBean;

/**
 * Contains tests for methods in the ApplicationStateBean class.
 * 
 * @author Jakub Kaleta
 */
public class ApplicationStateBeanTests extends TestCase
{
	/**
	 * For testing creation of the bean and properties
	 */
	public void testCreationAndGetters()
	{
		// Arrange
		int activityId = 1;
		int categoryId = 2;
		String mode = "TEST";
		int activityListSortOrder = 3;

		// Act
		ApplicationStateBean bean = new ApplicationStateBean(activityId, categoryId, mode,
				activityListSortOrder);

		// Assert
		assertEquals(activityId, bean.getActivityId());
		assertEquals(categoryId, bean.getCategoryId());
		assertEquals(mode, bean.getMode());
		assertEquals(ActivityListSortOrder.MostToDoFirst, bean.getActivityListSortOrder());

	}

}
