package com.greenbean.libeventaz;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chrisding on 16/9/12.
 * Function: Eventaz
 */
final class DefaultEventaz extends Eventaz implements Handler.Callback {

    private final SubscriptionFinder finder;

    private final Map<Object, List<Class<?>>> commonTopicsBySubscriber;
    private final Map<Class<?>, List<Subscription>> commonSubscriptionsByTopic;
    private final Map<Object, List<Subscription>> commonSubscriptionsBySubscriber;

    private final Map<Object, List<Class<?>>> immediateTopicsBySubscriber;
    private final Map<Class<?>, Subscription> immediateSubscriptionsByTopic;
    private final Map<Object, List<Subscription>> immediateSubscriptionsBySubscriber;

    private final Handler uiHandler;
    private boolean inHandling = false;

    private EventFlow currentFlow;

    DefaultEventaz() {
        this.finder = new SubscriptionFinder();

        this.commonSubscriptionsByTopic = new HashMap<>();
        this.commonSubscriptionsBySubscriber = new HashMap<>();
        this.commonTopicsBySubscriber = new HashMap<>();

        this.immediateTopicsBySubscriber = new HashMap<>();
        this.immediateSubscriptionsByTopic = new HashMap<>();
        this.immediateSubscriptionsBySubscriber = new HashMap<>();

        this.uiHandler = new Handler(Looper.getMainLooper(), this);
        this.registerInner(new DefaultNoSubscriberEventSubscriber());
    }

    void registerInner(Object subscriber) {
        if (hasRegistered(subscriber)) {
            throw new EventazException("subscriber " + subscriber + " has been already registered.");
        }

        SubscriptionInfo info = finder.findSubscriptionInfo(subscriber.getClass());
        List<Class<?>> commonTopics = info.commonTopics;
        List<Subscription> commonSubs = info.commonSubscriptions;
        List<Class<?>> immediateTopics = info.immediateTopics;
        List<Subscription> immediateSubs = info.immediateSubscriptions;

        subscribeCommonSubscriber(subscriber, commonTopics, commonSubs);
        subscribeImmediateSubscriber(subscriber, immediateTopics, immediateSubs);
    }

    private void subscribeImmediateSubscriber(Object subscriber, List<Class<?>> immediateTopics, List<Subscription> immediateSubs) {
        immediateTopicsBySubscriber.put(subscriber, immediateTopics);
        for (int i = 0; i < immediateTopics.size(); i++) {
            Class<?> immediateTopic = immediateTopics.get(i);
            Subscription subscription = immediateSubs.get(i);
            subscription.subscriber = subscriber;   // IMPORTANT!!! fill subscriber

            // add into immediateSubscriptionsBySubscriber
            List<Subscription> subsBySubscriber = immediateSubscriptionsBySubscriber.get(subscriber);
            if (subsBySubscriber == null) {
                subsBySubscriber = new ArrayList<>();
                immediateSubscriptionsBySubscriber.put(subscriber, subsBySubscriber);
            }
            subsBySubscriber.add(subscription);

            // add into immediateSubscriptionsByTopic
            if (immediateSubscriptionsByTopic.get(immediateTopic) != null) {
                throw new EventazException("Only just one immediately subscriber for one type event[" + immediateTopic.getName() + "]");
            }
            immediateSubscriptionsByTopic.put(immediateTopic, subscription);
        }
    }

    private void subscribeCommonSubscriber(Object subscriber, List<Class<?>> commonTopics, List<Subscription> commonSubs) {
        commonTopicsBySubscriber.put(subscriber, commonTopics);
        for (int i = 0; i < commonTopics.size(); i++) {
            Class<?> commonTopic = commonTopics.get(i);
            Subscription subscription = commonSubs.get(i);
            subscription.subscriber = subscriber;   // IMPORTANT!!! fill subscriber

            // add into commonSubscriptionsBySubscriber
            List<Subscription> subsBySubscriber = commonSubscriptionsBySubscriber.get(subscriber);
            if (subsBySubscriber == null) {
                subsBySubscriber = new ArrayList<>();
                commonSubscriptionsBySubscriber.put(subscriber, subsBySubscriber);
            }
            subsBySubscriber.add(subscription);

            // add into commonSubscriptionsByTopic
            List<Subscription> subscriptions = commonSubscriptionsByTopic.get(commonTopic);
            if (subscriptions == null) {
                subscriptions = new ArrayList<>();
                commonSubscriptionsByTopic.put(commonTopic, subscriptions);
            }
            subscriptions.add(subscription);
        }
    }

