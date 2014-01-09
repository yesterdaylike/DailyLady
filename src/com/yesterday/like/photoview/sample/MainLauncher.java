package com.yesterday.like.photoview.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import net.youmi.android.AdManager;
import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yesterday.like.ActionBar.app.SherlockActivity;
import com.yesterday.like.PullToRefresh.library.PullToRefreshBase;
import com.yesterday.like.PullToRefresh.library.PullToRefreshBase.OnRefreshListener;
import com.yesterday.like.PullToRefresh.library.PullToRefreshBase.State;
import com.yesterday.like.PullToRefresh.library.PullToRefreshScrollView;
import com.yesterday.like.PullToRefresh.library.extras.SoundPullEventListener;
import com.yesterday.like.appmsg.AppMsg;
import com.yesterday.like.appmsg.AppMsg.Style;

public class MainLauncher extends SherlockActivity {
	private LinearLayout mPhotoListLayout;
	private static String TAG = "MainLauncher";
	public static final String FIRST_START = "first_start";
	private static String GALLERY_DIRECTORY = Environment.getExternalStorageDirectory().toString()+"/Aphoto/";
	private static String INDEX_PATH = Environment.getExternalStorageDirectory().toString()+"/Aphoto/index.txt";
	private String uri_index_file = "https://dl.dropboxusercontent.com/s/pmdlhgpn3g9tda4/version.txt?dl=1&token_hash=AAF7OIIyirM7C43bN875YWWwDM7SGZZ3VTuP3oPgYuls2w";

	private HashMap<String, ArrayList<String>> mPhotoMap;
	private static DisplayImageOptions OPTIONS = null;
	private PullToRefreshScrollView mPullRefreshScrollView;

