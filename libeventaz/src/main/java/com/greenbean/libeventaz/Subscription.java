package com.greenbean.libeventaz;

import java.lang.reflect.Method;

/**
 * Created by chrisding on 16/9/13.
 * Function: Eventaz
 */
final class Subscription {
    public Object subscriber;
    public final Method method;
    public final boolean abortable;

    Subscription(Method method, boolean abortable) {
        this.method = method;
        this.abortable = abortable;
    }

    public void setSubscriber(Object subscriber) {
        this.subscriber = subscriber;
    }
}