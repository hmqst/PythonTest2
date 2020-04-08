package com.test.pythontest2.pythonToJava;

// 用于Python数据返回（实际用）（油脂检测）
public class YouZhi {
    // 指数
    private Integer number;
    // Python处理用时
    private Double time;
    // 目前时间
    private String this_time;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public String getThis_time() {
        return this_time;
    }

    public void setThis_time(String this_time) {
        this.this_time = this_time;
    }

    @Override
    public String toString() {
        return "number=" + number +
                "\ntime=" + time +
                "\nthis_time='" + this_time;
    }
}
