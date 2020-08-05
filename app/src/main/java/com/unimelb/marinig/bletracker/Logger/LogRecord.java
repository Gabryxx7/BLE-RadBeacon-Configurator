package com.unimelb.marinig.bletracker.Logger;

public class LogRecord{
    public int priority;
    public String date;
    public String tag;
    public String msg;
    public Throwable tr;

    public LogRecord(int priority, String date, String tag, String msg, Throwable tr){
        this.priority = priority;
        this.date = date;
        this.tag = tag;
        this.msg = msg;
        this.tr = tr;
    }
}
