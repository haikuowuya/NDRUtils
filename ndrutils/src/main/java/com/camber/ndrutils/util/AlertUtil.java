package com.camber.ndrutils.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Looper;
import android.widget.TextView;

import com.camber.ndrutils.NdrApp;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by CamberCreak on 19/04/2014.
 */
public class AlertUtil {

 public static void showRebootAlert(final String message, final String command) {
        final List<String> messages = new LinkedList<String>();
        if (Looper.myLooper() != Looper.getMainLooper()) {
            NdrApp.getInstance().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showRebootAlert(message, command);
                }
            });
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(NdrApp.getInstance())
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        RootUtil.getRoot().ShellForCode(command);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.show();
        TextView txtMessage = (TextView) dialog.findViewById(android.R.id.message);
        txtMessage.setTextSize(14);
    }

}
