package com.beatoflove.standardcalculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Shared result actions; no network, no new permissions. UI-thread only. */
final class CalcActions {
    private static final int BG=0xff0b141d, CARD=0xff142732, FG=0xfff4f8ff, MUTED=0xffb4c9d6;
    private CalcActions() {}
    static int dp(Context a,int n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
    static LinearLayout row(Activity a, Runnable copy, Runnable history){
        LinearLayout bar=new LinearLayout(a);bar.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        bar.addView(button(a,R.drawable.ic_calc_copy,"Copy result",copy),new LinearLayout.LayoutParams(dp(a,48),dp(a,48)));
        bar.addView(button(a,R.drawable.ic_calc_history,"History",history),new LinearLayout.LayoutParams(dp(a,48),dp(a,48)));
        return bar;
    }
    static void add(Activity a, LinearLayout parent,Runnable copy,Runnable history){
        parent.addView(row(a,copy,history),new LinearLayout.LayoutParams(-1,dp(a,48)));
    }
    private static ImageButton button(Activity a,int icon,String label,Runnable action){
        ImageButton b=new ImageButton(a);b.setImageResource(icon);b.setImageTintList(ColorStateList.valueOf(MUTED));
        b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);int pad=dp(a,12);b.setPadding(pad,pad,pad,pad);
        GradientDrawable mask=new GradientDrawable();mask.setColor(0xffffffff);mask.setCornerRadius(dp(a,10));
        b.setBackground(new RippleDrawable(ColorStateList.valueOf(0x334fc3f7),null,mask));
        b.setContentDescription(label);b.setTooltipText(label);b.setFocusable(true);b.setOnClickListener(v->action.run());return b;
    }
    static boolean valid(String s){if(s==null)return false;String v=s.trim().toLowerCase(java.util.Locale.ROOT);return !v.isEmpty()&&!v.equals("—")&&!v.equals("-")&&!v.startsWith("error")&&!v.startsWith("math / syntax error")&&!v.startsWith("invalid")&&!v.startsWith("unavailable")&&!v.equals("nan")&&!v.contains("infinity");}
    static void copy(Activity a,String value){if(!valid(value)){Toast.makeText(a,"No valid result",Toast.LENGTH_SHORT).show();return;}ClipboardManager c=(ClipboardManager)a.getSystemService(Context.CLIPBOARD_SERVICE);if(c!=null){c.setPrimaryClip(ClipData.newPlainText("SiraCalc",value));Toast.makeText(a,"คัดลอกแล้ว",Toast.LENGTH_SHORT).show();}}
    static final class Entry {
        final String label,value;final Runnable recall;
        Entry(String label,String value,Runnable recall){this.label=label;this.value=value;this.recall=recall;}
    }
    static Entry split(String line,String delimiter,Consumer<String> recall){int i=line.lastIndexOf(delimiter);return new Entry(i<0?"":line.substring(0,i),i<0?line:line.substring(i+delimiter.length()),recall==null?null:()->recall.accept(line));}
    static void show(Activity a,String title,List<Entry> entries,Runnable clear){
        ScrollView scroll=new ScrollView(a);LinearLayout content=new LinearLayout(a);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(a,16),dp(a,8),dp(a,16),dp(a,8));scroll.addView(content);content.setBackgroundColor(BG);
        if(entries.isEmpty())content.addView(text(a,"ยังไม่มีประวัติ",16,MUTED));
        final AlertDialog[] dialog=new AlertDialog[1];
        for(Entry e:entries){LinearLayout line=new LinearLayout(a);line.setGravity(Gravity.CENTER_VERTICAL);line.setPadding(dp(a,12),dp(a,8),dp(a,4),dp(a,8));GradientDrawable bg=new GradientDrawable();bg.setColor(CARD);bg.setCornerRadius(dp(a,10));line.setBackground(bg);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(a,6);content.addView(line,lp);
            LinearLayout info=new LinearLayout(a);info.setOrientation(LinearLayout.VERTICAL);line.addView(info,new LinearLayout.LayoutParams(0,-2,1));if(!e.label.isEmpty())info.addView(text(a,e.label,13,MUTED));TextView value=text(a,e.value,18,FG);value.setTypeface(Typeface.DEFAULT,Typeface.BOLD);info.addView(value);
            if(e.recall!=null){info.setFocusable(true);info.setContentDescription(e.label+" "+e.value+" Recall");info.setOnClickListener(v->{dialog[0].dismiss();e.recall.run();});}else{value.setTextIsSelectable(true);}
            line.addView(button(a,R.drawable.ic_calc_copy,"Copy history result",()->copy(a,e.value)),new LinearLayout.LayoutParams(dp(a,48),dp(a,48)));
        }
        AlertDialog.Builder b=new AlertDialog.Builder(a,android.R.style.Theme_Material_Dialog_Alert).setTitle(title).setView(scroll).setPositiveButton("ปิด",null);
        if(!entries.isEmpty())b.setNeutralButton("ล้างประวัติ",(d,w)->new AlertDialog.Builder(a,android.R.style.Theme_Material_Dialog_Alert).setTitle("ล้างประวัติ?").setMessage("ลบเฉพาะประวัติที่แสดงในหน้าต่างนี้ ไม่ล้าง Memory").setPositiveButton("ล้าง",(x,y)->clear.run()).setNegativeButton("ยกเลิก",null).show());
        dialog[0]=b.create();dialog[0].show();
    }
    private static TextView text(Activity a,String s,int size,int color){TextView t=new TextView(a);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(a,2),0,dp(a,2));return t;}
    static final class Log {
        final Activity a;final String key;final SharedPreferences prefs;int revision;
        Log(Activity a,String key){this.a=a;this.key=key;prefs=a.getSharedPreferences("siracalc_history_v536",Context.MODE_PRIVATE);}
        ArrayList<Entry> read(){ArrayList<Entry> list=new ArrayList<>();try{JSONArray rows=new JSONArray(prefs.getString(key,"[]"));for(int i=0;i<rows.length()&&i<100;i++){JSONArray row=rows.getJSONArray(i);list.add(new Entry(row.getString(0),row.getString(1),null));}}catch(Exception ignored){}return list;}
        void record(String label,String value){if(!valid(value))return;ArrayList<Entry> list=read();if(!list.isEmpty()&&list.get(0).label.equals(label)&&list.get(0).value.equals(value))return;JSONArray rows=new JSONArray();rows.put(new JSONArray().put(label).put(value));for(int i=0;i<list.size()&&i<99;i++)rows.put(new JSONArray().put(list.get(i).label).put(list.get(i).value));prefs.edit().putString(key,rows.toString()).apply();}
        void show(String title){CalcActions.show(a,title,read(),()->{revision++;prefs.edit().remove(key).apply();});}
        void copyRecord(String label,String value){if(valid(value))record(label,value);copy(a,value);}
    }
    /** Record a stable automatic conversion, not each intermediate keystroke. */
    static final class AutoRecord {
        final TextView view;final Log log;final Supplier<String> label;Runnable pending;
        AutoRecord(TextView view,Log log,Supplier<String> label){this.view=view;this.log=log;this.label=label;}
        void changed(){if(pending!=null)view.removeCallbacks(pending);final String value=view.getText().toString();if(!valid(value))return;final String context=label.get();final int revision=log.revision;pending=()->{if(revision==log.revision&&view.isAttachedToWindow()&&value.equals(view.getText().toString())&&context.equals(label.get()))log.record(context,value);};view.postDelayed(pending,1200);}
    }
}
