// app/src/main/java/com/swiftpay/util/ImageLoader.java
package com.swiftpay.util;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.swiftpay.R;
import java.io.File;

/**
 * Centralized image loading wrapper honoring data saver preferences.
 */
public final class ImageLoader {

    private ImageLoader() {
    }

    public static void loadImage(Context context, String url, ImageView imageView, boolean imagesEnabled) {
        if (!imagesEnabled) {
            imageView.setImageResource(R.drawable.ic_image);
            return;
        }
        
        Glide.with(context)
             .load(url)
             .placeholder(R.drawable.ic_image)
             .error(R.drawable.ic_image)
             .into(imageView);
    }

    public static void loadLocalImage(Context context, String relativePath, ImageView imageView, boolean imagesEnabled) {
        if (!imagesEnabled) {
            imageView.setImageResource(R.drawable.ic_image);
            return;
        }

        if (relativePath == null || relativePath.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.ic_image);
            return;
        }

        File imageFile = new File(context.getFilesDir(), relativePath);
        Glide.with(context)
                .load(imageFile)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .centerCrop()
                .into(imageView);
    }
}