	private boolean downloadweb = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Sherlock_Light);
		setContentView(R.layout.main_launcher);

		initPullRefreshScrollView();

		initImageLoader(this);
		mPhotoListLayout = (LinearLayout) findViewById(R.id.photo_list);
		downloadImageFromLocal();
		//downloadImageFromWeb();
		AdManager.getInstance(this).init("d2443810b3f145f5","e7dec3afae6297d9", false);
	}

	private void initPullRefreshScrollView(){
		mPullRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
		mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				//new GetDataTask().execute();
				Log.e(TAG, "setOnRefreshListener");
				downloadImageFromWeb();
			}
		});

		SoundPullEventListener<ScrollView> soundListener = new SoundPullEventListener<ScrollView>(this);
		soundListener.addSoundEvent(State.PULL_TO_REFRESH, R.raw.pull_event);
		soundListener.addSoundEvent(State.RESET, R.raw.reset_sound);
		soundListener.addSoundEvent(State.REFRESHING, R.raw.refreshing_sound);
		mPullRefreshScrollView.setOnPullEventListener(soundListener);
	}

	public static DisplayImageOptions getOptions(){
		if( OPTIONS == null ){
			OPTIONS = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.load)
			.showImageOnFail(R.drawable.load)
			.resetViewBeforeLoading(true)
			.cacheOnDisc(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.build();
		}
		return OPTIONS;
	}

	public static void showMsg(Activity activity,  String msg){
		showMsg(activity, msg, Gravity.BOTTOM);
	}
	public static void showMsg(Activity activity, String msg, int gravity){
		Style style = AppMsg.STYLE_INFO;
		AppMsg appMsg = AppMsg.makeText(activity, msg, style);
		appMsg.setLayoutGravity(gravity);
		appMsg.show();
		//显示Msg
	}

	public static void showMsg(Activity activity,  int strResId){
		showMsg(activity, strResId, Gravity.BOTTOM);
	}
	public static void showMsg(Activity activity, int strResId, int gravity){
		String msg = activity.getString(strResId);
		showMsg(activity, msg, gravity);
	}

	public void initPhotoListview(){
		String[] keyArray = mPhotoMap.keySet().toArray(new String[0]);
		Arrays.sort(keyArray);
		for (String key : keyArray) {
			
			int c = mPhotoListLayout.getChildCount();
			Log.e("initPhotoListview getChildCount","getChildCount ："+c);
			if( c%3 == 0 ){
				//实例化广告条
				AdView adView = new AdView(MainLauncher.this, AdSize.FIT_SCREEN);
				//将广告条加入到布局中
				mPhotoListLayout.addView(adView, 0);
			}
			
			View item = createPhotoItemView(key,mPhotoListLayout);
			//mPhotoList.addView(item);
			mPhotoListLayout.addView(item, 0);
		}
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
		.threadPriority(Thread.NORM_PRIORITY - 2)
		.denyCacheImageMultipleSizesInMemory()
		.discCacheFileNameGenerator(new Md5FileNameGenerator())
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.writeDebugLogs() // Remove for release app
		.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	/** 构建一个view，其中包含一组图片 */
	public View createPhotoItemView(String key, ViewGroup parent) {
		//String[] keyArray = mPhotoInGroups.keySet().toArray(new String[0]);
		LayoutInflater inflater = getLayoutInflater();
		View itemView = inflater.inflate(R.layout.photo_listitem, parent, false);

		ViewHolder viewHolder = new ViewHolder();
		viewHolder.title = (TextView)itemView.findViewById(R.id.listitem_title);
		viewHolder.group = (ImageButton)itemView.findViewById(R.id.listitem_group);
		viewHolder.preview =(LinearLayout)itemView.findViewById(R.id.listitem_preview);

		//String key = keyArray[position];
		viewHolder.title.setText(key);

		//Log.e("getPhotoItemView:",path);
		String[]photos = mPhotoMap.get(key).toArray(new String[0]);
		//Log.i(TAG,"viewHolder: "+floders[floders.length-1]);
		for (String photoUri : photos) {
			viewHolder.preview.addView(createImageViewWithBitmap(key,photoUri));
		}

		viewHolder.group.setTag(key);
		if(photos.length<2){
			viewHolder.group.setVisibility(View.GONE);
		}
		return itemView;
	}

	private class ViewHolder{
		TextView title;
		ImageButton group;
		LinearLayout preview;
	};

	private View createImageViewWithBitmap(String key,String photoUri) {
		// TODO Auto-generated method stub  
		int height = (int) getResources().getDimension(R.dimen.listitem_group_height);
		//Bitmap bm=decodeBitmapFromUri(absolutePath,height,height);
		ImageView imageView=new ImageView(getApplicationContext());
		imageView.setPadding(10, 0, 0, 0);
		imageView.setLayoutParams(new LayoutParams(height+10,height));  
		imageView.setScaleType(ScaleType.CENTER_CROP);
		imageView.setTag(photoUri);
		imageView.setTag(R.string.PHOTO_GROUP, key);
		imageView.setOnClickListener(imageClickListener);
		ImageLoader.getInstance().displayImage(photoUri, imageView, getOptions(), null);
		Log.v("createImageViewWithBitmap", photoUri);
		return imageView;
	}  


	public void onClickGroup(View view){
		//Log.e(TAG, view.getTag().toString());
		String key = view.getTag().toString();
		String[]photos = mPhotoMap.get(key).toArray(new String[0]);

		Intent intent = new Intent(MainLauncher.this, ViewPagerActivity.class);
		intent.putExtra("PATH", key);
		intent.putExtra("PHOTOS", photos);
		intent.putExtra("INDEX", 0);
		startActivity(intent);
	}

	OnClickListener imageClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			String file = view.getTag().toString();
			String path = view.getTag(R.string.PHOTO_GROUP).toString();

			ArrayList<String> photoGroup = mPhotoMap.get(path);
			if(photoGroup!=null){
				String[]photos = photoGroup.toArray(new String[0]);
				int index = mPhotoMap.get(path).indexOf(file);
				Intent intent = new Intent(MainLauncher.this, ViewPagerActivity.class);
				intent.putExtra("PATH", path);
				intent.putExtra("PHOTOS", photos);
				intent.putExtra("INDEX", index);
				startActivity(intent);
			}
		}
	};

	private void downloadImageFromLocal(){
		SharedPreferences settings = getSharedPreferences(FIRST_START, 0);
		boolean first = settings.getBoolean(FIRST_START, true);

		if( first ){
			showMsg(this, R.string.pull_to_refresh_pull_label, Gravity.TOP);
			FileCopyFromAssetsToSD(MainLauncher.this, "index.txt");
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(FIRST_START, false);
			editor.commit();
		}
		mPhotoMap = ReadIndexFromSD(MainLauncher.this, "index.txt");
		initPhotoListview();
	}

	private void downloadImageFromWeb(){
		if(!downloadweb){
			Log.e(TAG, "downloadImageFromWeb !downloadweb");
			final DownloadTask downloadTask = new DownloadTask(this);
			downloadTask.execute(uri_index_file);
		}
		else{
			Log.e(TAG, "mPullRefreshScrollView.onRefreshComplete");
			//mPullRefreshScrollView.onRefreshComplete();
		}
	}

	private class DownloadTask extends AsyncTask<String, Integer, String> {
		private Context context;
		HashMap<String, ArrayList<String>> photoMap;

		public DownloadTask(Context context) {
			this.context = context;
		}

		@SuppressLint("Wakelock")
		@Override
		protected String doInBackground(String... sUrl) {
			Log.e(TAG, "doInBackground downloadweb = true");
			downloadweb = true;
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,getClass().getName());
			wl.acquire();

			try {
				HttpURLConnection connection = null;

				URL url = new URL(sUrl[0]);
				connection = (HttpURLConnection) url.openConnection();
				connection.connect();

				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
					return "Server returned HTTP " + connection.getResponseCode() 
							+ " " + connection.getResponseMessage();

				InputStream input = connection.getInputStream();
				photoMap = ReadIndexFromFile(context, input);

			} catch (Exception e) {
				return e.toString();
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// if we get here, length is known, now set indeterminate to false
			/*mProgressDialog.setIndeterminate(false);
			mProgressDialog.setMax(100);
			mProgressDialog.setProgress(progress[0]);*/
		}

		@Override
		protected void onPostExecute(String result) {
			Log.e(TAG, "onPostExecute");
			//mProgressDialog.dismiss();
			ArrayList<String> updateKey = new ArrayList<String>();

			if(	null == photoMap || photoMap.size() < 1 ){
				Log.e(TAG, "null == photoMap");
				showMsg(MainLauncher.this, R.string.no_try_label, Gravity.BOTTOM);
				return;
			}

			String[] keyArray = photoMap.keySet().toArray(new String[0]);
			Arrays.sort(keyArray);
			for (String key : keyArray) {
				if(!mPhotoMap.containsKey(key)){
					int c = mPhotoListLayout.getChildCount();
					Log.e(" getChildCount","getChildCount ："+c);
					if( c%3 == 0 ){
						//实例化广告条
						AdView adView = new AdView(MainLauncher.this, AdSize.FIT_SCREEN);
						//将广告条加入到布局中
						mPhotoListLayout.addView(adView, 0);
					}

					mPhotoMap.put(key, photoMap.get(key));
					View view = createPhotoItemView(key, mPhotoListLayout);
					mPhotoListLayout.addView(view, 0);

					updateKey.add(key);
					break;////
				}
			}

			if(	null == updateKey || updateKey.size() < 1 ){
				Log.e(TAG, "null == updateKey");
				showMsg(MainLauncher.this, R.string.no_try_label, Gravity.TOP);
			}
			else{
				byte []newLine="\n".getBytes();
				FileOutputStream fos = null;
				File file = new File(INDEX_PATH);
				try {
					fos = new FileOutputStream(file, true);
					for (String path : updateKey) {
						fos.write(path.getBytes());
						fos.write(newLine);

						for (String string : mPhotoMap.get(path)) {
							Log.i("photos", string);
							fos.write(string.getBytes());
							fos.write(newLine);
						}
					}
					fos.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			downloadweb = false;
			mPullRefreshScrollView.onRefreshComplete();
		}
	}

	/**首次打开程序，将assets/index.txt复制到Environment.getExternalStorageDirectory().toString()+"/Aphoto/index.txt"*/
	public void FileCopyFromAssetsToSD(Context context, String file){
		byte []newLine="\n".getBytes(); 
		try {
			InputStream istream = getAssets().open(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(istream));
			File dir = new File(GALLERY_DIRECTORY);
			if(!dir.exists()){
				dir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(new File(GALLERY_DIRECTORY + file));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				fos.write(strLine.getBytes());
				fos.write(newLine);
			}
			fos.close();
			br.close();
			istream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 从sd卡中的index.txt中读取数据构建数据库
	 */
	public static HashMap<String, ArrayList<String>> ReadIndexFromSD(Context context, String file){
		FileInputStream istream;
		HashMap<String, ArrayList<String>> photoMap = null;
		try {
			istream = new FileInputStream(new File(INDEX_PATH));
			photoMap = ReadIndexFromFile(context, istream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return photoMap;
	}

	public static HashMap<String, ArrayList<String>> ReadIndexFromFile(Context context, InputStream inputStream){
		HashMap<String, ArrayList<String>> photoMap = new HashMap<String, ArrayList<String>>();
		Log.d(TAG, "ReadIndexFromFile HashMap");
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String strLine;
		ArrayList<String> group = null;

		Log.d(TAG, "ReadIndexFromFile HashMap");

		try {
			while ((strLine = br.readLine()) != null) {
				if( strLine.startsWith("20")){
					group = new ArrayList<String>();
					Log.d(TAG, "ReadIndexFromSD add group:"+strLine);
					photoMap.put(strLine, group);
				}
				else{
					Log.d(TAG, "ReadIndexFromSD add file:"+strLine);
					group.add(strLine);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
		}

		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
		}

		return photoMap;
	}
}

/*
"http://cdn.urbanislandz.com/wp-content/uploads/2011/10/MMSposter-large.jpg", // Very large image
"http://4.bp.blogspot.com/-LEvwF87bbyU/Uicaskm-g6I/AAAAAAAAZ2c/V-WZZAvFg5I/s800/Pesto+Guacamole+500w+0268.jpg", // Image with "Mark has been invalidated" problem
"file:///sdcard/Universal Image Loader @#&=+-_.,!()~'%20.png", // Image from SD card with encoded symbols
"assets://Living Things @#&=+-_.,!()~'%20.jpg", // Image from assets
"drawable://" + R.drawable.ic_launcher, // Image from drawables
"http://upload.wikimedia.org/wikipedia/ru/b/b6/Как_кот_с_мышами_воевал.png", // Link with UTF-8
"https://www.eff.org/sites/default/files/chrome150_0.jpg", // Image from HTTPS
"http://bit.ly/soBiXr", // Redirect link
"http://img001.us.expono.com/100001/100001-1bc30-2d736f_m.jpg", // EXIF
"", // Empty link
"http://wrong.site.com/corruptedLink", // Wrong link
 */
