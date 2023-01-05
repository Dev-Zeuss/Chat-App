package com.zeus.chatme.bluetoothMode.tools;


import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class Tools {
    /**
     * Returns true if the app was granted all the permissions. Otherwise, returns false.
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}