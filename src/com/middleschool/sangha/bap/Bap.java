package com.middleschool.sangha.bap;

import java.util.Calendar;

import toast.library.meal.MealLibrary;
import whdghks913.tistory.floatingactionbutton.FloatingActionButton;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.middleschool.sangha.R;
import com.tistory.whdghks913.croutonhelper.CroutonHelper;

import de.keyboardsurfer.android.widget.crouton.Style;

public class Bap extends Activity {
	/**
	 * 한번 급식정보를 받을때마다 272kb 데이터 소모
	 */

	private BapListViewAdapter mAdapter;
	private ListView mListView;

	// private Handler mHandler;
	private ProcessTask mProcessTask;

	private String[] calender, morning, lunch, night;

	private CroutonHelper mHelper;

	private SharedPreferences bapList;
	private SharedPreferences.Editor bapListeditor;

	private ProgressDialog mDialog;

	private final String savedList = "저장된 정보를 불러왔습니다\n과거 정보일경우 새로고침 해주세요";
	private final String noMessage = "연결상태가 좋지 않아 급식 정보를 받아오는대 실패했습니다";
	private final String loadingList = "급식 정보를 받아오고 있습니다...";
	private final String loadList = "인터넷에서 급식 정보를 받아왔습니다";
	private final String Syncing = "지금 로딩중입니다";

