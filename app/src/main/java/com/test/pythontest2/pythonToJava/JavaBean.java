package com.test.pythontest2.pythonToJava;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// 用于Python数据返回（测试用）
public class JavaBean {
    // 自定义名称
    private String name;
    // 返回数据
    private List<String> data;

    public JavaBean(String n){
        this.name = n;
        data = new ArrayList<String>();
    }

    public void setData(String el){
        this.data.add(el);
    }

    public void print(){
        for (String it: data) {
            Log.d("Java Bean - "+this.name,it);
        }
    }
}
