package com.waroengweb.absensi.helpers;

import androidx.exifinterface.media.ExifInterface;
import android.util.Log;

import java.io.IOException;

public class ExifHelper {

    public static void copyExif(String originalPath, String newPath) throws IOException {

        String[] attributes = new String[]
                {
                        ExifInterface.TAG_DATETIME,
                        ExifInterface.TAG_DATETIME_DIGITIZED,
                        ExifInterface.TAG_EXPOSURE_TIME,
                        ExifInterface.TAG_FLASH,
                        ExifInterface.TAG_FOCAL_LENGTH,
                        ExifInterface.TAG_GPS_ALTITUDE,
                        ExifInterface.TAG_GPS_ALTITUDE_REF,
                        ExifInterface.TAG_GPS_DATESTAMP,
                        ExifInterface.TAG_GPS_LATITUDE,
                        ExifInterface.TAG_GPS_LATITUDE_REF,
                        ExifInterface.TAG_GPS_LONGITUDE,
                        ExifInterface.TAG_GPS_LONGITUDE_REF,
                        ExifInterface.TAG_GPS_PROCESSING_METHOD,
                        ExifInterface.TAG_GPS_TIMESTAMP,
                        ExifInterface.TAG_MAKE,
                        ExifInterface.TAG_MODEL,
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.TAG_SUBSEC_TIME,
                        ExifInterface.TAG_WHITE_BALANCE
                };

        ExifInterface oldExif = new ExifInterface(originalPath);

        ExifInterface newExif = new ExifInterface(newPath);

        if (attributes.length > 0) {
            for (int i = 0; i < attributes.length; i++) {
                String value = oldExif.getAttribute(attributes[i]);
                if (value != null)
                    newExif.setAttribute(attributes[i], value);
            }
            newExif.saveAttributes();
        }

    }
}
