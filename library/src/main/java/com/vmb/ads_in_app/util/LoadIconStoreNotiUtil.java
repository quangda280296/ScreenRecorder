package com.vmb.ads_in_app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoadIconStoreNotiUtil extends AsyncTask<String, Integer, Bitmap> {

    private Context context;
    private String title;
    private String message;
    private String url_store;

    public LoadIconStoreNotiUtil(Context context, String title, String message, String url_store) {
        this.context = context;
        this.title = title;
        this.message = message;
        this.url_store = url_store;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        try {
            //Tiến hành tạo đối tượng URL
            URL urlConnection = new URL(urls[0]);

            //Mở kết nối
            HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
            connection.setDoInput(true);
            connection.connect();

            //Đọc dữ liệu
            InputStream input = connection.getInputStream();

            //Convert
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap icon) {
        super.onPostExecute(icon);
        Log.i("LoadBitmapFromURL", "onPostExecute");

        NotificationPushOpenStore notifyDemo = new NotificationPushOpenStore(context, title, message, icon, url_store);
        notifyDemo.addNotify();
    }
}