package com.beatoflove.standardcalculator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import org.json.JSONArray;
import org.json.JSONException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Native calculator with seven modes and offline last-known-good market prices. */
public final class MainActivity extends Activity {
    private static final int BG=0xff202020, HEADER=0xff1e1e1e, WHITE=Color.WHITE, MUTED=0xff9b9b9b;
    private static final int FUNCTION=0xff313432, DIGIT=0xff393c39, BLUE=0xff4cc2ff, WARNING=0xffffba57;
    private static final String[] MODES={"Basic","Standard","Advance","Science","แปลงหน่วย","ราคาน้ำมัน","ราคาโลหะ"};
    private static final String[] METALS={"ทองคำ (Gold / XAU)","เงิน (Silver / XAG)","แพลทินัม (Platinum / XPT)","พัลลาเดียม (Palladium / XPD)"};
    private static final String[] SYMBOLS={"XAU","XAG","XPT","XPD"};
    private final CalculatorEngine engine=new CalculatorEngine();
    private LinearLayout root, page, tabsRow;
    private HorizontalScrollView tabsScroll, resultScroll;
    private TextView result, expr, hint, memoryClear, memoryRecall, memoryArrow;
    private int currentMode=1, fetchToken=0;
    private boolean full=false, updatingUnit=false;
    private String selectedMetal="XAU";
    private static final String[] FUEL_BRANDS={"เปรียบเทียบ","PTT OR","บางจาก","Shell"};
    private static final String[] FUEL_TYPES={"ทั้งหมด","Gasohol 91","Gasohol 95","E20","E85","Diesel"};
    private Spinner fuelBrand, goldMarket, metalPicker;
    private OilPriceParser.Feed latestPtt;
    private String pttProblem,bangchakProblem;
    private boolean pttCached,bangchakCached;
    private long pttCachedAt,bangchakCachedAt;
    private boolean pttFinished,bangchakFinished;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable periodic= new Runnable(){@Override public void run(){
        if(currentMode==5){loadFuel();handler.postDelayed(this,15*60*1000L);}
        else if(currentMode==6){loadMetal();handler.postDelayed(this,90*1000L);}
    }};
    private LinearLayout liveResults;
    private TextView liveStatus;
    private Spinner unitCategory,unitFrom,unitTo,fuelFilter;
    private PriceService.FuelData latestFuel;
    private EditText unitInput,unitRate;
    private TextView unitFromLabel,unitToLabel,unitRateLabel,unitSwap;
    private TextView unitOutput,unitHelp;

    @Override public void onCreate(Bundle saved){super.onCreate(saved);getWindow().setStatusBarColor(HEADER);
        getWindow().setNavigationBarColor(HEADER);restore();buildShell();selectMode(1);}
    @Override protected void onPause(){handler.removeCallbacks(periodic);super.onPause();}
    @Override protected void onResume(){super.onResume();if(root!=null&&currentMode>=5)startPolling();}
    @Override protected void onDestroy(){handler.removeCallbacks(periodic);fetchToken++;super.onDestroy();}

