package com.aviq.tv.android.test.zapperlist;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.VideoView;

import com.aviq.tv.android.test.R;

public class ZapperListActivity extends Activity
{
	private static final String TAG = ZapperListActivity.class.getSimpleName();
	private ZapperListView _zapperListView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zapper_list);
		_zapperListView = (ZapperListView)findViewById(R.id.zapperListView);
		_zapperListView.addDrawable(R.drawable.vtx_2m_maroc);
		_zapperListView.addDrawable(R.drawable.vtx_3sat);
		_zapperListView.addDrawable(R.drawable.vtx_3_p);
		_zapperListView.addDrawable(R.drawable.vtx_4_p);
		_zapperListView.addDrawable(R.drawable.vtx_5star);
		_zapperListView.addDrawable(R.drawable.vtx_5usa);
		_zapperListView.addDrawable(R.drawable.vtx_aljazeera_en);
		_zapperListView.addDrawable(R.drawable.vtx_al_arabiya);
		_zapperListView.addDrawable(R.drawable.vtx_arte);
		_zapperListView.addDrawable(R.drawable.vtx_artede);
		_zapperListView.addDrawable(R.drawable.vtx_bbc_four_cbeebies);
		_zapperListView.addDrawable(R.drawable.vtx_bbc_one);
		_zapperListView.addDrawable(R.drawable.vtx_bbc_three_cbbc);
		_zapperListView.addDrawable(R.drawable.vtx_bbc_two);
		_zapperListView.addDrawable(R.drawable.vtx_bbc_world);
		_zapperListView.addDrawable(R.drawable.vtx_bfmtv);
		_zapperListView.addDrawable(R.drawable.vtx_bloomberg_eu);
		_zapperListView.addDrawable(R.drawable.vtx_buzmusik);
		_zapperListView.addDrawable(R.drawable.vtx_bvn_tv);
		_zapperListView.addDrawable(R.drawable.vtx_canal9);
		_zapperListView.addDrawable(R.drawable.vtx_canale5);
		_zapperListView.addDrawable(R.drawable.vtx_canal_24_horas);
		_zapperListView.addDrawable(R.drawable.vtx_canal_algerie);
		_zapperListView.addDrawable(R.drawable.vtx_canal_alpha_delemont);
		_zapperListView.addDrawable(R.drawable.vtx_canal_alpha_neuchatel);
		_zapperListView.addDrawable(R.drawable.vtx_canal_p);
		_zapperListView.addDrawable(R.drawable.vtx_cbs_drama);
		_zapperListView.addDrawable(R.drawable.vtx_cctv_news);
		_zapperListView.addDrawable(R.drawable.vtx_channel_4);
		_zapperListView.addDrawable(R.drawable.vtx_channel_5);
		_zapperListView.addDrawable(R.drawable.vtx_chart_show_dance);
		_zapperListView.addDrawable(R.drawable.vtx_chart_show_tv);
		_zapperListView.addDrawable(R.drawable.vtx_citv);
		_zapperListView.addDrawable(R.drawable.vtx_cnbc);
		_zapperListView.addDrawable(R.drawable.vtx_cnn);
		_zapperListView.addDrawable(R.drawable.vtx_das_erste);
		_zapperListView.addDrawable(R.drawable.vtx_direct8);
		_zapperListView.addDrawable(R.drawable.vtx_direct_star);
		_zapperListView.addDrawable(R.drawable.vtx_e4);
		_zapperListView.addDrawable(R.drawable.vtx_einsfestival);
		_zapperListView.addDrawable(R.drawable.vtx_einsplus);
		_zapperListView.addDrawable(R.drawable.vtx_equipe21_hd);
		_zapperListView.addDrawable(R.drawable.vtx_euronews);
		_zapperListView.addDrawable(R.drawable.vtx_euronews_en);
		_zapperListView.addDrawable(R.drawable.vtx_eurosport);
		_zapperListView.addDrawable(R.drawable.vtx_fashion_one);
		_zapperListView.addDrawable(R.drawable.vtx_film4);
		_zapperListView.addDrawable(R.drawable.vtx_food_network);
		_zapperListView.addDrawable(R.drawable.vtx_france2);
		_zapperListView.addDrawable(R.drawable.vtx_france24);
		_zapperListView.addDrawable(R.drawable.vtx_france24_en);
		_zapperListView.addDrawable(R.drawable.vtx_france2_hd);
		_zapperListView.addDrawable(R.drawable.vtx_france3);
		_zapperListView.addDrawable(R.drawable.vtx_france4);
		_zapperListView.addDrawable(R.drawable.vtx_france5);
		_zapperListView.addDrawable(R.drawable.vtx_franceo);
		_zapperListView.addDrawable(R.drawable.vtx_ftv);
		_zapperListView.addDrawable(R.drawable.vtx_ftv_en);
		_zapperListView.addDrawable(R.drawable.vtx_gulli);
		_zapperListView.addDrawable(R.drawable.vtx_horror_channel);
		_zapperListView.addDrawable(R.drawable.vtx_italia1);
		_zapperListView.addDrawable(R.drawable.vtx_itele);
		_zapperListView.addDrawable(R.drawable.vtx_itv_four);
		_zapperListView.addDrawable(R.drawable.vtx_itv_one);
		_zapperListView.addDrawable(R.drawable.vtx_itv_three);
		_zapperListView.addDrawable(R.drawable.vtx_itv_two);
		_zapperListView.addDrawable(R.drawable.vtx_kabel1);
		_zapperListView.addDrawable(R.drawable.vtx_kanal9);
		_zapperListView.addDrawable(R.drawable.vtx_kika);
		_zapperListView.addDrawable(R.drawable.vtx_latele);
		_zapperListView.addDrawable(R.drawable.vtx_lcp);
		_zapperListView.addDrawable(R.drawable.vtx_lemanbleu);
		_zapperListView.addDrawable(R.drawable.vtx_m6);
		_zapperListView.addDrawable(R.drawable.vtx_m6_hd);
		_zapperListView.addDrawable(R.drawable.vtx_montagnetv);
		_zapperListView.addDrawable(R.drawable.vtx_more4);
		_zapperListView.addDrawable(R.drawable.vtx_nhk_world);
		_zapperListView.addDrawable(R.drawable.vtx_nick);
		_zapperListView.addDrawable(R.drawable.vtx_nrj12);
		_zapperListView.addDrawable(R.drawable.vtx_nt1);
		_zapperListView.addDrawable(R.drawable.vtx_ntv);
		_zapperListView.addDrawable(R.drawable.vtx_orf1);
		_zapperListView.addDrawable(R.drawable.vtx_orf2);
		_zapperListView.addDrawable(R.drawable.vtx_pro7);
		_zapperListView.addDrawable(R.drawable.vtx_rai_due);
		_zapperListView.addDrawable(R.drawable.vtx_rai_news);
		_zapperListView.addDrawable(R.drawable.vtx_rai_sport1);
		_zapperListView.addDrawable(R.drawable.vtx_rai_tre);
		_zapperListView.addDrawable(R.drawable.vtx_rai_uno);
		_zapperListView.addDrawable(R.drawable.vtx_regio_collect);
		_zapperListView.addDrawable(R.drawable.vtx_rete4);
		_zapperListView.addDrawable(R.drawable.vtx_rougetv);
		_zapperListView.addDrawable(R.drawable.vtx_rtl);
		_zapperListView.addDrawable(R.drawable.vtx_rtl2);
		_zapperListView.addDrawable(R.drawable.vtx_rtp);
		_zapperListView.addDrawable(R.drawable.vtx_rts);
		_zapperListView.addDrawable(R.drawable.vtx_rts1_hd);
		_zapperListView.addDrawable(R.drawable.vtx_rts2_hd);
		_zapperListView.addDrawable(R.drawable.vtx_russia_today);
		_zapperListView.addDrawable(R.drawable.vtx_sat1);
		_zapperListView.addDrawable(R.drawable.vtx_sf1);
		_zapperListView.addDrawable(R.drawable.vtx_sf1_hd);
		_zapperListView.addDrawable(R.drawable.vtx_sf2);
		_zapperListView.addDrawable(R.drawable.vtx_sf2_hd);
		_zapperListView.addDrawable(R.drawable.vtx_sfi);
		_zapperListView.addDrawable(R.drawable.vtx_skynews);
		_zapperListView.addDrawable(R.drawable.vtx_sport1);
		_zapperListView.addDrawable(R.drawable.vtx_ssf);
		_zapperListView.addDrawable(R.drawable.vtx_super_rtl);
		_zapperListView.addDrawable(R.drawable.vtx_swr);
		_zapperListView.addDrawable(R.drawable.vtx_tagesschau24);
		_zapperListView.addDrawable(R.drawable.vtx_tele1);
		_zapperListView.addDrawable(R.drawable.vtx_telem1);
		_zapperListView.addDrawable(R.drawable.vtx_teletop);
		_zapperListView.addDrawable(R.drawable.vtx_telezuri);
		_zapperListView.addDrawable(R.drawable.vtx_tf1);
		_zapperListView.addDrawable(R.drawable.vtx_tf1_hd);
		_zapperListView.addDrawable(R.drawable.vtx_tmc);
		_zapperListView.addDrawable(R.drawable.vtx_trt);
		_zapperListView.addDrawable(R.drawable.vtx_tsi1);
		_zapperListView.addDrawable(R.drawable.vtx_tsi2);
		_zapperListView.addDrawable(R.drawable.vtx_tsr1);
		_zapperListView.addDrawable(R.drawable.vtx_tsr2);
		_zapperListView.addDrawable(R.drawable.vtx_tv5);
		_zapperListView.addDrawable(R.drawable.vtx_tv5monde);
		_zapperListView.addDrawable(R.drawable.vtx_tv8);
		_zapperListView.addDrawable(R.drawable.vtx_tve);
		_zapperListView.addDrawable(R.drawable.vtx_viva);
		_zapperListView.addDrawable(R.drawable.vtx_vox);
		_zapperListView.addDrawable(R.drawable.vtx_w9);
		_zapperListView.addDrawable(R.drawable.vtx_wdr);
		_zapperListView.addDrawable(R.drawable.vtx_zdf);
		_zapperListView.addDrawable(R.drawable.vtx_zdfinfo);
		_zapperListView.addDrawable(R.drawable.vtx_zdfkultur);
		_zapperListView.addDrawable(R.drawable.vtx_zdfneo);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		final VideoView videoView = (VideoView)findViewById(R.id.player);
		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
		{
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra)
			{
				Log.e(TAG, ".onError: what=" + what + ", extra=" + extra);
				return true;
			}
		});
		videoView.setVideoURI(Uri.parse("http://hls.wilmaa.com/master?channelId=france_2&b=1000-1800"));
		videoView.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)
		{
			onBackPressed();
			return true;
		}

		return _zapperListView.onKeyDown(keyCode, event);
	}
}
