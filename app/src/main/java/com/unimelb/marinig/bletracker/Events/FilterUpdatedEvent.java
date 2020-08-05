package com.unimelb.marinig.bletracker.Events;

public class FilterUpdatedEvent {
    public String filter;
    public boolean filterState;
    public FilterUpdatedEvent(String filter, boolean filterState){
        this.filter = filter;
        this.filterState = filterState;
    }
}