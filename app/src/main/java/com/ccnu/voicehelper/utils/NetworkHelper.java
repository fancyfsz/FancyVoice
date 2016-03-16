package com.ccnu.voicehelper.utils;

/**
 * Created by mona on 16/3/16.
 */
public class NetworkHelper {

    private static final String loginUrl = "http://10.146.126.8:8080/VoiceStudyManage/user/checkStudentLogin.action";

    private static final String insertUrl = "http://10.146.126.8:8080/VoiceStudyManage/record/insert.action";

    public static String getLoginUrl(){
        return loginUrl;
    }

    public static String getInsertUrl(){
        return insertUrl;
    }
}
