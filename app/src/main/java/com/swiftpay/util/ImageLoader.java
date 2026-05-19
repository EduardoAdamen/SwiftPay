package com.swiftpay.util;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.swiftpay.R;

public class ImageLoader {
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
}
