package com.example.xuntongwatch.main;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.xuntongwatch.R;
import com.example.xuntongwatch.abstract_.DatabaseUpdataActivity;
import com.example.xuntongwatch.entity.PhoneRecord;
import com.example.xuntongwatch.util.Constant;
import com.example.xuntongwatch.util.PhoneRecordUtil;
import com.example.xuntongwatch.util.Utils;
import com.example.xuntongwatch.view.MyListView;

public class Record_Activity extends DatabaseUpdataActivity implements
		OnClickListener {

	private MyListView lv;
	private ArrayList<PhoneRecord> list;
	private Call_Record_Adapter adapter;
	private View showView, hiddenView, v;
	private long currentTime, todayZeroTime, yesterdayZeroTime;
	private Handler handler;
	private int state;
	private final int LEFT_IN = 1;
	private final int RIGHT_IN = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		PhoneRecordUtil.sTateReplace(this);
		handler = new Handler();
		v = this.findViewById(R.id.call_record_view);
		lv = (MyListView) this.findViewById(R.id.call_record_listview);
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Utils.isFastClick()) {
					return;
				}
				if (showView != null) {
					if (adapter != null) {
						adapter.hiddenOtherLinear();
						adapter.ss = -1;
					}
				}
			}
		});
		initArrayList();
		initCurrentTime();
		super.handleObserverUtil(CallLog.Calls.CONTENT_URI);
		// handleObserverUtil();
	}

	private void initCurrentTime() {
		Calendar c = Calendar.getInstance();
		currentTime = c.getTimeInMillis();
		c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		todayZeroTime = c.getTimeInMillis();
		c.set(Calendar.DAY_OF_MONTH, -1);
		yesterdayZeroTime = c.getTimeInMillis();
	}

	public void initArrayList() {
		if (list == null) {
			// arrayList = PhoneDatabaseUtil.readAllCallRecord(this);
			list = PhoneRecordUtil.findAllPhoneRecordByPhone(this);
			// for (int i = 0; i < list.size(); i++) {
			// if (list.get(i).getType() == 3 && list.get(i).getNew_() == 1) {
			//
			// }
			// }
			adapter = new Call_Record_Adapter();
			lv.setAdapter(adapter);
			TextView view = (TextView) findViewById(R.id.lv_empty);
			lv.setEmptyView(view);
		}
	}

	public void back() {
		finish();
	}

	@Override
	public void onClick(View v) {
		if (Utils.isFastClick()) {
			return;
		}
	}

	public class Call_Record_Adapter extends BaseAdapter {

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

		float x = 0;
		float ux = 0;
		private int ss = -1;

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Record_Activity_Holder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(Record_Activity.this)
						.inflate(R.layout.record_item, null);
				holder = new Record_Activity_Holder();
				holder.main = (LinearLayout) convertView
						.findViewById(R.id.call_record_item_main);
				holder.other = (LinearLayout) convertView
						.findViewById(R.id.call_record_item_other);

				LinearLayout.LayoutParams params_ll = (LayoutParams) holder.other
						.getLayoutParams();
				params_ll.height = Constant.screenWidth / 4;
				holder.other.setLayoutParams(params_ll);

				holder.name = (TextView) convertView
						.findViewById(R.id.call_record_item_name);
				holder.msg = (TextView) convertView
						.findViewById(R.id.call_record_item_msg);
				holder.time = (TextView) convertView
						.findViewById(R.id.call_record_item_time);
				holder.img_style = (ImageView) convertView
						.findViewById(R.id.call_record_item_style);
				holder.img_tou = (ImageView) convertView
						.findViewById(R.id.call_record_item_iv);

				LinearLayout.LayoutParams params = (LayoutParams) holder.img_tou
						.getLayoutParams();
				params.width = Constant.screenWidth / 4;
				params.height = Constant.screenWidth / 4;
				holder.img_tou.setLayoutParams(params);
				holder.call = (RelativeLayout) convertView
						.findViewById(R.id.call_record_item_call_rl);
				holder.delete = (RelativeLayout) convertView
						.findViewById(R.id.call_record_item_delete_rl);
				holder.message = (RelativeLayout) convertView
						.findViewById(R.id.call_record_item_message_rl);
				convertView.setTag(holder);
			} else {
				holder = (Record_Activity_Holder) convertView.getTag();
			}
			holder.img_tou.setImageResource(R.drawable.image_men);

			PhoneRecord record = list.get(position);
			String name = record.getName();
			String number = record.getNumber();
			long date = record.getDate();
			long duration = record.getDuration();
			int type = record.getType();
			int new_ = record.getNew_();
			String photo = record.getPhoto();
			if (!TextUtils.isEmpty(name)) {
				holder.name.setText(name);
			} else {
				holder.name.setText(number);
			}
			if (type == 1) {// 呼入
				holder.img_style.setImageResource(R.drawable.call_in);
			} else if (type == 2) {// 呼出
				holder.img_style.setImageResource(R.drawable.call_out);
			} else if (type == 3) {// 未接
				holder.img_style.setImageResource(R.drawable.call_no_joint);
			}
			if (!TextUtils.isEmpty(photo)) {
				// File file = new File(photo);
				// if (file.exists()) {
				// Bitmap bitmap = BitmapFactory.decodeFile(photo);
				// if (bitmap != null) {
				// holder.img_tou.setImageBitmap(bitmap);
				// }
				// }
				Uri uri = Uri.parse(photo);
				holder.img_tou.setImageURI(uri);
			}
			if (date != 0) {
				holder.time.setText(getDetailTime(date));
			}

			initConvertView(convertView, record);
			initHolderCall(holder.call, record);
			initHolderDelete(holder.delete, record);
			initHolderMessage(holder.message, record);

			return convertView;
		}

		/**
		 * 处理holder.call的点击事件
		 * 
		 * @param call
		 */
		private void initHolderCall(RelativeLayout call,
				final PhoneRecord record) {
			call.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (Utils.isFastClick()) {
						return;
					}
					if (showView == null || hiddenView == null)
						return;
					// 点击事件 在这里执行
					hiddenOtherLinear();
					ss = -1;
					lv.setIsScroll(true);
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							call(record);
						}
					}, 200);
				}
			});
		}

		/**
		 * 处理holder.delete的点击事件
		 * 
		 * @param call
		 */
		private void initHolderDelete(RelativeLayout delete,
				final PhoneRecord record) {
			delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (Utils.isFastClick()) {
						return;
					}
					if (showView == null || hiddenView == null)
						return;
					deleteRecord(record);
					hiddenOtherLinear();
					ss = -1;
					lv.setIsScroll(true);
				}
			});
		}

		/**
		 * 处理holder.message的点击事件
		 * 
		 * @param call
		 */
		private void initHolderMessage(RelativeLayout message,
				final PhoneRecord record) {
			message.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (Utils.isFastClick()) {
						return;
					}
					if (showView == null || hiddenView == null)
						return;
					hiddenOtherLinear();
					ss = -1;
					lv.setIsScroll(true);
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							sendMsg(record);
						}
					}, 200);
				}
			});
		}

		/**
		 * 处理ConvertView的点击事件 与触摸事件
		 * 
		 * @param convertView
		 */
		private void initConvertView(View convertView, final PhoneRecord record) {
			// convertView.setOnTouchListener(new OnTouchListener() {
			//
			// @Override
			// public boolean onTouch(View v, MotionEvent event) {
			// final Record_Activity_Holder holder = (Record_Activity_Holder)
			// v.getTag();
			// if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// x = event.getX();
			// if (showView != null) {
			// hiddenOtherLinear();
			// }
			// } else if (event.getAction() == MotionEvent.ACTION_UP) {
			// if (ss == 0) {
			// ss = -1;
			// lv.setIsScroll(true);
			// return true;
			// }
			// ux = event.getX();
			// if (x - ux > 20 && ss == -1)// 右进
			// {
			// state = RIGHT_IN;
			// holder.main.setVisibility(View.GONE);
			// holder.other.setVisibility(View.VISIBLE);
			// Animation leftOut =
			// AnimationUtils.loadAnimation(Record_Activity.this,R.anim.activity_back_left_to_right);
			// Animation rightIn =
			// AnimationUtils.loadAnimation(Record_Activity.this,R.anim.activity_into_right_to_left);
			// holder.main.setAnimation(leftOut);
			// holder.other.setAnimation(rightIn);
			// showView = holder.other;
			// hiddenView = holder.main;
			// lv.setIsScroll(false);
			// ss = 0;
			// return true;
			// } else if (ux - x > 20 && ss == -1)// 左进
			// {
			// state = LEFT_IN;
			// Animation rightOut =
			// AnimationUtils.loadAnimation(Record_Activity.this,R.anim.activity_back_right_to_left);
			// Animation leftIn =
			// AnimationUtils.loadAnimation(Record_Activity.this,R.anim.activity_into_left_to_right);
			// holder.main.setVisibility(View.GONE);
			// holder.other.setVisibility(View.VISIBLE);
			// holder.main.setAnimation(rightOut);
			// holder.other.setAnimation(leftIn);
			// showView = holder.other;
			// hiddenView = holder.main;
			// lv.setIsScroll(false);
			// ss = 0;
			// return true;
			// }
			//
			// }
			// return false;
			// }
			// });

			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (Utils.isFastClick()) {
						return;
					}
					if (ss == 0) {
						ss = -1;
					} else {
						Intent intent = new Intent(Record_Activity.this,
								RecordDetails_Activity.class);
						String record_phone = record.getNumber();
						String contact_name = record.getName();
						String contact_head = record.getPhoto();
						intent.putExtra("record_phone", record_phone);
						intent.putExtra("contact_name", contact_name);
						intent.putExtra("contact_head", contact_head);
						Record_Activity.this.startActivityForResult(intent,
								RecordDetails_Activity.RESULT_CODE);
						// Intent intent = new
						// Intent(Record_Activity.this,RecordDetails_Activity.class);
						// String record_phone = record.getNumber();
						// String contact_name = record.getName();
						// String contact_head = record.getPhoto();
						// intent.putExtra("record_phone", record_phone);
						// intent.putExtra("contact_name", contact_name);
						// intent.putExtra("contact_head", contact_head);
						// Record_Activity.this.startActivityForResult(intent,RecordDetails_Activity.RESULT_CODE);
						// Intent phoneIntent = new
						// Intent("android.intent.action.CALL",
						// Uri.parse("tel:"+ record.getNumber()));
						// startActivity(phoneIntent);
					}

				}
			});
		}

		/**
		 * 隐藏另一个界面
		 */
		private void hiddenOtherLinear() {
			showView.setVisibility(View.GONE);
			hiddenView.setVisibility(View.VISIBLE);
			if (state == LEFT_IN) {
				Animation leftOut = AnimationUtils.loadAnimation(
						Record_Activity.this,
						R.anim.activity_back_left_to_right);
				Animation rightIn = AnimationUtils.loadAnimation(
						Record_Activity.this,
						R.anim.activity_into_right_to_left);
				showView.setAnimation(leftOut);
				hiddenView.setAnimation(rightIn);
			} else if (state == RIGHT_IN) {
				Animation rightOut = AnimationUtils.loadAnimation(
						Record_Activity.this,
						R.anim.activity_back_right_to_left);
				Animation leftIn = AnimationUtils.loadAnimation(
						Record_Activity.this,
						R.anim.activity_into_left_to_right);
				showView.setAnimation(rightOut);
				hiddenView.setAnimation(leftIn);
			}
			showView = null;
			hiddenView = null;
		}

	}

	public class Record_Activity_Holder {
		TextView name, time, msg;
		ImageView img_tou, img_style;
		LinearLayout main, other;
		RelativeLayout call, delete, message;
	}

	private void deleteRecord(PhoneRecord record) {
		String number = record.getNumber();
		PhoneRecordUtil.deleteRecordByNumber(this, number);
		// recordUtil.deleteRecordByPhone(phone);
		// recordUtil.deleteRecordByRecord_id(record.getRecord_id());
	}

	private void sendMsg(PhoneRecord record) {
		Intent intent = new Intent(this, Message_Chat_Activity.class);
		intent.putExtra("state", Message_Chat_Activity.ONE);
		intent.putExtra("contact_name", record.getName());
		intent.putExtra("message_phone", record.getNumber());
		startActivity(intent);
	}

	private void call(PhoneRecord record) {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RecordDetails_Activity.RESULT_CODE) {
			if (data != null) {
				boolean isDelete = data.getBooleanExtra("isDelete", false);
				if (isDelete) {
					// adapter = new Call_Record_Adapter();
					// lv.setAdapter(adapter);
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void operation() {
		list = PhoneRecordUtil.findAllPhoneRecordByPhone(Record_Activity.this);
	}

	@Override
	public void update() {
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onRestart() {
		list = PhoneRecordUtil.findAllPhoneRecordByPhone(this);
		adapter = new Call_Record_Adapter();
		lv.setAdapter(adapter);
		super.onRestart();
	}

	/**
	 * 将获得的date值进行判断获取想要的文本值
	 * 
	 * @return 转换为目标文本，规则如下：
	 *         <p>
	 *         1.若在今天范围内的时间，就返回：xx分钟前，超过60分钟的，返回xx小时xx分钟前
	 *         </p>
	 *         <p>
	 *         2.若在昨天范围内的事件，就返回：昨天xx:xx，即时间点
	 *         </p>
	 *         <p>
	 *         3.在其他范围内的时间，就返回：xxxx年xx月xx日xx:xx，即时间点
	 *         </p>
	 */
	public static String getDetailTime(long date) {
		StringBuilder strBuilder = new StringBuilder("");
		Calendar canlendar = Calendar.getInstance();
		long currentTime = canlendar.getTimeInMillis();
		canlendar.set(canlendar.get(Calendar.YEAR),
				canlendar.get(Calendar.MONTH),
				canlendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		long todayZeroLongTime = canlendar.getTimeInMillis();
		long yesterdayZeroLongTime = todayZeroLongTime - 24 * 60 * 60 * 1000;
		if (date != 0) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(date);
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH) + 1;
			int day = c.get(Calendar.DAY_OF_MONTH);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			Log.i("bb", year + "年" + month + "月" + day + "天" + hour + "小时"
					+ minute + "分钟");
			if (date > todayZeroLongTime || date == todayZeroLongTime) {// 今天
				long timeDiff = currentTime - date;// 当前时间与参数时间 的差值
				long time = timeDiff / (long) (1000 * 60);// 换算为分钟值

				if (time == 0) {// 若在一分钟之内
					strBuilder.append("刚刚");
				} else if (time < 60) {
					strBuilder.append(time).append("分钟前");
				} else {// 一小时之外
					strBuilder.append(time / 60).append("小时").append(time % 60)
							.append("分钟前");
				}
			} else if (date > yesterdayZeroLongTime
					|| date == yesterdayZeroLongTime) {// 昨天
				strBuilder.append("昨天").append(getDoubleInt(hour)).append(":")
						.append(getDoubleInt(minute));
			} else {// 其他时间
				strBuilder.append(getDoubleInt(month)).append("月")
						.append(getDoubleInt(day)).append("日");
			}
		}
		return strBuilder.toString();
	}

	public static String getDoubleInt(int i) {
		StringBuffer sb = new StringBuffer("");
		if (i < 10) {
			sb.append("0");
		}
		sb.append(i);
		return sb.toString();
	}

}
