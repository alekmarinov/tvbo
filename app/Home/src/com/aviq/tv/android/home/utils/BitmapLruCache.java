package com.aviq.tv.android.home.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageCache
{
	public BitmapLruCache(int maxSize)
	{
		super(maxSize);
	}
	
	@Override
	protected int sizeOf(String key, Bitmap value)
	{
		return value.getRowBytes() * value.getHeight();
	}
	
	@Override
	public Bitmap getBitmap(String url)
	{
		return get(url);
	}
	
	@Override
	public void putBitmap(String url, Bitmap bitmap)
	{
		put(url, bitmap);
	}
}
