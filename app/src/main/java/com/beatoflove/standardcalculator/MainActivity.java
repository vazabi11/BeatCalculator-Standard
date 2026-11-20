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
    private static final int BG=0xff0b141d, HEADER=0xff0b141d, WHITE=Color.WHITE, MUTED=0xff90a4b7;
    private static final int FUNCTION=0xff142735, DIGIT=0xff233441, BLUE=0xff35c6f4, WARNING=0xffffba57;
    private static final String[] MODES={"","Standard","Scientific","Formula Pro","Converter","Fuel","Markets","Financial","Currency"};
    private static final int[] NAV={1,2,3,7,8,5,6,4};
    private FinancialPanel financialPanel; private CurrencyPanel currencyPanel; private MarketStream stream;
    private Spinner provincePicker,districtPicker; private EditText districtName; private boolean paused=false;
    private static final String[] PROVINCES=("Bangkok reference|กรุงเทพมหานคร|กระบี่|กาญจนบุรี|กาฬสินธุ์|กำแพงเพชร|ขอนแก่น|จันทบุรี|ฉะเชิงเทรา|ชลบุรี|ชัยนาท|ชัยภูมิ|ชุมพร|เชียงราย|เชียงใหม่|ตรัง|ตราด|ตาก|นครนายก|นครปฐม|นครพนม|นครราชสีมา|นครศรีธรรมราช|นครสวรรค์|นนทบุรี|นราธิวาส|น่าน|บึงกาฬ|บุรีรัมย์|ปทุมธานี|ประจวบคีรีขันธ์|ปราจีนบุรี|ปัตตานี|พระนครศรีอยุธยา|พะเยา|พังงา|พัทลุง|พิจิตร|พิษณุโลก|เพชรบุรี|เพชรบูรณ์|แพร่|ภูเก็ต|มหาสารคาม|มุกดาหาร|แม่ฮ่องสอน|ยโสธร|ยะลา|ร้อยเอ็ด|ระนอง|ระยอง|ราชบุรี|ลพบุรี|ลำปาง|ลำพูน|เลย|ศรีสะเกษ|สกลนคร|สงขลา|สตูล|สมุทรปราการ|สมุทรสงคราม|สมุทรสาคร|สระแก้ว|สระบุรี|สิงห์บุรี|สุโขทัย|สุพรรณบุรี|สุราษฎร์ธานี|สุรินทร์|หนองคาย|หนองบัวลำภู|อ่างทอง|อำนาจเจริญ|อุดรธานี|อุตรดิตถ์|อุทัยธานี|อุบลราชธานี").split("\\|");
    private static final String[] METALS={"ทองคำ (Gold / XAU)","เงิน (Silver / XAG)","แพลทินัม (Platinum / XPT)","พัลลาเดียม (Palladium / XPD)"};
    private static final String[] SYMBOLS={"XAU","XAG","XPT","XPD"};
    private final CalculatorEngine engine=new CalculatorEngine();
    private FormulaPanel formulaPanel;
    private LinearLayout root, page, tabsRow;
    private HorizontalScrollView tabsScroll, resultScroll;
    private TextView result, expr, hint, memoryClear, memoryRecall, memoryArrow;
    private int currentMode=1, fetchToken=0;
    private boolean full=false, updatingUnit=false;
    private String selectedMetal="XAU";
    private static final String[] FUEL_BRANDS={"All Brands","PTT OR","Bangchak"};
    private static final String[] FUEL_TYPES={"ทั้งหมด","Gasohol 91","Gasohol 95","E20","E85","Diesel"};
    private Spinner fuelBrand, goldMarket, metalPicker, metalMode;
    private OilPriceParser.Feed latestPtt;
    private String pttProblem,bangchakProblem;
    private boolean pttCached,bangchakCached;
    private long pttCachedAt,bangchakCachedAt;
    private boolean pttFinished,bangchakFinished;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable periodic= new Runnable(){@Override public void run(){
        if(currentMode==5){loadFuel();handler.postDelayed(this,15*60*1000L);}
        else if(currentMode==6&&marketIndex()<2){loadMetal();handler.postDelayed(this,marketIndex()==0?60*1000L:5*60*1000L);}
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
    @Override protected void onPause(){paused=true;fetchToken++;handler.removeCallbacks(periodic);if(stream!=null)stream.stop();if(currencyPanel!=null)currencyPanel.stop();super.onPause();}
    @Override protected void onResume(){super.onResume();paused=false;if(root!=null&&(currentMode==5||currentMode==6))startPolling();else if(root!=null&&currentMode==8&&currencyPanel!=null)currencyPanel.resume();}
    @Override protected void onDestroy(){handler.removeCallbacks(periodic);fetchToken++;if(stream!=null)stream.stop();if(currencyPanel!=null)currencyPanel.stop();super.onDestroy();}

    private void buildShell(){
        root=vertical();root.setBackgroundColor(BG);root.setPadding(dp(4),dp(2),dp(4),dp(5));setContentView(root);
        LinearLayout bar=horizontal();bar.setGravity(Gravity.CENTER_VERTICAL);bar.setBackgroundColor(HEADER);
        root.addView(bar,new LinearLayout.LayoutParams(-1,dp(54)));
        android.widget.ImageView icon=new android.widget.ImageView(this);
        icon.setImageResource(R.drawable.ic_calculator_foreground);icon.setContentDescription("Calculator — About");
        icon.setOnClickListener(v->showAbout());bar.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(44)));
        LinearLayout identity=vertical();identity.setGravity(Gravity.CENTER_VERTICAL);
        bar.addView(identity,new LinearLayout.LayoutParams(0,-1,1));
        identity.addView(label("Calculator",18,WHITE,true));identity.addView(label("by Sirawit Labs",10,MUTED,false));
        identity.setOnClickListener(v->showAbout());
        TextView version=label("v"+versionName(),10,BLUE,true);version.setGravity(Gravity.CENTER);
        version.setPadding(dp(3),0,dp(3),0);version.setOnClickListener(v->showAbout());
        bar.addView(version,new LinearLayout.LayoutParams(-2,-1));
        bar.addView(topButton("−",()->moveTaskToBack(true)));
        bar.addView(topButton("□",this::toggleFull));
        bar.addView(topButton("×",this::finish));
        tabsScroll=new HorizontalScrollView(this);tabsScroll.setHorizontalScrollBarEnabled(false);tabsScroll.setFillViewport(false);
        root.addView(tabsScroll,new LinearLayout.LayoutParams(-1,dp(52)));
        tabsRow=horizontal();tabsRow.setGravity(Gravity.CENTER_VERTICAL);tabsScroll.addView(tabsRow);
        page=vertical();root.addView(page,new LinearLayout.LayoutParams(-1,0,1));
    }
    private void selectMode(int mode){
        if(mode<1||mode>=MODES.length)return;
        if(stream!=null)stream.stop();if(currencyPanel!=null)currencyPanel.stop();
        if(currentMode==3&&formulaPanel!=null)formulaPanel.save();
        fetchToken++;handler.removeCallbacks(periodic);currentMode=mode;
        tabsRow.removeAllViews();
        for(int i:NAV){
            final int n=i;
            TextView tab=label(MODES[i],i==mode?16:14,WHITE,i==mode);
            tab.setPadding(dp(11),0,dp(11),0);tab.setGravity(Gravity.CENTER);tab.setBackground(ripple(i==mode?0xff075b82:BG,dp(10)));
            tab.setOnClickListener(v->selectMode(n));tabsRow.addView(tab,new LinearLayout.LayoutParams(-2,dp(40)));
            if(i==mode){tabsScroll.post(()->tabsScroll.smoothScrollTo(Math.max(0,tab.getLeft()-dp(60)),0));}
        }
        page.removeAllViews();result=null;expr=null;resultScroll=null;liveResults=null;liveStatus=null;
        memoryClear=null;memoryRecall=null;memoryArrow=null;fuelFilter=null;fuelBrand=null;
        goldMarket=null;metalPicker=null;metalMode=null;latestFuel=null;latestPtt=null;pttProblem=null;bangchakProblem=null;
        pttFinished=false;bangchakFinished=false;
        pttCached=false;bangchakCached=false;pttCachedAt=0;bangchakCachedAt=0;
        if(mode<=2)buildCalculator(mode);
        else if(mode==3){if(formulaPanel==null)formulaPanel=new FormulaPanel(this);formulaPanel.attach(page);}
        else if(mode==4)buildUnits();
        else if(mode==5)buildPrice(true);
        else if(mode==6)buildPrice(false);
        else if(mode==7){if(financialPanel==null)financialPanel=new FinancialPanel(this);financialPanel.attach(page);}
        else if(mode==8){if(currencyPanel==null)currencyPanel=new CurrencyPanel(this);currencyPanel.attach(page);}
        if(mode==5||mode==6)startPolling();
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
        display.setBackground(bordered(0xff102531,0xff304c5c,9));page.addView(display,new LinearLayout.LayoutParams(-1,0,1));
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
        if(mode==2){
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
        else keys=new String[][]{
            {"sin","cos","tan","π","C"},{"asin","acos","atan","e","⌫"},
            {"ln","log","exp","10^x","÷"},{"n!","x²","√x","^","×"},
            {"7","8","9","mod","−"},{"4","5","6","1/x","+"},
            {"1","2","3","%","CE"},{"±","0",".","00","="}
        };
        DisplayMetrics screen=getResources().getDisplayMetrics();
        int heightDp=Math.round(screen.heightPixels/screen.density);
        int dpHeight=Math.min(mode==2?530:510,Math.max(mode==2?350:290,Math.round(heightDp*(mode==2?.57f:.55f))));
        LinearLayout keypad=vertical();page.addView(keypad,new LinearLayout.LayoutParams(-1,dp(dpHeight)));
        for(String[] keysRow:keys){
            LinearLayout row=horizontal();keypad.addView(row,new LinearLayout.LayoutParams(-1,0,1));
            for(String k:keysRow){
                TextView b=label("1/x".equals(k)?"¹⁄ₓ":k,mode==2?18:25,"=".equals(k)?Color.BLACK:WHITE,false);
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
        .setItems(java.util.Arrays.stream(NAV).mapToObj(i->MODES[i]).toArray(String[]::new),(d,i)->selectMode(NAV[i]))
        .setNeutralButton("ล้างประวัติและ Memory",(d,i)->new AlertDialog.Builder(this)
            .setTitle("ล้างข้อมูลทั้งหมด?").setNegativeButton("ยกเลิก",null)
            .setPositiveButton("ล้าง",(dd,j)->{engine.clearSavedData();afterKey();}).show()).show();}

    private void buildUnits(){
        ScrollView scroll=new ScrollView(this);page.addView(scroll,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout container=vertical();container.setPadding(dp(18),dp(14),dp(18),dp(20));scroll.addView(container);
        container.addView(sectionTitle("Converter"));
        container.addView(description("คำนวณออฟไลน์ • เลือกหมวดหมู่และหน่วยที่ต้องการ"));
        container.addView(fieldLabel("หมวดหมู่"));
        unitCategory=spinner(java.util.Arrays.stream(UnitConverter.categories()).filter(c->!c.equals("ภาษี")).toArray(String[]::new));container.addView(unitCategory,new LinearLayout.LayoutParams(-1,dp(52)));
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
    private String versionName(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "?";}}
    private void showAbout(){
        int code=0;try{code=getPackageManager().getPackageInfo(getPackageName(),0).versionCode;}catch(Exception ignored){}
        new AlertDialog.Builder(this).setTitle("Calculator · Sirawit Labs")
        .setIcon(R.drawable.ic_calculator_foreground)
        .setMessage("Smart Tools for Everyday Life\n\nDeveloper: Sirawit Dh.\nBrand: Sirawit Labs\nVersion: "+versionName()+"\nVersion code: "+code+"\nAndroid · Unified Calculator & Live Markets\n© 2026 Sirawit Labs")
        .setNeutralButton("GitHub",(d,w)->openUrl("https://github.com/vazabi11/BeatCalculator-Standard"))
        .setPositiveButton("ปิด",null).show();
    }
    private GradientDrawable bordered(int fill,int stroke,int radius){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));d.setStroke(dp(1),stroke);return d;}
    private void space(LinearLayout parent,int height){parent.addView(new View(this),new LinearLayout.LayoutParams(-1,dp(height)));}
    private void rule(LinearLayout parent){View separator=new View(this);separator.setBackgroundColor(0xff30404d);parent.addView(separator,new LinearLayout.LayoutParams(-1,dp(1)));}
    private LinearLayout selector(LinearLayout holder,String title,Spinner sp){
        LinearLayout row=horizontal();row.setGravity(Gravity.CENTER_VERTICAL);TextView name=label(title,14,MUTED,true);
        row.addView(name,new LinearLayout.LayoutParams(dp(96),-2));
        LinearLayout outline=horizontal();outline.setGravity(Gravity.CENTER_VERTICAL);outline.setBackground(bordered(0xff142735,0xff304c5c,9));
        sp.setBackgroundTintList(ColorStateList.valueOf(WHITE));outline.addView(sp,new LinearLayout.LayoutParams(-1,dp(46)));
        row.addView(outline,new LinearLayout.LayoutParams(0,-2,1));holder.addView(row,new LinearLayout.LayoutParams(-1,-2));space(holder,8);return row;
    }
    private void buildPrice(boolean fuel){
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);page.addView(sc,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout holder=vertical();holder.setPadding(dp(12),dp(8),dp(12),dp(22));sc.addView(holder);
        TextView title=label(fuel?"Fuel Prices":"Markets",23,WHITE,true);holder.addView(title);space(holder,14);
        if(fuel){
            provincePicker=spinner(PROVINCES);selector(holder,"Province",provincePicker);
            districtPicker=spinner(new String[]{"ระดับจังหวัด / ไม่ระบุอำเภอ","ระบุอำเภอเอง (ยังไม่มี feed)"});selector(holder,"District scope",districtPicker);districtName=new EditText(this);districtName.setSingleLine(true);districtName.setTextColor(WHITE);districtName.setHintTextColor(MUTED);districtName.setHint("ชื่ออำเภอ (กรอกเอง)");districtName.setText(getSharedPreferences("fuel53",0).getString("district",""));districtName.setVisibility(View.GONE);holder.addView(districtName,new LinearLayout.LayoutParams(-1,dp(48)));districtName.addTextChangedListener(watch(()->{getSharedPreferences("fuel53",0).edit().putString("district",districtName.getText().toString()).apply();}));
            int saved=getSharedPreferences("fuel53",0).getInt("province",0);provincePicker.setSelection(Math.max(0,Math.min(saved,PROVINCES.length-1)));
            fuelBrand=spinner(FUEL_BRANDS);selector(holder,"Brand",fuelBrand);
            fuelFilter=spinner(FUEL_TYPES);selector(holder,"Fuel Type",fuelFilter);
            fuelBrand.setSelection(1);fuelFilter.setSelection(0);
        }else{
            goldMarket=spinner(new String[]{"Thai Gold","Metals","Crypto","Forex"});selector(holder,"Market",goldMarket);
            metalMode=spinner(new String[]{"Live Streaming (key required)","Reference / latest available"});LinearLayout metalModeRow=selector(holder,"Feed",metalMode);metalModeRow.setVisibility(View.GONE);
            metalPicker=spinner(METALS);LinearLayout metalRow=selector(holder,"โลหะ",metalPicker);metalRow.setVisibility(View.GONE);
            Runnable changeMarket=()->{if(currentMode!=6)return;boolean metal=goldMarket.getSelectedItemPosition()==1;metalModeRow.setVisibility(metal?View.VISIBLE:View.GONE);metalRow.setVisibility(metal&&metalMode.getSelectedItemPosition()==1?View.VISIBLE:View.GONE);startPolling();};goldMarket.setOnItemSelectedListener(selected(changeMarket));metalMode.setOnItemSelectedListener(selected(()->{if(goldMarket.getSelectedItemPosition()==1)changeMarket.run();}));
            metalPicker.setOnItemSelectedListener(selected(()->{int i=metalPicker.getSelectedItemPosition();if(i>=0&&i<SYMBOLS.length)selectedMetal=SYMBOLS[i];if(marketIndex()==1)loadMetal();}));
        }
        liveResults=vertical();holder.addView(liveResults,new LinearLayout.LayoutParams(-1,-2));space(holder,10);
        TextView refresh=button("⟳  ตรวจสอบราคาใหม่",BLUE,Color.BLACK);holder.addView(refresh,new LinearLayout.LayoutParams(-1,dp(48)));
        refresh.setOnClickListener(v->{if(fuel)loadFuel();else loadMetal();});
        liveStatus=label("กำลังตรวจสอบราคา",12,MUTED,false);liveStatus.setPadding(0,dp(8),0,dp(8));holder.addView(liveStatus);
        TextView note=label("ⓘ  ออฟไลน์: ใช้ราคาล่าสุดที่บันทึกไว้",12,0xffb7d9eb,false);note.setPadding(dp(12),dp(12),dp(12),dp(12));note.setBackground(bordered(0xff102531,0xff304c5c,10));holder.addView(note);
        TextView links=label("แหล่งข้อมูลและรายละเอียด  ↗",12,MUTED,false);links.setPadding(0,dp(14),0,dp(10));holder.addView(links);
        links.setOnClickListener(v->{String[] titles=fuel?new String[]{"PTT OR","บางจาก","EPPO","รายละเอียดการเชื่อมต่อ"}:new String[]{"Rakhagold — เครดิตข้อมูล","ThaiGoldToday","สมาคมค้าทองคำ","Gold API"};
            String[] urls=fuel?new String[]{"https://www.pttor.com/news/oil-price","https://www.bangchak.co.th/th/OilPrice/","https://www.eppo.go.th/"}:new String[]{"https://rakhagold.com/","https://www.thaigoldtoday.com/","https://www.goldtraders.or.th/","https://gold-api.com/"};
            new AlertDialog.Builder(this).setTitle("แหล่งข้อมูลอ้างอิง").setItems(titles,(d,i)->{if(i<urls.length)openUrl(urls[i]);else showFuelDetails();}).setPositiveButton("ปิด",null).show();});
        if(fuel){fuelBrand.setOnItemSelectedListener(selected(this::loadFuel));fuelFilter.setOnItemSelectedListener(selected(this::renderFuel));provincePicker.setOnItemSelectedListener(selected(()->{getSharedPreferences("fuel53",0).edit().putInt("province",provincePicker.getSelectedItemPosition()).apply();loadFuel();}));districtPicker.setOnItemSelectedListener(selected(()->{districtName.setVisibility(districtPicker.getSelectedItemPosition()==1?View.VISIBLE:View.GONE);loadFuel();}));}
    }
    private void showFuelDetails(){new AlertDialog.Builder(this).setTitle("วินิจฉัยราคาน้ำมัน").setMessage((pttProblem==null?"":"PTT OR: "+pttProblem+"\n")+(bangchakProblem==null?"":"บางจาก: "+bangchakProblem)).setPositiveButton("ปิด",null).show();}
    private LinearLayout marketCard(String name,String status,int color,String unit){
        LinearLayout card=vertical();card.setPadding(dp(12),dp(12),dp(12),dp(12));card.setBackground(bordered(0xff19222a,0xff3d4954,12));
        LinearLayout head=horizontal();head.setGravity(Gravity.CENTER_VERTICAL);head.addView(label(name,20,WHITE,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView tag=label(status,11,color,true);tag.setPadding(dp(4),dp(5),dp(4),dp(5));head.addView(tag);card.addView(head);space(card,7);card.addView(label(unit,12,MUTED,false));space(card,8);rule(card);return card;
    }
    private String fuelCode(String n){String x=n.toLowerCase(Locale.ROOT);String grade=FreshnessPolicy.commonFuelType(n);if(x.contains("b20"))return "B20";if(x.contains("b7")&&grade.equals("Diesel"))return "B7";return grade.isEmpty()?"อื่น ๆ":grade;}
    private int fuelColor(String grade){switch(grade){case "Gasohol 95":return 0xffffcf27;case "Gasohol 91":return 0xffe97624;case "E20":return 0xff57b72e;case "E85":return 0xff773cbd;case "Diesel":case "B7":case "B20":return 0xff086bbd;default:return 0xff455660;}}
    private void addFuelRow(LinearLayout card,String name,double price){
        String grade=fuelCode(name);LinearLayout row=horizontal();row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(8),0,dp(8));
        TextView badge=label(grade,11,(grade.equals("Gasohol 95")||grade.equals("E20"))?Color.BLACK:WHITE,true);badge.setGravity(Gravity.CENTER);badge.setPadding(dp(3),dp(7),dp(3),dp(7));badge.setBackground(bordered(fuelColor(grade),fuelColor(grade),7));row.addView(badge,new LinearLayout.LayoutParams(dp(86),-2));
        TextView product=label(name,13,WHITE,false);product.setPadding(dp(8),0,dp(6),0);row.addView(product,new LinearLayout.LayoutParams(0,-2,1));row.addView(label(String.format(Locale.US,"%.2f",price),22,WHITE,true),new LinearLayout.LayoutParams(-2,-2));card.addView(row);rule(card);
    }
    private void attachCard(LinearLayout card,String footer){space(card,9);TextView source=label(footer,11,MUTED,false);source.setLineSpacing(dp(2),1);card.addView(source);liveResults.addView(card,new LinearLayout.LayoutParams(-1,-2));space(liveResults,8);}
    private void emptyCard(String name,boolean checking){LinearLayout c=marketCard(name,checking?"กำลังตรวจสอบ":"ไม่มีข้อมูล",checking?MUTED:WARNING,"");TextView empty=label(checking?"กำลังดึงราคาใหม่":"ยังไม่มีราคาที่ตรวจสอบได้",15,WHITE,true);empty.setPadding(0,dp(10),0,dp(8));c.addView(empty);attachCard(c,checking?"":"ยังไม่มีราคาสำรองสำหรับรายการนี้");}
    private void startPolling(){
        handler.removeCallbacks(periodic);
        if(paused)return;
        if(currentMode==5){loadFuel();handler.postDelayed(periodic,15*60*1000L);}
        else if(currentMode==6){loadMetal();if(marketIndex()<2)handler.postDelayed(periodic,marketIndex()==0?60*1000L:5*60*1000L);}
    }
    private int currentFuelBrand(){return fuelBrand==null?0:Math.max(0,fuelBrand.getSelectedItemPosition());}
    private void loadFuel(){
        if(currentMode!=5||liveResults==null||liveStatus==null||paused)return;
        int token=++fetchToken;
        if(!referenceArea()){renderRegionalUnavailable();return;}
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
        if(!referenceArea()){renderRegionalUnavailable();return;}
        liveResults.removeAllViews();int brand=currentFuelBrand();String selected=String.valueOf(fuelFilter.getSelectedItem());long now=System.currentTimeMillis();
        if(brand==0||brand==1){
            if(latestPtt==null)emptyCard("PTT OR",!pttFinished);
            else{
                boolean recent=true;long stamp=0;String rawTime="";int count=0;
                for(OilPriceParser.Item e:latestPtt.items)if("ทั้งหมด".equals(selected)||selected.equals(FreshnessPolicy.commonFuelType(e.product))){recent&=e.isRecent(now);if(e.publishedMillis>=stamp){stamp=e.publishedMillis;rawTime=e.publishedAt;}}
                String state=pttCached?"ราคาที่บันทึกไว้":recent?"✓ ข้อมูลล่าสุด":"ข้อมูลย้อนหลัง";
                LinearLayout c=marketCard("PTT OR",state,pttCached||!recent?WARNING:0xff95dfbf,"Bangkok reference · บาท/ลิตร");
                for(OilPriceParser.Item e:latestPtt.items)if("ทั้งหมด".equals(selected)||selected.equals(FreshnessPolicy.commonFuelType(e.product))){addFuelRow(c,e.product,e.price);count++;}
                if(count==0)c.addView(label("ไม่มีราคาสำหรับชนิดที่เลือก",14,MUTED,false));
                attachCard(c,"ประกาศ: "+(stamp>0?formatSavedAt(stamp):rawTime.isEmpty()?"ไม่ระบุ":rawTime)+"\nแหล่งข้อมูล: PTT OR"+(pttCached?" · แคช "+formatSavedAt(pttCachedAt):""));
            }
        }
        if(brand==0||brand==2){
            if(latestFuel==null)emptyCard("บางจาก",!bangchakFinished);
            else{LinearLayout c=marketCard("บางจาก",bangchakCached?"ราคาที่บันทึกไว้":latestFuel.fresh?"✓ ข้อมูลล่าสุด":"ข้อมูลย้อนหลัง",bangchakCached||!latestFuel.fresh?WARNING:0xff95dfbf,"Bangkok reference · บาท/ลิตร");int count=0;
                for(PriceService.FuelItem e:latestFuel.items)if("ทั้งหมด".equals(selected)||selected.equals(FreshnessPolicy.commonFuelType(e.name))){addFuelRow(c,e.name,e.price);count++;}if(count==0)c.addView(label("ไม่มีราคาสำหรับชนิดที่เลือก",14,MUTED,false));
                attachCard(c,"ประกาศ: "+latestFuel.updateString+"\nแหล่งข้อมูล: บางจาก"+(bangchakCached?" · แคช "+formatSavedAt(bangchakCachedAt):""));}
        }
        boolean failed=pttProblem!=null||bangchakProblem!=null;
        liveStatus.setText(failed?"อัปเดตไม่สำเร็จ · แตะดูรายละเอียด":"");liveStatus.setVisibility(failed?View.VISIBLE:View.GONE);liveStatus.setTextColor(WARNING);liveStatus.setOnClickListener(failed?v->showFuelDetails():null);
    }
    private int marketIndex(){int i=goldMarket==null?0:goldMarket.getSelectedItemPosition();return i==1&&metalMode!=null&&metalMode.getSelectedItemPosition()==0?4:Math.max(0,i);}
    private boolean referenceArea(){return provincePicker==null||(provincePicker.getSelectedItemPosition()==0&&(districtPicker==null||districtPicker.getSelectedItemPosition()==0));}
    private void renderRegionalUnavailable(){liveResults.removeAllViews();liveStatus.setVisibility(View.VISIBLE);liveStatus.setText("Regional data unavailable · "+provincePicker.getSelectedItem());liveStatus.setTextColor(WARNING);liveStatus.setOnClickListener(null);liveResults.addView(description("ยังไม่มี API รายจังหวัด/อำเภอที่ตรวจสอบได้สำหรับแบรนด์นี้ — ไม่ใช้ราคากรุงเทพฯ แทน"));TextView official=button("Open OR regional price information ↗",FUNCTION,WHITE);liveResults.addView(official,new LinearLayout.LayoutParams(-1,dp(48)));official.setOnClickListener(v->openUrl("https://www.pttor.com/news/oil-price"));}
    private void loadMetal(){
        if(currentMode!=6||liveResults==null||liveStatus==null||paused)return;
        if(marketIndex()>=2){fetchToken++;liveResults.removeAllViews();liveStatus.setVisibility(View.VISIBLE);liveStatus.setTextColor(MUTED);liveStatus.setOnClickListener(null);if(stream==null)stream=new MarketStream(this);stream.attach(liveResults,marketIndex()==2?"Crypto":marketIndex()==3?"Forex":"Metals",liveStatus);return;}
        if(stream!=null)stream.stop();
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
    private void marketStatus(String failure,boolean checking,boolean cached){liveStatus.setVisibility(View.VISIBLE);liveStatus.setText(checking?"กำลังตรวจสอบราคาใหม่":failure!=null?"อัปเดตไม่สำเร็จ · แตะดูรายละเอียด":cached?"ราคาที่บันทึกไว้ (ไม่ใช่ราคาสด)":"");liveStatus.setTextColor(failure!=null||cached?WARNING:MUTED);liveStatus.setVisibility(liveStatus.getText().length()==0?View.GONE:View.VISIBLE);liveStatus.setOnClickListener(failure==null?null:v->new AlertDialog.Builder(this).setTitle("รายละเอียดการเชื่อมต่อ").setMessage(failure).setPositiveButton("ปิด",null).show());}
    private void renderThaiGold(PriceService.ThaiGoldData data,boolean cache,long savedAt,String failure,boolean checking){
        if(liveResults==null||liveStatus==null)return;liveResults.removeAllViews();
        if(data==null)emptyCard("ทองไทย 96.5%",checking);
        else{LinearLayout c=marketCard("ทองไทย 96.5%",cache?"ราคาที่บันทึกไว้":data.fresh?"✓ ข้อมูลล่าสุด":"ข้อมูลย้อนหลัง",cache||!data.fresh?WARNING:0xff95dfbf,"บาท / บาททองคำ");
        c.addView(priceRow("ทองคำแท่ง · รับซื้อ",formatMoney(data.buyBar)),new LinearLayout.LayoutParams(-1,dp(46)));rule(c);
        c.addView(priceRow("ทองคำแท่ง · ขายออก",formatMoney(data.sellBar)),new LinearLayout.LayoutParams(-1,dp(46)));rule(c);
        c.addView(priceRow("รูปพรรณ · ฐานรับซื้อ",formatMoney(data.buyOrnament)),new LinearLayout.LayoutParams(-1,dp(46)));rule(c);
        c.addView(priceRow("รูปพรรณ · ขายออก",formatMoney(data.sellOrnament)),new LinearLayout.LayoutParams(-1,dp(46)));rule(c);
        attachCard(c,"ประกาศ: "+formatThaiTime(data.publishedAt)+(data.updateRound>0?" · ครั้งที่ "+data.updateRound:"")+"\nแหล่งข้อมูล: "+data.source+" · ไม่รวมค่ากำเหน็จ"+(cache?"\nแคช: "+formatSavedAt(savedAt):""));}
        marketStatus(failure,checking,cache&&data!=null);
    }
    private void renderSpot(PriceService.MetalData data,boolean cache,long savedAt,String failure,boolean checking){
        if(liveResults==null||liveStatus==null)return;liveResults.removeAllViews();
        if(data==null)emptyCard("ราคา Spot",checking);
        else{LinearLayout c=marketCard(data.symbol,cache?"ราคาที่บันทึกไว้":data.fresh?"✓ ข้อมูลล่าสุด":"ข้อมูลย้อนหลัง",cache||!data.fresh?WARNING:0xff95dfbf,"USD / ทรอยออนซ์");c.addView(priceRow(data.symbol,formatMoney(data.price)),new LinearLayout.LayoutParams(-1,dp(64)));attachCard(c,"ประกาศ: "+formatThaiTime(data.updatedAt)+"\nแหล่งข้อมูล: Gold API"+(cache?" · แคช "+formatSavedAt(savedAt):""));}
        marketStatus(failure,checking,cache&&data!=null);
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
        row.setBackgroundColor(Color.TRANSPARENT);
        TextView label=label(name,14,WHITE,false);row.addView(label,new LinearLayout.LayoutParams(0,-2,1));
        TextView price=label(value,20,WHITE,true);row.addView(price,new LinearLayout.LayoutParams(-2,-2));
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
    @Override public boolean onKeyDown(int code,KeyEvent e){if(currentMode>=3)return super.onKeyDown(code,e);
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
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values){
            @Override public View getView(int position,View convertView,ViewGroup parent){
                TextView t=(TextView)super.getView(position,convertView,parent);t.setTextColor(WHITE);t.setTextSize(16);t.setPadding(dp(8),0,dp(8),0);return t;
            }
            @Override public View getDropDownView(int position,View convertView,ViewGroup parent){
                TextView t=(TextView)super.getDropDownView(position,convertView,parent);t.setTextColor(Color.BLACK);return t;
            }
        };adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);return adapter;
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
        b.setBackground(ripple(HEADER,dp(3)));b.setOnClickListener(v->tap.run());b.setLayoutParams(new LinearLayout.LayoutParams(dp(32),-1));return b;}
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
