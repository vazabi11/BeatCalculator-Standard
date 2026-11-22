package com.beatoflove.standardcalculator;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

/** Scoped to Currency and Converter: content measures itself, scrolls only on overflow.
 * No changes to shared calculator keypad sizing or market/financial panels. */
class ResponsiveForm extends PanelUi {
    ResponsiveForm(Activity activity) { super(activity); }
    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    @Override LinearLayout screen(LinearLayout parent, String title, String desc) {
        parent.removeAllViews();
        ScrollView scroll = new ScrollView(a);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(true);
        parent.addView(scroll, new LinearLayout.LayoutParams(MATCH, MATCH));
        LinearLayout content = col();
        content.setPadding(dp(12), dp(4), dp(12), dp(8));
        content.setFocusableInTouchMode(true);
        scroll.addView(content, new ScrollView.LayoutParams(MATCH, WRAP));
        if (!title.isEmpty()) content.addView(compactText(title, 20, WHITE, true));
        if (!desc.isEmpty()) content.addView(compactText(desc, 12, MUTE, false));
        content.requestFocus();
        return content;
    }
    TextView compactText(String s, int size, int color, boolean bold) {
        TextView t = text(s, size, color, bold);
        t.setPadding(dp(3), dp(2), dp(3), dp(2));
        return t;
    }
    @Override Spinner spin(LinearLayout l, String name, String[] options) {
        l.addView(compactText(name, 12, MUTE, false));
        Spinner s = new Spinner(a);
        s.setAdapter(options(options));
        s.setBackgroundTintList(ColorStateList.valueOf(MUTE));
        s.setPopupBackgroundDrawable(bg(FUNC));
        s.setMinimumHeight(dp(48));
        GradientDrawable border = bg(FUNC);
        border.setStroke(dp(1), 0xff304c5c);
        LinearLayout frame = col(); frame.setBackground(border);
        frame.addView(s, new LinearLayout.LayoutParams(MATCH, WRAP));
        addField(l, frame);
        return s;
    }
    ArrayAdapter<String> options(String[] values) {
        return new ArrayAdapter<String>(a, android.R.layout.simple_spinner_item, values) {
            private TextView item(int p, boolean drop) {
                TextView t = new TextView(a);
                t.setText(getItem(p)); t.setTextSize(16); t.setTextColor(WHITE);
                t.setSingleLine(false); t.setGravity(Gravity.CENTER_VERTICAL);
                t.setPadding(dp(8), dp(4), dp(8), dp(4));
                t.setMinimumHeight(dp(48));
                if (drop) t.setBackgroundColor(FUNC);
                return t;
            }
            @Override public View getView(int p, View v, ViewGroup g) { return item(p, false); }
            @Override public View getDropDownView(int p, View v, ViewGroup g) { return item(p, true); }
        };
    }
    @Override EditText input(LinearLayout l, String title, String value, boolean free) {
        l.addView(compactText(title, 12, MUTE, false));
        EditText e = new EditText(a);
        e.setTextColor(WHITE); e.setTextSize(20); e.setBackground(bg(FUNC));
        e.setPadding(dp(12), dp(4), dp(12), dp(4));
        e.setInputType(free ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                : InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        if (!free) { e.setSingleLine(true); e.setSelectAllOnFocus(true); }
        e.setMinimumHeight(dp(48)); e.setText(value);
        addField(l, e); return e;
    }
    @Override TextView result(LinearLayout l) {
        TextView t = compactText("—", 26, BLUE, true);
        t.setBackground(bg(CARD)); t.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        t.setPadding(dp(10), dp(6), dp(10), dp(6)); t.setMinimumHeight(dp(56));
        t.setSingleLine(false); t.setTextIsSelectable(true);
        addField(l, t); return t;
    }
    TextView formButton(String title, Runnable action, boolean primary) {
        TextView t = key(title, primary ? BLUE : FUNC, primary ? BG : WHITE, action);
        t.setMinimumHeight(dp(48)); t.setPadding(dp(6), dp(5), dp(6), dp(5));
        t.setSingleLine(false); return t;
    }
    @Override void action(LinearLayout l, String title, Runnable action) { addField(l, formButton(title, action, true)); }
    void addField(LinearLayout l, View v) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH, WRAP);
        lp.bottomMargin = dp(3); l.addView(v, lp);
    }
    /** Measured width, not device model. Stack when space or accessibility font requires it. */
    LinearLayout adaptivePair(LinearLayout l, int minWidthDp) {
        LinearLayout pair = new LinearLayout(a) {
            @Override protected void onMeasure(int w, int h) {
                int widthDp = (int)(View.MeasureSpec.getSize(w) / getResources().getDisplayMetrics().density);
                boolean side = UiPolicy.sideBySide(widthDp, getResources().getConfiguration().fontScale, minWidthDp);
                int orientation = side ? HORIZONTAL : VERTICAL;
                if (getOrientation() != orientation) setOrientation(orientation);
                for (int i = 0; i < getChildCount(); i++) {
                    View v = getChildAt(i);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)v.getLayoutParams();
                    lp.width = side ? 0 : MATCH; lp.height = WRAP; lp.weight = side ? 1 : 0;
                    lp.setMargins(side && i > 0 ? dp(5) : 0, 0, 0, side ? 0 : dp(3));
                }
                super.onMeasure(w, h);
            }
        };
        pair.setGravity(Gravity.CENTER_VERTICAL); addField(l, pair); return pair;
    }
    void pairChild(LinearLayout pair, View child) {
        pair.addView(child, new LinearLayout.LayoutParams(MATCH, WRAP));
    }
}
