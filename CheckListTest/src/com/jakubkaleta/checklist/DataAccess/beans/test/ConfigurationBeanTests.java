package com.jakubkaleta.checklist.DataAccess.beans.test;

import junit.framework.TestCase;

import com.jakubkaleta.checklist.DataAccess.beans.ConfigurationBean;

/**
 * Contains tests for methods in the ConfigurationBean class.
 * 
 * @author Jakub Kaleta
 */
public class ConfigurationBeanTests extends TestCase
{
	/**
	 * For testing creation of the bean and properties
	 */
	public void testCreationAndGetters()
	{
		// Arrange
		Boolean disablePromptInToDoMode = true;
		
		// Act
		ConfigurationBean bean = new ConfigurationBean(disablePromptInToDoMode);

		// Assert
		assertEquals(disablePromptInToDoMode, bean.getDisablePromptInToDoMode());
	}

}
