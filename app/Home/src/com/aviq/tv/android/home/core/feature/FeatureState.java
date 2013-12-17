/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureState.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Defines the base class for state feature type
 */

package com.aviq.tv.android.home.core.feature;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.event.EventMessenger;
import com.aviq.tv.android.home.core.state.BaseState;
import com.aviq.tv.android.home.core.state.StateException;
import com.aviq.tv.android.home.utils.Prefs;

/**
 * Defines the base class for state feature type
 */
public abstract class FeatureState extends BaseState implements IFeature
{
	public static final String TAG = FeatureState.class.getSimpleName();
	protected FeatureSet _dependencies = new FeatureSet();
	private List<Subscription> _subscriptions = new ArrayList<Subscription>();

	@Override
	public void initialize(OnFeatureInitialized onFeatureInitialized)
	{
	}

	@Override
	public FeatureSet dependencies()
	{
		return _dependencies;
	}

	@Override
	public Type getType()
	{
		return IFeature.Type.STATE;
	}

	@Override
	public String getName()
	{
		return getStateName().toString();
	}

	@Override
	public Prefs getPrefs()
	{
		return Environment.getInstance().getFeaturePrefs(getStateName());
	}

	/**
	 * @return an event messenger associated with this feature
	 */
	@Override
	public EventMessenger getEventMessanger()
	{
		return Environment.getInstance().getEventMessenger();
	}

	@Override
    public void onEvent(int msgId, Bundle bundle)
	{
		Log.i(TAG, ".onEvent: msgId = " + msgId);
	}

	/**
	 * Subscribes this feature to event triggered from another feature
	 *
	 * @param feature
	 *            the feature to subscribe to
	 * @param msgId
	 *            the id of the message to subscribe
	 */
	protected void subscribe(IFeature featureTo, int msgId)
	{
		Log.i(TAG, ".subscribe: for " + featureTo.getName() + " on " + msgId);
		_subscriptions.add(new Subscription(featureTo, msgId));
	}

	/**
	 * On showing this FeatureState
	 *
	 * @param params
	 *            The params set to this State when showing
	 * @param isOverlay
	 *            set to true to show this state as Overlay
	 * @throws StateException
	 */
	@Override
	protected void onShow()
	{
		Log.i(TAG, ".onShow");
		for (Subscription subscription: _subscriptions)
		{
			Log.i(TAG, "Register " + subscription.Feature.getName() + " " + subscription.Feature.getType() + " to event id = " + subscription.MsgId);
			subscription.Feature.getEventMessanger().register(this, subscription.MsgId);
		}
	}

	/**
	 * On hiding this FeatureState
	 */
	@Override
	protected void onHide()
	{
		Log.i(TAG, ".onHide");
		for (Subscription subscription: _subscriptions)
		{
			Log.i(TAG, "Unregister " + subscription.Feature.getName() + " " + subscription.Feature.getType() + " to event id = " + subscription.MsgId);
			subscription.Feature.getEventMessanger().unregister(this, subscription.MsgId);
		}
	}

	public abstract FeatureName.State getStateName();

	private class Subscription
	{
		IFeature Feature;
		int MsgId;

		Subscription(IFeature feature, int msgId)
		{
			Feature = feature;
			MsgId = msgId;
		}
	}
}
