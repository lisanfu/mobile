package com.example.hp.mobile.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.CallLog.*;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.example.hp.mobile.internal.ITelephony;
import com.example.hp.mobile.db.DbAdapter;
import com.example.hp.mobile.info.Call_Record_Info;
import com.example.hp.mobile.info.Message_Rubbish_Info;
import com.example.hp.mobile.info.PhoneInfo;
import com.example.hp.mobile.info.SmsInfo;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;


/**
 * Created by hp on 2016/6/24.
 */
public class BroadCastTool extends BroadcastReceiver {
    String SMScontent_01;
    String SMScontent_02;
    static HashMap<String,PhoneInfo> mPhoneMap=new HashMap<String,PhoneInfo>();
    static HashMap<String,SmsInfo> mSMSMap=new HashMap<String,SmsInfo>();
    public static final String SMS_SYSTEM_ACTION="android.provider.Telephony.SMS_RECEIVED";//接收短信的ACTION表示
    public static final String SYSTEM_BOOT_COMPLETED="android.intent.action.BOOT_COMPETED";
    public static final String SMS_RECEIVED_ACTION="com.example.hp.mobile.SMS_RECEIVED_ACTION";//当收到垃圾短信时发出广播的ACTION标识
    public static final String CALL_RECEIVED_ACTION="com.example.hp.mobile.CALL_RECEIVED_ACTION";//当收到垃圾短信是发出广播的ACTION标识
    public static final String AUTO_START_SERVIER="com.example.hp.mobile.AUTO_START_SERVICE";//当收到系统启动的广播
    public static String SMS_PHONENUMBER;//接收短信号码
    public static String SMS_CONTENT;//接收短信内容
    private DbAdapter mDbAdapter;//操作数据库
    private ITelephony iTelephony;//挂断电话的一个对象
    private TelephonyManager telephonyMgr;//电话管理类
    public SharedPreferences mSharedPreferences;//存储基本信息的共享类
    private boolean isReceiveCall;//是否接收电话
    private boolean isAutoStartWithPhone;//是否随系统启动
    private boolean isReceiveSMS;//是否接受短信
    private String mUndisturbedMode;//夜间模式信息
    public BroadCastTool()
    {
        SMS_PHONENUMBER=new String();
        SMS_CONTENT=new String();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        //读取配置文件信息，实时读取
        SharedPreferences setting=context.getSharedPreferences("DEMO",Context.MODE_PRIVATE);
        SMScontent_01=setting.getString("smartE01","");
        SMScontent_02=setting.getString("smartE02","");
        mSharedPreferences=context.getSharedPreferences("SharedPreferences",Context.MODE_PRIVATE);
        isAutoStartWithPhone=mSharedPreferences.getBoolean("isAutoStartWithPhone",false);
        isReceiveCall=mSharedPreferences.getBoolean("isReceiveCall",false);
        isReceiveSMS=mSharedPreferences.getBoolean("isReceiveSMS",false);
        mUndisturbedMode=mSharedPreferences.getString("UndisturbedMode","关闭");
        //监听开机广播，实现开机自动启动软件
        if(intent.getAction().equals(SYSTEM_BOOT_COMPLETED))
        {
            if(isAutoStartWithPhone)
            {
                Intent mIntent=new Intent(AUTO_START_SERVIER);
                context.startService(mIntent);//启动服务
            }
        }
        //监听短信广播，实现拦截垃圾短信
        if(intent.getAction().equals(SMS_SYSTEM_ACTION))
        {
            //1拒绝短信的优先级最高，在前面判断
            if(isReceiveSMS)
            {
                Toast.makeText(context,"设置信息之拒绝短信：勾选",Toast.LENGTH_SHORT).show();
                abortBroadcast();//终止短信广播；当局收短信是勾选状态是，拒收一切短信
            }
            else
            {
                //2.拒收短信为勾选状态时，需要判断夜间模式是否开启
                //如果选择了拦截短信和电话是，需要判断时间段
                //如果在拦截的时间段内则终止广播
                if(("拦截短信".equals(mUndisturbedMode)||"拦截短信和电话".equals(mUndisturbedMode))&&isIncludeTime(context))
                {
                    abortBroadcast();//终止短信广播，当拒收短信状态是勾选状态时
                    //拒收一切短信
                }
                else
                {
                    //2.1 当可以接收短信的时候，首先解析短信的号码和内容
                    //接着判断号码是否微短信黑名单中的号码如果是直接屏蔽
                    //并把短信发到垃圾箱中
                    StringBuilder mMessagePhone=new StringBuilder();
                    StringBuilder mMessageContent=new StringBuilder();
                    Bundle mBundle=intent.getExtras();
                    if(null!=mBundle)
                    {
                        Object[] mObject=(Object[])mBundle.get("pdus");
                        SmsMessage[] mMessage=new SmsMessage[mObject.length];
                        for(int i=0;i<mObject.length;i++)
                        {
                            mMessage[i]=SmsMessage.createFromPdu((byte[])mObject[i]);
                        }
                        for(SmsMessage currentMessage:mMessage)
                        {
                            mMessagePhone.append(currentMessage.getDisplayOriginatingAddress());//读取电话号码
                            mMessageContent.append(currentMessage.getDisplayMessageBody());//读取短信内容
                        }
                        SMS_PHONENUMBER=mMessagePhone.toString();
                        SMS_CONTENT=mMessageContent.toString();
                        SmsManager smsManager= SmsManager.getDefault();
                        if(SMS_CONTENT.equals(SMScontent_01))
                        {
                            Uri uriSMS= Uri.parse("content://sms/inbox");
                            Cursor cursor=context.getContentResolver().query(uriSMS,null,"red=0",null,null);
                            while(cursor.moveToNext())
                            {
                                SmsInfo info=new SmsInfo();
                                String smsno=cursor.getString(cursor.getColumnIndex("_id"));
                                String address=cursor.getString(cursor.getColumnIndex("address"));
                                info.setAddress(address);
                                Calendar cal= Calendar.getInstance();
                                Long date =cursor.getLong(cursor.getColumnIndex("date"));
                                cal.setTimeInMillis(date);
                                SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                                String dateStr=sdf.format(cal.getTime());
                                info.setDate(dateStr);
                                String body=cursor.getString(cursor.getColumnIndex("body"));
                                info.setBody(body);
                                if((!mSMSMap.containsKey(smsno))&&(!body.equals(SMScontent_01))&&(!body.equals(SMScontent_02)));
                                {
                                    info.setState("yes");
                                    mSMSMap.put(smsno,info);
                                }
                            }
                            Set<String> key_1=mSMSMap
                                    .keySet();
                            Object[] keyStr_1=key_1.toArray();
                            for(int i=0;i<keyStr_1.length;i++)
                            {
                                SmsInfo info=mSMSMap.get(keyStr_1[i].toString());
                                if((info.getState().equals("yes")))
                                {
                                    smsManager.sendTextMessage(
                                            SMS_PHONENUMBER,
                                            null,
                                            info.getAddress()+"于"
                                            +info.getDate()
                                            +"发送,内容如下："
                                            +info.getBody(),null,null);
                                    info.setState("no");
                                    mSMSMap.remove(keyStr_1[i].toString());
                                    mSMSMap.put(keyStr_1[i].toString(),info);
                                }
                            }
                        }
                        if(SMS_CONTENT.equals(SMScontent_02))
                        {
                            StringBuilder builder=new StringBuilder();
                            builder.append("type = 3 and new = 1 ");

                               Cursor csor =context.getContentResolver().query(Calls.CONTENT_URI,
                                       null,builder.toString(),null,null);


                            while(csor.moveToNext())
                            {
                                PhoneInfo info=new PhoneInfo();
                                String smsno=csor.getString(csor.getColumnIndex("_id"));
                                String number=csor.getString(csor.getColumnIndex("number"));
                                info.setPhoneNum(number);
                                Calendar cal=Calendar.getInstance();
                                long date=csor.getLong(csor.getColumnIndex("date"));
                                cal.setTimeInMillis(date);
                                SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                                String dateStr=sdf.format(cal.getTime());
                                info.setDate(dateStr);
                                if(!mPhoneMap.containsKey(smsno))
                                {
                                    info.setState("yes");
                                    mPhoneMap.put(smsno,info);
                                }
                            }
                            Set<String> key=mPhoneMap.keySet();
                            Object[] keyStr=key.toArray();
                            for(int i=0;i<keyStr.length;i++)
                            {
                                PhoneInfo info=mPhoneMap.get(keyStr[i].toString());
                                if(info.getState().equals("yes"))
                                {
                                    smsManager.sendTextMessage(SMS_PHONENUMBER,
                                            null,
                                            info.getPhoneNum()+"于"+info.getDate()+"打电话给你",
                                            null,
                                            null);
                                    mPhoneMap.remove(keyStr[i].toString());
                                    info.setState("no");
                                    mPhoneMap.put(keyStr[i].toString(),info);
                                }
                            }
                        }
                        SMS_PHONENUMBER=mMessagePhone.toString();
                        SMS_CONTENT=mMessageContent.toString();
                        //Toast.makeText(BroadCastTool.this,"<------原始号码----->"+SMS_PHONENUMBER+"\n"+"<-------处理之后-------->"+trimSmsNumber("+86",SMS_PHONENUMBER), Toast.LENGTH_SHORT).show();
                        mDbAdapter=new DbAdapter(context);
                        mDbAdapter.open();
                        boolean isContainSensitive=false;
                        //2.2判断给号码是否在短信黑名单中，如果存在则拦截该短信，并保存短信内容等信息到垃圾数据库中
                        Cursor mCursor=mDbAdapter.getPhone(trimSmsNumber("+86",SMS_PHONENUMBER),2);
                        if(mCursor.moveToFirst())
                        {
                            abortBroadCastAndSaveData(context,1);//号码在黑名单中被拦截
                        }else
                        {
                            //2.3 如果不在黑名单中则接下来的工作就是判断短信得内容
                            mSharedPreferences=context.getSharedPreferences("SharedPreferences",Context.MODE_PRIVATE);
                            String xmlInfo=mSharedPreferences.getString("sensitive","");
                            if(xmlInfo.length()!=0)//当敏感词数据库不为空时判断
                            {
                                String[] mArray=xmlInfo.substring(0,xmlInfo.length()).split(",");//
                                for(int i=0;i!=mArray.length;i++)
                                {
                                    if(SMS_CONTENT.contains(mArray[i]))
                                    {
                                        isContainSensitive=true;
                                        abortBroadCastAndSaveData(context,2);//因为短信内容含敏感词被拦截
                                        break;
                                    }
                                }
                            }
                            if(isContainSensitive==false)//判断是否更改情景模式的内容，如果做相应的更改
                            {
                                mSharedPreferences=context.getSharedPreferences("QJMO",Context.MODE_PRIVATE);
                                String mQJ_MS=mSharedPreferences.getString("mQJ_MS","");
                                String mQJ_JY=mSharedPreferences.getString("mQJ_JY","");
                                String mQJ_ZD=mSharedPreferences.getString("mQJ_ZD","");
                                String mQJ_XL=mSharedPreferences.getString("mQJ_XL","");
                                AudioManager audio=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                                if(SMS_CONTENT.equals(mQJ_MS+"+"+mQJ_JY))
                                {
                                    silent(audio);
                                }else if(SMS_CONTENT.equals(mQJ_MS+"+"+mQJ_ZD))
                                {
                                    vibrate(audio);
                                }
                                else if(SMS_CONTENT.equals(mQJ_MS+"+"+mQJ_XL))
                                {
                                    ring(audio);
                                }
                            }
                        }
                        mDbAdapter.close();//关闭数据库
                    }
                }
            }
        }
        //监听来电
        if(intent.getAction().equals("android.intent.action.PHONE_STATE"))
        {
            Log.d("call","get action");
            telephonyMgr=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            MyPhoneStateListener mMPSL=new MyPhoneStateListener(context);
            telephonyMgr.listen(mMPSL,MyPhoneStateListener.LISTEN_CALL_STATE);
            //利用反射获取隐藏的endCall方法
            try{
                Method getITelephonyMethod=TelephonyManager.class.getDeclaredMethod("getITelephony",(Class[])null);
                getITelephonyMethod.setAccessible(true);
                iTelephony=(ITelephony)getITelephonyMethod.invoke(telephonyMgr,(Object[])null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    // 铃声
    protected void ring(AudioManager audio) {
        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_OFF);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,AudioManager.VIBRATE_SETTING_OFF);
    }
    // 静音
    protected void silent(AudioManager audio) {
        audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_OFF);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,AudioManager.VIBRATE_SETTING_OFF);
    }

