package com.zcitc.updatelibrary.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.text.DecimalFormat;

public class UpdateUtil {


    /**
     * @param x     当前值
     * @param total 总值
     *              [url=home.php?mod=space&uid=7300]@return[/url] 当前百分比
     * @Description 返回百分之值
     */
    private String getPercent(int x, int total) {
        String result = "";// 接受百分比的值
        double x_double = x * 1.0;
        double tempresult = x_double / total;
        // 百分比格式，后面不足2位的用0补齐 ##.00%
        DecimalFormat df1 = new DecimalFormat("0.00%");
        result = df1.format(tempresult);
        return result;
    }

//    /**
//     * @return
//     * @Description 获取当前应用的名称
//     */
//    private String getApplicationName() {
//        PackageManager packageManager = null;
//        ApplicationInfo applicationInfo = null;
//        try {
//            packageManager = getApplicationContext().getPackageManager();
//            applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
//        } catch (PackageManager.NameNotFoundException e) {
//            applicationInfo = null;
//        }
//        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
//        return applicationName;
//    }
}