    void unregisterInner(Object subscriber) {
        unsubscribeCommonEventSubscribers(subscriber);
        unsubscribeImmediateEventSubscribers(subscriber);
    }

    private boolean hasRegistered(Object subscriber) {
        Class<?> subscriberType = subscriber.getClass();
        return commonTopicsBySubscriber.containsKey(subscriberType) || immediateTopicsBySubscriber.containsKey(subscriber);
    }

    private void unsubscribeCommonEventSubscribers(Object subscriber) {
        List<Class<?>> topics = commonTopicsBySubscriber.remove(subscriber);
        if (topics != null) {
            for (Class<?> topic : topics) {
                commonSubscriptionsByTopic.remove(topic);
            }
        }
        List<Subscription> subs = commonSubscriptionsBySubscriber.remove(subscriber);
        if (subs != null) {
            for (Subscription sub : subs) {
                sub.subscriber = null;      // IMPORTANT!!! clear subscriber
            }
        }
    }

    private void unsubscribeImmediateEventSubscribers(Object subscriber) {
        List<Class<?>> topics = immediateTopicsBySubscriber.remove(subscriber);
        if (topics != null) {
            for (Class<?> topic : topics) {
                immediateSubscriptionsByTopic.remove(topic);
            }
        }
        List<Subscription> subs = immediateSubscriptionsBySubscriber.remove(subscriber);
        if (subs != null) {
            for (Subscription sub : subs) {
                sub.subscriber = null;      // IMPORTANT!!! clear subscriber
            }
        }
    }

    void postInner(Object event) {
        if (event == null) {
            throw new NullPointerException("event can't be null.");
        }

        if (Looper.getMainLooper() == Looper.myLooper()) {
            if (inHandling) {
                throw new EventazException("can't use Eventaz.getDefault().post(event) in onEvent()/onEventImmediately() methods, " +
                        "use EventFlow.postForNew/abortForNew/immediate instead.");
            } else {
                inHandling = true;
                handleCommonEvent(event);
                inHandling = false;
            }
        } else {
            uiHandler.sendMessage(Message.obtain(uiHandler, 0, event));
        }
    }

    private void handleCommonEvent(Object event) {
        Object newEvent = event;
        do {
            newEvent = dispatchEventToSubscribers(newEvent);
        } while (newEvent != null);
    }

    private Object dispatchEventToSubscribers(Object event) throws EventazException {
        Class<?> topic = event.getClass();
        if (commonSubscriptionsByTopic.get(topic) == null) {
            topic = NoSubscriberEvent.class;
            event = new NoSubscriberEvent(event);
        }

        try {
            Object newEvent = null;
            List<Subscription> subscriptions = commonSubscriptionsByTopic.get(topic);
            EventFlowImpl flow = new EventFlowImpl(this, event);
            currentFlow = flow;
            for (Subscription subscription : subscriptions) {
                if (subscription.abortable) {
                    subscription.method.invoke(subscription.subscriber, event, flow);
                } else {
                    subscription.method.invoke(subscription.subscriber, event);
                }
                if (flow.isAborted()) {
                    newEvent = flow.getNewEvent();
                    break;
                }
            }
            currentFlow = null;
            return newEvent;
        } catch (Exception e) {
            throw new EventazException("dispatchEventToSubscribers fails.", e);
        }
    }

    public <T> T immediateInner(Object event) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new EventazException("can't post an immediate event in a worker thread.");
        }

        if (inHandling) {
            throw new EventazException("can't post an Eventaz.getDefault().immediate(event) in an event flow, " +
                    "use EventFlow.postForNew/abortForNew/immediate instead.");
        }

        return handleImmediateEvent(event);
    }

    <T> T handleImmediateEvent(Object event) {
        //noinspection unchecked
        return (T) dispatchImmediateEvent(event);
    }

    private Object dispatchImmediateEvent(Object event) {
        Class<?> topic = event.getClass();
        if (immediateSubscriptionsByTopic.get(topic) == null) {
            topic = NoSubscriberEvent.class;
            event = new NoSubscriberEvent(event);
        }

        try {
            Subscription subscription = immediateSubscriptionsByTopic.get(topic);
            return subscription.method.invoke(subscription.subscriber, event);
        } catch (Exception e) {
            throw new EventazException("dispatchEventToSubscribers fails.", e);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        handleCommonEvent(msg.obj);
        return true;
    }

    public EventFlow getCurrentFlowInner() {
        return currentFlow;
    }

}
