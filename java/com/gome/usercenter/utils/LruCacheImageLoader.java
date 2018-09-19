package com.gome.usercenter.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.gome.usercenter.R;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by dongzq on 2017/8/22.
 * LRU_1(Latest Recently Used) Bitmap memory cache
 * cache size : maxMemory/10
 */

public class LruCacheImageLoader {

    private LruCache<String, Bitmap> mBitmapCache;

    private Set<LruCacheTask> mCacheTasks;

    private static LruCacheImageLoader sLruCacheImageLoader;

    private Context mContext;

    private LruCacheImageLoader(Context context) {
        this.mContext = context.getApplicationContext();
        mCacheTasks = new HashSet();
        long maxMemory = Runtime.getRuntime().maxMemory();
        int cacheSize = (int) (maxMemory / 10);

        mBitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }

            protected Bitmap create(String key) {
                return null;
            }
        };
    }

    public static LruCacheImageLoader newInstance(Context context) {
        if (sLruCacheImageLoader == null) {
            synchronized (LruCacheImageLoader.class) {
                if (sLruCacheImageLoader == null) {
                    sLruCacheImageLoader = new LruCacheImageLoader(context);
                }
            }
        }
        return sLruCacheImageLoader;
    }

    public Bitmap getBitmapFromCache(String key) {
        return (Bitmap) mBitmapCache.get(key);
    }

    public void putBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null) {
            mBitmapCache.put(key, bitmap);
        }
    }

    /**
     * display image form cache if exist in cache otherwise load base64 image to cache and display it
     * @param v target ImageView
     * @param key
     * @param base64 .png encoding in base64 , if null just show default drawable
     * @param defaultResId default drawable id
     */
    public void displayImage(ImageView v, String key, String base64, int defaultResId) {
        Bitmap bitmap = getBitmapFromCache(key);
        if (bitmap != null) {
            v.setImageBitmap(bitmap);
        } else if (base64 == null || base64.isEmpty()){
            v.setImageResource(defaultResId);
        } else {
            displayImageFromBase64(v, key, base64, defaultResId);
        }
    }

    /**
     * load base64 image to cache and display it
     * @param v
     * @param key
     * @param base64
     * @param defaultResId
     */
    public void displayImageFromBase64(ImageView v, String key, String base64, int defaultResId) {
        new LruCacheTask(v, defaultResId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key, base64);
    }

    public void loadBitmapFromBase64ToCache(String key, String base64) {
        new LruCacheTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, key, base64);
    }

    public void removeBitmapFromCache(String key) {
        mBitmapCache.remove(key);
    }

    /**
     * remove all cached bitmap
     */
    public void releaseLruCacheImageLoader() {
        mBitmapCache.evictAll();
    }

    /**
     * cache bitmap from base64 code
     */
    private class LruCacheTask extends AsyncTask<String, Void, Bitmap>{

        private ImageView mImageView;
        private int mDefaultImageId;

        public LruCacheTask(ImageView iv, int id) {
            super();
            mImageView = iv;
            mDefaultImageId = id;
        }
        public LruCacheTask() {
            super();
        }

        protected Bitmap doInBackground(String... params) {

            Bitmap bitmap = loadBitmap(params[1]);
            String key = params[0];
            putBitmapToCache(key, bitmap);
            return bitmap;
        }

        protected void onPostExecute(Bitmap bitmap) {
            if (mImageView == null) {
                return;
            }
            if (bitmap == null) {
                mImageView.setImageResource(mDefaultImageId);
            } else {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

    private Bitmap loadBitmap(String param) {
        String sub = param.substring("data:image/png;base64,".length());
        byte[] bytes = Base64.decode(sub, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }
}
