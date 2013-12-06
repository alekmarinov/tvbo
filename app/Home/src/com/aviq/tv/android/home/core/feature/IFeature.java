/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    IFeature.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Feature interface defining one functional element
 */

package com.aviq.tv.android.home.core.feature;

import com.aviq.tv.android.home.utils.Prefs;




/**
 * Feature interface defining one functional element
 */
public interface IFeature
{
	enum Type
	{
		COMPONENT,
		SCHEDULER,
		STATE
	}

	public interface OnFeatureInitialized
	{
		public void onInitialized(IFeature feature, int resultCode);
	}

	/**
	 * Method to be invoked to initialize this feature
	 */
	void initialize(OnFeatureInitialized onFeatureInitialized);

	/**
	 * Define the other features this feature is depending on
	 * @return FeatureSet
	 */
	FeatureSet dependencies();

	/**
	 * @return feature name
	 */
	String getName();

	/**
	 * @return feature type
	 */
	Type getType();

	/**
	 * @return feature preferences
	 */
	Prefs getPrefs();
}
