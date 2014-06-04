package com.jakubkaleta.checklist.DataAccess.beans;

/**
 * PONO representing the current configuration of the application
 * 
 * @author Jakub Kaleta
 */
public class ConfigurationBean
{
	private Boolean disablePromptInToDoMode;

	/**
	 * Initializes a new object of ConfigurationBean
	 * 
	 * @param disablePromptInToDoMode
	 *            Current setting for disabling prompt in todo mode.
	 */
	public ConfigurationBean(Boolean disablePromptInToDoMode)
	{
		this.disablePromptInToDoMode = disablePromptInToDoMode;
	}

	/**
	 * Returns current setting for disabling prompt in todo mode.
	 * 
	 * @return Current setting for disabling prompt in todo mode.
	 */
	public Boolean getDisablePromptInToDoMode()
	{
		return disablePromptInToDoMode;
	}
}
