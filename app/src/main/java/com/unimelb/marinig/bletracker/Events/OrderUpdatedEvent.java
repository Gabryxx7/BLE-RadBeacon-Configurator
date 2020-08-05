package com.unimelb.marinig.bletracker.Events;

public class OrderUpdatedEvent{
    public int order;
    public OrderUpdatedEvent(int order){
        this.order = order;
    }
}