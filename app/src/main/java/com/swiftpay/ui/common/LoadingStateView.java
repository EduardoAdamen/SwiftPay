// app/src/main/java/com/swiftpay/ui/common/LoadingStateView.java
package com.swiftpay.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.swiftpay.R;

/**
 * Reusable view that manages Loading / Empty / Error / Content states.
 * UX-A4: every list screen must show feedback while data loads and when there is no data.
 *
 * <p>Usage: include this view alongside the RecyclerView in each list fragment.
 * Call {@link #showLoading()}, {@link #showEmpty(String)}, {@link #showError(String, OnRetryListener)}
 * or {@link #showContent()} from the fragment depending on the adapter/paging load state.</p>
 */
public class LoadingStateView extends FrameLayout {

    private View layoutLoading;
    private View layoutEmpty;
    private View layoutError;
    private TextView tvEmptyMessage;
    private TextView tvErrorMessage;
    private MaterialButton btnRetry;

    public LoadingStateView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public LoadingStateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LoadingStateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_loading_state, this, true);
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutError = findViewById(R.id.layoutError);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRetry = findViewById(R.id.btnRetry);
    }

    /** Shows the indeterminate progress indicator. */
    public void showLoading() {
        setVisibility(VISIBLE);
        layoutLoading.setVisibility(VISIBLE);
        layoutEmpty.setVisibility(GONE);
        layoutError.setVisibility(GONE);
    }

    /** Shows the empty-data placeholder with a custom message. */
    public void showEmpty(@Nullable String message) {
        setVisibility(VISIBLE);
        layoutLoading.setVisibility(GONE);
        layoutEmpty.setVisibility(VISIBLE);
        layoutError.setVisibility(GONE);
        if (message != null && !message.isEmpty()) {
            tvEmptyMessage.setText(message);
        }
    }

    /** Shows the error state with a retry button. */
    public void showError(@Nullable String message, @Nullable OnRetryListener retryListener) {
        setVisibility(VISIBLE);
        layoutLoading.setVisibility(GONE);
        layoutEmpty.setVisibility(GONE);
        layoutError.setVisibility(VISIBLE);
        if (message != null && !message.isEmpty()) {
            tvErrorMessage.setText(message);
        }
        if (retryListener != null) {
            btnRetry.setVisibility(VISIBLE);
            btnRetry.setOnClickListener(v -> retryListener.onRetry());
        } else {
            btnRetry.setVisibility(GONE);
        }
    }

    /** Hides all loading states so the content (RecyclerView) can be shown. */
    public void showContent() {
        setVisibility(GONE);
    }

    /** Callback for the retry action. */
    public interface OnRetryListener {
        void onRetry();
    }
}
