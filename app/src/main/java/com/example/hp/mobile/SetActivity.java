package com.example.hp.mobile;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
        mCalendar.setTimeInMillis(mSharedPreferences.getLong("UndisturbedEndTime",System.currentTimeMillis()+600000000L));
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
                      setUndisturbedTime(1,mStartTime[0],mStartTime[1]);
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
            final EditText mPW_Enter=(EditText)mLL.findViewById(R.id.protected_pw_enter);
            final EditText mPW_confirm=(EditText)mLL.findViewById(R.id.protected_pw_confirm);
            final EditText mPW_Question=(EditText)mLL.findViewById(R.id.protected_pw_question);
            final EditText mPW_Answer=(EditText)mLL.findViewById(R.id.protected_pw_answer);
            new AlertDialog.Builder(SetActivity.this)
                    .setTitle("密码保护设置")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String pw_enter=mPW_Enter.getText().toString();
                            String pw_confirm=mPW_confirm.getText().toString();
                            String pw_question=mPW_Question.getText().toString();
                            String pw_answer=mPW_Answer.getText().toString();
                            if(mPW_Enter.length()!=0&&mPW_confirm.length()!=0&&mPW_Answer.length()!=0&&mPW_Question.length()!=0&&pw_enter.equals(pw_confirm))
                            {
                                mSharedPreferences=getSharedPreferences("SharedPreferences",Context.MODE_PRIVATE);
                                SharedPreferences.Editor mEditor=mSharedPreferences.edit();
                                mEditor.putString("mPW",pw_enter);
                                mEditor.putString("PW_Question",pw_question);
                                mEditor.putString("mPW_Answer",pw_answer);
                                mEditor.commit();
                            }
                            else
                            {
                                Toast.makeText(SetActivity.this,"输入错误",Toast.LENGTH_LONG).show();
                                canclePwProtected();
                            }

                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
               canclePwProtected();
                }
            }).show();

        }
    }
    /**
     * 取消密码保护选中状态
     * */
    private  void canclePwProtected()
    {
        MainActivity.isCheckBoxChecked[5]=false;
        setListAdapter(new SetAdapter(SetActivity.this));
    }
    String hourStr;
    String minuteStr;
    /**
     *时间设置
    * */
    private void setUndisturbedTime(final int start_or_end, final String hour, String minute)
    {

                new TimePickerDialog(SetActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        String mStartTime[]=MainActivity.mStartTime.split(":");
                         String mEndTime[]=MainActivity.mEndTime.split(":");
                          if(start_or_end==1)
                          {
                                hourStr = (hour<10?"0"+hour:""+hour);
                                minuteStr = (minute<10?"0"+minute:""+minute);
                                if(hourStr.equals(mEndTime[0])&&minuteStr.equals(mEndTime[1]))
                                {
                                    Toast.makeText(getApplicationContext(), "不能输入一样的时间!", Toast.LENGTH_SHORT).show();
                                                        setUndisturbedTime(1,mStartTime[0],mStartTime[1]);
                                }else{
                                            MainActivity.mStartTime = hourStr+":"+minuteStr;
                                }
                          }  else
                          {
                               hourStr=(hour<10?"0"+hour:""+hour);
                              minuteStr=(minute<10?")"+minute:""+minute);
                              if(mStartTime[0].equals(hourStr)&&mStartTime[1].equals(minuteStr))
                              {
                                  Toast.makeText(getApplicationContext(),"不能输入一样的时间",Toast.LENGTH_SHORT).show();
                                  setUndisturbedTime(2,mEndTime[0],mEndTime[1]);
                              }
                          }
                        setListAdapter(new SetAdapter(SetActivity.this));
                    }
                },Integer.valueOf(hour),Integer.valueOf(minute),true).show();
    }
    /**
     * 夜间免打扰模式选择框
     * 包含拦截短信，拦截电话，拦截短信和电话以及关闭
     *
     * */
    private  void initUnDisturbedDialog()
    {
        LayoutInflater mLI=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout mLL=(LinearLayout)mLI.inflate(R.layout.set_undisturbed,null);
        final RadioGroup mRG=(RadioGroup)mLL.findViewById(R.id.RadioGroup01);
        final RadioButton mRB1=(RadioButton)mLL.findViewById(R.id.RadioButton01);
        final RadioButton mRB2=(RadioButton)mLL.findViewById(R.id.RadioButton02);
        final RadioButton mRB3=(RadioButton)mLL.findViewById(R.id.RadioButton03);
        final RadioButton mRB4=(RadioButton)mLL.findViewById(R.id.RadioButton04);
        RadioGroup.OnCheckedChangeListener mOCCL=new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==mRB1.getId())
                {
                    MainActivity.mUndisturbedContent="拦截短信";

                }else if(i==mRB2.getId())
                {
                    MainActivity.mUndisturbedContent="拦截电话";
                } else if(i==mRB3.getId())
                {
                    MainActivity.mUndisturbedContent="拦截短信和电话";

                }   else if(i==mRB4.getId())
                {
                    MainActivity.mUndisturbedContent="关闭";

                }
            }
        };
        mRG.setOnCheckedChangeListener(mOCCL);
        new AlertDialog.Builder(this)
                .setTitle("请选择")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setListAdapter(new SetAdapter(SetActivity.this));
                    }
                }).show();
    }
    /**
     * 自定义适配器
     * */
    class SetAdapter extends BaseAdapter
    {
        private ArrayList<Map<String,String>> mList=new ArrayList<Map<String,String>>();
        private Map<String,String> mMap;
        private String[] mTitle={"夜间免打扰模式","开始时间设置","结束时间设置","拒收短信","拒接电话","密码保护",
        "开机启动","自动IP拨号","敏感词设置"};
        private String[] mContent={MainActivity.mUndisturbedContent,MainActivity.mStartTime
        ,MainActivity.mEndTime,"屏蔽一切短信","不接受任何人的骚扰","设置密码更安全","开机启动更方便","IP拨号小助手","自定义的敏感词汇"};

        public  SetAdapter(Context context)
        {
            Log.d("debug","SetAdapter");
            initListData();
        }
        public void initListData()
        {
            for(int i=0;i!=9;i++)
            {
                mMap=new HashMap<String,String>();
                mMap.put("set_title",mTitle[i]);
                mMap.put("set_content",mContent[i]);
                mList.add(mMap);
            }
        }
        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            LayoutInflater mLI=(LayoutInflater)SetActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout mLL=(RelativeLayout)mLI.inflate(R.layout.set_listview,null);
            TextView mTitle=(TextView)mLL.findViewById(R.id.set_title);
            TextView mContent=(TextView)mLL.findViewById(R.id.set_content);
            final CheckBox mCheckBox=(CheckBox)mLL.findViewById(R.id.set_checkbox);
            mTitle.setText(mList.get(i).get("set_title"));
            mContent.setText(mList.get(i).get("set_content"));
            mCheckBox.setFocusable(false);
            mCheckBox.setEnabled(false);
            if(MainActivity.isCheckBoxChecked[i]==true)
            {
                mCheckBox.setChecked(true);
            }
            else
            {
                mCheckBox.setChecked(false);
            }
            if(i==0||i==1||i==2||i==7||i==8)
            {
                mCheckBox.setVisibility(View.GONE);
            }
            return mLL;

        }
    }


}
