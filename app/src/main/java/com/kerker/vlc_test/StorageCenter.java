package com.kerker.vlc_test;

import android.os.Environment;

public class StorageCenter {

    private static String imageDirectory = null;
    static {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            imageDirectory = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .getAbsolutePath();
        }
    }

    public static String getImageDirectory() {
        return imageDirectory;
    }
}
