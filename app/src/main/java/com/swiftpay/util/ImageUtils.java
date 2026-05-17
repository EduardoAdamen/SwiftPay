// com/swiftpay/util/ImageUtils.java
package com.swiftpay.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Utilidades para guardar, cargar y comprimir imágenes.
 * UX-C4: Subir/actualizar foto de perfil.
 */
public final class ImageUtils {

    private ImageUtils() {}

    /**
     * Guarda la imagen de perfil del usuario.
     * Se comprime a 80% JPEG y se almacena en getFilesDir()/images/profiles/user_{id}.jpg.
     *
     * @param context contexto de la aplicación
     * @param userId  ID del usuario
     * @param bitmap  imagen a guardar
     * @return ruta relativa de la imagen guardada, o null si hay error
     */
    public static String saveProfileImage(Context context, long userId, Bitmap bitmap) {
        try {
            File dir = new File(context.getFilesDir(), Constants.PROFILE_IMAGE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = "user_" + userId + ".jpg";
            File file = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_COMPRESSION_QUALITY, fos);
            fos.flush();
            fos.close();

            return Constants.PROFILE_IMAGE_DIR + "/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Carga un Bitmap desde una Uri, validando el tamaño máximo.
     *
     * @param context contexto
     * @param uri     Uri de la imagen seleccionada
     * @return Bitmap cargado o null si excede el tamaño o hay error
     */
    public static Bitmap loadBitmapFromUri(Context context, Uri uri) {
        try {
            // Validar tamaño del archivo antes de cargar
            InputStream sizeStream = context.getContentResolver().openInputStream(uri);
            if (sizeStream != null) {
                int size = sizeStream.available();
                sizeStream.close();
                if (size > Constants.MAX_IMAGE_SIZE_BYTES) {
                    return null; // Archivo demasiado grande
                }
            }

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene el archivo de imagen de perfil de un usuario.
     *
     * @param context contexto
     * @param imagePath ruta relativa almacenada en BD
     * @return File de la imagen o null si no existe
     */
    public static File getImageFile(Context context, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return null;
        File file = new File(context.getFilesDir(), imagePath);
        return file.exists() ? file : null;
    }

    /**
     * Elimina la imagen de perfil de un usuario.
     *
     * @param context contexto
     * @param imagePath ruta relativa
     * @return true si se eliminó correctamente
     */
    public static boolean deleteImage(Context context, String imagePath) {
        if (imagePath == null) return false;
        File file = new File(context.getFilesDir(), imagePath);
        return file.exists() && file.delete();
    }
}
