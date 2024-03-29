/*
 * Copyright (c) 2013 Wobang Network.
 *
 * Licensed under the GNU General Public License, version 2 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ihelpoo.app.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ihelpoo.app.AppContext;
import com.ihelpoo.app.AppException;
import com.ihelpoo.app.adapter.ListViewFriendAdapter;
import com.ihelpoo.app.bean.FriendList;
import com.ihelpoo.app.common.UIHelper;
import com.ihelpoo.app.widget.PullToRefreshListView;
import com.ihelpoo.app.R;
import com.ihelpoo.app.bean.Notice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 用户关注、粉丝
 *
 * @version 1.0
 * @created 2012-3-21
 */
public class UserFriend extends BaseActivity {

    private ImageView refresh;
    private Button mBack;
    private ProgressBar mProgressbar;

    private Button btn_followers;
    private Button btn_friends;

    private PullToRefreshListView mlvFriend;
    private ListViewFriendAdapter lvFriendAdapter;
    private List<FriendList.Friend> lvFriendData = new ArrayList<FriendList.Friend>();
    private View lvFriend_footer;
    private TextView lvFriend_foot_more;
    private ProgressBar lvFriend_foot_progress;
    private Handler mFriendHandler;
    private int lvSumData;

    private int curLvCatalog;
    private int curLvDataState;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_friend);

        this.initView();

        this.initData();
    }

    /**
     * 头部按钮展示
     *
     * @param type
     */
    private void headButtonSwitch(int type) {
        switch (type) {
            case DATA_LOAD_ING:
                mProgressbar.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.GONE);
                break;
            case DATA_LOAD_COMPLETE:
                mProgressbar.setVisibility(View.GONE);
                refresh.setVisibility(View.VISIBLE);
                break;
        }
    }

    //初始化视图控件
    private void initView() {
        refresh = (ImageView) findViewById(R.id.user_info_refresh);
        refresh.setOnClickListener(refreshClickListener);
        mBack = (Button) findViewById(R.id.friend_head_back);
        mBack.setOnClickListener(UIHelper.finish(this));
        mProgressbar = (ProgressBar) findViewById(R.id.friend_head_progress);

        btn_followers = (Button) findViewById(R.id.friend_type_followers);
        btn_friends = (Button) findViewById(R.id.friend_type_friends);

        btn_followers.setOnClickListener(this.friendBtnClick(btn_followers, FriendList.TYPE_FOLLOWER));
        btn_friends.setOnClickListener(this.friendBtnClick(btn_friends, FriendList.TYPE_FRIEND));

        //设置当前分类
        curLvCatalog = getIntent().getIntExtra("friend_type", FriendList.TYPE_FRIEND);
        if (curLvCatalog == FriendList.TYPE_FOLLOWER) {
            btn_followers.setEnabled(false);
        } else {
            btn_friends.setEnabled(false);
        }

        //设置粉丝与关注的数量
        int followers = getIntent().getIntExtra("friend_followers", 0);
        int fans = getIntent().getIntExtra("friend_fans", 0);
        btn_friends.setText(getString(R.string.user_friend_follower, followers));
        btn_followers.setText(getString(R.string.user_friend_fans, fans));

        lvFriend_footer = getLayoutInflater().inflate(R.layout.listview_footer, null);
        lvFriend_foot_more = (TextView) lvFriend_footer.findViewById(R.id.listview_foot_more);
        lvFriend_foot_progress = (ProgressBar) lvFriend_footer.findViewById(R.id.listview_foot_progress);

        lvFriendAdapter = new ListViewFriendAdapter(this, lvFriendData, R.layout.friend_listitem);
        mlvFriend = (PullToRefreshListView) findViewById(R.id.friend_listview);

        mlvFriend.addFooterView(lvFriend_footer);//添加底部视图  必须在setAdapter前
        mlvFriend.setAdapter(lvFriendAdapter);
        mlvFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击头部、底部栏无效
                if (position == 0 || view == lvFriend_footer) return;

                TextView name = (TextView) view.findViewById(R.id.friend_listitem_name);
                FriendList.Friend friend = (FriendList.Friend) name.getTag();

                if (friend == null) return;

                //跳转
                UIHelper.showUserCenter(view.getContext(), friend.getUid(), friend.getNickname());
            }
        });
        mlvFriend.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mlvFriend.onScrollStateChanged(view, scrollState);

                //数据为空--不用继续下面代码了
                if (lvFriendData.size() == 0) return;

                //判断是否滚动到底部
                boolean scrollEnd = false;
                try {
                    if (view.getPositionForView(lvFriend_footer) == view.getLastVisiblePosition())
                        scrollEnd = true;
                } catch (Exception e) {
                    scrollEnd = false;
                }

                if (scrollEnd && curLvDataState == UIHelper.LISTVIEW_DATA_MORE) {
                    mlvFriend.setTag(UIHelper.LISTVIEW_DATA_LOADING);
                    lvFriend_foot_more.setText(R.string.load_ing);
                    lvFriend_foot_progress.setVisibility(View.VISIBLE);
                    //当前pageIndex
                    int pageIndex = lvSumData / 20;
                    loadLvFriendData(curLvCatalog, pageIndex, mFriendHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
                }
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mlvFriend.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        });
        mlvFriend.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                loadLvFriendData(curLvCatalog, 0, mFriendHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });
    }

    //初始化控件数据
    private void initData() {
        mFriendHandler = new Handler() {
            public void handleMessage(Message msg) {

                headButtonSwitch(DATA_LOAD_COMPLETE);

                if (msg.what >= 0) {
                    FriendList list = (FriendList) msg.obj;
                    Notice notice = list.getNotice();
                    //处理listview数据
                    switch (msg.arg1) {
                        case UIHelper.LISTVIEW_ACTION_INIT:
                        case UIHelper.LISTVIEW_ACTION_REFRESH:
                        case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
                            lvSumData = msg.what;
                            lvFriendData.clear();//先清除原有数据
                            lvFriendData.addAll(list.getFriendlist());
                            break;
                        case UIHelper.LISTVIEW_ACTION_SCROLL:
                            lvSumData += msg.what;
                            if (lvFriendData.size() > 0) {
                                for (FriendList.Friend friend1 : list.getFriendlist()) {
                                    boolean b = false;
                                    for (FriendList.Friend friend2 : lvFriendData) {
                                        if (friend1.getUid() == friend2.getUid()) {
                                            b = true;
                                            break;
                                        }
                                    }
                                    if (!b) lvFriendData.add(friend1);
                                }
                            } else {
                                lvFriendData.addAll(list.getFriendlist());
                            }
                            break;
                    }

                    if (msg.what < 20) {
                        curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
                        lvFriendAdapter.notifyDataSetChanged();
                        lvFriend_foot_more.setText(R.string.load_full);
                    } else if (msg.what == 20) {
                        curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
                        lvFriendAdapter.notifyDataSetChanged();
                        lvFriend_foot_more.setText(R.string.load_more);
                    }
                    //发送通知广播
                    if (notice != null) {
                        UIHelper.sendBroadCast(UserFriend.this, notice);
                    }
                } else if (msg.what == -1) {
                    //有异常--显示加载出错 & 弹出错误消息
                    curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
                    lvFriend_foot_more.setText(R.string.load_error);
                    ((AppException) msg.obj).makeToast(UserFriend.this);
                }
                if (lvFriendData.size() == 0) {
                    curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
                    lvFriend_foot_more.setText(R.string.load_empty);
                }
                lvFriend_foot_progress.setVisibility(View.GONE);
                if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
                    mlvFriend.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
                    mlvFriend.setSelection(0);
                } else if (msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG) {
                    mlvFriend.onRefreshComplete();
                    mlvFriend.setSelection(0);
                }
            }
        };
        this.loadLvFriendData(curLvCatalog, 0, mFriendHandler, UIHelper.LISTVIEW_ACTION_INIT);
    }

    /**
     * 线程加载好友列表数据
     *
     * @param type      0:显示自己的粉丝 1:显示自己的关注者
     * @param pageIndex 当前页数
     * @param handler   处理器
     * @param action    动作标识
     */
    private void loadLvFriendData(final int type, final int pageIndex, final Handler handler, final int action) {
        headButtonSwitch(DATA_LOAD_ING);
        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    FriendList FriendList = ((AppContext) getApplication()).getFriendList(type, pageIndex, isRefresh);
                    msg.what = FriendList.getFriendlist().size();
                    msg.obj = FriendList;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;//告知handler当前action
                if (curLvCatalog == type)
                    handler.sendMessage(msg);
            }
        }.start();
    }

    private View.OnClickListener friendBtnClick(final Button btn, final int catalog) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                if (btn == btn_followers)
                    btn_followers.setEnabled(false);
                else
                    btn_followers.setEnabled(true);
                if (btn == btn_friends)
                    btn_friends.setEnabled(false);
                else
                    btn_friends.setEnabled(true);

                lvFriend_foot_more.setText(R.string.load_more);
                lvFriend_foot_progress.setVisibility(View.GONE);

                curLvCatalog = catalog;
                loadLvFriendData(curLvCatalog, 0, mFriendHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
            }
        };
    }

    private View.OnClickListener refreshClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            loadLvFriendData(curLvCatalog, 0, mFriendHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            headButtonSwitch(DATA_LOAD_ING);

        }
    };
}
