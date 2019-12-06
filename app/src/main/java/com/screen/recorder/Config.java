package com.screen.recorder;

import android.hardware.display.DisplayManager;

public class Config {

    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final String VERSION_APP = BuildConfig.VERSION_NAME;
    public static final String CODE_CONTROL_APP = "40498";

    public static final String URL_FILE = "URL_FILE";
    public static final String URL_WEB = "http://smarttoolstudio.online/screen-recorder/privacy-policy.html";

    public static final int VIRT_DISPLAY_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

    private static final String BASE = "com.screen.recorder.ScreenRecorderService.";
    public static final String EXTRA_RESULT_CODE = BASE + "EXTRA_RESULT_CODE";
    public static final String EXTRA_RESULT_INTENT = BASE + "EXTRA_RESULT_INTENT";

    public class Directory {
        public static final String ROOT_DIRECTORY = "VMB_Screen_Recorder";
        public static final String VIDEO_DIRECTORY = "video";
        public static final String SCREENSHOT_DIRECTORY = "screenshot";
    }

    public class KeySharePrefference {
        public static final String RESOLUTION = "resolution";
        public static final String QUALITY = "quality";
        public static final String FPS = "fps";
        public static final String ORIENTATION = "orientation";
        public static final String AUDIO = "audio";

        public static final String OVERLAY = "overlay";
    }

    public static final int[] resource_title_menu = {
            R.string.share_app, R.string.rate_5_stars, R.string.more_app, R.string.settings, R.string.privacy_policy
    };

    public static final int[] resource_icon_menu = {
            R.drawable.ic_share_app, R.drawable.ic_rate_5_stars, R.drawable.ic_more_app,
            R.drawable.ic_settings, R.drawable.ic_privacy_policy
    };

    public static final String[] action_menu = {
            "share_app", "rate_5_stars", "more_apps", "settings", "privacy_policy"
    };

    public class RequestCode {
        public static final int ICON_INTERACT_RECORD = 15;
        public static final int ICON_INTERACT_HOME = 16;
        public static final int ICON_INTERACT_SCREENSHOT = 17;
        public static final int ICON_INTERACT_CLOSE = 18;

        public static final int ICON_INTERACT_PAUSE = 19;
        public static final int ICON_INTERACT_STOP = 20;

        public static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 1000;
        public static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 1001;
        public static final int REQUEST_CODE_PERMISSION_AUDIO_AND_WRITE = 1002;
        public static final int REQUEST_CODE_CAPTURE_PERMISSION = 1003;
        public static final int REQUEST_CODE_RECORD_PERMISSION = 1004;

        public static final int REQUEST_CODE_PERMISSION_EDIT = 1008;
        public static final int REQUEST_CODE_PERMISSION_DELETE = 1009;

        public static final int REQUEST_CODE_PERMISSION_OVERLAY = 1010;
    }

    public class Action {
        public static final String ACTION_RECORD = BASE + "ACTION_RECORD";
        public static final String ACTION_HOME = BASE + "ACTION_HOME";
        public static final String ACTION_STOP = BASE + "ACTION_STOP";
        public static final String ACTION_PAUSE = BASE + "ACTION_PAUSE";
        public static final String ACTION_RESUME = BASE + "ACTION_RESUME";
        public static final String ACTION_SCREENSHOT = BASE + "ACTION_SCREENSHOT";
        public static final String ACTION_CLOSE = BASE + "ACTION_CLOSE";
    }

    public class KeyIntentData {
        public static final String KEY_LIST_DATA = "list_data";
        public static final String KEY_X = "x";
        public static final String KEY_Y = "y";
    }

    public class FragmentTag {
        public static final String Fragment_Video = "Fragment_Video";
        public static final String Fragment_Screenshot = "Fragment_Screenshot";
        public static final String Fragment_Setting = "Fragment_Setting";
    }

    public class Notification {
        public static final int ID_INTERACTIVE = 2;
        public static final int ID_VIDEO = 3;
        public static final int ID_SCREENSHOT = 4;
    }
}