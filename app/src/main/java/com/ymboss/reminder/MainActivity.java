package com.ymboss.reminder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;

public class MainActivity extends Activity {

    private static final int REQ_OVERLAY = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BossStore.initDefaults(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(20));
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("圆梦三职业 Boss 悬浮提醒");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("\n本工具只做 Boss 刷新时间提醒，不自动点击、不自动跑图、不自动打怪。\n\n使用方法：\n1. 授权悬浮窗权限；\n2. 点启动悬浮窗；\n3. 进游戏后点悬浮窗记录击杀/跳过/下一个目标。");
        desc.setTextSize(15);
        desc.setLineSpacing(4, 1);
        root.addView(desc);

        Button perm = new Button(this);
        perm.setText("1. 授权悬浮窗权限");
        root.addView(perm);
        perm.setOnClickListener(v -> requestOverlayPermission());

        Button start = new Button(this);
        start.setText("2. 启动 Boss 悬浮窗");
        root.addView(start);
        start.setOnClickListener(v -> startFloating());

        Button stop = new Button(this);
        stop.setText("停止悬浮窗");
        root.addView(stop);
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, FloatingBossService.class));
            Toast.makeText(this, "已停止悬浮窗", Toast.LENGTH_SHORT).show();
        });

        TextView note = new TextView(this);
        note.setText("\n提示：如果悬浮窗没有显示，请到系统设置里允许“显示在其他应用上层”。不同品牌手机可能还要允许“后台弹出界面/悬浮窗”。");
        note.setTextSize(13);
        root.addView(note);

        setContentView(root);
    }

    private void requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_OVERLAY);
        } else {
            Toast.makeText(this, "已经有悬浮窗权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void startFloating() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先授权悬浮窗权限", Toast.LENGTH_SHORT).show();
            requestOverlayPermission();
            return;
        }
        startService(new Intent(this, FloatingBossService.class));
        Toast.makeText(this, "悬浮窗已启动，可以切回游戏", Toast.LENGTH_LONG).show();
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
