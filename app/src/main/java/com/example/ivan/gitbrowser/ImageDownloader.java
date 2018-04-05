package com.example.ivan.gitbrowser;
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * This helper class download images from the Internet and binds those with the
 * provided ImageView.
 * <p>
 * <p>
 * It requires the INTERNET permission, which should be added to your
 * application's manifest file.
 * </p>
 * <p>
 * A local cache of downloaded images is maintained internally to improve
 * performance.
 */
class ImageDownloader {
    private static final String LOG_TAG = ImageDownloader.class.getSimpleName();
    private BitmapDownloaderTask task;

    /**
     * Download the specified image from the Internet and binds it to the
     * provided ImageView. The binding is immediate if the image is found in the
     * cache and will be done asynchronously otherwise. A null bitmap will be
     * associated to the ImageView if an error occurs.
     *
     * @param url       The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void download(String url, ImageView imageView) {

        resetPurgeTimer();
        Bitmap bitmap = getBitmapFromCache(url);

        if (bitmap == null) {
            forceDownload(url, imageView);
        } else {
            cancelPotentialDownload(url, imageView);
            imageView.setImageBitmap(bitmap);
        }
    }


    /**
     * Same as download but the image is always downloaded and the cache is not
     * used. Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, ImageView imageView) {
        // State sanity: url is guaranteed to never be null in
        // DownloadedDrawable and cache keys.
        if (url == null) {
            imageView.setImageDrawable(null);
            return;
        }

        if (cancelPotentialDownload(url, imageView)) {
            task = new BitmapDownloaderTask(imageView);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);
            task.execute(url);
        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no
     * download in progress on this image view. Returns false if the download in
     * progress deals with the same url. The download is not stopped in that
     * case.
     */
    private static boolean cancelPotentialDownload(String url,
                                                   ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated
     * with this imageView. null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(
            ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static byte[] getBytesFromIs(InputStream is)
    {
        try {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
                byte[] buffer = new byte[1024];
                for (int len = 0; (len = is.read(buffer)) != -1;) {
                    os.write(buffer, 0, len);
                }
                os.flush();
                return os.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private static Bitmap decodeSampledBitmapFromStream(InputStream res,
                                                        int reqWidth, int reqHeight) {
        byte[] byteRes = getBytesFromIs(res);
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if(byteRes != null)
        BitmapFactory.decodeByteArray(byteRes, 0,byteRes.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        if(byteRes != null)
        return  BitmapFactory.decodeByteArray(byteRes, 0,byteRes.length, options);
        else return null;
    }

    private Bitmap downloadBitmap(URL url, int width, int height) throws IOException {

        InputStream stream = null;
        HttpsURLConnection connection = null;
         try {
            connection = (HttpsURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.
            connection.setDoInput(true);
            // Open communications link (network traffic occurs here).
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            if (stream != null) {
                // Converts Stream to Bitmap .
                return   ImageDownloader.decodeSampledBitmapFromStream(
                        stream,width,height);

            }
        } catch (IOException e) {
            Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
        } catch (IllegalStateException e) {
            Log.w(LOG_TAG, "Incorrect URL: " + url);
        } catch (Exception e) {
            Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;

    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    @SuppressLint("StaticFieldLeak")
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;
        private int width =  96;
        private int height = 96;

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            String urlString = params[0];
            try {
                URL url = new URL(urlString);
                return downloadBitmap(url,width,height);
            } catch (Exception e) {
                return null;
            }


        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            addBitmapToCache(url, bitmap);

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with
                // it
                // Or if we don't use any bitmap to task association
                // (NO_DOWNLOADED_DRAWABLE mode)
                if (this == bitmapDownloaderTask) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    /**
     * A fake Drawable that will be attached to the imageView while the download
     * is in progress.
     * <p>
     * <p>
     * Contains a reference to the actual download task, so that a download task
     * can be stopped if a new binding is required, and makes sure that only the
     * last started download process can bind its result, independently of the
     * download finish order.
     * </p>
     */
    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
            super(Color.BLACK);
            bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(
                    bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

  /*
   * Cache-related fields and methods.
   *
   * We use a hard and a soft cache. A soft reference cache is too
   * aggressively cleared by the Garbage Collector.
   */

    private static final int HARD_CACHE_CAPACITY = 20;
    private static final int DELAY_BEFORE_PURGE = 120 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, Bitmap> sHardBitmapCache = new LinkedHashMap<String, Bitmap>(
            HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(
                LinkedHashMap.Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to
                // soft reference cache
                sSoftBitmapCache.put(eldest.getKey(),
                        new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(
            HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };

    /**
     * Adds this bitmap to the cache.
     *
     * @param bitmap The newly downloaded bitmap.
     */
    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(url, bitmap);
            }
        }
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url) {
        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(url);
            }
        }

        return null;
    }

    /**
     * Clears the image cache used internally to improve performance. Note that
     * for memory efficiency reasons, the cache will automatically be cleared
     * after a certain inactivity delay.
     */
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }
}
