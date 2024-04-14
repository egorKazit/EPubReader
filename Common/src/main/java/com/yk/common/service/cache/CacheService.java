package com.yk.common.service.cache;

import android.util.Log;
import android.util.LruCache;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheService {

    private final LruCache<String, byte[]> memoryCache = new LruCache<>((int) (Runtime.getRuntime().maxMemory() / 1024) / 4) {
        @Override
        protected int sizeOf(String key, byte[] bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.length / 1024;
        }
    };

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public byte[] loadBitmapBytes(String id, InputStream inputStream) {
        byte[] bitmap = memoryCache.get(id);
        if (bitmap != null) {
            return bitmap.length > 0 ? bitmap : null;
        } else {
            Log.d("Load", "Loading new");
            if (inputStream == null)
                bitmap = new byte[]{};
            else {
                try {
                    var byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    while ((length = inputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, length);
                    }
                    byteArrayOutputStream.close();
                    inputStream.close();
                    bitmap = byteArrayOutputStream.toByteArray();
                } catch (Exception e) {
                    bitmap = new byte[]{};
                }
            }
            byte[] finalBitmap = bitmap;
            executorService.submit(() -> {
                memoryCache.put(id, finalBitmap);
            });
            return bitmap.length > 0 ? bitmap : null;
        }
    }

    public enum Instance {
        INSTANCE;
        public final CacheService cacheService = new CacheService();
    }

}
