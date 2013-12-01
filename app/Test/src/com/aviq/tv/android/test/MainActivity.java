package com.aviq.tv.android.test;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.aviq.tv.android.test.volley.VolleyActivity;
import com.aviq.tv.android.test.zapperlist.ZapperListActivity;

public class MainActivity extends ListActivity
{
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		adapter = new ArrayAdapter<String>(this, R.layout.list_item);
		adapter.add(ZapperListActivity.class.getName());
		adapter.add(VolleyActivity.class.getName());

		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		String testClassName = (String)l.getSelectedItem();
		Intent intent = new Intent();
		intent.setClassName(this, testClassName);
		startActivity(intent);
	}

}
