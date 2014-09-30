package com.camber.ndrutils.util;

import android.widget.Toast;

import com.camber.ndrutils.NdrApp;

/**
 * Created by CamberCreak on 19/04/2014.
 */
public class ToastUtil {

    public static void popToast(String message) {
        Toast.makeText(NdrApp.getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    public static void popBurntToast(String message) {
        Toast.makeText(NdrApp.getInstance(), message, Toast.LENGTH_SHORT).show();
    }
}
