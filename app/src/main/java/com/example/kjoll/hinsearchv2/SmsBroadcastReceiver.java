package com.example.kjoll.hinsearchv2;

/**
 * Created by kjoll on 07-Jul-16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        /**/
        /*Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsBody="";
            String address="";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                 smsBody = smsMessage.getMessageBody().toString();
                 address = smsMessage.getOriginatingAddress();

             //   smsMessageStr += "SMS From: " + address + "\n";
              //  smsMessageStr += smsBody + "\n";
            }
            */

        Bundle bundle  = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        SmsMessage[] messages = new SmsMessage[pdus.length];
        String body=null;
        for (int i = 0; i < pdus.length; i++)
        {
            messages[i] =
                    SmsMessage.createFromPdu((byte[]) pdus[i]);
        }

        SmsMessage sms = messages[0];
        try {
            if (messages.length == 1 || sms.isReplace()) {
                body = sms.getDisplayMessageBody();
            } else {
                StringBuilder bodyText = new StringBuilder();
                for (int i = 0; i < messages.length; i++) {
                    bodyText.append(messages[i].getMessageBody());
                }
                body = bodyText.toString();
            }
        } catch (Exception e) {

        }
          //  Toast.makeText(context, smsMessageStr, Toast.LENGTH_SHORT).show();

            //this will update the UI with message
        Toast.makeText(context,body,Toast.LENGTH_LONG).show();

            SmsFragment inst = SmsFragment.newInstance();

            inst.updateList(body,"yo");
        }
    }

