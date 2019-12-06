package com.screen.recorder.utils;

import java.io.File;
import java.util.Comparator;
import java.util.Date;

public class Compare implements Comparator<File> {
    @Override
    public int compare(File item_1, File item_2) {
        Date lastModDate_1 = new Date(item_1.lastModified());
        Date lastModDate_2 = new Date(item_2.lastModified());

        long a = lastModDate_1.getTime();
        long b = lastModDate_2.getTime();

        if (a > b)
            return -1;
        else if (a < b)
            return 1;
        else
            return 0;
    }
}