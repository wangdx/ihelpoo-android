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

package com.ihelpoo.app.widget;

import com.ihelpoo.app.R;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;

/**
 * 用户信息对话框控件
 * @version 1.0
 * @created 2012-7-2
 */
public class UserInfoDialog extends Dialog {
	
	private LayoutParams lp;

	public UserInfoDialog(Context context) {
		super(context, R.style.Dialog);
		setContentView(R.layout.user_center_content);
		
		// 设置点击对话框之外能消失
		setCanceledOnTouchOutside(true);
		// 设置window属性
		lp = getWindow().getAttributes();
		lp.gravity = Gravity.TOP;
		lp.dimAmount = 0; // 去背景遮盖
		lp.alpha = 1.0f;
		lp.y = 55;
		getWindow().setAttributes(lp);

	}
}
