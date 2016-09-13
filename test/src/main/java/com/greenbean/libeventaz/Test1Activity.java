package com.greenbean.libeventaz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class Test1Activity extends AppCompatActivity {

    private final EventHandler handler = new EventHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test1);
        Eventaz.getDefault().register(handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Eventaz.getDefault().unregister(handler);
    }

    private void handleEvent1() {
        // logic code
    }

    private class EventHandler {

        @Subscribe()
        public void onEvent(TestEvent1 e, EventFlow flow) {
            if (e.value.equals("some code")) {
                handleEvent1();
                // abort the flow, the event will be not delivered to next handler
                flow.abort();
            } else if (e.value.equals("some code 2")) {
                flow.abortForNew(new TestEvent2("code 2"));
            }
        }

        @Subscribe()
        public void onEvent(TestEvent1 e) {
            Log.d("TestBeanWithCircleLife", "test event " + e.value);
        }

        @Subscribe()
        public void onEvent(TestEvent2 e) {
            EventFlow flow = Eventaz.getDefault().getCurrentFlow();
            String val = flow.immediate(new TestEvent3(e.value));
            Log.d("TestBeanWithCircleLife", "onEventImmediately " + val);
        }

        @Subscribe()
        public String onEventImmediately(TestEvent3 e) {
            return e.value + ", Immediately";
        }
    }
}
