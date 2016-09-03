package com.tomer.alwayson.receivers;

import android.content.Context;

import com.tomer.alwayson.Globals;
import com.tomer.alwayson.helpers.Utils;

import java.util.Date;

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        Utils.stopMainService(ctx);
        Utils.logInfo(CallReceiver.class.getSimpleName(), "Call detected");
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Globals.inCall = true;
        Utils.stopMainService(ctx);
        Utils.logInfo(CallReceiver.class.getSimpleName(), "Call detected");
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Globals.inCall = false;
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Globals.inCall = true;
        Utils.stopMainService(ctx);
        Utils.logInfo(CallReceiver.class.getSimpleName(), "Call detected");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Globals.inCall = false;
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
    }
}



