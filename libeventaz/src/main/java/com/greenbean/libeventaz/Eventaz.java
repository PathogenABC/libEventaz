package com.greenbean.libeventaz;

/**
 * Created by chrisding on 16/9/12.
 * Function: Eventaz
 */
public abstract class Eventaz {
    private static DefaultEventaz sInstance;

    public static Eventaz getDefault() {
        if (sInstance == null) {
            synchronized (Eventaz.class) {
                if (sInstance == null) {
                    sInstance = new DefaultEventaz();
                }
            }
        }
        return sInstance;
    }

    /**
     * register a subscriber with onEvent & onEventImmediately methods those with @Subscribe annotation
     *
     * @param subscriber 订阅者
     */
    public final void register(Object subscriber) {
        sInstance.registerInner(subscriber);
    }

    /**
     * unregister a subscriber
     *
     * @param subscriber 订阅者
     */
    public final void unregister(Object subscriber) {
        sInstance.unregisterInner(subscriber);
    }

    /**
     * Post an event. If no subscribers for this event, a new NoSubscriberEvent will be thrown.
     *
     * @param event 事件
     */
    public final void post(Object event) {
        sInstance.postInner(event);
    }

    /**
     * Call the only one subscriber's onEventImmediately(this event), return immediately.
     *
     * @param event 事件
     * @param <T>   结果类型
     * @return 结果
     */
    public final <T> T immediate(Object event) {
        return sInstance.immediateInner(event);
    }

    /**
     * Get current event flow. If no flow ,return null. Use in an onEvent(XXX) to abort the event delivery.
     *
     * @return current flow
     */
    public final EventFlow getCurrentFlow() {
        return sInstance.getCurrentFlowInner();
    }

}
