package com.ecc.bigdata.tv.util;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Created by Mr.Yangxiufeng
 * DATE 2017/2/14
 * BigDataController
 */

public class Utils {
    /**
     * 获取手机IMEI号
     */
    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        return imei;
    }
    public static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }
}
