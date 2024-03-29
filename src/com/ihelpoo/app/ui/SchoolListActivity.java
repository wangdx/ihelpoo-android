package com.ihelpoo.app.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ihelpoo.app.AppContext;
import com.ihelpoo.app.AppException;
import com.ihelpoo.app.R;
import com.ihelpoo.app.bean.SchoolInfo;
import com.ihelpoo.app.bean.SchoolList;
import com.ihelpoo.app.common.UIHelper;
import com.ihelpoo.app.widget.SchoolLetterListView;

import java.util.HashMap;
import java.util.List;

public class SchoolListActivity extends BaseActivity {
    public static final String SCHOOL_NAME_SELECTED = "school_name_selected";
    public static final String IS_SELECT = "is_select";
    private BaseAdapter adapter;
    private ListView mSchoolLit;
    private TextView overlay;
    private Button btnBack;
    private SchoolLetterListView letterListView;
    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;
    private Handler handler;
    private OverlayThread overlayThread;
    private List<SchoolInfo> schoolInfos;
    private ProgressDialog mProgress;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.school_list);

        mSchoolLit = (ListView) findViewById(R.id.school_list);
        btnBack = (Button) findViewById(R.id.btn_school_title_left);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            }
        });
        letterListView = (SchoolLetterListView) findViewById(R.id.school_letter_list_view);
        final AppContext ac = (AppContext) getApplication();

        mProgress = ProgressDialog.show(SchoolListActivity.this, "串校", "正在获取学校···");
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {

                if (mProgress != null) mProgress.dismiss();

                if (msg.what == 1 && msg.obj != null) {
                    SchoolList res = (SchoolList) msg.obj;

                    schoolInfos = res.getSchoolList();
                    letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
                    overlayThread = new OverlayThread();
                    initOverlay();
                    setAdapter(schoolInfos);
                    mSchoolLit.setOnItemClickListener(new SchoolListOnItemClick());
                } else {
                    ((AppException) msg.obj).makeToast(SchoolListActivity.this);
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                try {
                    SchoolList schooollist = ac.getSchoolList();
                    msg.what = 1;
                    msg.obj = schooollist;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                handler.sendMessage(msg);
            }
        }.start();

    }

    /**
     * @author sy
     */
    class SchoolListOnItemClick implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {


            SchoolInfo schoolInfo = (SchoolInfo) mSchoolLit.getAdapter().getItem(pos);
            SharedPreferences preferences = getSharedPreferences(NavWelcome.GLOBAL_CONFIG, MODE_PRIVATE);
            int mySchool = preferences.getInt(NavWelcome.CHOOSE_SCHOOL, NavWelcome.DEFAULT_SCHOOL);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(NavWelcome.CHOOSE_SCHOOL, schoolInfo.getId());
            editor.putString(NavWelcome.CHOOSE_SCHOOL_NAME, schoolInfo.getSchool());
            editor.commit();

            Intent intent = new Intent(SchoolListActivity.this, NavWhatsnewAnimation.class);
            intent.putExtra(SCHOOL_NAME_SELECTED, schoolInfo.getSchool());
            intent.putExtra(IS_SELECT, true);
//            if (isFirstIn(mySchool)) {
                startActivity(intent);
//            }
            SchoolListActivity.this.setResult(UIHelper.REQUEST_CODE_SCHOOL, intent);
            SchoolListActivity.this.finish();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        SharedPreferences preferences = getSharedPreferences(NavWelcome.GLOBAL_CONFIG, MODE_PRIVATE);
        int mySchool = preferences.getInt(NavWelcome.CHOOSE_SCHOOL, NavWelcome.DEFAULT_SCHOOL);
        if (isFirstIn(mySchool)) {
            UIHelper.ToastMessage(this.getApplicationContext(), "初次进入系统，请选择您所在的学校");
            return false;
        }

//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//            Intent intent = new Intent(SchoolListActivity.this, NavWhatsnewAnimation.class);
//            SchoolListActivity.this.setResult(Main.REQUEST_CODE_SCHOOL, intent);
//            startActivity(intent);
//            finish();
//        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isFirstIn(int mySchool) {
        return mySchool == NavWelcome.DEFAULT_SCHOOL;
    }

    /**
     * @param list
     */
    private void setAdapter(List<SchoolInfo> list) {
        if (list != null) {
            adapter = new ListAdapter(this, list);
            mSchoolLit.setAdapter(adapter);
        }

    }

    /**
     * ListViewAdapter
     *
     * @author sy
     */
    private class ListAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<SchoolInfo> list;

        public ListAdapter(Context context, List<SchoolInfo> list) {

            this.inflater = LayoutInflater.from(context);
            this.list = list;
            alphaIndexer = new HashMap<String, Integer>();
            sections = new String[list.size()];

            for (int i = 0; i < list.size(); i++) {
                String currentStr = list.get(i).getInitial();
                String previewStr = (i - 1) >= 0 ? list.get(i - 1).getInitial() : " ";
                if (!previewStr.equals(currentStr)) {
                    String name = list.get(i).getInitial();
                    alphaIndexer.put(name, i);
                    sections[i] = name;
                }
            }

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.school_list_item, null);
                holder = new ViewHolder();
                holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(list.get(position).getSchool());
            String currentStr = list.get(position).getInitial();
            String previewStr = (position - 1) >= 0 ? list.get(position - 1).getInitial() : " ";
            if (!previewStr.equals(currentStr)) {
                holder.alpha.setVisibility(View.VISIBLE);
                holder.alpha.setText(currentStr);
            } else {
                holder.alpha.setVisibility(View.GONE);
            }
            return convertView;
        }

        private class ViewHolder {
            TextView alpha;
            TextView name;
        }

    }

    private void initOverlay() {
        LayoutInflater inflater = LayoutInflater.from(this);
        overlay = (TextView) inflater.inflate(R.layout.school_overlay, null);
        overlay.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(overlay, lp);
    }

    private class LetterListViewListener implements SchoolLetterListView.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {
            if (alphaIndexer.get(s) != null) {
                int position = alphaIndexer.get(s);
                mSchoolLit.setSelection(position);
                overlay.setText(sections[position]);
                overlay.setVisibility(View.VISIBLE);
                handler.removeCallbacks(overlayThread);
                handler.postDelayed(overlayThread, 1500);
            }
        }

    }

    private class OverlayThread implements Runnable {

        @Override
        public void run() {
            overlay.setVisibility(View.GONE);
        }

    }

}