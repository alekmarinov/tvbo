/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureDependencies.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Encapsulates various features types
 */

package com.aviq.tv.android.home.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates various features types
 *
 */
public class FeatureSet
{
	public List<FeatureName.Component> Components = new ArrayList<FeatureName.Component>();
	public List<FeatureName.Scheduler> Schedulers = new ArrayList<FeatureName.Scheduler>();
	public List<FeatureName.State> States = new ArrayList<FeatureName.State>();

	public boolean isEmpty()
	{
		return Components.size() == 0 && Schedulers.size() == 0 && States.size() == 0;
	}

	public boolean has(IFeature feature)
	{
		switch (feature.getType())
		{
			case COMPONENT:
				return Components.indexOf(((FeatureComponent)feature).getId()) >= 0;
			case SCHEDULER:
				return Schedulers.indexOf(((FeatureScheduler)feature).getId()) >= 0;
			case STATE:
				return States.indexOf(((FeatureScheduler)feature).getId()) >= 0;
		}
		return false;
	}
}
