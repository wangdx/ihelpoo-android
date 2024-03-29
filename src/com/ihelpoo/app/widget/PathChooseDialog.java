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

import java.io.File;
import java.util.List;
import java.util.Stack;

import com.ihelpoo.app.common.StringUtils;
import com.ihelpoo.app.common.UIHelper;
import com.ihelpoo.app.R;
import com.ihelpoo.app.adapter.ListViewPathAdapter;
import com.ihelpoo.app.adapter.ListViewPathAdapter.OnPathOperateListener;
import com.ihelpoo.app.common.FileUtils;
import com.ihelpoo.app.common.FileUtils.PathStatus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 路径选择弹窗
 * @author yeguozhong@yeah.net
 * 
 */
public class PathChooseDialog extends Dialog {

	private ListView lv;
	private Button btnComfirm;
	private Button btnBack;
	private Button btnNew;

	private TextView tvCurPath;

	private Context ctx;

	private List<String> data;
	private ListAdapter listAdapter;

	private ChooseCompleteListener listener;

	private Stack<String> pathStack = new Stack<String>();

	private int firstIndex = 0;
	private boolean isBack = false;

	private View lastSelectItem; //上一个长按操作的View
	
	//监听操作事件
	private OnPathOperateListener pListener = new OnPathOperateListener() {
		@Override
		public void onPathOperate(int type, final int position,
				final TextView pathName) {
			if (type == OnPathOperateListener.DEL) {
				String path = data.get(position);
				int rs = FileUtils.deleteBlankPath(path);
				if (rs == 0) {
					data.remove(position);
					refleshListView(data, firstIndex);
					UIHelper.ToastMessage(ctx, "删除成功");
				} else if (rs == 1) {
					UIHelper.ToastMessage(ctx, "没有权限");
				} else if (rs == 2) {
					UIHelper.ToastMessage(ctx, "不能删除非空目录");
				}

			} else if (type == OnPathOperateListener.RENAME) {
				final EditText et = new EditText(ctx);
				et.setText(FileUtils.getPathName(data.get(position)));
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				builder.setTitle("重命名");
				builder.setView(et);
				builder.setCancelable(true);
				builder.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String input = et.getText().toString();
						if (StringUtils.isEmpty(input)) {
							UIHelper.ToastMessage(ctx, "输入不能为空");
						} else {
							String newPath = pathStack.peek() + File.separator
									+ input;
							boolean rs = FileUtils.reNamePath(
									data.get(position), newPath);
							if (rs == true) {
								pathName.setText(input);
								data.set(position, newPath);
								UIHelper.ToastMessage(ctx, "重命名成功");
							} else {
								UIHelper.ToastMessage(ctx, "重命名失败");
							}
						}
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
		}
	};

	public interface ChooseCompleteListener {
		void onComplete(String finalPath);
	}

	public PathChooseDialog(Context context, ChooseCompleteListener listener) {
		super(context);
		this.ctx = context;
		this.listener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.file_path_chooser);
		setCanceledOnTouchOutside(true);
		init();
	}

	private void init() {
		lv = (ListView) findViewById(android.R.id.list);
		btnComfirm = (Button) findViewById(R.id.btn_comfirm);
		btnBack = (Button) findViewById(R.id.btn_back);
		btnNew = (Button) findViewById(R.id.btn_new);
		tvCurPath = (TextView) findViewById(R.id.tv_cur_path);

		String rootPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		data = FileUtils.listPath(rootPath);
		tvCurPath.setText(rootPath);

		pathStack.add(rootPath);

		refleshListView(data, 0);
        //单击
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				firstIndex = position;
				String currentPath = data.get(position);
				tvCurPath.setText(currentPath);
				data = FileUtils.listPath(currentPath);
				pathStack.add(currentPath);
				refleshListView(data, pathStack.size() - 1);
			}
		});
		 //长按
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(lastSelectItem!=null&&!lastSelectItem.equals(view)){
					 lastSelectItem.findViewById(R.id.ll_op).setVisibility(View.GONE);
				}
				LinearLayout llOp = (LinearLayout) view
						.findViewById(R.id.ll_op);
				int visible = llOp.getVisibility() == View.GONE ? View.VISIBLE
						: View.GONE;
				llOp.setVisibility(visible);
				lastSelectItem = view;
				return true;
			}
		});
        
		//确认
		btnComfirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onComplete(pathStack.peek());
				dismiss();
			}
		});
         
		//后退
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pathStack.size() >= 2) {
					isBack = true;
					pathStack.pop();
					data = FileUtils.listPath(pathStack.peek());
					tvCurPath.setText(pathStack.peek());
					refleshListView(data, firstIndex);
				}
			}
		});
        
		//新建
		btnNew.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText et = new EditText(ctx);
				et.setText("新建文件夹");
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
				builder.setTitle("新建文件夹");
				builder.setView(et);
				builder.setCancelable(true);
				builder.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String rs = et.getText().toString();
						if (StringUtils.isEmpty(rs)) {
							UIHelper.ToastMessage(ctx, "输入不能为空");
						} else {
							String newPath = pathStack.peek() + File.separator
									+ rs;
							PathStatus status = FileUtils.createPath(newPath);
							switch (status) {
							case SUCCESS:
								data.add(newPath);
								refleshListView(data, data.size()-1);
								UIHelper.ToastMessage(ctx, "创建成功");
								break;
							case ERROR:
								UIHelper.ToastMessage(ctx, "创建失败");
								break;
							case EXITS:
								UIHelper.ToastMessage(ctx, "文件名重复");
								break;
							}
						}
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
			}
		});
	}

	/**
	 * 更新listView视图
	 * 
	 * @param data
	 */
	private void refleshListView(List<String> data, int firstItem) {
		String lost = FileUtils.getSDRoot() + "lost+found";
		data.remove(lost);
		listAdapter = new ListViewPathAdapter(ctx, data,
				R.layout.file_path_listitem, pListener);
		lv.setAdapter(listAdapter);
		lv.setSelection(firstItem);
	}
}