    private void buildShell(){
        root=vertical();root.setBackgroundColor(BG);root.setPadding(dp(4),dp(2),dp(4),dp(5));setContentView(root);
        LinearLayout bar=horizontal();bar.setGravity(Gravity.CENTER_VERTICAL);bar.setBackgroundColor(HEADER);
        root.addView(bar,new LinearLayout.LayoutParams(-1,dp(42)));
        TextView icon=label("▦",24,BLUE,false);icon.setGravity(Gravity.CENTER);bar.addView(icon,new LinearLayout.LayoutParams(dp(47),-1));
        TextView app=label("Calculator",14,WHITE,false);app.setGravity(Gravity.CENTER_VERTICAL);bar.addView(app,new LinearLayout.LayoutParams(0,-1,1));
        bar.addView(topButton("−",()->moveTaskToBack(true)));
        bar.addView(topButton("□",this::toggleFull));
        bar.addView(topButton("×",this::finish));
        tabsScroll=new HorizontalScrollView(this);tabsScroll.setHorizontalScrollBarEnabled(false);tabsScroll.setFillViewport(false);
        root.addView(tabsScroll,new LinearLayout.LayoutParams(-1,dp(52)));
        tabsRow=horizontal();tabsRow.setGravity(Gravity.CENTER_VERTICAL);tabsScroll.addView(tabsRow);
        page=vertical();root.addView(page,new LinearLayout.LayoutParams(-1,0,1));
    }
    private void selectMode(int mode){
        if(mode<0||mode>=MODES.length)return;
        fetchToken++;handler.removeCallbacks(periodic);currentMode=mode;
        tabsRow.removeAllViews();
        for(int i=0;i<MODES.length;i++){
            final int n=i;
            TextView tab=label(MODES[i],i==mode?16:14,i==mode?BLUE:WHITE,i==mode);
            tab.setPadding(dp(11),0,dp(11),0);tab.setGravity(Gravity.CENTER);tab.setBackground(ripple(i==mode?FUNCTION:BG,dp(8)));
            tab.setOnClickListener(v->selectMode(n));tabsRow.addView(tab,new LinearLayout.LayoutParams(-2,dp(40)));
            if(i==mode){tabsScroll.post(()->tabsScroll.smoothScrollTo(Math.max(0,tab.getLeft()-dp(60)),0));}
        }
        page.removeAllViews();result=null;expr=null;resultScroll=null;liveResults=null;liveStatus=null;
        memoryClear=null;memoryRecall=null;memoryArrow=null;fuelFilter=null;fuelBrand=null;
        goldMarket=null;metalPicker=null;latestFuel=null;latestPtt=null;pttProblem=null;bangchakProblem=null;
        pttFinished=false;bangchakFinished=false;
        pttCached=false;bangchakCached=false;pttCachedAt=0;bangchakCachedAt=0;
        if(mode<=3)buildCalculator(mode);
        else if(mode==4)buildUnits();
        else if(mode==5)buildPrice(true);
        else buildPrice(false);
        if(mode>=5)startPolling();
    }
    private void buildCalculator(int mode){
        LinearLayout modeBar=horizontal();modeBar.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(modeBar,new LinearLayout.LayoutParams(-1,dp(54)));
        modeBar.addView(action("☰",dp(52),()->showModeDialog()));
        TextView modeTitle=label(MODES[mode],24,WHITE,true);modeTitle.setGravity(Gravity.CENTER_VERTICAL);
        modeBar.addView(modeTitle,new LinearLayout.LayoutParams(-2,-1));
        modeBar.addView(action("⧉",dp(50),this::copyResult));
        modeBar.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
        modeBar.addView(action("◴",dp(52),this::showHistory));
        LinearLayout display=vertical();display.setGravity(Gravity.BOTTOM|Gravity.RIGHT);display.setPadding(dp(8),dp(3),dp(13),dp(4));
        page.addView(display,new LinearLayout.LayoutParams(-1,0,1));
        expr=label("",16,MUTED,false);expr.setSingleLine(true);expr.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        display.addView(expr,new LinearLayout.LayoutParams(-1,dp(30)));
        resultScroll=new HorizontalScrollView(this);resultScroll.setHorizontalScrollBarEnabled(false);resultScroll.setFillViewport(true);
        resultScroll.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        display.addView(resultScroll,new LinearLayout.LayoutParams(-1,dp(85)));
        result=label("0",64,WHITE,true);result.setSingleLine(true);result.setTextDirection(View.TEXT_DIRECTION_LTR);
        result.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);result.setOnClickListener(v->showFullResult());
        resultScroll.addView(result,new HorizontalScrollView.LayoutParams(-2,-1));
        hint=label("↔ ปัดซ้ายเพื่อดูตัวเลขที่เหลือ • แตะเพื่อดูทั้งหมด",10,MUTED,false);
        hint.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);display.addView(hint,new LinearLayout.LayoutParams(-1,dp(17)));
        if(mode>=1){
            LinearLayout memories=horizontal();page.addView(memories,new LinearLayout.LayoutParams(-1,dp(48)));
            for(String k:new String[]{"MC","MR","M+","M−","MS","M▾"}){
                TextView b=label(k,15,WHITE,false);b.setGravity(Gravity.CENTER);b.setBackground(ripple(BG,dp(5)));
                b.setOnClickListener(v->{if("M▾".equals(k))showMemory();else {engine.press(k);afterKey();}});
                memories.addView(b,new LinearLayout.LayoutParams(0,-1,1));
                if("MC".equals(k))memoryClear=b;if("MR".equals(k))memoryRecall=b;if("M▾".equals(k))memoryArrow=b;
            }
        }
        if(mode==3){
            LinearLayout options=horizontal();page.addView(options,new LinearLayout.LayoutParams(-1,dp(35)));
            TextView deg=label(engine.isDegreeMode()?"DEG  (แตะเพื่อเปลี่ยน RAD)":"RAD  (แตะเพื่อเปลี่ยน DEG)",12,BLUE,false);
            deg.setGravity(Gravity.CENTER_VERTICAL);deg.setPadding(dp(12),0,0,0);
            deg.setOnClickListener(v->{engine.setDegreeMode(!engine.isDegreeMode());deg.setText(engine.isDegreeMode()?"DEG  (แตะเพื่อเปลี่ยน RAD)":"RAD  (แตะเพื่อเปลี่ยน DEG)");});
            options.addView(deg,new LinearLayout.LayoutParams(-1,-1));
        }
        String[][] keys;
        if(mode==0)keys=new String[][]{
            {"C","CE","⌫","÷"},{"7","8","9","×"},{"4","5","6","−"},{"1","2","3","+"},{"±","0",".","="}
        };
        else if(mode==1)keys=new String[][]{
            {"%","CE","C","⌫"},{"1/x","x²","√x","÷"},{"7","8","9","×"},{"4","5","6","−"},{"1","2","3","+"},{"±","0",".","="}
        };
        else if(mode==2)keys=new String[][]{
            {"%","CE","C","⌫"},{"x²","√x","1/x","÷"},{"^","mod","cube","abs"},
            {"7","8","9","×"},{"4","5","6","−"},{"1","2","3","+"},{"±","0",".","="}
        };
        else keys=new String[][]{
            {"sin","cos","tan","π","C"},{"asin","acos","atan","e","⌫"},
            {"ln","log","exp","10^x","÷"},{"n!","x²","√x","^","×"},
            {"7","8","9","mod","−"},{"4","5","6","1/x","+"},
            {"1","2","3","%","CE"},{"±","0",".","00","="}
        };
        DisplayMetrics screen=getResources().getDisplayMetrics();
        int heightDp=Math.round(screen.heightPixels/screen.density);
        int dpHeight=Math.min(mode==3?530:510,Math.max(mode==3?350:290,Math.round(heightDp*(mode==3?.57f:.55f))));
        LinearLayout keypad=vertical();page.addView(keypad,new LinearLayout.LayoutParams(-1,dp(dpHeight)));
        for(String[] keysRow:keys){
            LinearLayout row=horizontal();keypad.addView(row,new LinearLayout.LayoutParams(-1,0,1));
            for(String k:keysRow){
                TextView b=label("1/x".equals(k)?"¹⁄ₓ":k,mode==3?18:25,"=".equals(k)?Color.BLACK:WHITE,false);
                int fill="=".equals(k)?BLUE:((k.length()==1&&"0123456789.".contains(k))||"00".equals(k)||"±".equals(k)?DIGIT:FUNCTION);
                b.setGravity(Gravity.CENTER);b.setClickable(true);b.setFocusable(true);b.setBackground(ripple(fill,dp(5)));
                b.setOnClickListener(v->{
                    if("00".equals(k)){engine.press("0");engine.press("0");}
                    else if("cube".equals(k))engine.press("cube");
                    else engine.press(k);
                    afterKey();
                });
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(dp(1),dp(2),dp(1),dp(2));row.addView(b,lp);
            }
        }
        updateScreen();
    }
    private void updateScreen(){
        if(result==null)return;
        String v=engine.getDisplay();result.setText(v);expr.setText(engine.getExpression());
        result.setContentDescription("ผลลัพธ์ "+v+" แตะเพื่อดูทั้งหมด");
        resultScroll.post(()->fitResult(v));
        if(memoryClear!=null){int c=engine.hasMemory()?WHITE:0xff707070;memoryClear.setTextColor(c);memoryRecall.setTextColor(c);memoryArrow.setTextColor(c);}
    }
    private void fitResult(String expected){
        if(result==null||resultScroll==null||!expected.contentEquals(result.getText()))return;
        int width=resultScroll.getWidth()-dp(2);if(width<=0)return;
        DisplayMetrics dm=getResources().getDisplayMetrics();
        float max=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,66,dm);
        float min=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,27,dm);
        Paint p=new Paint(result.getPaint());p.setTextSize(max);
        float natural=p.measureText(expected), size=natural<=width?max:Math.max(min,max*width/natural);
        result.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);p.setTextSize(size);
        boolean overflow=p.measureText(expected)>width;
        result.setGravity((overflow?Gravity.LEFT:Gravity.RIGHT)|Gravity.CENTER_VERTICAL);
        hint.setVisibility(overflow?View.VISIBLE:View.INVISIBLE);
        resultScroll.scrollTo(0,0);resultScroll.post(()->resultScroll.scrollTo(0,0));
    }
    private void afterKey(){updateScreen();persist();}
    private void showFullResult(){new AlertDialog.Builder(this).setTitle("ผลลัพธ์ทั้งหมด").setMessage(engine.getDisplay())
            .setPositiveButton("คัดลอก",(d,w)->copyResult()).setNegativeButton("ปิด",null).show();}
    private void copyResult(){copy(engine.getDisplay());}
    private void copy(String value){ClipboardManager cb=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if(cb!=null)cb.setPrimaryClip(ClipData.newPlainText("Beat Calculator",value));
        Toast.makeText(this,"คัดลอกแล้ว",Toast.LENGTH_SHORT).show();}
    private void showHistory(){List<String> lines=engine.getHistory();if(lines.isEmpty()){
        new AlertDialog.Builder(this).setTitle("History").setMessage("ยังไม่มีประวัติ").setPositiveButton("ปิด",null).show();return;}
        new AlertDialog.Builder(this).setTitle("History").setItems(lines.toArray(new String[0]),(d,i)->{
            if(engine.useHistoryResult(lines.get(i)))afterKey();})
            .setNeutralButton("ล้างประวัติ",(d,w)->{engine.clearHistory();afterKey();})
            .setNegativeButton("ปิด",null).show();}
    private void showMemory(){if(!engine.hasMemory()){
        new AlertDialog.Builder(this).setTitle("Memory").setMessage("ยังไม่มีข้อมูล กด MS เพื่อบันทึก")
                .setPositiveButton("ปิด",null).show();return;}
        new AlertDialog.Builder(this).setTitle("Memory").setMessage(engine.getMemory())
            .setPositiveButton("MR",(d,w)->{engine.press("MR");afterKey();})
            .setNeutralButton("MC",(d,w)->{engine.press("MC");afterKey();})
            .setNegativeButton("ปิด",null).show();}
    private void showModeDialog(){new AlertDialog.Builder(this).setTitle("เลือกรูปแบบ")
        .setItems(MODES,(d,i)->selectMode(i))
        .setNeutralButton("ล้างประวัติและ Memory",(d,i)->new AlertDialog.Builder(this)
            .setTitle("ล้างข้อมูลทั้งหมด?").setNegativeButton("ยกเลิก",null)
            .setPositiveButton("ล้าง",(dd,j)->{engine.clearSavedData();afterKey();}).show()).show();}

    private void buildUnits(){
        ScrollView scroll=new ScrollView(this);page.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout container=vertical();container.setPadding(dp(18),dp(14),dp(18),dp(20));scroll.addView(container);
        container.addView(sectionTitle("แปลงหน่วย"));
        container.addView(description("คำนวณออฟไลน์ • เลือกหมวดหมู่และหน่วยที่ต้องการ"));
        container.addView(fieldLabel("หมวดหมู่"));
        unitCategory=spinner(UnitConverter.categories());container.addView(unitCategory,new LinearLayout.LayoutParams(-1,dp(52)));
        container.addView(fieldLabel("จำนวน / ราคา"));
        unitInput=numberBox("1");unitInput.setText("1");container.addView(unitInput,new LinearLayout.LayoutParams(-1,dp(58)));
        unitFromLabel=fieldLabel("จาก");container.addView(unitFromLabel);
        unitFrom=spinner(new String[]{""});container.addView(unitFrom,new LinearLayout.LayoutParams(-1,dp(50)));
        unitSwap=button("⇅ สลับหน่วย",FUNCTION,WHITE);container.addView(unitSwap,new LinearLayout.LayoutParams(-1,dp(43)));
        unitSwap.setOnClickListener(v->{
            if("ภาษี".equals(unitCategory.getSelectedItem()))unitFrom.setSelection(1-unitFrom.getSelectedItemPosition());
            else {int a=unitFrom.getSelectedItemPosition(),b=unitTo.getSelectedItemPosition();unitFrom.setSelection(b);unitTo.setSelection(a);}
            updateUnits();
        });
        unitToLabel=fieldLabel("เป็น");container.addView(unitToLabel);
        unitTo=spinner(new String[]{""});container.addView(unitTo,new LinearLayout.LayoutParams(-1,dp(50)));
        unitRate=numberBox("7");unitRate.setText("7");
        unitRateLabel=fieldLabel("อัตราภาษี (%) — เปลี่ยนได้");container.addView(unitRateLabel);
        container.addView(unitRate,new LinearLayout.LayoutParams(-1,dp(48)));
        container.addView(fieldLabel("ผลลัพธ์"));
        unitOutput=label("",35,BLUE,true);unitOutput.setPadding(dp(8),dp(13),dp(8),dp(13));
        unitOutput.setBackground(ripple(FUNCTION,dp(8)));unitOutput.setTextIsSelectable(true);
        container.addView(unitOutput,new LinearLayout.LayoutParams(-1,-2));
        unitHelp=description("ค่าจะแสดงเมื่อกรอกตัวเลข");container.addView(unitHelp);
        TextView copy=button("คัดลอกผลลัพธ์",FUNCTION,WHITE);
        container.addView(copy,new LinearLayout.LayoutParams(-1,dp(45)));
        copy.setOnClickListener(v->copy(unitOutput.getText().toString()));
        unitCategory.setOnItemSelectedListener(selected(()->{
            String c=(String)unitCategory.getSelectedItem();String[] choices=UnitConverter.units(c);
            unitFrom.setAdapter(spinnerAdapter(choices));unitTo.setAdapter(spinnerAdapter(choices));
            if(choices.length>1)unitTo.setSelection(1);
            boolean tax="ภาษี".equals(c);
            unitFromLabel.setText(tax?"ประเภทคำนวณ":"จาก");
            unitToLabel.setVisibility(tax?View.GONE:View.VISIBLE);unitTo.setVisibility(tax?View.GONE:View.VISIBLE);
            unitRateLabel.setVisibility(tax?View.VISIBLE:View.GONE);unitRate.setVisibility(tax?View.VISIBLE:View.GONE);
            unitSwap.setText(tax?"⇅ สลับ ก่อน/หลังภาษี":"⇅ สลับหน่วย");updateUnits();
        }));
        unitFrom.setOnItemSelectedListener(selected(this::updateUnits));
        unitTo.setOnItemSelectedListener(selected(this::updateUnits));
        unitInput.addTextChangedListener(watch(this::updateUnits));unitRate.addTextChangedListener(watch(this::updateUnits));
        unitCategory.setSelection(0);updateUnits();
    }
    private void updateUnits(){
        if(updatingUnit||unitCategory==null||unitFrom==null||unitTo==null||unitInput==null||unitOutput==null)return;
        if(unitCategory.getSelectedItem()==null||unitFrom.getSelectedItem()==null||unitTo.getSelectedItem()==null)return;
        updatingUnit=true;
        try {
            String category=unitCategory.getSelectedItem().toString();
            BigDecimal out;
            if("ภาษี".equals(category)){
                boolean inclusive=unitFrom.getSelectedItemPosition()==1;
                out=UnitConverter.calculateTax(unitInput.getText().toString(),unitRate.getText().toString(),inclusive);
                unitHelp.setText(inclusive?"หักภาษีออกจากราคาที่รวมภาษีแล้ว":"บวกภาษีเข้าในราคาก่อนภาษี");
            }else{
                out=UnitConverter.convert(category,unitFrom.getSelectedItem().toString(),unitTo.getSelectedItem().toString(),unitInput.getText().toString());
                unitHelp.setText("คำนวณจากนิยามการแปลงหน่วย • ผลแสดงสูงสุด 10 ตำแหน่งทศนิยม");
            }
            unitOutput.setText(UnitConverter.display(out));
        }catch(Exception e){unitOutput.setText("—");unitHelp.setText(e.getMessage());}
        updatingUnit=false;
    }
    private void buildPrice(boolean fuel){
        ScrollView sc=new ScrollView(this);page.addView(sc,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout holder=vertical();holder.setPadding(dp(18),dp(14),dp(18),dp(20));sc.addView(holder);
        holder.addView(sectionTitle(fuel?"ราคาน้ำมันหลายแบรนด์":"ราคาทองไทยและโลหะ"));
        holder.addView(description(fuel?
                "ข้อมูลตามประกาศแหล่งอ้างอิง • บาท/ลิตร • เมื่อออฟไลน์ใช้ราคาล่าสุดที่บันทึกไว้ โดยระบุเวลาและสถานะ":
                "เลือกทองคำไทย 96.5% (บาทต่อหนึ่งบาททองคำ) หรือโลหะ Spot (USD/oz) • มีสำรองราคาล่าสุดเมื่อออฟไลน์"));
        if(fuel){
            holder.addView(fieldLabel("เลือกแบรนด์"));
            fuelBrand=spinner(FUEL_BRANDS);
            holder.addView(fuelBrand,new LinearLayout.LayoutParams(-1,dp(52)));
            holder.addView(fieldLabel("เลือกชนิดน้ำมัน"));
            fuelFilter=spinner(FUEL_TYPES);
            holder.addView(fuelFilter,new LinearLayout.LayoutParams(-1,dp(52)));
            fuelFilter.setSelection(3); // E20 is a directly comparable standard grade
            fuelBrand.setOnItemSelectedListener(selected(this::loadFuel));
            fuelFilter.setOnItemSelectedListener(selected(this::renderFuel));
        } else {
            holder.addView(fieldLabel("เลือกตลาดราคา"));
            goldMarket=spinner(new String[]{"ทองคำไทย 96.5%","โลหะตลาดโลก (Spot)"});
            holder.addView(goldMarket,new LinearLayout.LayoutParams(-1,dp(54)));
            holder.addView(fieldLabel("เลือกโลหะ Spot"));
            metalPicker=spinner(METALS);
            holder.addView(metalPicker,new LinearLayout.LayoutParams(-1,dp(54)));
            metalPicker.setVisibility(View.GONE);
            goldMarket.setOnItemSelectedListener(selected(()->{
                if(metalPicker!=null)metalPicker.setVisibility(goldMarket.getSelectedItemPosition()==0?View.GONE:View.VISIBLE);
                loadMetal();
            }));
            metalPicker.setOnItemSelectedListener(selected(()->{
                int i=metalPicker.getSelectedItemPosition();if(i>=0 && i<SYMBOLS.length)selectedMetal=SYMBOLS[i];
                if(goldMarket!=null&&goldMarket.getSelectedItemPosition()==1)loadMetal();
            }));
        }
        TextView refresh=button("⟳  ตรวจสอบประกาศล่าสุด",BLUE,Color.BLACK);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(52));rp.topMargin=dp(12);
        holder.addView(refresh,rp);
        refresh.setOnClickListener(v->{if(fuel)loadFuel();else loadMetal();});
        liveStatus=description("ยังไม่ได้ตรวจสอบข้อมูลจากแหล่งราคา");holder.addView(liveStatus);
        liveResults=vertical();holder.addView(liveResults,new LinearLayout.LayoutParams(-1,-2));
        if(fuel){
            holder.addView(description("PTT OR: Web Service อย่างเป็นทางการ (SOAP/XML) • บางจาก: Oil Price API • Shell: ยังไม่พบ API สาธารณะที่ยืนยันสิทธิ์ จึงไม่แสดงราคาอัตโนมัติ"));
            TextView ptt=button("เว็บไซต์ราคาน้ำมัน PTT OR ↗",FUNCTION,WHITE);
            LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(44));pp.topMargin=dp(10);holder.addView(ptt,pp);
            ptt.setOnClickListener(v->openUrl("https://www.pttor.com/news/oil-price"));
            TextView bc=button("เว็บไซต์ราคาน้ำมันบางจาก ↗",FUNCTION,WHITE);
            pp=new LinearLayout.LayoutParams(-1,dp(44));pp.topMargin=dp(7);holder.addView(bc,pp);
            bc.setOnClickListener(v->openUrl("https://www.bangchak.co.th/th/OilPrice/"));
            TextView ep=button("ราคาน้ำมันหลายแบรนด์ — EPPO ↗",FUNCTION,WHITE);
            pp=new LinearLayout.LayoutParams(-1,dp(44));pp.topMargin=dp(7);holder.addView(ep,pp);
            ep.setOnClickListener(v->openUrl("https://www.eppo.go.th/data-energy-statistic/energy-price-th/ราคาขายปลีกน้ำมัน/"));
        } else {
            holder.addView(description("ทองไทย: ThaiGoldToday Public API (อ้างอิงประกาศสมาคมค้าทองคำ) • ราคา Spot: Gold API • ตรวจเวลาแหล่งข้อมูลทุกครั้ง"));
            TextView src=button("เครดิตข้อมูลราคาทองไทย: ThaiGoldToday ↗",FUNCTION,WHITE);
            LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(44));pp.topMargin=dp(9);holder.addView(src,pp);
            src.setOnClickListener(v->openUrl("https://www.thaigoldtoday.com/"));
            TextView official=button("ตรวจสอบสมาคมค้าทองคำ ↗",FUNCTION,WHITE);
            pp=new LinearLayout.LayoutParams(-1,dp(44));pp.topMargin=dp(7);holder.addView(official,pp);
            official.setOnClickListener(v->openUrl("https://www.goldtraders.or.th/"));
            holder.addView(description("ราคาประกาศอ้างอิงไม่ใช่ราคาเสนอซื้อขาย ณ ร้านจริง; รูปพรรณอาจมีค่ากำเหน็จเพิ่มเติม และราคาซื้อคืนขึ้นกับเงื่อนไขร้าน"));
        }
    }
    private void startPolling(){
        handler.removeCallbacks(periodic);
        if(currentMode==5){loadFuel();handler.postDelayed(periodic,15*60*1000L);}
        else if(currentMode==6){loadMetal();handler.postDelayed(periodic,90*1000L);}
    }
    private int currentFuelBrand(){return fuelBrand==null?0:Math.max(0,fuelBrand.getSelectedItemPosition());}
    private void loadFuel(){
        if(currentMode!=5||liveResults==null)return;
        int token=++fetchToken;
        latestFuel=null;latestPtt=null;pttProblem=null;bangchakProblem=null;
        pttFinished=false;bangchakFinished=false;
        pttCached=false;bangchakCached=false;pttCachedAt=0;bangchakCachedAt=0;
        int brand=currentFuelBrand();
        liveResults.removeAllViews();liveStatus.setTextColor(MUTED);
        if(brand==3){
            liveStatus.setText("Shell: ไม่มี API ที่ยืนยันสิทธิ์ใช้ในรุ่นนี้ จึงไม่มีราคาอัตโนมัติหรือแคช");
            liveResults.addView(description("เปิดตารางเทียบของ EPPO หรือเว็บไซต์ Shell เพื่อตรวจราคา • ไม่แสดงราคาสมมติ"));
            return;
        }
        // Read the last successfully validated quotes BEFORE starting network requests.
        if(brand==0||brand==1){
            PriceCache.Snapshot<OilPriceParser.Feed> snapshot=PriceCache.getPtt(this);
            if(snapshot!=null){latestPtt=snapshot.data;pttCached=true;pttCachedAt=snapshot.savedAtMillis;}
        }
        if(brand==0||brand==2){
            PriceCache.Snapshot<PriceService.FuelData> snapshot=PriceCache.getBangchak(this);
            if(snapshot!=null){latestFuel=snapshot.data;bangchakCached=true;bangchakCachedAt=snapshot.savedAtMillis;}
        }
        renderFuel();
        if(brand==0||brand==1){
            PriceService.ptt((data,error)->{
                if(currentMode!=5||token!=fetchToken)return;
                pttFinished=true;
                if(error==null && data!=null){
                    boolean stored=PriceCache.putPtt(this,data);
                    PriceCache.Snapshot<OilPriceParser.Feed> accepted=PriceCache.getPtt(this);
                    if(stored){latestPtt=data;pttCached=false;pttProblem=null;}
                    else if(accepted!=null){latestPtt=accepted.data;pttCached=true;pttCachedAt=accepted.savedAtMillis;
                        pttProblem="ใช้ราคาที่บันทึกไว้ (ต้นทางเก่ากว่าหรือบันทึกใหม่ไม่สำเร็จ)";}
                    else {latestPtt=data;pttCached=false;pttProblem="ได้รับราคาใหม่ แต่ไม่สามารถบันทึกสำรองในเครื่อง";}
                }else {pttProblem=error==null?"ไม่มีราคาที่ตรวจสอบได้":error;}
                renderFuel();
            });
        }
        if(brand==0||brand==2){
            PriceService.fuel((data,error)->{
                if(currentMode!=5||token!=fetchToken)return;
                bangchakFinished=true;
                if(error==null&&data!=null){
                    boolean stored=PriceCache.putBangchak(this,data);
                    PriceCache.Snapshot<PriceService.FuelData> accepted=PriceCache.getBangchak(this);
                    if(stored){latestFuel=data;bangchakCached=false;bangchakProblem=null;}
                    else if(accepted!=null){latestFuel=accepted.data;bangchakCached=true;bangchakCachedAt=accepted.savedAtMillis;
                        bangchakProblem="ใช้ราคาที่บันทึกไว้ (ต้นทางเก่ากว่าหรือบันทึกใหม่ไม่สำเร็จ)";}
                    else {latestFuel=data;bangchakCached=false;bangchakProblem="ได้รับราคาใหม่ แต่ไม่สามารถบันทึกสำรองในเครื่อง";}
                }else {bangchakProblem=error==null?"ไม่มีราคาที่ตรวจสอบได้":error;}
                renderFuel();
            });
        }
    }
    private void renderFuel(){
        if(currentMode!=5||liveResults==null||fuelFilter==null)return;
        int brand=currentFuelBrand();
        if(brand==3)return;
        liveResults.removeAllViews();
        String selected=fuelFilter.getSelectedItem()==null?"ทั้งหมด":fuelFilter.getSelectedItem().toString();
        long now=System.currentTimeMillis();int rendered=0;
        StringBuilder status=new StringBuilder("ราคาประกาศตามแหล่งข้อมูล • ราคาที่บันทึกไว้ไม่ใช่ราคาสด");
        boolean pending=false;
        boolean caution=false;
        if(brand==0||brand==1){
            if(!pttFinished){status.append("\nPTT OR: กำลังตรวจสอบราคาใหม่");pending=true;}
            if(pttProblem!=null){status.append("\n⚠ PTT OR: ").append(pttProblem);caution=true;}
            if(latestPtt!=null){
                if(pttCached){status.append("\nPTT OR: ใช้ราคาล่าสุดที่บันทึกไว้ ").append(formatSavedAt(pttCachedAt));caution=true;}
                for(OilPriceParser.Item item:latestPtt.items){
                    String grade=FreshnessPolicy.commonFuelType(item.product);
                    if(!"ทั้งหมด".equals(selected)&&!grade.equals(selected))continue;
                    boolean recent=item.isRecent(now);
                    liveResults.addView(priceRow("PTT OR · "+item.product,
                        String.format(Locale.US,"%.2f ฿/ลิตร",item.price)),new LinearLayout.LayoutParams(-1,dp(66)));
                    String sourceTime=item.publishedAt.isEmpty()?"ไม่ระบุ":item.publishedAt;
                    liveResults.addView(description("ประกาศ: "+sourceTime+
                            (pttCached?" • [ราคาที่บันทึกไว้]":" • [ข้อมูลที่รับจากต้นทาง]")+
                            (recent?"":" • ⚠ วันประกาศเก่าหรือไม่ยืนยัน")));
                    if(!recent)caution=true;
                    rendered++;
                }
            }else if(pttFinished){status.append("\nPTT OR: ไม่มีราคาสำรองที่บันทึกไว้");caution=true;}
        }
        if(brand==0||brand==2){
            if(!bangchakFinished){status.append("\nบางจาก: กำลังตรวจสอบราคาใหม่");pending=true;}
            if(bangchakProblem!=null){status.append("\n⚠ บางจาก: ").append(bangchakProblem);caution=true;}
            if(latestFuel!=null){
                status.append("\nบางจาก — วันข้อมูล: ").append(latestFuel.dateString)
                    .append("; ประกาศ: ").append(latestFuel.updateString);
                if(bangchakCached){status.append("\nบางจาก: ใช้ราคาล่าสุดที่บันทึกไว้ ").append(formatSavedAt(bangchakCachedAt));caution=true;}
                for(PriceService.FuelItem item:latestFuel.items){
                    String grade=FreshnessPolicy.commonFuelType(item.name);
                    if(!"ทั้งหมด".equals(selected)&&!grade.equals(selected))continue;
                    liveResults.addView(priceRow("บางจาก · "+item.name,
                        String.format(Locale.US,"%.2f ฿/ลิตร",item.price)),new LinearLayout.LayoutParams(-1,dp(66)));
                    liveResults.addView(description((bangchakCached?"ราคาล่าสุดที่บันทึกไว้":"ข้อมูลที่รับจากต้นทาง")+
                        " • ประกาศ: "+latestFuel.updateString+
                        (latestFuel.fresh?"":" • ⚠ วันข้อมูลเก่าหรือไม่ยืนยัน")));
                    if(!latestFuel.fresh)caution=true;
                    rendered++;
                }
            }else if(bangchakFinished){status.append("\nบางจาก: ไม่มีราคาสำรองที่บันทึกไว้");caution=true;}
        }
        if(brand==0 && "Diesel".equals(selected)){
            liveResults.addView(description("ดีเซลต่างแบรนด์อาจมีเกรดหรือส่วนผสมต่างกัน ควรตรวจชื่อผลิตภัณฑ์ก่อนเปรียบเทียบ"));
        }
        if(rendered==0)liveResults.addView(description(pending?
            "กำลังดึงข้อมูล • ยังไม่มีราคาที่บันทึกไว้สำหรับชนิดที่เลือก":
            "ไม่มีราคาที่ตรวจสอบได้หรือราคาสำรองสำหรับชนิดที่เลือก (ไม่ใช่ 0 บาท)"));
        if(brand==0)status.append("\nShell: ดูผ่าน EPPO (ยังไม่มี API ที่อนุญาต)");
        liveStatus.setText(status.toString());
        liveStatus.setTextColor(caution?WARNING:(pending?MUTED:0xff95dfbf));
    }
    private void loadMetal(){
        if(currentMode!=6||liveResults==null)return;
        int token=++fetchToken;
        boolean thai=goldMarket==null||goldMarket.getSelectedItemPosition()==0;
        if(thai){
            PriceCache.Snapshot<PriceService.ThaiGoldData> cached=PriceCache.getThaiGold(this);
            renderThaiGold(cached==null?null:cached.data,true,cached==null?0:cached.savedAtMillis,null,true);
            PriceService.thaiGold((data,error)->{
                if(currentMode!=6||token!=fetchToken||liveResults==null)return;
                if(error==null&&data!=null){
                    boolean stored=PriceCache.putThaiGold(this,data);
                    PriceCache.Snapshot<PriceService.ThaiGoldData> accepted=PriceCache.getThaiGold(this);
                    if(stored){renderThaiGold(data,false,0,null,false);}
                    else if(accepted!=null){renderThaiGold(accepted.data,true,accepted.savedAtMillis,
                        "ต้นทางส่งข้อมูลที่เก่ากว่าแคช หรือบันทึกสำรองไม่สำเร็จ",false);}
                    else {renderThaiGold(data,false,0,"ได้รับราคาใหม่ แต่ยังบันทึกลงเครื่องไม่ได้",false);}
                }else {
                    renderThaiGold(cached==null?null:cached.data,true,cached==null?0:cached.savedAtMillis,
                        error==null?"ไม่มีราคาที่ตรวจสอบได้":error,false);
                }
            });
        }else {
            String symbol=selectedMetal;
            PriceCache.Snapshot<PriceService.MetalData> cached=PriceCache.getMetal(this,symbol);
            renderSpot(cached==null?null:cached.data,true,cached==null?0:cached.savedAtMillis,null,true);
            PriceService.metal(symbol,(data,error)->{
                if(currentMode!=6||token!=fetchToken||liveResults==null)return;
                if(error==null&&data!=null){
                    boolean stored=PriceCache.putMetal(this,data);
                    PriceCache.Snapshot<PriceService.MetalData> accepted=PriceCache.getMetal(this,symbol);
                    if(stored){renderSpot(data,false,0,null,false);}
                    else if(accepted!=null){renderSpot(accepted.data,true,accepted.savedAtMillis,
                            "ต้นทางส่งข้อมูลเก่ากว่าแคช หรือบันทึกสำรองไม่สำเร็จ",false);}
                    else {renderSpot(data,false,0,"ได้รับราคาใหม่ แต่ยังบันทึกลงเครื่องไม่ได้",false);}
                }else {
                    renderSpot(cached==null?null:cached.data,true,cached==null?0:cached.savedAtMillis,
                            error==null?"ไม่มีราคาที่ตรวจสอบได้":error,false);
                }
            });
        }
    }
    private void renderThaiGold(PriceService.ThaiGoldData data,boolean cache,long savedAt,String failure,boolean checking){
        if(liveResults==null||liveStatus==null)return;
        liveResults.removeAllViews();
        StringBuilder status=new StringBuilder(checking?
            (data==null?"กำลังตรวจสอบราคาทองไทยจาก ThaiGoldToday...":"กำลังตรวจสอบราคาใหม่ • ⚠ แสดงราคาทองล่าสุดที่บันทึกไว้ (ไม่ใช่ราคาสด)"):
            cache?"⚠ ใช้ราคาทองไทยล่าสุดที่บันทึกไว้ (ไม่ใช่ราคาสด)":"ได้รับราคาจาก ThaiGoldToday");
        if(failure!=null)status.append("\n⚠ ").append(failure);
        if(data==null){status.append("\nยังไม่มีราคาที่บันทึกไว้ในเครื่อง");liveStatus.setText(status.toString());liveStatus.setTextColor(WARNING);return;}
        if(cache)status.append("\nบันทึกสำเร็จครั้งล่าสุด: ").append(formatSavedAt(savedAt));
        status.append("\nเวลาประกาศ (ไทย): ").append(formatThaiTime(data.publishedAt));
        if(data.updateRound>0)status.append(" • รอบที่ ").append(data.updateRound);
        if(!data.fresh)status.append("\n⚠ วันประกาศเก่าหรือไม่ยืนยัน");
        liveStatus.setText(status.toString());liveStatus.setTextColor(cache||!data.fresh||failure!=null?WARNING:(checking?MUTED:0xff95dfbf));
        liveResults.addView(priceRow("ทองแท่ง 96.5% · รับซื้อ",formatMoney(data.buyBar)+" ฿"),new LinearLayout.LayoutParams(-1,dp(70)));
        liveResults.addView(priceRow("ทองแท่ง 96.5% · ขายออก",formatMoney(data.sellBar)+" ฿"),new LinearLayout.LayoutParams(-1,dp(70)));
        liveResults.addView(priceRow("ทองรูปพรรณ · ฐานรับซื้อ",formatMoney(data.buyOrnament)+" ฿"),new LinearLayout.LayoutParams(-1,dp(70)));
        liveResults.addView(priceRow("ทองรูปพรรณ · ขายออก",formatMoney(data.sellOrnament)+" ฿"),new LinearLayout.LayoutParams(-1,dp(70)));
        liveResults.addView(description("บาทต่อหนึ่งบาททองคำ • ค่ากำเหน็จขึ้นกับร้าน • ข้อมูล: thaigoldtoday.com"));
    }
    private void renderSpot(PriceService.MetalData data,boolean cache,long savedAt,String failure,boolean checking){
        if(liveResults==null||liveStatus==null)return;
        liveResults.removeAllViews();
        StringBuilder status=new StringBuilder(checking?
            (data==null?"กำลังตรวจสอบราคา Spot จาก Gold API...":"กำลังตรวจสอบราคาใหม่ • ⚠ แสดงราคา Spot ที่บันทึกไว้ (ไม่ใช่ราคาสด)"):
            cache?"⚠ ใช้ราคาโลหะล่าสุดที่บันทึกไว้ (ไม่ใช่ราคาสด)":"ได้รับราคา Spot จาก Gold API");
        if(failure!=null)status.append("\n⚠ ").append(failure);
        if(data==null){status.append("\nยังไม่มีราคาที่บันทึกไว้ในเครื่อง");liveStatus.setText(status.toString());liveStatus.setTextColor(WARNING);return;}
        if(cache)status.append("\nบันทึกสำเร็จครั้งล่าสุด: ").append(formatSavedAt(savedAt));
        status.append("\nเวลาแหล่งข้อมูล: ").append(formatThaiTime(data.updatedAt));
        if(!data.fresh)status.append("\n⚠ วันประกาศเก่าหรือไม่ยืนยัน");
        liveStatus.setText(status.toString());liveStatus.setTextColor(cache||!data.fresh||failure!=null?WARNING:(checking?MUTED:0xff95dfbf));
        liveResults.addView(priceRow(data.symbol,formatMoney(data.price)+" USD/oz"),new LinearLayout.LayoutParams(-1,dp(85)));
        liveResults.addView(description("ราคา Spot ตลาดโลก ไม่ใช่ราคาขายปลีกในประเทศไทย"));
    }
    private static String formatSavedAt(long epochMillis){
        if(epochMillis<=0)return "ไม่ทราบเวลา";
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm 'น.'",Locale.US)
                .withZone(java.time.ZoneId.of("Asia/Bangkok"))
                .format(java.time.Instant.ofEpochMilli(epochMillis));
    }
    private static String formatThaiTime(String utc){
        try{
            java.time.Instant stamp=java.time.Instant.parse(utc);
            return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.US)
                    .withZone(java.time.ZoneId.of("Asia/Bangkok")).format(stamp);
        }catch(Exception e){return utc.isEmpty()?"ไม่มีเวลาอ้างอิง":utc;}
    }
    private static String formatMoney(double price){
        DecimalFormat f=new DecimalFormat("#,##0.00",DecimalFormatSymbols.getInstance(Locale.US));
        return f.format(price);
    }
    private LinearLayout priceRow(String name,String value){
        LinearLayout row=horizontal();row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(12),0,dp(12),0);
        row.setBackground(ripple(FUNCTION,dp(6)));
        TextView label=label(name,14,WHITE,false);row.addView(label,new LinearLayout.LayoutParams(0,-2,1));
        TextView price=label(value,18,BLUE,true);row.addView(price,new LinearLayout.LayoutParams(-2,-2));
        return row;
    }
    private void openUrl(String link){startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));}
    private void toggleFull(){full=!full;if(full)getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);}
    private void restore(){android.content.SharedPreferences p=getSharedPreferences("calculator_standard_v2",MODE_PRIVATE);
        engine.restoreMemory(p.getString("memory",null));
        try{JSONArray a=new JSONArray(p.getString("history","[]"));List<String> items=new ArrayList<>();
            for(int i=0;i<a.length();i++)items.add(a.optString(i));engine.restoreHistory(items);
        }catch(JSONException ignored){engine.clearHistory();}}
    private void persist(){JSONArray a=new JSONArray();for(String h:engine.getHistory())a.put(h);
        getSharedPreferences("calculator_standard_v2",MODE_PRIVATE).edit().putString("memory",engine.getMemory())
                .putString("history",a.toString()).apply();}
    @Override public boolean onKeyDown(int code,KeyEvent e){if(currentMode>3)return super.onKeyDown(code,e);
        String key=null;
        if(code>=KeyEvent.KEYCODE_0&&code<=KeyEvent.KEYCODE_9)key=""+(code-KeyEvent.KEYCODE_0);
        else if(code>=KeyEvent.KEYCODE_NUMPAD_0&&code<=KeyEvent.KEYCODE_NUMPAD_9)key=""+(code-KeyEvent.KEYCODE_NUMPAD_0);
        else switch(code){case KeyEvent.KEYCODE_DEL:key="⌫";break;case KeyEvent.KEYCODE_ENTER:case KeyEvent.KEYCODE_NUMPAD_ENTER:key="=";break;
            case KeyEvent.KEYCODE_PLUS:case KeyEvent.KEYCODE_NUMPAD_ADD:key="+";break;
            case KeyEvent.KEYCODE_NUMPAD_SUBTRACT:key="−";break;case KeyEvent.KEYCODE_NUMPAD_MULTIPLY:key="×";break;
            case KeyEvent.KEYCODE_NUMPAD_DIVIDE:key="÷";break;case KeyEvent.KEYCODE_PERIOD:case KeyEvent.KEYCODE_NUMPAD_DOT:key=".";break;}
        if(key!=null){engine.press(key);afterKey();return true;}return super.onKeyDown(code,e);}

    private Spinner spinner(String[] values){
        Spinner sp=new Spinner(this);sp.setAdapter(spinnerAdapter(values));return sp;
    }
    private ArrayAdapter<String> spinnerAdapter(String[] values){
        return new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values){
            @Override public View getView(int position,View convertView,ViewGroup parent){
                TextView t=(TextView)super.getView(position,convertView,parent);t.setTextColor(WHITE);t.setTextSize(16);t.setPadding(dp(8),0,dp(8),0);return t;
            }
            @Override public View getDropDownView(int position,View convertView,ViewGroup parent){
                TextView t=(TextView)super.getDropDownView(position,convertView,parent);t.setTextColor(Color.BLACK);return t;
            }
        };
    }
    private AdapterView.OnItemSelectedListener selected(Runnable onPick){
        return new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int position,long id){onPick.run();}
        public void onNothingSelected(AdapterView<?> p){} };}
    private TextWatcher watch(Runnable act){return new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int a){}
        public void onTextChanged(CharSequence s,int st,int before,int count){act.run();}
        public void afterTextChanged(Editable e){} };}
    private EditText numberBox(String placeholder){EditText v=new EditText(this);v.setSingleLine(true);v.setTextColor(WHITE);
        v.setHintTextColor(MUTED);v.setHint(placeholder);v.setTextSize(23);
        v.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL|android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        v.setPadding(dp(12),0,dp(12),0);v.setBackground(ripple(FUNCTION,dp(7)));return v;}
    private TextView sectionTitle(String title){TextView v=label(title,25,WHITE,true);
        v.setPadding(0,dp(10),0,dp(8));return v;}
    private TextView fieldLabel(String title){TextView v=label(title,14,MUTED,false);
        v.setPadding(0,dp(15),0,dp(6));return v;}
    private TextView description(String s){TextView v=label(s,12,MUTED,false);v.setPadding(0,dp(9),0,dp(12));return v;}
    private TextView button(String s,int bg,int fg){TextView v=label(s,16,fg,true);v.setGravity(Gravity.CENTER);v.setBackground(ripple(bg,dp(7)));return v;}
    private TextView topButton(String symbol,Runnable tap){TextView b=label(symbol,20,WHITE,false);b.setGravity(Gravity.CENTER);
        b.setBackground(ripple(HEADER,dp(3)));b.setOnClickListener(v->tap.run());b.setLayoutParams(new LinearLayout.LayoutParams(dp(46),-1));return b;}
    private TextView action(String symbol,int width,Runnable tap){TextView b=label(symbol,24,WHITE,false);b.setGravity(Gravity.CENTER);
        b.setOnClickListener(v->tap.run());b.setBackground(ripple(BG,dp(3)));b.setLayoutParams(new LinearLayout.LayoutParams(width,-1));return b;}
    private LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    private LinearLayout horizontal(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.HORIZONTAL);return v;}
    private TextView label(String value,float size,int color,boolean bold){TextView v=new TextView(this);v.setText(value);v.setTextColor(color);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);v.setIncludeFontPadding(false);
        if(bold)v.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);return v;}
    private RippleDrawable ripple(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);
        return new RippleDrawable(ColorStateList.valueOf(0xff777777),d,null);}
    private int dp(float value){return (int)(value*getResources().getDisplayMetrics().density+.5f);}
}
