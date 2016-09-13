package com.greenbean.libeventaz;

/**
 * Created by chrisding on 16/9/12.
 * Function: Eventaz
 */
public class EventazException extends RuntimeException {

    public EventazException(String msg) {
        super(msg);
    }

    public EventazException(Throwable e) {
        super(e);
    }

    public EventazException(String msg, Throwable e) {
        super(msg, e);
    }
}
