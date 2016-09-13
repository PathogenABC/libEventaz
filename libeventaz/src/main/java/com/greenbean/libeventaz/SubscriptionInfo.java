package com.greenbean.libeventaz;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chrisding on 16/9/13.
 * Function: Eventaz
 */
final class SubscriptionInfo {

    public final List<Class<?>> commonTopics = new ArrayList<>();
    public final List<Subscription> commonSubscriptions = new ArrayList<>();
    public final List<Class<?>> immediateTopics = new ArrayList<>();
    public final List<Subscription> immediateSubscriptions = new ArrayList<>();

    public boolean isEmpty() {
        return commonTopics.isEmpty() && immediateTopics.isEmpty();
    }

}
