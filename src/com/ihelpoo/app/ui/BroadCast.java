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

import com.ihelpoo.app.AppContext;
import com.ihelpoo.app.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * 通知信息广播接收器
 * @version 1.0
 * @created 2012-4-16
 */
public class  BroadCast extends BroadcastReceiver {

	private final static int NOTIFICATION_ID = R.layout.main;
	
	private static int lastNoticeCount;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String ACTION_NAME = intent.getAction();
		if("com.ihelpoo.app.action.APPWIDGET_UPDATE".equals(ACTION_NAME))
		{
            int systemCount = intent.getIntExtra("systemCount", 0);//首页system消息
			int atmeCount = intent.getIntExtra("atmeCount", 0);//@我
			int commentCount = intent.getIntExtra("commentCount", 0);//评论
			int activeCount = intent.getIntExtra("activeCount", 0);//活跃
			int chatCount = intent.getIntExtra("chatCount", 0);//悄悄话
			int totalCount = systemCount+ atmeCount + commentCount + activeCount + chatCount;//信息总数
            //首页system消息
            if(Main.bv_system != null){
                if(systemCount > 0){
                    Main.bv_system.setText(systemCount+"");
                    Main.bv_system.show();
                }else{
                    Main.bv_system.setText("");
                    Main.bv_system.hide();
                }
            }
            //@我
            if(Main.bv_atme != null){
                if(atmeCount > 0){
                    Main.bv_atme.setText(atmeCount+"");
                    Main.bv_atme.show();
                }else{
                    Main.bv_atme.setText("");
                    Main.bv_atme.hide();
                }
            }
            //评论
            if(Main.bv_comment != null){
                if(commentCount > 0){
                    Main.bv_comment.setText(commentCount+"");
                    Main.bv_comment.show();
                }else{
                    Main.bv_comment.setText("");
                    Main.bv_comment.hide();
                }
            }
			//活跃
			if(Main.bv_active != null){
				if(activeCount > 0){
					Main.bv_active.setText(activeCount+"");
					Main.bv_active.show();
				}else{
					Main.bv_active.setText("");
					Main.bv_active.hide();
				}
			}
			//留言
			if(Main.bv_chat != null){
				if(chatCount > 0){
					Main.bv_chat.setText(chatCount+"");
					Main.bv_chat.show();
				}else{
					Main.bv_chat.setText("");
					Main.bv_chat.hide();
				}
			}
            //动态-总数
            if(Main.bv_Word != null){
                if(totalCount > 0){
                    Main.bv_Word.setText(totalCount+"");
                    Main.bv_Word.show();
                }else{
                    Main.bv_Word.setText("");
                    Main.bv_Word.hide();
                }
            }

            //通知栏显示
			this.notification(context, totalCount);
		}
	}

	private void notification(Context context, int noticeCount){
		//创建 NotificationManager
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		String contentTitle = "我帮圈圈";
		String contentText = "您有 " + noticeCount + " 条最新信息";
		int _lastNoticeCount;
		
		//判断是否发出通知信息
		if(noticeCount == 0)
		{
			notificationManager.cancelAll();
			lastNoticeCount = 0;
			return;
		}
		else if(noticeCount == lastNoticeCount)
		{
			return; 
		}
		else
		{
			_lastNoticeCount = lastNoticeCount;
			lastNoticeCount = noticeCount;
		}
		
		//创建通知 Notification
		Notification notification = null;
		
		if(noticeCount > _lastNoticeCount)
		{
			String noticeTitle = "您有 " + (noticeCount-_lastNoticeCount) + " 条最新信息";
			notification = new Notification(R.drawable.icon, noticeTitle, System.currentTimeMillis());
		}
		else
		{
			notification = new Notification();
		}
		
		//设置点击通知跳转
		Intent intent = new Intent(context, Main.class);
		intent.putExtra("NOTICE", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK); 
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		//设置最新信息
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		//设置点击清除通知
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		if(noticeCount > _lastNoticeCount)
		{
			//设置通知方式
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			
			//设置通知音-根据app设置是否发出提示音
			if(((AppContext)context.getApplicationContext()).isAppSound())
				notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notificationsound);
			
			//设置振动 <需要加上用户权限android.permission.VIBRATE>
			//notification.vibrate = new long[]{100, 250, 100, 500};
		}
		
		//发出通知
		notificationManager.notify(NOTIFICATION_ID, notification);		
	}
	
}
