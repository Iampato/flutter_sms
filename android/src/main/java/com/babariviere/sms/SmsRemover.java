package com.babariviere.sms;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

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
            context.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
            // context.getContentResolver().delete(Uri.parse("content://sms"), "thread_id=? and _id=?", new String[]{String.valueOf(thread_id), String.valueOf(id)} );
            Log.i("DELETE-SMS", "deleted sms with id: " + id);
        } catch (Exception e) {
            Log.e(TAG, "deleteSms: id + " + id, e);
            return false;
        }
        return true;
    }

    @Override
    public void onMethodCall(MethodCall methodCall, MethodChannel.Result result) {
        switch (methodCall.method){
            case "removeSms":
                if(methodCall.hasArgument("id")){
                    Log.i("SMSREMOVER", "method called for removing sms: " + methodCall.argument("id") );
                    result.success(this.deleteSms(Integer.parseInt(methodCall.argument("id").toString()), Integer.parseInt(methodCall.argument("thread_id").toString())));
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


