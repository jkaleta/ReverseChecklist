package com.jakubkaleta.checklist.DataAccess.tables.src;

import junit.framework.TestCase;

import com.jakubkaleta.checklist.DataAccess.ActivityListSortOrder;

/**
 * Contains tests for methods in the ActivityListSortOrder enum.
 * @author Jakub Kaleta 
 */
public class ActivityListSortOrderTests extends TestCase {
	
	/**
	 * For testing the fromNumber method
	 */
	public void testMethod_fromNumber() {
		
		// Assert
		assertEquals(ActivityListSortOrder.ShortestListsFirst, ActivityListSortOrder.fromNumber(0));
		assertEquals(ActivityListSortOrder.LongestListsFirst, ActivityListSortOrder.fromNumber(1));
		assertEquals(ActivityListSortOrder.LeastToDoFirst, ActivityListSortOrder.fromNumber(2));
		assertEquals(ActivityListSortOrder.MostToDoFirst, ActivityListSortOrder.fromNumber(3));
		assertEquals(ActivityListSortOrder.AlphabeticallyAsc, ActivityListSortOrder.fromNumber(4));
		assertEquals(ActivityListSortOrder.AlphabeticallyDesc, ActivityListSortOrder.fromNumber(5));
		
		assertEquals(ActivityListSortOrder.MostToDoFirst, ActivityListSortOrder.fromNumber(-4));
		assertEquals(ActivityListSortOrder.MostToDoFirst, ActivityListSortOrder.fromNumber(10));		
	}
	
	

}
