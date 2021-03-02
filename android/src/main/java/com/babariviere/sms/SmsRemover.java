package com.babariviere.sms;

import android.Manifest;
import android.content.Context;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.database.Cursor;

import com.babariviere.sms.permisions.Permissions;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

import static android.content.ContentValues.TAG;

public class SmsRemover implements PluginRegistry.RequestPermissionsResultListener, MethodChannel.MethodCallHandler {
    private final PluginRegistry.Registrar registrar;
    private final Permissions permissions;


    SmsRemover(PluginRegistry.Registrar registrar){
        this.registrar = registrar;
        this.permissions = new Permissions(registrar.activity());
        registrar.addRequestPermissionsResultListener(this);
    }

    private boolean deleteSms(int id, int thread_id) {
        Context context = registrar.context();
        try{
            // int result = context.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
            context.getContentResolver().delete(Uri.parse("content://sms"), "thread_id=? and _id=?", new String[]{String.valueOf(thread_id), String.valueOf(id)} );
            Log.i("DELETE-SMS", "deleted sms with id: " + id);
        } catch (Exception e) {
            Log.e(TAG, "deleteSms: id + " + id, e);
            return false;
        }
        return true;
    }
    private boolean deleteSms2(String fromAddress) {
        Context context = registrar.context();
        boolean isDeleted = false;
        try {
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = context.getContentResolver().query(uriSms,
                    new String[] { "_id", "thread_id", "address", "person", "date", }, "read=0", null, null);

            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    long threadId = c.getLong(1);
                    String address = c.getString(2);
                    String date = c.getString(3);
                    Log.d("log>>>", "0--->" + c.getString(0) + "1---->" + c.getString(1) + "2---->" + c.getString(2)
                            + "3--->" + c.getString(3) + "4----->" + c.getString(4));
                    Log.d("log>>>", "date" + c.getString(0));

                    ContentValues values = new ContentValues();
                    values.put("read", true);
                    context.getContentResolver().update(Uri.parse("content://sms/"), values, "_id=" + id, null);

                    if (address.equals(fromAddress)) {
                        // mLogger.logInfo("Deleting SMS with id: " + threadId);
                        context.getContentResolver().delete(Uri.parse("content://sms/" + id), "date=?",
                                new String[] { c.getString(4) });
                        Log.d("log>>>", "Delete success.........");
                    }
                } while (c.moveToNext());
            }
            isDeleted = true;
        } catch (Exception e) {
            isDeleted = false;
            Log.e("log>>>", e.toString());
        }
        return isDeleted;
    }


    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method){
            case "removeSms":
                if(methodCall.hasArgument("id")){
                    Log.i("SMSREMOVER", "method called for removing sms: " + methodCall.argument("id") );
                    result.success(this.deleteSms(Integer.parseInt(methodCall.argument("id").toString()), Integer.parseInt(methodCall.argument("thread_id").toString())));
                }
            case "removeSms2":
                if (methodCall.hasArgument("fromAddress")) {
                    Log.i("SMSREMOVER", "method called for removing sms: " + methodCall.argument("fromAddress"));
                    deleteSms2(methodCall.argument("fromAddress").toString());
                }
        }


    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != Permissions.READ_SMS_ID_REQ) {
            return false;
        }
        boolean isOk = true;
        for (int res : grantResults) {
            if (res != PackageManager.PERMISSION_GRANTED) {
                isOk = false;
                break;
            }
        }
        if (isOk) {
            return true;
        }
        return false;
    }
}


