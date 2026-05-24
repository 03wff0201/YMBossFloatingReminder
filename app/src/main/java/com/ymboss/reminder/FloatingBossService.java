package com.ymboss.reminder;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FloatingBossService extends Service {

    private WindowManager wm;
    private View root;
    private WindowManager.LayoutParams params;
    private boolean expanded = false;
    private TextView title;
    private TextView detail;
    private Handler handler = new Handler(Looper.getMainLooper());

    private BossStore.Boss currentTarget;
    private ArrayList<String> skippedKeys = new ArrayList<>();

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refreshTarget(false);
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        createView();
        handler.post(ticker);
    }

    private void createView() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(10), dp(8), dp(10), dp(8));
        box.setBackgroundResource(com.ymboss.reminder.R.drawable.float_bg);

        title = new TextView(this);
        title.setText("Boss");
        title.setTextSize(15);
        title.setTextColor(Color.rgb(217,45,32));
        title.setGravity(Gravity.CENTER);
        box.addView(title);

        detail = new TextView(this);
        detail.setText("点开");
        detail.setTextSize(12);
        detail.setTextColor(Color.DKGRAY);
        box.addView(detail);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.VERTICAL);
        buttons.setVisibility(View.GONE);

        Button kill = new Button(this);
        kill.setText("已击杀并记录");
        buttons.addView(kill);
        kill.setOnClickListener(v -> markKill());

        Button skip = new Button(this);
        skip.setText("跳过");
        buttons.addView(skip);
        skip.setOnClickListener(v -> skipCurrent());

        Button next = new Button(this);
        next.setText("下一个");
        buttons.addView(next);
        next.setOnClickListener(v -> nextTarget());

        Button collapse = new Button(this);
        collapse.setText("收起");
        buttons.addView(collapse);
        collapse.setOnClickListener(v -> setExpanded(false));

        box.addView(buttons);

        box.setOnClickListener(v -> {
            if (!expanded) setExpanded(true);
        });

        final float[] downX = new float[1];
        final float[] downY = new float[1];
        final int[] startX = new int[1];
        final int[] startY = new int[1];

        box.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX[0] = event.getRawX();
                    downY[0] = event.getRawY();
                    startX[0] = params.x;
                    startY[0] = params.y;
                    return false;
                case MotionEvent.ACTION_MOVE:
                    int dx = (int) (event.getRawX() - downX[0]);
                    int dy = (int) (event.getRawY() - downY[0]);
                    if (Math.abs(dx) > 8 || Math.abs(dy) > 8) {
                        params.x = startX[0] + dx;
                        params.y = startY[0] + dy;
                        wm.updateViewLayout(root, params);
                        return true;
                    }
                    return false;
            }
            return false;
        });

        root = box;

        params = new WindowManager.LayoutParams(
                dp(92),
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dp(20);
        params.y = dp(160);

        wm.addView(root, params);
        refreshTarget(true);
    }

    private void setExpanded(boolean value) {
        expanded = value;
        LinearLayout box = (LinearLayout) root;
        View buttons = box.getChildAt(2);
        buttons.setVisibility(expanded ? View.VISIBLE : View.GONE);
        params.width = expanded ? dp(260) : dp(92);
        wm.updateViewLayout(root, params);
        refreshTarget(true);
    }

    private ArrayList<BossStore.Boss> candidates() {
        ArrayList<BossStore.Boss> all = BossStore.loadBosses(this);
        ArrayList<BossStore.Boss> list = new ArrayList<>();

        for (BossStore.Boss b : all) {
            if (skippedKeys.contains(b.key)) continue;
            BossStore.Record r = BossStore.getRecord(this, b.key);
            if (r != null) list.add(b);
        }

        Collections.sort(list, new Comparator<BossStore.Boss>() {
            @Override
            public int compare(BossStore.Boss a, BossStore.Boss b) {
                return rank(a).compareTo(rank(b));
            }
        });
        return list;
    }

    private String rank(BossStore.Boss b) {
        BossStore.Record r = BossStore.getRecord(this, b.key);
        long sec = r == null ? 999999999 : (r.nextMs - System.currentTimeMillis()) / 1000;
        int statusRank;
        if (sec <= 0 && "红怪".equals(b.kind)) statusRank = 0;
        else if (sec <= 300 && "红怪".equals(b.kind)) statusRank = 1;
        else if (sec <= 0) statusRank = 2;
        else if (sec <= 300) statusRank = 3;
        else if ("红怪".equals(b.kind)) statusRank = 4;
        else statusRank = 5;
        return statusRank + "|" + String.format("%010d", Math.max(0, sec)) + "|" + b.map + "|" + b.name + "|" + b.index;
    }

    private void refreshTarget(boolean force) {
        if (currentTarget == null || skippedKeys.contains(currentTarget.key) || force) {
            ArrayList<BossStore.Boss> list = candidates();
            currentTarget = list.isEmpty() ? null : list.get(0);
        }

        if (currentTarget == null) {
            title.setText(expanded ? "暂无巡图目标" : "Boss");
            detail.setText(expanded ? "先在主工具里记录一个 Boss 倒计时" : "暂无");
            return;
        }

        BossStore.Record r = BossStore.getRecord(this, currentTarget.key);
        long sec = r == null ? 0 : (r.nextMs - System.currentTimeMillis()) / 1000;
        String status = sec <= 0 ? "已刷新" : (sec <= 300 ? "即将刷新" : "等待中");
        String remain = BossStore.dur(sec);

        if (expanded) {
            title.setText(currentTarget.map + "｜" + currentTarget.kind);
            detail.setText(currentTarget.name + "\n状态：" + status + "｜剩余：" + remain + "\n手动前往地图，打完点记录。");
        } else {
            title.setText("Boss");
            detail.setText(status + "\n" + remain);
        }
    }

    private void markKill() {
        if (currentTarget == null) {
            refreshTarget(true);
            return;
        }
        BossStore.recordKill(this, currentTarget, "手机悬浮窗-已击杀");
        vibrate();
        skippedKeys.remove(currentTarget.key);
        currentTarget = null;
        refreshTarget(true);
    }

    private void skipCurrent() {
        if (currentTarget != null && !skippedKeys.contains(currentTarget.key)) {
            skippedKeys.add(currentTarget.key);
        }
        currentTarget = null;
        refreshTarget(true);
    }

    private void nextTarget() {
        skipCurrent();
    }

    private void vibrate() {
        try {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) v.vibrate(120);
        } catch (Exception ignored) {}
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (wm != null && root != null) {
            try { wm.removeView(root); } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
