package com.greenbean.libeventaz;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chrisding on 16/9/13.
 * Function: Eventaz
 */
final class SubscriptionFinder {

    private final Map<Class<?>, SubscriptionInfo> subscriptionCaches = new ConcurrentHashMap<>();

    SubscriptionInfo findSubscriptionInfo(Class<?> subscriberClass) {
        SubscriptionInfo subscriberMethods = subscriptionCaches.get(subscriberClass);
        if (subscriberMethods != null) {
            return subscriberMethods;
        }

        subscriberMethods = findUsingReflection(subscriberClass);
        if (subscriberMethods == null || subscriberMethods.isEmpty()) {
            throw new EventazException("Subscriber " + subscriberClass
                    + " and its super classes have no public methods with the @Subscribe annotation");
        }
        subscriptionCaches.put(subscriberClass, subscriberMethods);
        return subscriberMethods;
    }

    private SubscriptionInfo findUsingReflection(Class<?> subscriberClass) {
        SubscriptionInfo info = new SubscriptionInfo();
        Method[] methods = subscriberClass.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                String name = method.getName();
                if (name.equals("onEvent")) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length == 1) {
                        Class<?> topic = paramTypes[0];
                        info.commonTopics.add(topic);
                        info.commonSubscriptions.add(new Subscription(method, false));
                    } else if (paramTypes.length == 2) {
                        if (paramTypes[1] == EventFlow.class) {
                            Class<?> topic = paramTypes[0];
                            info.commonTopics.add(topic);
                            info.commonSubscriptions.add(new Subscription(method, true));
                        }
                    }
                } else if (name.equals("onEventImmediately")) {
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length == 1) {
                        Class<?> topic = paramTypes[0];
                        info.immediateTopics.add(topic);
                        info.immediateSubscriptions.add(new Subscription(method, true));
                    }
                }
            }
        }
        return info;
    }

    void clearSubscriptionInfosCache() {
        subscriptionCaches.clear();
    }
}
