package com.ccnu.voicehelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ccnu.voicehelper.utils.ActivityCollector;
import com.ccnu.voicehelper.utils.BaseActivity;


public class EnterActivity extends BaseActivity implements View.OnClickListener{

    private EditText stuId = null;
    private EditText pwd = null;
    private Button login_btn = null;

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
                if(checkAccount()) {
                    Intent intent = new Intent(EnterActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(EnterActivity.this,"学号或密码不正确",Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                break;
        }
    }

    private boolean checkAccount(){
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
