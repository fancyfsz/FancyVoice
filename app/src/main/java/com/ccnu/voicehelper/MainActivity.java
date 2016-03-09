package com.ccnu.voicehelper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.UnderstanderResult;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageButton help_btn = null;
    private ImageButton voice_btn = null;
    private ImageButton setting_btn = null;
    private List<Msg> msgList = new ArrayList<Msg>();
    private SlidingMenu slidingMenu;
    /*
      *  module one:a speech recognizer(ASR)
      *  module two: natural language understanding
      *  (recognition into a concept structure)
    */
    //语义理解对象(从语音到语义)
    private SpeechUnderstander speechUnderstander;
    private Toast toast;
    //理解的结果,这里是用xml表示的
    private String xmlString;
    //sendMessage
    private String rawText;
    //receiveMessage
    private String content;
    private SharedPreferences understanderSpf;

    /*
    *   module three: text-to-speech
    *   (TTS语音合成)
    * */
    //语音合成对象
    private SpeechSynthesizer tts;
    //默认发音人小燕
    private String voicer = "xiaoyan";
    private SharedPreferences ttsSpf;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //创建语音配置对象,只有初始化后才可以使用MSC的各项服务
        SpeechUtility.createUtility(MainActivity.this, "appid=" + getString(R.string.app_id));

        super.onCreate(savedInstanceState);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //加载布局
        setContentView(R.layout.activity_main);
        initSlidingMenu();
        initLayout();
        //初始化对象
        speechUnderstander = SpeechUnderstander.createUnderstander(MainActivity.this, speechUdrInitListener);
        tts = SpeechSynthesizer.createSynthesizer(MainActivity.this, ttsInitListener);
        toast = toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT);

    }

    //初始化Layout
    public void initLayout(){

        findViewById(R.id.voice_btn).setOnClickListener(MainActivity.this);
        findViewById(R.id.help_btn).setOnClickListener(MainActivity.this);
        findViewById(R.id.setting_btn).setOnClickListener(MainActivity.this);
        understanderSpf = getSharedPreferences(UnderstanderSettings.PREFER_NAME, Activity.MODE_PRIVATE);
        ttsSpf = getSharedPreferences(TtsSettings.PREFER_NAME, Activity.MODE_PRIVATE);
    }

    //初始化侧边栏(点击设置按钮出现的滑动侧边栏效果)
    private void initSlidingMenu() {

        slidingMenu = new SlidingMenu(this);
        //设置菜单模式
        slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
        slidingMenu.setMenu(R.layout.help_fragment);
        slidingMenu.setSecondaryMenu(R.layout.setting_fragment);
        slidingMenu.attachToActivity(this,SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setShadowWidth(10);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
    }



    @Override
    public void onClick(View v) {

        int ret = 0;//函数调用的返回值
        switch (v.getId()){
            //开始语音理解
            case R.id.voice_btn:
            {
                Toast.makeText(MainActivity.this,"开始说话",Toast.LENGTH_LONG).show();
                //设置相关参数
                setVoiceParams();
                if(speechUnderstander.isUnderstanding()){
                    speechUnderstander.stopUnderstanding();
                    showTip("停止录音");
                }
                else {
                    ret = speechUnderstander.startUnderstanding(speechUnderstanderListener);//设置对语音的监听
                    if(ret != 0){
                        showTip("语义理解失败,错误码:"+ret);
                    }
                    else {
                        showTip(getString(R.string.text_begin));
                    }
                }
                break;
            }
            case R.id.setting_btn:
            {
                //todo设置相关,如发音人和发音情感
                //账号设置,登出
//                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
//                startActivity(intent);
                if(!slidingMenu.isSecondaryMenuShowing()){
                    slidingMenu.showSecondaryMenu();
                }
                else {
                    slidingMenu.toggle();
                }

                break;
            }
            case R.id.help_btn:
            {
                //todo帮助,如何使用语音助手进行学习
                slidingMenu.toggle();
                break;
            }
            default:
                break;
        }
    }

    //设置语音相关参数
    public void setVoiceParams(){
        //对语言的设置
        String lang = understanderSpf.getString("understander_language_preference","mandarin");
        if(lang.equals("en_us")){
            speechUnderstander.setParameter(SpeechConstant.LANGUAGE,"en_us");
        }
        else {
            speechUnderstander.setParameter(SpeechConstant.LANGUAGE,"zh_cn");
            speechUnderstander.setParameter(SpeechConstant.ACCENT,lang);
        }
        //设置语音前端点
        speechUnderstander.setParameter(SpeechConstant.VAD_BOS, understanderSpf.getString("understander_vadbos_preference", "4000"));
        //设置语音后端点
        speechUnderstander.setParameter(SpeechConstant.VAD_EOS, understanderSpf.getString("understander_vadbos_preference", "1000"));
        //设置标点符号
        speechUnderstander.setParameter(SpeechConstant.ASR_PTT, understanderSpf.getString("understander_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        speechUnderstander.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        speechUnderstander.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/sud.wav");
    }
    //初始化从语音到语义的监听器
    private InitListener speechUdrInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG,"speechUdrInitListener init() code ="+code);
            if(code != ErrorCode.SUCCESS){
                showTip("初始化失败,错误码:"+code);
            }
        }
    };

    //设置合成相关参数
    private void setTtsParams(){
        tts.setParameter(SpeechConstant.PARAMS,null);
        //设置合成发音人
        tts.setParameter(SpeechConstant.VOICE_NAME,voicer);
        //设置合成语速
        tts.setParameter(SpeechConstant.SPEED,ttsSpf.getString("speed_preference","50"));
        //设置合成音量
        tts.setParameter(SpeechConstant.VOLUME,ttsSpf.getString("volumn_preference","50"));
        //设置合成音调
        tts.setParameter(SpeechConstant.PITCH,ttsSpf.getString("pitch_preference","50"));
        //设置播放器音频流类型
        tts.setParameter(SpeechConstant.STREAM_TYPE,ttsSpf.getString("stream_preference","3"));
        //设置播放合成音频打断音乐播放,默认为true
        tts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS,"true");
        tts.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        //设置音频保存路径
        tts.setParameter(SpeechConstant.TTS_AUDIO_PATH,Environment.getExternalStorageDirectory()+"/msc/tts.wav");

    }
    //语义理解回调
    private SpeechUnderstanderListener speechUnderstanderListener = new SpeechUnderstanderListener() {

        @Override
        public void onResult(final UnderstanderResult result) {
            if (null != result) {
                Log.d(TAG, result.getResultString());

                //得到xml解析结果
                xmlString = result.getResultString();
                Log.i(TAG, xmlString);
                parseXmlWithPull(xmlString);
                //将文本以聊天界面的样式进行显示
                showChatting();

            } else {
                showTip("识别结果不正确。");
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, data.length+"");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };


    private InitListener ttsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "ttsInitListener init() code = " + code);
            if(code != ErrorCode.SUCCESS){
                showTip("初始化失败,错误码:"+code);
            }
            else{
                //初始化成功可以调用startSpeaking方法了
            }
        }
    };

    //合成回调监听
    private SynthesizerListener ttsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    //使用Pull方式解析得到的xml
    private void parseXmlWithPull(String xmlString){
        //将<rawtext>标签作为发出的消息
        //将<content>标签作为语音助手的应答消息,再利用语音合成技术进行发音
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlString));
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT){
                String nodeName = xmlPullParser.getName();
                switch (eventType){
                    //开始解析某一个节点
                    case XmlPullParser.START_TAG: {
                        if ("rawtext".equals(nodeName)) {
                            rawText = xmlPullParser.nextText();
                        }
                        else if("content".equals(nodeName)) {
                            content = xmlPullParser.nextText();
                        }
                        break;
                    }
                    default:
                        break;

                }
                eventType = xmlPullParser.next();
            }
        }
        catch (Exception e){
           e.printStackTrace();
        }
        setTtsParams();
        tts.startSpeaking(content,ttsListener);
    }

    public void showChatting(){

        Msg sendMsg = new Msg(rawText,Msg.TYPE_SENT);
        msgList.add(sendMsg);
        Msg receiveMsg = new Msg(content,Msg.TYPE_RECEIVED);
        msgList.add(receiveMsg);
        MsgAdapter msgAdapter = new MsgAdapter(MainActivity.this,R.layout.msg_item,msgList);
        ListView msgListView = (ListView)findViewById(R.id.msg_list_view);
        msgListView.setAdapter(msgAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechUnderstander.cancel();
        speechUnderstander.destroy();
        tts.stopSpeaking();
        tts.destroy();
    }

    private void showTip(final String str) {
        toast.setText(str);
        toast.show();
    }
}
