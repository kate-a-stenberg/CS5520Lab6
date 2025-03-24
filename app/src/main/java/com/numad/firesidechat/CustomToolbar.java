package com.numad.firesidechat;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.numad.firesidechat.databinding.CustomToolbarBinding;

class CustomToolbar extends ConstraintLayout {

    private CustomToolbarBinding binding;
    private AlertDialog dialog;

    public CustomToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        binding = CustomToolbarBinding.inflate(LayoutInflater.from(context), this, true);
        initListeners();
    }

    private void initListeners() {
        binding.user.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    public void setTitle(String title) {
        binding.appName.setText(title);
    }

    public void setAppLogo(Drawable appLogo) {
        binding.appLogo.setImageDrawable(appLogo);
    }

    private void showLogoutDialog() {
        Context context = getContext();
        if (dialog == null) {
            dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.log_out))
                    .setMessage(context.getString(R.string.log_out_message))
                    .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {
                        //TODO: Need to remove the shared pref storing the jwt token
                    })
                    .setNegativeButton(context.getString(R.string.no), null)
                    .create();
        } else if (!dialog.isShowing()) {
            dialog.show();
        } else {
            return;
        }
    }
}