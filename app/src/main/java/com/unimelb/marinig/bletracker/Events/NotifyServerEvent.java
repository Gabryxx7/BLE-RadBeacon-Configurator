package com.unimelb.marinig.bletracker.Events;

public class NotifyServerEvent{
    public String notifTitle;
    public String notifContent;
    public NotifyServerEvent(String pNotifTitle, String pNotifContent){
        this.notifTitle = pNotifTitle;
        this.notifContent = pNotifContent;
    }
}