	private boolean isSync = false;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_bap);
		
		
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3d9970")));
        getActionBar().setDisplayShowHomeEnabled(false);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			ActionBar actionBar1 = getActionBar();
			actionBar1.setHomeButtonEnabled(true);
			actionBar1.setDisplayHomeAsUpEnabled(true);
		}

		// mHandler = new MyHandler(this);
		mHelper = new CroutonHelper(this);
		mAdapter = new BapListViewAdapter(this);

		bapList = getSharedPreferences("bapList", 0);
		bapListeditor = bapList.edit();

		mListView = (ListView) findViewById(R.id.mBapList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View view, int position,
					long id) {
				BapListData mData = mAdapter.getItem(position);

				Intent msg = new Intent(Intent.ACTION_SEND);
				msg.addCategory(Intent.CATEGORY_DEFAULT);
				msg.putExtra(Intent.EXTRA_TITLE, String.format(
						getString(R.string.shareBap_message_title),
						mData.mCalender));
				msg.putExtra(Intent.EXTRA_TEXT, String.format(
						getString(R.string.shareBap_message_msg),
						mData.mCalender, mData.mMorning, mData.mLunch,
						mData.mNight));
				msg.setType("text/plain");
				startActivity(Intent.createChooser(msg,
						getString(R.string.shareBap_title)));
			}
		});

		if (bapList.getBoolean("checker", false)) {
			calender = restore("calender");
			morning = restore("morning");
			lunch = restore("lunch");
			night = restore("night");

			getBapList();
			// mHandler.sendEmptyMessage(1);

			autoScroll();

			mHelper.setText(savedList);
			mHelper.setStyle(Style.CONFIRM);
			mHelper.show();
		} else {
			if (isNetwork()) {
				calender = new String[7];
				morning = new String[7];
				lunch = new String[7];
				night = new String[7];

				mProcessTask = new ProcessTask();
				mProcessTask.execute();

				// sync();

			} else {
				mHelper.setText(noMessage);
				mHelper.setStyle(Style.ALERT);
				mHelper.show();
			}
		}
		FloatingActionButton mFloatingButton = (FloatingActionButton) findViewById(R.id.mFloatingActionButton);
		mFloatingButton.attachToListView(mListView);
		mFloatingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 새로고침 소스 추가
				{
				if (isNetwork()) {
					if (!isSync) {
							// sync();

						mProcessTask = new ProcessTask();
						mProcessTask.execute();

					} else {
						mHelper.clearCroutonsForActivity();
						mHelper.setText(Syncing);
						mHelper.setStyle(Style.INFO);
						mHelper.show();
					}
				} else {
					mHelper.clearCroutonsForActivity();
					mHelper.setText(noMessage);
					mHelper.setStyle(Style.ALERT);
					mHelper.show();
					}
				}
			}
		});
	}
	
	private void autoScroll() {
		Calendar Date = Calendar.getInstance();
		int dateIndex = Date.get(Calendar.DAY_OF_WEEK);
		mListView.setSelection(dateIndex - 1);
	}

	/*
	 * private void sync() { isSync = true;
	 * 
	 * mAdapter.clearData();
	 * 
	 * new Thread() {
	 * 
	 * @Override public void run() { // mHandler.sendEmptyMessage(0);
	 * 
	 * try { calender = MealLibrary.getDateNew("ice.go.kr", "E100001786", "4",
	 * "04", "1"); morning = MealLibrary.getMealNew("ice.go.kr", "E100001786",
	 * "4", "04", "1"); lunch = MealLibrary.getMealNew("ice.go.kr",
	 * "E100001786", "4", "04", "2"); night =
	 * MealLibrary.getMealNew("ice.go.kr", "E100001786", "4", "04", "3");
	 * 
	 * save("calender", calender); save("morning", morning); save("lunch",
	 * lunch); save("night", night);
	 * 
	 * // mHandler.sendEmptyMessage(1);
	 * 
	 * mHelper.setText(loadList); mHelper.setStyle(Style.CONFIRM);
	 * mHelper.show();
	 * 
	 * } catch (Exception ex) { ex.printStackTrace();
	 * 
	 * mAdapter.clearData(); mAdapter.notifyDataSetChanged();
	 * 
	 * mHelper.setText(noMessage); mHelper.setStyle(Style.ALERT);
	 * mHelper.show(); } // mHandler.sendEmptyMessage(2); isSync = false; }
	 * }.start(); }
	 */

	private String getDate(int num) {
		if (num == 0)
			return "일요일";
		else if (num == 1)
			return "월요일";
		else if (num == 2)
			return "화요일";
		else if (num == 3)
			return "수요일";
		else if (num == 4)
			return "목요일";
		else if (num == 5)
			return "금요일";
		else if (num == 6)
			return "토요일";
		return null;
	}

	private void save(String name, String[] value) {
		for (int i = 0; i < value.length; i++) {
			bapListeditor.putString(name + "_" + i, value[i]);
		}
		bapListeditor.putBoolean("checker", true);
		bapListeditor.putInt(name, value.length);
		bapListeditor.commit();
	}

	private String[] restore(String name) {
		int length = bapList.getInt(name, 0);
		String[] string = new String[length];

		for (int i = 0; i < length; i++) {
			string[i] = bapList.getString(name + "_" + i, "");
		}
		return string;
	}

	private boolean isNetwork() {
		ConnectivityManager manager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobile = manager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifi = manager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (wifi.isConnected() || mobile.isConnected())
			return true;
		else
			return false;
	}

	// private void addErrorList() {
	// mAdapter.addItem("알수 없음", "알수 없음", noMessage, noMessage, noMessage);
	// }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.bap, menu);

		return super.onCreateOptionsMenu(menu);
	}


	@Override
	protected void onPause() {
		super.onPause();

		if (mDialog != null)
			mDialog.dismiss();

		mHelper.cencle(true);
	}

	/*
	 * private class MyHandler extends Handler { private final
	 * WeakReference<Bap> mActivity;
	 * 
	 * public MyHandler(Bap bap) { mActivity = new WeakReference<Bap>(bap); }
	 * 
	 * @Override public void handleMessage(Message msg) { Bap activity =
	 * mActivity.get(); if (activity != null) {
	 * 
	 * if (msg.what == 0) { if (mDialog == null) { mDialog = ProgressDialog
	 * .show(Bap.this, "", loadingList); } } else if (msg.what == 1) { for (int
	 * i = 0; i < 7; i++) { mAdapter.addItem(calender[i], getDate(i),
	 * morning[i], lunch[i], night[i]); } mAdapter.notifyDataSetChanged(); }
	 * else if (msg.what == 2) { mDialog.dismiss(); } } } }
	 */

	public void getBapList() {
		for (int i = 0; i < 7; i++) {
			mAdapter.addItem(calender[i], getDate(i), morning[i], lunch[i],
					night[i]);
		}
	}

	public class ProcessTask extends AsyncTask<String, Integer, Long> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (mDialog == null)
				mDialog = ProgressDialog.show(Bap.this, "", loadingList);

			isSync = true;
			mAdapter.clearData();
		}

		@Override
		protected Long doInBackground(String... params) {
			final String CountryCode = "goe.go.kr"; // 접속 할 교육청 도메인
			final String schulCode = "J100005861"; // 학교 고유 코드
			final String schulCrseScCode = "3"; // 학교 종류 코드 1
			final String schulKndScCode = "03"; // 학교 종류 코드 2

			try {
				calender = MealLibrary.getDateNew(CountryCode, schulCode,
						schulCrseScCode, schulKndScCode, "1");
				morning = MealLibrary.getMealNew(CountryCode, schulCode,
						schulCrseScCode, schulKndScCode, "1");
				lunch = MealLibrary.getMealNew(CountryCode, schulCode,
						schulCrseScCode, schulKndScCode, "2");
				night = MealLibrary.getMealNew(CountryCode, schulCode,
						schulCrseScCode, schulKndScCode, "3");

				save("calender", calender);
				save("morning", morning);
				save("lunch", lunch);
				save("night", night);

				isSync = false;

			} catch (Exception e) {
				Bap.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {

						// 에러
						mAdapter.clearData();
						mAdapter.notifyDataSetChanged();

						mHelper.setText(noMessage);
						mHelper.setStyle(Style.ALERT);
						mHelper.show();

						isSync = false;
					}
				});
				return -1l;
			}
			return 0l;
		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);

			mDialog.dismiss();

			if (result == -1l)
				return;

			getBapList();
			mAdapter.notifyDataSetChanged();

			mHelper.setText(loadList);
			mHelper.setStyle(Style.CONFIRM);
			mHelper.show();
		}
	}
}
