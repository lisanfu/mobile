package com.example.hp.mobile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.mobile.handler.PhoneNumberXml;
import com.example.hp.mobile.tool.BroadCastTool;
import com.example.hp.mobile.tool.ServiceTool;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.crypto.Mac;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.StringReader;
import java.net.HttpURLConnection;
public class MainActivity extends Activity implements View.OnClickListener {
    /**
     * 成员变量
     */
    private final static String TAG = MainActivity.class.getSimpleName();
    public static boolean[] isCheckBoxChecked = new boolean[9];
    public static boolean[] isRadioButtonChecked = new boolean[4];
    public static String mUndisturbedContent = "关闭";
    public static String mStartTime = "23:00";
    public static String mEndTime = "07:00";
    private ImageButton mMenu_Message, mMenu_Call, mMenu_Phone, mMenu_State, mMenu_Smart, mMenu_Set;
    private SlidingDrawer mSlidingDrawer;
    private ImageView mImageView;
    private Intent intent;
    private Intent mServiceIntent;
    public static final String MESSAGE_RUBBISH_TABLE_NAME = "message_rubbish_table";
    EditText mInputPhoneNumber;
    String result;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    BroadCastTool mBroadCastTool;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        mBroadCastTool=new BroadCastTool();
        IntentFilter filter=new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction("android.intent.action.BOOT_COMPETED");
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("com.example.hp.mobile.AUTO_START");
        registerReceiver(mBroadCastTool,filter);
        mServiceIntent = new Intent(this, ServiceTool.class);
        startService(mServiceIntent);
        confirmPw();
    }
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("提示")
                    .setMessage("你确定要退出")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent mIntent = new Intent(MainActivity.this,ServiceTool.class);
                                    startService(mIntent);
                                    stopService(mIntent);
                                    MainActivity.this.finish();
                                }
                            }).setNegativeButton("取消", null).show();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestory");
        startService(mServiceIntent);
    }
    private void confirmPw() {
        mSharedPreferences = this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        boolean isPasswordProtected = mSharedPreferences.getBoolean("isPasswordProtected", false);
        if (isPasswordProtected) {
            LayoutInflater mLI = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final RelativeLayout mRL = (RelativeLayout) mLI.inflate(R.layout.protected_password_confirm, null);
            final EditText mEnterPw = (EditText) mRL.findViewById(R.id.protected_enter_pw);
            final TextView mForgetPw = (TextView) mRL.findViewById(R.id.protected_forget_pw);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("密码保护")
                    .setView(mRL)
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mSharedPreferences = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
                                    String mPw = mSharedPreferences.getString("mPW", "");
                                    if (!mPw.equals(mEnterPw.getText().toString())) {
                                        Toast.makeText(MainActivity.this, "密码错误", Toast.LENGTH_LONG).show();
                                        confirmPw();
                                    } else {
                                        initContentView();
                                    }
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MainActivity.this.finish();
                                }
                            }).show();
            mForgetPw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getBackPwByView();
                }
            });
        } else {
            initContentView();
        }
    }
    /** 用保密问题取回密码*/
    private void getBackPwByView() {
        LayoutInflater mLI=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout mLL=(LinearLayout)mLI.inflate(R.layout.getback_pw_by_question,null);
        final EditText mQuestion=(EditText)mLL.findViewById(R.id.getback_pw_question);
        final EditText mAnswer=(EditText)mLL.findViewById(R.id.getback_pw_answer);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("取回密码")
                .setView(mLL)
                .setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mSharedPreferences=getSharedPreferences("SharedPreferences",Context.MODE_PRIVATE);
                        String mQuestionStr=mSharedPreferences.getString("mPW_Question","");
                        String mAnswerStr=mSharedPreferences.getString("mPW_Answer","");
                        if(mQuestionStr.equals(mQuestion.getText().toString())&&mAnswerStr.equals(mAnswer.getText().toString()))
                        {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("恭喜你，你的密码是"+mSharedPreferences.getString("mPW","")+"记住了！")
                                    .show();
                        }
                        else
                        {
                            new AlertDialog.Builder(MainActivity.this).setMessage("哦！悲剧了").show();
                        }
                    }
                })
                .setNegativeButton("取消",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).show();
    }
    /** 当密码正确或未设置密码保护石的初始化界面*/
    private void initContentView() {
        startService(mServiceIntent);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        initSlidingListener();
        stopService(mServiceIntent);
    }
    /** SlidingDrawerde开关事件监听*/
    private void initSlidingListener() {
        mSlidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener()
        {
            @Override
            public void onDrawerOpened() {
                mImageView.setImageResource(R.drawable.spliding_close);
            }
        });
        mSlidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                mImageView.setImageResource(R.drawable.spliding_open);
            }
        });
    }
    /** 注册监听*/
    private void initListener() {
       mMenu_Call.setOnClickListener(this);
        mMenu_Message.setOnClickListener(this);
        mMenu_Phone.setOnClickListener(this);
        mMenu_Set.setOnClickListener(this);
        mMenu_Smart.setOnClickListener(this);
        mMenu_State.setOnClickListener(this);
    }
    /** 初始化基本组件*/
    private void initView() {
        mMenu_Message = (ImageButton) findViewById(R.id.ImageView01);
        mMenu_Call = (ImageButton) findViewById(R.id.ImageView02);
        mMenu_Phone = (ImageButton) findViewById(R.id.ImageView03);
        mMenu_State = (ImageButton) findViewById(R.id.ImageView04);
        mMenu_Smart = (ImageButton) findViewById(R.id.ImageView05);
        mMenu_Set = (ImageButton) findViewById(R.id.ImageView06);
        mSlidingDrawer = (SlidingDrawer) findViewById(R.id.menu_sliding);
        mImageView = (ImageView) findViewById(R.id.sliding_image);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.ImageView01:
                intent=new Intent(MainActivity.this,MessageActivity.class);
               MainActivity.this.startActivity(intent);
                break;
            case R.id.ImageView02:
                intent =new Intent(MainActivity.this,CallActivity.class);
                MainActivity.this.startActivity(intent);
                break;
            case R.id.ImageView03:
                phoneArea();
                break;
            case R.id.ImageView04:
                initChangeAudioDialog();
                break;
            case R.id.ImageView05:
                smartSet();
                break;
            case R.id.ImageView06:
                intent=new Intent(MainActivity.this,SetActivity.class);
                MainActivity.this.startActivity(intent);
                break;
        }
    }
    /** 更改情景模式的弹出框*/
    private void initChangeAudioDialog() {
        LinearLayout mLL=(LinearLayout)getLayoutInflater().inflate(R.layout.main_audio_change,null);
        final EditText mQJ_MS=(EditText)mLL.findViewById(R.id.edit_qingjing_ms);
        final EditText mQJ_JY=(EditText)mLL.findViewById(R.id.edit_qingjing_jy);
        final EditText mQJ_ZD=(EditText)mLL.findViewById(R.id.edit_qingjing_zd);
        final EditText mQJ_XL=(EditText)mLL.findViewById(R.id.edit_qingjing_xl);
        mSharedPreferences=getSharedPreferences("QJMO",MODE_PRIVATE);
        mQJ_MS.setText(mSharedPreferences.getString("mQJ_MS",""));
        mQJ_JY.setText(mSharedPreferences.getString("mQJ_JY",""));
        mQJ_ZD.setText(mSharedPreferences.getString("mQJ_ZD",""));
        mQJ_XL.setText(mSharedPreferences.getString("mQJ_xl",""));
        new AlertDialog.Builder(this)
                .setTitle("情景设置")
                .setIcon(getResources().getDrawable(R.drawable.menu_state_press))
                .setMessage("发送格式：情景指令+相应指令，如情景指令为qjzl，静音指令为jy，则发送格式为qjzl+jy")
                .setView(mLL)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String mMS=mQJ_MS.getText().toString();
                                String mJY=mQJ_JY.getText().toString();
                                String mZD=mQJ_ZD.getText().toString();
                                String mXL=mQJ_XL.getText().toString();
                                if(mMS.length()==0||mJY.length()==0||mZD.length()==0||mXL.length()==0)
                                {
                                    Toast.makeText(getApplicationContext(),"输入有误，请注意任何一项不能为空",Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    mSharedPreferences =getSharedPreferences("QJMO",MODE_PRIVATE);
                                    mEditor=mSharedPreferences.edit();
                                    mEditor.putString("mQJ_MS",mMS);
                                    mEditor.putString("mQJ_JY",mJY);
                                    mEditor.putString("mQJ_ZD",mZD);
                                    mEditor.putString("mQJ_XL",mXL);
                                    mEditor.commit();
                                }
                            }
                        }).setNegativeButton("取消",null).show();
    }
    /**号码归属地查询*/
    private void phoneArea() {
        LinearLayout mInputNumber=(LinearLayout)getLayoutInflater().inflate(R.layout.searchphone,null);
        mInputPhoneNumber=(EditText)mInputNumber.findViewById(R.id.phonenumber);
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("号码查询")
                .setIcon(getResources().getDrawable(R.drawable.menu_phone_press))
                .setView(mInputNumber)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                HttpResponse mHttpResponse=null;
                                try{
                                    HttpGet mHttpGet=new HttpGet("\"http://www.webxml.com.cn/WebServices/MobileCodeWS.asmx/getMobileCodeInfo?mobileCode=\"+mInputPhoneNumber.getText().toString()+\"&userID=\"");
                                    mHttpResponse=new DefaultHttpClient().execute(mHttpGet);
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setMessage("StatusCode is"+mHttpResponse.getStatusLine().getStatusCode()).show();
                                    if(mHttpResponse.getStatusLine().getStatusCode()==200)
                                    {
                                        result= EntityUtils.toString(mHttpResponse.getEntity());
                                        Log.d("phonenumber","result is"+result);
                                        SAXParserFactory mSAXParserFactory=SAXParserFactory.newInstance();
                                        SAXParser mSAXParser=mSAXParserFactory.newSAXParser();
                                        XMLReader mXMLReader=mSAXParser.getXMLReader();
                                        PhoneNumberXml mPhoneNumberXml=new PhoneNumberXml();
                                        mXMLReader.setContentHandler(mPhoneNumberXml);
                                        StringReader mStringReader=new StringReader(result);
                                        InputSource mInputSource=new InputSource(mStringReader);
                                        mXMLReader.parse(mInputSource);
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setMessage(PhoneNumberXml.mPhoneNumberStr)
                                                .create().show();
                                    }
                                    else
                                    {
                                        Toast.makeText(MainActivity.this,"连接网络有误",Toast.LENGTH_LONG).show();
                                    }
                                }catch(Exception e){
                                    e.printStackTrace();
                                }finally{
                                }
                            }
                        }).setNegativeButton("取消",null).show();
    }
    private CheckBox mCheckbox01;
    private CheckBox mCheckbox02;
    private CheckBox mCheckbox03;
    private EditText mEditText01;
    private EditText mEditText02;
    /** 智能收发设置*/
    private void smartSet() {
        RelativeLayout intellectLayout=(RelativeLayout)getLayoutInflater().inflate(R.layout.intellect,null);
        final SharedPreferences settings=getSharedPreferences("DEMO",MODE_PRIVATE);
        //或的设置界面的组件
        mCheckbox01=(CheckBox)intellectLayout.findViewById(R.id.intellect_checbox_1);
        mCheckbox02=(CheckBox)intellectLayout.findViewById(R.id.intellect_checbox_2);
        mCheckbox03=(CheckBox)intellectLayout.findViewById(R.id.intellect_checbox_3);
        mEditText01=(EditText)intellectLayout.findViewById(R.id.intellect_edittext01);
        mEditText02=(EditText)intellectLayout.findViewById(R.id.intellect_edittext02);
        //加载页面的原始设置
        if(settings.getBoolean("bool01",false))
        {
            mCheckbox01.setChecked(true);
            mEditText01.setVisibility(View.VISIBLE);
            mEditText01.setText(settings.getString("smartE01",""));

        }
        if (settings.getBoolean("bool02", false)) {
            mCheckbox02.setChecked(true);
            mEditText02.setVisibility(View.VISIBLE);
            mEditText02.setText(settings.getString("smartE02", ""));
        }
        if (settings.getBoolean("bool03", false)) {
            mCheckbox03.setChecked(true);
        }
        // 给checkbox加监听
        mCheckbox01.setOnCheckedChangeListener(new Checklistener());
        mCheckbox02.setOnCheckedChangeListener(new Checklistener());
        mCheckbox03.setOnCheckedChangeListener(new Checklistener());
        new AlertDialog.Builder(this)
                .setTitle("只能收发设置")
                .setIcon(getResources().getDrawable(R.drawable.menu_smart_press))
                .setView(intellectLayout)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences.Editor sedit=settings.edit();
                                if(mCheckbox01.isChecked())
                                {
                                    boolean bool01=true;
                                    sedit.putBoolean("bool01",bool01).commit();
                                    sedit.putString("smartE01",mEditText01.getText().toString()).commit();
                                }else
                                {
                                    sedit.remove("bool01").commit();
                                    sedit.remove("smartE01").commit();
                                }
                                if(mCheckbox02.isChecked())
                                {
                                    boolean bool02=true;
                                    sedit.putBoolean("bool02",bool02).commit();
                                    sedit.putString("smartE02",
                                            mEditText02.getText().toString()).commit();
                                }
                                else
                                {
                                    sedit.remove("bool02").commit();
                                    sedit.remove("smartE02").commit();
                                }
                                if(mCheckbox03.isChecked())
                                {
                                    boolean bool03=true;
                                    sedit.putBoolean("bool03",bool03).commit();
                                }
                                else
                                {
                                    sedit.remove("bool03").commit();
                                }
                            }
                        }).setNegativeButton("取消",null).show();
    }
    class Checklistener implements CompoundButton.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(!mCheckbox01.isChecked())
            {
                mEditText01.setVisibility(View.GONE);
            }
            else
            {
                mEditText01.setVisibility(View.VISIBLE);
            }
            if(!mCheckbox02.isChecked())
            {
                mEditText02.setVisibility(View.GONE);
            }
            else
            {
                mEditText02.setVisibility(View.VISIBLE);
            }
            if(!mCheckbox03.isChecked())
            {
                Toast.makeText(MainActivity.this,"关闭电话留言",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(MainActivity.this,"开启电话留言",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
