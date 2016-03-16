package com.ccnu.voicehelper;

/**
 * Created by mona on 16/3/16.
 */
public class User  {

    private String stuNo = null;
    private String stuName = null;


    public void setStuNo(String stuNo){
         this.stuNo = stuNo;
    }

    public void setStuName(String stuName){
         this.stuName = stuName;
    }

    public String getStuName() {
        return stuName;
    }

    public String getStuNo() {
        return stuNo;
    }
}
