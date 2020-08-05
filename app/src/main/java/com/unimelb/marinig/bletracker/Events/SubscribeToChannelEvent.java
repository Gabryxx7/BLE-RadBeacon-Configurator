package com.unimelb.marinig.bletracker.Events;

public class SubscribeToChannelEvent {
    public String channelId;
    public SubscribeToChannelEvent(String channelId){
        this.channelId = channelId;
    }
}