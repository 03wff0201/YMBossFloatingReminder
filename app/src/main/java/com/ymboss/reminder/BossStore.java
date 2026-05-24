package com.ymboss.reminder;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BossStore {
    private static final String SP = "boss_store";
    private static final String KEY_BOSSES = "bosses";
    private static final String KEY_RECORDS = "records";

    public static class Boss {
        public String key;
        public String map;
        public String kind;
        public String name;
        public int index;
        public int refreshMinutes;

        public String display() {
            return map + " - " + kind + (index > 1 ? index : "") + " - " + name;
        }
    }

    public static class Record {
        public String key;
        public long nextMs;
        public String source;
        public long lastUpdateMs;
    }

    public static void initDefaults(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(SP, Context.MODE_PRIVATE);
        if (sp.contains(KEY_BOSSES)) return;

        try {
            JSONArray arr = new JSONArray();

            addMap(arr, "迷雾圣地1层", "至尊·混沌·苍穹法王", 90, "巨峰·将军", 4, 30);
            addMap(arr, "迷雾圣地2层", "至尊·混沌·苍穹法王", 90, "虚空·魔化战神", 4, 30);
            addMap(arr, "潮汐墓地", "至尊·混沌·苍穹法王", 90, "墓地·变异尸王", 4, 30);
            addMap(arr, "雾霭山谷", "至尊·混沌·苍穹法王", 90, "山谷·暗夜君主", 4, 30);
            addMap(arr, "霜火平原", "至尊·诸神黄昏·阿修罗", 90, "霜火·魔君", 4, 30);
            addMap(arr, "废墟城镇", "至尊·诸神黄昏·阿修罗", 90, "王城·镇边将军", 2, 30);
            addMap(arr, "巨人山峰", "至尊·毁灭·暗灭君主", 90, "巨峰·将军", 4, 30);
            addMap(arr, "元素领域", "至尊·毁灭·暗灭君主", 90, "元素·掌控者", 4, 30);
            addMap(arr, "虚空裂缝", "至尊·毁灭·暗灭君主", 90, "虚空·魔化战神", 3, 30);
            addMap(arr, "幻梦沼泽", "至尊·毁灭·暗灭君主", 90, "腐化·树灵", 4, 30);

            sp.edit().putString(KEY_BOSSES, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private static void addMap(JSONArray arr, String map, String redName, int redMin, String purpleName, int purpleCount, int purpleMin) throws Exception {
        JSONObject red = new JSONObject();
        red.put("key", map + "|红怪|1");
        red.put("map", map);
        red.put("kind", "红怪");
        red.put("name", redName);
        red.put("index", 1);
        red.put("refreshMinutes", redMin);
        arr.put(red);

        for (int i = 1; i <= purpleCount; i++) {
            JSONObject p = new JSONObject();
            p.put("key", map + "|紫怪|" + i);
            p.put("map", map);
            p.put("kind", "紫怪");
            p.put("name", purpleName);
            p.put("index", i);
            p.put("refreshMinutes", purpleMin);
            arr.put(p);
        }
    }

    public static ArrayList<Boss> loadBosses(Context ctx) {
        ArrayList<Boss> list = new ArrayList<>();
        try {
            SharedPreferences sp = ctx.getSharedPreferences(SP, Context.MODE_PRIVATE);
            JSONArray arr = new JSONArray(sp.getString(KEY_BOSSES, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Boss b = new Boss();
                b.key = o.getString("key");
                b.map = o.getString("map");
                b.kind = o.getString("kind");
                b.name = o.getString("name");
                b.index = o.optInt("index", 1);
                b.refreshMinutes = o.optInt("refreshMinutes", 60);
                list.add(b);
            }
        } catch (Exception ignored) {}
        return list;
    }

    private static JSONObject loadRecordsJson(Context ctx) {
        try {
            SharedPreferences sp = ctx.getSharedPreferences(SP, Context.MODE_PRIVATE);
            return new JSONObject(sp.getString(KEY_RECORDS, "{}"));
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static void saveRecordsJson(Context ctx, JSONObject obj) {
        ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).edit().putString(KEY_RECORDS, obj.toString()).apply();
    }

    public static Record getRecord(Context ctx, String key) {
        try {
            JSONObject all = loadRecordsJson(ctx);
            if (!all.has(key)) return null;
            JSONObject o = all.getJSONObject(key);
            Record r = new Record();
            r.key = key;
            r.nextMs = o.getLong("nextMs");
            r.source = o.optString("source", "--");
            r.lastUpdateMs = o.optLong("lastUpdateMs", 0);
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    public static void recordKill(Context ctx, Boss boss, String source) {
        long now = System.currentTimeMillis();
        long next = now + boss.refreshMinutes * 60L * 1000L;
        putRecord(ctx, boss, next, source);
    }

    public static void putRecord(Context ctx, Boss boss, long nextMs, String source) {
        try {
            JSONObject all = loadRecordsJson(ctx);
            JSONObject o = new JSONObject();
            o.put("key", boss.key);
            o.put("nextMs", nextMs);
            o.put("source", source);
            o.put("lastUpdateMs", System.currentTimeMillis());
            all.put(boss.key, o);
            saveRecordsJson(ctx, all);
        } catch (Exception ignored) {}
    }

    public static String fmtTime(long ms) {
        return new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(new Date(ms));
    }

    public static String dur(long sec) {
        if (sec < 0) sec = 0;
        long h = sec / 3600;
        long m = (sec % 3600) / 60;
        long s = sec % 60;
        return String.format(Locale.CHINA, "%02d:%02d:%02d", h, m, s);
    }
}
