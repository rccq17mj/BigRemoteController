package com.ecc.bigdata.tv.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static android.provider.Contacts.SettingsColumns.KEY;

/**
 * Created by Mr.Yangxiufeng
 * DATE 2017/5/22
 * BigDataController
 */

public class DeviceUuidFactory {

    static final String PREFS_FILE = "device_id.xml";
    static final String PREFS_DEVICE_ID = "device_id";
    static UUID uuid;
    private static final String DEVICE_UUID_FILE_NAME = ".device_info.txt";

    public static void buildUuidFactory(Context context) {
        if (uuid == null) {
            synchronized (DeviceUuidFactory.class) {
                if (uuid == null) {
                    final SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        // Use the ids previously computed and stored in the prefs file
                        uuid = UUID.fromString(id);
                        Log.e("XXX", "DeviceUuidFactory share UUID 1 =" + uuid.toString());
                    } else {
                        String UUIDDisk = recoverDeviceUuidFromSD();
                        if (UUIDDisk != null) {
                            uuid = UUID.fromString(UUIDDisk);
                            Log.e("XXX", "DeviceUuidFactory UUIDDisk=" + UUIDDisk);
                        } else {
                            final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                            // Use the Android ID unless it's broken, in which case fallback on deviceId,
                            // unless it's not available, then fallback on a random number which we store
                            // to a prefs file
                            try {
                                if (!"9774d56d682e549c".equals(androidId)) {
                                    Log.e("XXX", "DeviceUuidFactory androidId =" + androidId);
                                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                                    saveDeviceUuidToSD(uuid.toString());
                                } else {
                                    final String deviceId = Utils.getIMEI(context);
                                    uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.nameUUIDFromBytes(Utils.getUniquePsuedoID().getBytes());
                                    Log.e("XXX", "DeviceUuidFactory UUID 1 =" + uuid.toString());
                                    saveDeviceUuidToSD(uuid.toString());
                                }
                            } catch (UnsupportedEncodingException e) {
                                throw new RuntimeException();
                            }
                            // Write the value out to the prefs file
                            prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).commit();
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns a unique UUID for the current android device.  As with all UUIDs, this unique ID is "very highly likely"
     * to be unique across all Android devices.  Much more so than ANDROID_ID is.
     * <p>
     * The UUID is generated by using ANDROID_ID as the base key if appropriate, falling back on
     * TelephonyManager.getDeviceID() if ANDROID_ID is known to be incorrect, and finally falling back
     * on a random UUID that's persisted to SharedPreferences if getDeviceID() does not return a
     * usable value.
     * <p>
     * In some rare circumstances, this ID may change.  In particular, if the device is factory reset a new device ID
     * may be generated.  In addition, if a user upgrades their phone from certain buggy implementations of Android 2.2
     * to a newer, non-buggy version of Android, the device ID may change.  Or, if a user uninstalls your app on
     * a device that has neither a proper Android ID nor a Device ID, this ID may change on reinstallation.
     * <p>
     * Note that if the code falls back on using TelephonyManager.getDeviceId(), the resulting ID will NOT
     * change after a factory reset.  Something to be aware of.
     * <p>
     * Works around a bug in Android 2.2 for many devices when using ANDROID_ID directly.
     *
     * @return a UUID that may be used to uniquely identify your device for most purposes.
     * @see //code.google.com/p/android/issues/detail?id=10603
     */
    public static UUID getDeviceUuid() {
        return uuid;
    }

    private static void saveDeviceUuidToSD(String uuid) {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File targetFile = new File(dirPath, DEVICE_UUID_FILE_NAME);
        if (targetFile != null) {
            Log.e("XXX", "DeviceUuidFactory targetFile 2 =");
            OutputStreamWriter osw;
            try {
                osw = new OutputStreamWriter(new FileOutputStream(targetFile), "utf-8");
                try {
                    osw.write(uuid);
                    osw.flush();
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private static String recoverDeviceUuidFromSD() {
        try {
            String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File dir = new File(dirPath);
            File uuidFile = new File(dir, DEVICE_UUID_FILE_NAME);
            if (!dir.exists() || !uuidFile.exists()) {
                return null;
            }
            FileReader fileReader = new FileReader(uuidFile);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[100];
            int readCount;
            while ((readCount = fileReader.read(buffer)) > 0) {
                sb.append(buffer, 0, readCount);
            }
            //通过UUID.fromString来检查uuid的格式正确性
            UUID uuid = UUID.fromString(sb.toString());
            Log.e("XXX", "DeviceUuidFactory recoverDeviceUuidFromSD =" + sb.toString());
            return uuid.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
