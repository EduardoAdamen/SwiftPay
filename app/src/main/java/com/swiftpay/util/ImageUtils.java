// app/src/main/java/com/swiftpay/util/ImageUtils.java
package com.swiftpay.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Utilities for validating, compressing and storing user-selected images.
 */
public final class ImageUtils {

    private ImageUtils() {
    }

    /** Saves a profile image as 80 percent JPEG after validation. */
    public static String saveProfileImage(Context context, long userId, Bitmap bitmap) {
        try {
            File dir = new File(context.getFilesDir(), Constants.PROFILE_IMAGE_DIR);
            ensureDirectory(dir);
            String fileName = "user_" + userId + ".jpg";
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_COMPRESSION_QUALITY, fos);
            }
            return Constants.PROFILE_IMAGE_DIR + "/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Saves a product image as 80 percent JPEG after validation. */
    public static String saveProductImage(Context context, Bitmap bitmap) {
        try {
            File dir = new File(context.getFilesDir(), Constants.PRODUCT_IMAGE_DIR);
            ensureDirectory(dir);
            String fileName = "product_" + System.currentTimeMillis() + ".jpg";
            File file = new File(dir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_COMPRESSION_QUALITY, fos);
            }
            return Constants.PRODUCT_IMAGE_DIR + "/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a bitmap only when the source size is known and below 5 MB.
     *
     * @param context Android context
     * @param uri selected image Uri
     * @return decoded bitmap or null when invalid/oversized
     */
    public static Bitmap loadBitmapFromUri(Context context, Uri uri) {
        try {
            long imageSize = resolveContentLength(context, uri);
            if (imageSize <= 0 || imageSize > Constants.MAX_IMAGE_SIZE_BYTES) {
                return null;
            }

            try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                return inputStream == null ? null : BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Returns a private image file or null if it does not exist. */
    public static File getImageFile(Context context, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        File file = new File(context.getFilesDir(), imagePath);
        return file.exists() ? file : null;
    }

    /** Deletes a private image file. */
    public static boolean deleteImage(Context context, String imagePath) {
        if (imagePath == null) {
            return false;
        }
        File file = new File(context.getFilesDir(), imagePath);
        return file.exists() && file.delete();
    }

    private static void ensureDirectory(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("No se pudo crear el directorio de imagenes.");
        }
    }

    private static long resolveContentLength(Context context, Uri uri) throws Exception {
        try (Cursor cursor = context.getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0) {
                    long size = cursor.getLong(sizeIndex);
                    if (size > 0) {
                        return size;
                    }
                }
            }
        }

        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            if (stream == null) {
                return -1L;
            }
            long total = 0L;
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                total += read;
                if (total > Constants.MAX_IMAGE_SIZE_BYTES) {
                    return total;
                }
            }
            return total;
        }
    }
}
