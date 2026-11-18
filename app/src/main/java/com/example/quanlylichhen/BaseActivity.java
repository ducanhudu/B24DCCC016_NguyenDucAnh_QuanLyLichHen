package com.example.quanlylichhen;

import android.app.Activity;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

public class BaseActivity extends Activity {
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (Build.VERSION.SDK_INT >= 35) {
            View root = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
            int left = root.getPaddingLeft(), top = root.getPaddingTop();
            int right = root.getPaddingRight(), bottom = root.getPaddingBottom();
            root.setOnApplyWindowInsetsListener((view, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(left + insets.left, top + insets.top, right + insets.right, bottom + insets.bottom);
                return windowInsets;
            });
            root.requestApplyInsets();
        }
    }
}
