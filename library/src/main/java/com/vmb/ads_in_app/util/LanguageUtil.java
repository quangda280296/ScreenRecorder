package com.vmb.ads_in_app.util;

import java.util.Locale;

public class LanguageUtil {

    public static boolean isVietnamese() {
        String language = Locale.getDefault().getLanguage();
        if (language == null)
            return false;

        if (language.equals("vi"))
            return true;
        else
            return false;
    }
}