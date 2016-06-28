package com.example.hp.mobile;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Calendar;

/**
 * Created by hp on 2016/6/27.
 */
public class SetActivity extends ListActivity {
        private Intent mIntent;
        private SharedPreferences mSharedPreferences;
        private SharedPreferences.Editor mEditor;
        Calendar mCalendar;
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Log.d("debug","onCreate");
        mCalendar=Calendar.getInstance();
        /*读取用Sharedpreferences保存的数据的配置文件*/
        mSharedPreferences=this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        MainActivity.mUndisturbedContent=mSharedPreferences.getString("UndisturbedMode",MainActivity.mUndisturbedContent);
        mCalendar.setTimeInMillis(mSharedPreferences.getLong("UndisturbedStartTime",System.currentTimeMillis()));
        int hourS=mCalendar.get(Calendar.HOUR_OF_DAY);
        int minuteS=mCalendar.get(Calendar.MINUTE);
        MainActivity.mStartTime=(hourS<10?"0"+hourS:"hourS")+":"+(minuteS<10?"0"+minuteS:minuteS);
        mCalendar.setTimeInMillis(mSharedPreferences.getLong("UndisturbedEndTime"，System.currentTimeMillis()+600000000L));
        int hourE=mCalendar.get(Calendar.HOUR_OF_DAY);
        int minuteE=mCalendar.get(Calendar.MINUTE);
        MainActivity.mEndTime=(hourE<10?"0"+hourE:hourE)+":"+(minuteE<10?"0"+minuteE:minuteE);
        MainActivity.isCheckBoxChecked[3]=mSharedPreferences.getBoolean("isReceiverSMS",false);
        MainActivity.isCheckBoxChecked[4]=mSharedPreferences.getBoolean("isReceiverCall",false);
        MainActivity.isCheckBoxChecked[5]=mSharedPreferences.getBoolean("isPasswordProtected",false);
        MainActivity.isCheckBoxChecked[6]=mSharedPreferences.getBoolean("isAutoStartWithPhone",false);
        /**
         * 读取结果后开始适配列表数据
        * */
        setListAdapter(new SetAdapter(SetActivity.this));
        initListViewListener();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 1.Context.MODE_PRIVATE: 为默认操作模式，代表文件是私有的，只能被应用本身访问，在该模式下写入的内容会覆盖源文件得内容
         * 如果想把新的内容追加到源文件可以使用Context.MODE_APPEND
         *  2.Context.MODE_APPEND:模式会检查文件是否存在，存在就往文件立追加内容，否则就创建新文件
         *  3.Context.MODE_WORLD_READABLE和Context.MODE_WORLD_WRITEABLE用来控制其他应用是否有权限读取该文件
         *  3.1MODE_WORLD_READABLE：表示当前文件被其他应用读取
         *  3.2MODE_WORLD_WRITEABLE:表示当前文件可以被其他应用写入
         *
         *
         * */
        mSharedPreferences = this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        mEditor.putString("UndisturbedMode", MainActivity.mUndisturbedContent);
        int hourStart = Integer.valueOf(MainActivity.mStartTime.split(":")[0]);
        int minuteStart = Integer.valueOf(MainActivity.mStartTime.split(":")[1]);
        int hourEnd = Integer.valueOf(MainActivity.mEndTime.split(":")[0]);
        int minuteEnd = Integer.valueOf(MainActivity.mEndTime.split(":")[1]);
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        mCalendar.set(mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH),
                hourStart,
                hourStart);
        mEditor.putLong("UndisturbedStartTime", mCalendar.getTimeInMillis());
        Log.d("yejian", "UndistrubedStartTime:" + mCalendar.getTimeInMillis());
        if ((hourStart == hourEnd && minuteStart > minuteEnd) || hourStart > hourEnd) {
            mCalendar.set(mCalendar.get(Calendar.YEAR)
                    , mCalendar.get(Calendar.MONTH)
                    , mCalendar.get(Calendar.DAY_OF_MONTH)
                    , hourEnd, minuteEnd);
            Log.d("yejian", "1------" + mCalendar.getTimeInMillis());

        } else {
            mCalendar.set(mCalendar.get(Calendar.YEAR),
                    mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH),
                    hourEnd,
                    minuteEnd);
            Log.d("yejian", "2-----------" + mCalendar.getTimeInMillis());
        }
        mEditor.putLong("UndisturbedEndTime", mCalendar.getTimeInMillis());
        Log.d("yejian", "UndisturbedEndTime" + mCalendar.getTimeInMillis());
        mEditor.putBoolean("isReceiveSMS", MainActivity.isCheckBoxChecked[3]);
        mEditor.putBoolean("isReceiveCall", MainActivity.isCheckBoxChecked[4]);
        mEditor.putBoolean("isPasswordProtected", MainActivity.isCheckBoxChecked[5]);
        mEditor.putBoolean("isAutoStartWithPhone", MainActivity.isCheckBoxChecked[6]);
        mEditor.commit();
    }
        /**
         * ListView的点击事件监听
         * */
    private void initListViewListener()
    {
      getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
              CheckBox mCheckBox=(CheckBox)view.findViewById(R.id.set_checkbox);
              if(!mCheckBox.isChecked())
              {
                  MainActivity.isCheckBoxChecked[i]=true;
              }
              else
              {
                  MainActivity.isCheckBoxChecked[i]=false;
                  mCheckBox.setChecked(true);
              }
              switch(i)
              {
                  case 0:
                      initUnDisturbedDialog();
                      break;
                  case 1:
                      String mStartTime[]=MainActivity.mStartTime.split(":");
                      setUnDisturbedTime(1,mStartTime[0],mStartTime[1]);
                      break;
                  case 2:
                      String mEndTime[]=MainActivity.mEndTime.split(":");
                      setUndisturbedTime(2,mEndTime[0],mEndTime[1]);
                      break;
                  case 5:
                      protectDialog(mCheckBox);
                      break;
                  case 7:
                      break;
                  case 8:
                      mIntent=new Intent(SetActivity.this,SensitiveActivity.class);
                      break;
              }
          }
      });
    }
    /**
     * 密码保护
     * */
    private void protectDialog(CheckBox checkbox)
    {
        if(checkbox.isChecked())
        {
            LayoutInflater mLI=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final LinearLayout mLL=(LinearLayout)mLI.inflate(R.layout.set_protected,null);
            final EditText mPW_Enter=(EditText)mLL.findViewById(R.)
        }
    }































    }
}
