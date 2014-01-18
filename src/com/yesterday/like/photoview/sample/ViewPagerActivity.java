package com.yesterday.like.photoview.sample;

import net.youmi.android.spot.SpotManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yesterday.like.ActionBar.app.SherlockActivity;
import com.yesterday.like.ActionBar.view.Menu;
import com.yesterday.like.ActionBar.view.MenuItem;
import com.yesterday.like.ActionBar.view.SubMenu;
import com.yesterday.like.photoview.PhotoView;

public class ViewPagerActivity extends SherlockActivity {
	private String mPath;
	private String[] mPhotos;
	private PhotoView[] mPhotoViews;
	private ViewPager mViewPager;
	private int NUM_PAGES;
	private int currentPage;
	//Animation animZoomIn;

	private PhotoView mCurPhoto;
	private float currentRotation = 0;
	private final Handler rotationhandler = new Handler();
	private final Handler playHandler = new Handler();
	private boolean rotating = false;
	private boolean playing = false;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private boolean playSound = false;
	//private int height;
	//private int width;
	private MediaPlayer mediaPlayer;
	public static int [] raws = {
		R.raw.akari_hosoda,
		R.raw.anri_sugihara,
		R.raw.fukasawa,
		R.raw.kana_tsugihara,
		R.raw.may_iikubo,
		R.raw.morishita,
		R.raw.nozomi_kawasaki,
		R.raw.saori_yamamoto,
		R.raw.saya_hikita,
		R.raw.sayaka_numajiri,
		R.raw.sayaka_uchida,
		R.raw.shizuka_miyazawa,
		R.raw.takahashi,
		R.raw.takaou_ayatsuki,
		R.raw.takayo_oyama,
		R.raw.tamiko_hasunuma,
		R.raw.toyomi_suzuki,
		R.raw.yu_misaki,
		R.raw.yui_minami,
		R.raw.yuika_hotta,
		R.raw.yuka_hirose,
		R.raw.yuka_kawamoto,
		R.raw.yuka_mizusawa,
		R.raw.yukiko_nanase,
		R.raw.yuko_nakazawa,
		R.raw.yuko_ogura,
		R.raw.yumi_ishikawa,
		R.raw.yuri_himegami,
		R.raw.yuri_kimura,
		R.raw.yurina_inoue,
		R.raw.yuu_tejima,
		R.raw.yuuna_kawai,
		R.raw.yuuri_morishita,
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Sherlock_Light);
		setContentView(R.layout.view_pager);

		mViewPager = (ViewPager)findViewById(R.id.photo_viewpager);
		/*DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics); 
		height = displayMetrics.heightPixels;
		width = displayMetrics.widthPixels;*/
		SpotManager.getInstance(this).loadSpotAds();
		SpotManager.getInstance(this).setSpotTimeout(5000);//5√Î

		Intent intent = getIntent();
		mPath = intent.getStringExtra("PATH");
		mPhotos = intent.getStringArrayExtra("PHOTOS");
		NUM_PAGES = mPhotos.length;
		mPhotoViews = new PhotoView[NUM_PAGES];
		currentPage = intent.getIntExtra("INDEX", 0);

		//mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mViewPager.setAdapter(new SamplePagerAdapter());
		mViewPager.setCurrentItem(currentPage);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				//mViewPager.
				//getChildAt(arg0).
				//startAnimation(animZoomIn);
				currentPage = arg0;
				Log.e("currentPage NUM_PAGES", " "+currentPage+" "+NUM_PAGES);
				if(currentPage == NUM_PAGES-1 ){
					SpotManager.getInstance(ViewPagerActivity.this).showSpotAds(ViewPagerActivity.this);
				}

				if(playSound){
					if(mediaPlayer!=null){
						mediaPlayer.reset();
					}
					mediaPlayer=MediaPlayer.create(ViewPagerActivity.this, 
							raws[currentPage%raws.length]);
					mediaPlayer.setLooping(true);
					mediaPlayer.start();
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	private class SamplePagerAdapter extends PagerAdapter {

		/*private static final int[] sDrawables = { R.drawable.wallpaper, R.drawable.wallpaper, R.drawable.wallpaper,
			R.drawable.wallpaper, R.drawable.wallpaper, R.drawable.wallpaper };*/

		@Override
		public int getCount() {
			return mPhotos.length;
		}

		@Override
		public View instantiateItem(ViewGroup container, int position) {
			PhotoView photoView = new PhotoView(container.getContext());
			imageLoader.displayImage(mPhotos[position], photoView, options, null);

			mPhotoViews[position] = photoView;
			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			return photoView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		rotationhandler.removeCallbacksAndMessages(null);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		/*menu.add(Menu.NONE, 0, Menu.NONE, "Rotate 10 Right")
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(Menu.NONE, 1, Menu.NONE, "Rotate 10 Left")
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);*/
		menu.add(Menu.NONE, 0, Menu.NONE, R.string.toggle_automatic_rotation)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		menu.add(Menu.NONE, 1, Menu.NONE, R.string.toggle_play)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		SubMenu subMenuReset = menu.addSubMenu(Menu.NONE, 2, Menu.NONE, R.string.toggle_reset);
		subMenuReset.add(Menu.NONE, 20, Menu.NONE, "0");
		subMenuReset.add(Menu.NONE, 21, Menu.NONE, "90");
		subMenuReset.add(Menu.NONE, 22, Menu.NONE, "180");
		subMenuReset.add(Menu.NONE, 23, Menu.NONE, "270");

		MenuItem subMenuResetItem = subMenuReset.getItem();
		//subMenuResetItem.setTitle("Reset");
		//subMenu1Item.setIcon(R.drawable.ic_title_share_default);
		subMenuResetItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		menu.add(Menu.NONE, 3, Menu.NONE, R.string.play_sound)
		.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int currentItem = mViewPager.getCurrentItem();
		mCurPhoto = mPhotoViews[currentItem];

		switch (item.getItemId()) {
		case 0:
			toggleRotation();
			return true;
		case 1:
			play();
			return true;
		case 2:
			return true;
		case 20:
			currentRotation = 0;
			mCurPhoto.setPhotoViewRotation(currentRotation);
			return true;
		case 21:
			currentRotation = 90;
			mCurPhoto.setPhotoViewRotation(currentRotation);
			return true;
		case 22:
			currentRotation = 180;
			mCurPhoto.setPhotoViewRotation(currentRotation);
			return true;
		case 23:
			currentRotation = 270;
			mCurPhoto.setPhotoViewRotation(currentRotation);
			return true;
		case 3:
			playSound = !playSound;
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void play() {
		if (playing) {
			playHandler.removeCallbacksAndMessages(null);
			MainLauncher.showMsg(this, R.string.toggle_stop_play);
		} 
		else{
			playLoop();
			MainLauncher.showMsg(this, R.string.toggle_start_play);
		}
		playing = !playing;
	}
	private void playLoop() {
		playHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (currentPage == NUM_PAGES - 1) {
					currentPage = 0;
				}
				mViewPager.setCurrentItem(currentPage+1, true);
				playLoop();
			}
		}, 5000);
	}

	private void toggleRotation() {
		if (rotating) {
			rotationhandler.removeCallbacksAndMessages(null);
			rotating = false;
		} 
		rotateLoop();
		rotating = true;
	}

	private void rotateLoop() {
		rotationhandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				currentRotation += 1;
				mCurPhoto.setPhotoViewRotation(currentRotation);
				rotateLoop();
			}
		}, 15);
	}
	@Override
	protected void onDestroy() {
		if(mediaPlayer!=null){
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer=null;
		}
		super.onDestroy();
	}
}
