package com.screen.recorder.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Utils {

    public static boolean changeName(File from, String newName) {
        if (from.exists()) {
            File to = new File(from.getParentFile().getPath(), newName);
            from.renameTo(to);
            return true;
        }
        return false;
    }

    public static List<File> getPhotoInDirectory(String path) {
        List<File> allFiles = null;
        File folder = new File(path);
        if (folder.exists()) {
            try {
                allFiles = new LinkedList(Arrays.asList(folder.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return (name.endsWith(".JPG")
                                || name.endsWith(".JPEG")
                                || name.endsWith(".PNG")
                                || name.endsWith(".jpg")
                                || name.endsWith(".jpeg")
                                || name.endsWith(".png"));
                    }
                })));

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("getPhotoInDirectory()", "Exception e: " + e.getMessage());
            }
        }
        return allFiles;
    }

    public static List<File> getVideoInDirectory(String path) {
        List<File> allFiles = null;
        File folder = new File(path);
        if (folder.exists()) {
            try {
                allFiles = new LinkedList(Arrays.asList(folder.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return (name.endsWith(".3gp")
                                || name.endsWith(".mp4")
                                || name.endsWith(".m4a")
                                || name.endsWith(".3GP")
                                || name.endsWith(".MP4")
                                || name.endsWith(".M4A"));
                    }
                })));

            } catch (Exception e) {
                e.printStackTrace();
                Log.d("getPhotoInDirectory()", "Exception e: " + e.getMessage());
            }
        }
        return allFiles;
    }

    public static long getDurationFile(String path) {
        long timeInMillisec = 0;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            //use one of overloaded setDataSource() functions to set your data source
            retriever.setDataSource(path);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillisec = Long.parseLong(time);
            retriever.release();
        } catch (Exception e) {

        }
        return timeInMillisec / 1000;
    }

    public static void addPhotoToGallery(Context context, String str) {
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        intent.setData(Uri.fromFile(new File(str)));
        context.sendBroadcast(intent);
    }

    public static Bitmap getThumbnailVideo(String path) {
        //Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(path);
        Bitmap thumb = retriever.getFrameAtTime(500000, MediaMetadataRetriever.OPTION_NEXT_SYNC);
        return thumb;
    }

    public static <T> void sortList(List<T> list, Comparator<? super T> comparator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.sort(comparator);
        } else {
            Collections.sort(list, comparator);
        }
    }
}