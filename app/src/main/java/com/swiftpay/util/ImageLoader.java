package com.swiftpay.util;

import android.content.Context;
import android.widget.ImageView;

public class ImageLoader {
    // Basic wrapper to toggle image loading
    public static void loadImage(Context context, String url, ImageView imageView, boolean imagesEnabled) {
        if (!imagesEnabled) {
            // Load a generic placeholder instead of performing network/disk read
            // imageView.setImageResource(R.drawable.ic_placeholder);
            imageView.setImageDrawable(null);
            return;
        }
        
        // Normally use Glide or Picasso
        // Glide.with(context).load(url).into(imageView);
    }
}