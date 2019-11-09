package com.tfswx.my_receive.utils;

import java.io.Serializable;


/**
 * 数据返回类，用于前后端交互
 * @param <T>
 */
public class JsonResult<T> implements Serializable {

    private static final long serialVersionUID = -1L;
    public static final int SUCCESS = 0;
    public static final int ERROR = 1;

    //状态值
    private int state;
    //返回的信息
    private T data;
//    private int total;
    //返回的错误信息
    private String message;

    public JsonResult() {
    }

    public JsonResult(T t) {
        state = SUCCESS;
        data = t;
    }
//    public JsonResult(T t, int total) {
//        state = SUCCESS;
//        data = t;
////        this.total = total;
//        message = "";
//    }
    public JsonResult(int state, String message) {
        this.state = state;
        this.message = message;
    }

    public JsonResult(Throwable e) {
        state = ERROR;
        message = e.getMessage();
    }

    public JsonResult(int state, Throwable e) {
        this.state = state;
        this.message = e.getMessage();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

//    public int getTotal() {return total;}

//    public void getTotal(int total) {this.total = total;}
}
