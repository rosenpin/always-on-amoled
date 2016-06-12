package com.tomer.alwayson.Receivers;

import android.content.Context;
import android.content.Intent;

import com.tomer.alwayson.Services.MainService;

import java.util.Date;

/**
 * Created by tomer on 6/10/16.
 */
public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        //
        ctx.stopService(new Intent(ctx, MainService.class));
        System.out.println("Call detected");
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        //
        ctx.stopService(new Intent(ctx, MainService.class));
        System.out.println("Call detected");
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        //
        System.out.println("Call detected");
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        //
        ctx.stopService(new Intent(ctx, MainService.class));
        System.out.println("Call detected");
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        //
        System.out.println("Call detected");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        //
        System.out.println("Call detected");
    }

}



