/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    BaseService.java
 * Author:      alek
 * Date:        15 Oct 2013
 * Description: Base for all application intent services
 */

package com.aviq.tv.android.home.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * Base for all application intent services
 */
public class BaseService extends IntentService
{
	public static final String EXTRA_RESULT_RECEIVER = "EXTRA_RESULT_RECEIVER";

	public BaseService(String arg0)
	{
		super(arg0);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		ResultReceiver resultReceiver = (ResultReceiver) intent.getExtra(EXTRA_RESULT_RECEIVER);
		int resultCode = 0;
		Bundle resultData = new Bundle();

		try
        {
	        Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }

		resultReceiver.send(resultCode, resultData);
	}
}
