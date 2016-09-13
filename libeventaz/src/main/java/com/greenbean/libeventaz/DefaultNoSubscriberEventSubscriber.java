package com.greenbean.libeventaz;

import android.util.Log;

/**
 * Created by chrisding on 16/9/12.
 * Function: Eventaz
 */
final class DefaultNoSubscriberEventSubscriber {

    @Subscribe
    public void onEvent(NoSubscriberEvent event) {
        Log.d("Eventaz", "NoSubscriberEvent: no subscriber for common event[" + event.originEvent.getClass().getName() + "]");
    }

    @Subscribe
    public Void onEventImmediately(NoSubscriberEvent event) {
        Log.d("Eventaz", "NoSubscriberEvent: no subscriber for immediate event[" + event.originEvent.getClass().getName() + "]");
        return null;
    }

}
