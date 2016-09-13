package com.greenbean.libeventaz;

/**
 * Created by chrisding on 16/9/12.
 * Function: Eventaz
 */
public interface EventFlow {

    /**
     * 终止当前事件流
     */
    void abort();

    /**
     * 终止当前事件流,并抛出新的事件
     *
     * @param newEvent 新事件，不能是原事件
     * @throws EventazException if newEvent == originEvent
     */
    void abortForNew(Object newEvent);

    /**
     * 发送新的事件，不会打断原有事件流，在原事件流结束之后处理
     * <p/>
     * #ex: 在同一个流中，此方法只能调用一次，不能和abortXXX()同时使用
     *
     * @param newEvent 新事件，不能是原事件
     */
    void postForNew(Object newEvent);

    <T> T immediate(Object inputEvent);
}