    // 震动
    protected void vibrate(AudioManager audio) {
        audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,AudioManager.VIBRATE_SETTING_ON);
        audio.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION,AudioManager.VIBRATE_SETTING_ON);
    }
    //电话状态监听类
    class MyPhoneStateListener extends PhoneStateListener
    {
        int i=0;
        Context mContext;
        AudioManager audioManager;
        TelephonyManager mTM;
        public MyPhoneStateListener(Context context)
        {
            mContext=context;
            mTM=(TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        }
        /** 设置铃声为静音并挂断电话*/
        private void audioSilentEndCall()
        {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            try{
                iTelephony.endCall();//挂断电话
            }catch(Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            audioManager=(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
            switch(state)
            {
                case TelephonyManager.CALL_STATE_IDLE://待机状态
                    if(audioManager!=null)
                    {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);//设置为普通模式
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING://来电状态
                //当是来电状态的时候需要判断“设置中”拦截电话的配置信息，如果时勾选的，则直接拒绝掉所有电话
                    if(isReceiveCall==true)
                    {
                        audioSilentEndCall();
                        Toast.makeText(mContext,"设置值拒绝电话：勾选",Toast.LENGTH_LONG).show();

                    }else
                    {
                        if(("拦截电话".equals(mUndisturbedMode)||"拦截短信和电话".equals(mUndisturbedMode))
                                &&isIncludeTime(mContext))
                        {
                            audioSilentEndCall();
                        }else
                        {
                           //判断该号码是否在黑名单中如果是则挂断，并存储电话信息到数据库中
                            mDbAdapter=new DbAdapter(mContext);
                            mDbAdapter.open();//打开数据库
                            if(mDbAdapter.getPhone(trimSmsNumber("+86",incomingNumber),4).moveToFirst())
                            {
                                audioSilentEndCall();;
                                //保存数据库
                                if(!mDbAdapter.getTime(DbAdapter.CALL_RECORD_TABLE_NAME,DbAdapter.CALL_RECORD_TIME,
                                        GetCurrentTime.getFormatDate()).moveToFirst())
                                {
                                    Call_Record_Info mCRI=new Call_Record_Info();
                                    mCRI.setCall_record_time(GetCurrentTime.getFormatDate());
                                    mCRI.setCall_record_phone(trimSmsNumber("+86",incomingNumber));
                                    mDbAdapter.getAdd(mCRI);
                                    Intent mIntent=new Intent();
                                    mIntent.setAction(CALL_RECEIVED_ACTION);
                                    mContext.sendBroadcast(mIntent);
                                }
                                int mCount1=mContext.getContentResolver().query(Calls.CONTENT_URI,
                                        null,
                                        null,
                                        null,
                                        Calls.DEFAULT_SORT_ORDER
                                        ).getCount();
                                boolean mDeleteCallLog = true;
                                while(mDeleteCallLog){
                                    int mCount2 = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DEFAULT_SORT_ORDER).getCount();
                                    if(mCount2 != mCount1){
                                        mDeleteCallLog = false;
                                        int mId = 0;
                                        Cursor mCursor = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, "number"+"=?", new String[]{incomingNumber}, CallLog.Calls.DEFAULT_SORT_ORDER);
                                        if(mCursor.moveToNext()){
                                            mId = mCursor.getInt(mCursor.getColumnIndex("_id"));
                                        }
                                        mId = mCursor.getInt(mCursor.getColumnIndex("_id"));
                                        mContext.getContentResolver().delete(CallLog.Calls.CONTENT_URI, "_id"+"=?", new String[]{String.valueOf(mId)});
                                        mCursor.close();
                                    }
                                }
                            }
                            mDbAdapter.close();//环比数据库
                        }
                    }
                     break;
            }
        }
    }
    /**
     * 判断当前时间是否在夜间免扰模式的时间段内
     *
     * */
    private boolean isIncludeTime(Context context)
    {
        long mStartTime=mSharedPreferences.getLong("UndisturedStartTime",0L);
        long mEndTime=mSharedPreferences.getLong("UndisturedEndTime",0L);
        long mCurrentTime=System.currentTimeMillis();
        if(mCurrentTime>=mStartTime&&mCurrentTime<=mEndTime)
        {
            return true;
        }
        return false;
    }
    /**
     * 去掉国家代号的方法
     * */
    public final static String trimSmsNumber(String prefix,String number)
    {
        String s=number;
        if(prefix.length()>0&&number.startsWith(prefix))
        {
            s=number.substring(prefix.length());
        }
        return s;
    }
    /**
     * 终止广播并存放数据到垃圾箱数据库中
     * */
     private void abortBroadCastAndSaveData(Context context,int i)
     {
         BroadCastTool.this.abortBroadcast();//终止短信广播，当收到垃圾短信之后，存放垃圾信息到数据库中，然后终止广播
         //数据库操作插入该垃圾短信数据导数据库中
         Message_Rubbish_Info mRMI=new Message_Rubbish_Info();
         mRMI.setMessage_rubbish_phone(SMS_PHONENUMBER);//短信号码
         mRMI.setMessage_rubbish_content(SMS_CONTENT);//短信内容
         mRMI.setMessage_rubbish_time(GetCurrentTime.getFormatDate());//收件时间
         mDbAdapter.getAdd(mRMI);//插入数据
         //拦截到垃圾短信或者黑名单短信之后发送广播，刷新短信的拦截记录页面
         Intent mIntent=new Intent();
         mIntent.setAction(SMS_RECEIVED_ACTION);
         context.sendBroadcast(mIntent);
         if (i == 1) {
             Toast.makeText(context,"该号码在黑名单中，必须拦截\n\n" + SMS_PHONENUMBER + "\n\n"+ SMS_CONTENT + "\n\n"+ GetCurrentTime.getFormatDate(),Toast.LENGTH_LONG).show();
         } else {
             Toast.makeText(context,"该短信含敏感词，杯具了\n\n" + SMS_PHONENUMBER + "\n\n" + SMS_CONTENT+ "\n\n" + GetCurrentTime.getFormatDate(),Toast.LENGTH_LONG).show();
         }
     }
}
