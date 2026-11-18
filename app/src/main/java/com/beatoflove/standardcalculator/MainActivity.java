package com.beatoflove.standardcalculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/** Android portrait adaptation of the user-provided Windows Standard Calculator screenshot. */
public final class MainActivity extends Activity {
    private static final int BACKGROUND = Color.rgb(32, 32, 32);
    private static final int HEADER = Color.rgb(30, 30, 30);
    private static final int TEXT = Color.WHITE;
    private static final int DIM = Color.rgb(155, 155, 155);
    private static final int FUNCTION = Color.rgb(49, 52, 50);
    private static final int DIGIT = Color.rgb(57, 60, 57);
    private static final int BLUE = Color.rgb(76, 194, 255);

    private final CalculatorEngine engine = new CalculatorEngine();
    private TextView resultView;
    private TextView expressionView;
    private TextView memoryClearView;
    private TextView memoryRecallView;
    private TextView memoryListView;
    private boolean fullscreen = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(HEADER);
        getWindow().setNavigationBarColor(HEADER);
        loadSavedData();
        createInterface();
        updateScreen();
    }

    private void createInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BACKGROUND);
        root.setPadding(dp(4), dp(2), dp(4), dp(5));
        setContentView(root);

        // The desktop reference has a Calculator title bar. These window controls
        // are mapped to actual Android actions rather than being nonfunctional art.
        LinearLayout titleBar = horizontal();
        titleBar.setGravity(Gravity.CENTER_VERTICAL);
        titleBar.setBackgroundColor(HEADER);
        root.addView(titleBar, new LinearLayout.LayoutParams(-1, dp(42)));

        TextView appIcon = text("▦", 24, BLUE, false);
        appIcon.setGravity(Gravity.CENTER);
        titleBar.addView(appIcon, new LinearLayout.LayoutParams(dp(47), -1));
        TextView appName = text("Calculator", 14, TEXT, false);
        appName.setGravity(Gravity.CENTER_VERTICAL);
        titleBar.addView(appName, new LinearLayout.LayoutParams(0, -1, 1));
        titleBar.addView(titleAction("−", "ย่อแอป", () -> moveTaskToBack(true)));
        titleBar.addView(titleAction("□", "สลับเต็มจอ", this::toggleFullscreen));
        titleBar.addView(titleAction("×", "ปิดแอป", this::finish));

        LinearLayout modeBar = horizontal();
        modeBar.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(modeBar, new LinearLayout.LayoutParams(-1, dp(56)));
        modeBar.addView(iconAction("☰", "เมนู", this::openMenu, dp(53), 24));
        TextView mode = text("Standard", 24, TEXT, true);
        mode.setGravity(Gravity.CENTER_VERTICAL);
        modeBar.addView(mode, new LinearLayout.LayoutParams(-2, -1));
        modeBar.addView(iconAction("⧉", "คัดลอกผลลัพธ์", this::copyResult, dp(54), 25));
        modeBar.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
        modeBar.addView(iconAction("◴", "ประวัติการคำนวณ", this::openHistory, dp(54), 27));

        LinearLayout resultSpace = new LinearLayout(this);
        resultSpace.setOrientation(LinearLayout.VERTICAL);
        resultSpace.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        resultSpace.setPadding(dp(8), dp(3), dp(13), dp(8));
        root.addView(resultSpace, new LinearLayout.LayoutParams(-1, 0, 1));

        expressionView = text("", 16, DIM, false);
        expressionView.setSingleLine(true);
        expressionView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        resultSpace.addView(expressionView, new LinearLayout.LayoutParams(-1, dp(30)));

        resultView = text("0", 64, TEXT, true);
        resultView.setSingleLine(true);
        resultView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        resultView.setAutoSizeTextTypeUniformWithConfiguration(22, 70, 1, TypedValue.COMPLEX_UNIT_SP);
        resultView.setContentDescription("ผลลัพธ์ 0");
        resultSpace.addView(resultView, new LinearLayout.LayoutParams(-1, dp(92)));

        LinearLayout memory = horizontal();
        root.addView(memory, new LinearLayout.LayoutParams(-1, dp(50)));
        String[] memoryKeys = {"MC", "MR", "M+", "M−", "MS", "M▾"};
        for (String key : memoryKeys) {
            final String k = key;
            TextView b = text(key, 15, TEXT, false);
            b.setGravity(Gravity.CENTER);
            b.setContentDescription(memoryName(key));
            b.setBackground(ripple(BACKGROUND, dp(4)));
            b.setOnClickListener(v -> {
                if ("M▾".equals(k)) { openMemory(); return; }
                if (("MC".equals(k) || "MR".equals(k)) && !engine.hasMemory()) return;
                engine.press(k);
                afterKey();
            });
            memory.addView(b, new LinearLayout.LayoutParams(0, -1, 1));
            if ("MC".equals(key)) memoryClearView = b;
            if ("MR".equals(key)) memoryRecallView = b;
            if ("M▾".equals(key)) memoryListView = b;
        }

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int displayHeightDp = Math.round(metrics.heightPixels / metrics.density);
        int keypadHeight = dp(Math.min(510, Math.max(300, Math.round(displayHeightDp * .60f))));
        LinearLayout keypad = new LinearLayout(this);
        keypad.setOrientation(LinearLayout.VERTICAL);
        root.addView(keypad, new LinearLayout.LayoutParams(-1, keypadHeight));

        String[][] keys = {
                {"%", "CE", "C", "⌫"},
                {"1/x", "x²", "√x", "÷"},
                {"7", "8", "9", "×"},
                {"4", "5", "6", "−"},
                {"1", "2", "3", "+"},
                {"±", "0", ".", "="}
        };
        for (String[] row : keys) {
            LinearLayout keyRow = horizontal();
            keypad.addView(keyRow, new LinearLayout.LayoutParams(-1, 0, 1));
            for (String key : row) {
                final String actual = key;
                int bg = "=".equals(key) ? BLUE : isDigit(key) ? DIGIT : FUNCTION;
                int fg = "=".equals(key) ? Color.BLACK : TEXT;
                String shown = "1/x".equals(key) ? "¹⁄ₓ" : key;
                TextView button = text(shown, "1/x".equals(key) ? 26 : 25, fg, false);
                button.setGravity(Gravity.CENTER);
                button.setContentDescription(buttonName(key));
                button.setClickable(true);
                button.setFocusable(true);
                button.setBackground(ripple(bg, dp(5)));
                button.setOnClickListener(v -> { engine.press(actual); afterKey(); });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1);
                lp.setMargins(dp(1), dp(2), dp(1), dp(2));
                keyRow.addView(button, lp);
            }
        }
    }

    private static boolean isDigit(String key) {
        return "0123456789.".contains(key) && key.length() == 1 || "±".equals(key);
    }

    private String buttonName(String key) {
        switch (key) {
            case "CE": return "ล้างรายการปัจจุบัน";
            case "C": return "ล้างค่าคำนวณ";
            case "⌫": return "ลบทีละหลัก";
            case "%": return "เปอร์เซ็นต์";
            case "1/x": return "หนึ่งส่วนเอ็กซ์";
            case "x²": return "ยกกำลังสอง";
            case "√x": return "รากที่สอง";
            case "÷": return "หาร";
            case "×": return "คูณ";
            case "−": return "ลบ";
            case "+": return "บวก";
            case "±": return "เปลี่ยนเครื่องหมาย";
            case "=": return "เท่ากับ";
            case ".": return "จุดทศนิยม";
            default: return key;
        }
    }

    private String memoryName(String key) {
        switch (key) {
            case "MC": return "ล้างหน่วยความจำ";
            case "MR": return "เรียกค่าจากหน่วยความจำ";
            case "M+": return "เพิ่มค่าเข้าหน่วยความจำ";
            case "M−": return "ลบค่าจากหน่วยความจำ";
            case "MS": return "บันทึกค่าในหน่วยความจำ";
            case "M▾": return "ดูหน่วยความจำ";
            default: return key;
        }
    }

    private void afterKey() { updateScreen(); saveData(); }

    private void updateScreen() {
        resultView.setText(engine.getDisplay());
        expressionView.setText(engine.getExpression());
        resultView.setContentDescription("ผลลัพธ์ " + engine.getDisplay());
        int memColor = engine.hasMemory() ? TEXT : Color.rgb(108,108,108);
        memoryClearView.setTextColor(memColor);
        memoryRecallView.setTextColor(memColor);
        memoryListView.setTextColor(memColor);
    }

    private void copyResult() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText("Calculator result", engine.getDisplay()));
        Toast.makeText(this, "คัดลอกผลลัพธ์แล้ว", Toast.LENGTH_SHORT).show();
    }

    private void openMenu() {
        String[] options = {"Standard", "ล้างประวัติและหน่วยความจำทั้งหมด", "เกี่ยวกับ Beat Calculator"};
        new AlertDialog.Builder(this).setTitle("Calculator").setItems(options, (d, i) -> {
            if (i == 1) {
                new AlertDialog.Builder(this)
                        .setTitle("ล้างข้อมูลทั้งหมด?")
                        .setMessage("ล้างค่าปัจจุบัน ประวัติการคำนวณ และหน่วยความจำที่บันทึกไว้")
                        .setNegativeButton("ยกเลิก", null)
                        .setPositiveButton("ล้างทั้งหมด", (dd, which) -> {
                            engine.clearSavedData(); afterKey();
                        }).show();
            } else if (i == 2) {
                new AlertDialog.Builder(this).setTitle("Beat Calculator — Standard")
                        .setMessage("เครื่องคิดเลข Android แบบออฟไลน์ ออกแบบตามภาพอ้างอิง Windows Calculator\nVersion 2.0")
                        .setPositiveButton("ตกลง", null).show();
            }
        }).show();
    }

    private void openHistory() {
        List<String> saved = engine.getHistory();
        if (saved.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("History")
                    .setMessage("ยังไม่มีประวัติการคำนวณ")
                    .setPositiveButton("ปิด", null).show();
            return;
        }
        CharSequence[] lines = saved.toArray(new CharSequence[0]);
        new AlertDialog.Builder(this)
                .setTitle("History")
                .setItems(lines, (dialog, index) -> {
                    if (engine.useHistoryResult(saved.get(index))) afterKey();
                })
                .setNeutralButton("ล้างประวัติ", (dialog, which) -> { engine.clearHistory(); afterKey(); })
                .setNegativeButton("ปิด", null).show();
    }

    private void openMemory() {
        if (!engine.hasMemory()) {
            new AlertDialog.Builder(this).setTitle("Memory")
                    .setMessage("ยังไม่มีข้อมูลในหน่วยความจำ กด MS เพื่อบันทึกค่าบนหน้าจอ")
                    .setPositiveButton("ปิด", null).show();
            return;
        }
        new AlertDialog.Builder(this).setTitle("Memory")
                .setMessage(engine.getMemory())
                .setPositiveButton("MR — เรียกค่า", (d, which) -> { engine.press("MR"); afterKey(); })
                .setNeutralButton("MC — ล้างค่า", (d, which) -> { engine.press("MC"); afterKey(); })
                .setNegativeButton("ปิด", null).show();
    }

    private void toggleFullscreen() {
        fullscreen = !fullscreen;
        if (fullscreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        Toast.makeText(this, fullscreen ? "เต็มจอ" : "หน้าจอปกติ", Toast.LENGTH_SHORT).show();
    }

    private void loadSavedData() {
        android.content.SharedPreferences prefs = getSharedPreferences("calculator_standard_v2", MODE_PRIVATE);
        engine.restoreMemory(prefs.getString("memory", null));
        try {
            JSONArray a = new JSONArray(prefs.getString("history", "[]"));
            List<String> list = new ArrayList<>();
            for (int i = 0; i < a.length(); i++) list.add(a.optString(i));
            engine.restoreHistory(list);
        } catch (JSONException ignored) { engine.clearHistory(); }
    }

    private void saveData() {
        JSONArray items = new JSONArray();
        for (String line : engine.getHistory()) items.put(line);
        getSharedPreferences("calculator_standard_v2", MODE_PRIVATE)
                .edit()
                .putString("history", items.toString())
                .putString("memory", engine.getMemory())
                .apply();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        String key = null;
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            key = String.valueOf(keyCode - KeyEvent.KEYCODE_0);
        } else if (keyCode >= KeyEvent.KEYCODE_NUMPAD_0 && keyCode <= KeyEvent.KEYCODE_NUMPAD_9) {
            key = String.valueOf(keyCode - KeyEvent.KEYCODE_NUMPAD_0);
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DEL: key = "⌫"; break;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_NUMPAD_ENTER: key = "="; break;
                case KeyEvent.KEYCODE_PLUS:
                case KeyEvent.KEYCODE_NUMPAD_ADD: key = "+"; break;
                case KeyEvent.KEYCODE_NUMPAD_SUBTRACT: key = "−"; break;
                case KeyEvent.KEYCODE_NUMPAD_MULTIPLY: key = "×"; break;
                case KeyEvent.KEYCODE_NUMPAD_DIVIDE: key = "÷"; break;
                case KeyEvent.KEYCODE_PERIOD:
                case KeyEvent.KEYCODE_NUMPAD_DOT: key = "."; break;
                case KeyEvent.KEYCODE_ESCAPE: key = "C"; break;
            }
        }
        if (key != null) { engine.press(key); afterKey(); return true; }
        return super.onKeyDown(keyCode, event);
    }

    private TextView titleAction(String glyph, String desc, Runnable click) {
        TextView b = text(glyph, 20, TEXT, false);
        b.setGravity(Gravity.CENTER);
        b.setContentDescription(desc);
        b.setBackground(ripple(HEADER, dp(3)));
        b.setOnClickListener(v -> click.run());
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(46), -1));
        return b;
    }

    private TextView iconAction(String glyph, String desc, Runnable click, int widthPx, int fontSizeSp) {
        TextView b = text(glyph, fontSizeSp, TEXT, false);
        b.setGravity(Gravity.CENTER);
        b.setContentDescription(desc);
        b.setBackground(ripple(BACKGROUND, dp(4)));
        b.setOnClickListener(v -> click.run());
        b.setLayoutParams(new LinearLayout.LayoutParams(widthPx, -1));
        return b;
    }

    private TextView text(String content, float sizeSp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(content);
        t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        t.setIncludeFontPadding(false);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }
    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }
    private RippleDrawable ripple(int fill, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(radius);
        return new RippleDrawable(ColorStateList.valueOf(Color.rgb(120, 120, 120)), g, null);
    }
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
}
