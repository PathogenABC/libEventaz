package com.greenbean.libeventaz;

/**
 * Created by chrisding on 16/9/12.
 * Function: Eventaz
 */
public class NoSubscriberEvent {

    public final Object originEvent;

    public NoSubscriberEvent(Object originEvent) {
        this.originEvent = originEvent;
    }
}
