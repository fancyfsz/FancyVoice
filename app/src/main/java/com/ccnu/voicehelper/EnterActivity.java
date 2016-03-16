package com.ccnu.voicehelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ccnu.voicehelper.utils.ActivityCollector;
import com.ccnu.voicehelper.utils.BaseActivity;
import com.ccnu.voicehelper.utils.NetworkHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;


public class EnterActivity extends BaseActivity implements View.OnClickListener{

    private EditText stuId = null;
    private EditText pwd = null;
    private Button login_btn = null;
    private String stuNum = null;//学号
    private String password = null;//密码
    private String jsonString = null;
    User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.login);
        initLayout();

    }

    private void initLayout(){

        stuId = (EditText)findViewById(R.id.stuId);
        pwd = (EditText)findViewById(R.id.pwd);
        findViewById(R.id.login_btn).setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id){
            case R.id.login_btn:
            {
                stuNum = stuId.getText().toString();
                password = pwd.getText().toString();

                RequestParams params = new RequestParams();
                NetworkHelper networkHelper = new NetworkHelper();
                params.put("userNo", stuNum);
                params.put("userPsd",password);

                AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                asyncHttpClient.post(networkHelper.getLoginUrl(), params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        jsonString = new String(bytes);

                        if(parseJson()){
                            Intent intent = new Intent(EnterActivity.this,MainActivity.class);
                            intent.putExtra("stuNo",user.getStuNo());
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(EnterActivity.this,"用户名或密码错误",Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        Toast.makeText(EnterActivity.this,"网络连接失败!",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    private Boolean parseJson(){

        Boolean bRet = false;
        try{
            JSONObject  jsonObject = new JSONObject(jsonString);
            String res = jsonString.toString();
            if( !res.contains("errorCode")){
                JSONObject object = jsonObject.getJSONObject("data");
                user = new User();
                user.setStuNo(object.getString("userNo"));
                bRet = true;
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return bRet;
    }
}
