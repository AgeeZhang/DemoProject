package com.zcitc.updatelibrary.contract;

public @interface DownloadTags {
    String MUST_BE_UPDATED = "mustBeUpdated";
    String NEW_VERSION = "newVersion";
    String APK_URL = "apkUrl";
    String FILE_MD5 = "fileMD5";
    String ICON_ID = "iconId";

    int DOWNLOAD_START = 0;
    int DOWNLOADING = 1;
    int DOWNLOAD_SUCCESS = 2;
    int DOWNLOAD_FAIL = 3;
    int DOWNLOAD_INSTALL_APK = 4;
}
