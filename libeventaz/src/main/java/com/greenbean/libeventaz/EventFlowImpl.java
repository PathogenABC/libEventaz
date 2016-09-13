package com.greenbean.libeventaz;

/**
 * Created by chrisding on 16/9/12.
 * Function: Eventaz
 */
final class EventFlowImpl implements EventFlow {

    private boolean aborted;

    private Object originEvent;

    private Object newEvent;

    private DefaultEventaz eventaz;

    EventFlowImpl(DefaultEventaz eventaz, Object originEvent) {
        this.eventaz = eventaz;
        this.originEvent = originEvent;
        this.aborted = false;
    }

    @Override
    public void abort() {
        if (aborted) {
            throw new EventazException("This event flow has been aborted ever.");
        }
        aborted = true;
    }

    @Override
    public void abortForNew(Object newEvent) {
        if (newEvent == originEvent) {
            throw new EventazException("Can't post the same event.");
        }

        abort();
        this.newEvent = newEvent;
    }

    @Override
    public void postForNew(Object newEvent) {
        if (newEvent == originEvent || (newEvent.getClass() == originEvent.getClass())) {
            throw new EventazException("Can't post for the SAME new event.");
        }

        if (aborted) {
            throw new EventazException("Can't post event when flow is aborted.");
        }
        if (this.newEvent != null) {
            throw new EventazException("Can't post a new event because the fow has ever post one.");
        }
        this.newEvent = newEvent;
    }

    @Override
    public <T> T immediate(Object inputEvent) {
        return eventaz.handleImmediateEvent(inputEvent);
    }

    boolean isAborted() {
        return aborted;
    }

    Object getNewEvent() {
        return newEvent;
    }
}
