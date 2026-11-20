package com.beatoflove.standardcalculator;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.time.Instant;

/** Network only: never injects hard-coded prices, clearly identifies the upstream feed. */
public final class PriceService {
    public static final String FUEL_SOURCE="https://oil-price.bangchak.co.th/ApiOilPrice2/th";
    public static final String METAL_SOURCE="https://api.gold-api.com/price/";
    public static final String PTT_SOURCE="https://orapiweb.pttor.com/oilservice/OilPrice.asmx";
    public static final String THAI_GOLD_SOURCE="https://www.thaigoldtoday.com/api/gold-price";
    public static final String THAI_GOLD_BACKUP_SOURCE="https://rakhagold.com/wp-json/priceradar/v1/gold/latest";
    private PriceService() { }

    public interface Callback<T> { void done(T value, String error); }
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private interface Task<T> { T fetch() throws Exception; }
    private static <T> void async(Task<T> task, Callback<T> callback) {
        new Thread(() -> {
            T result=null; String problem=null;
            try { result=task.fetch(); }
            catch(Exception ex) { problem=ex.getMessage()==null?"ดึงข้อมูลไม่สำเร็จ":ex.getMessage(); }
            final T value=result; final String error=problem;
            MAIN.post(() -> callback.done(value,error));
        },"BeatPriceFetch").start();
    }
    private static String read(String endpoint) throws Exception {
        HttpURLConnection con=(HttpURLConnection)new URL(endpoint).openConnection();
        con.setConnectTimeout(10000); con.setReadTimeout(10000);
        con.setRequestProperty("Accept","application/json");
        con.setRequestProperty("User-Agent","SirawitLabs-Calculator/5.3 (Android)");
        try {
            int code=con.getResponseCode();
            if(code!=200) throw new Exception("HTTP "+code);
            StringBuilder out=new StringBuilder();
            try(BufferedReader b=new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))){
                String s;while((s=b.readLine())!=null){out.append(s);if(out.length()>500000) throw new Exception("ข้อมูลเกินขนาด");}
            }
            return out.toString();
        } finally { con.disconnect(); }
    }
    // Public Rakhagold API permits at most one call per minute; share the last response
    // within this process, including failures, to protect against repeated refresh taps.
    private static long goldAttempt=-60_000L;
    private static String goldBody,goldError;
    private static synchronized String readRakhagold() throws Exception {
        long now=android.os.SystemClock.elapsedRealtime();
        if(now-goldAttempt<60_000L){if(goldBody!=null)return goldBody;throw new Exception(goldError==null?"รอรอบอัปเดต":goldError);}
        goldAttempt=now;goldBody=null;goldError=null;
        try{return goldBody=read(THAI_GOLD_BACKUP_SOURCE);}catch(Exception ex){goldError=ex.getMessage();throw ex;}
    }
    public static void reference(String from,String to,Callback<JSONObject> cb){
        if(!from.matches("[A-Z]{3}")||!to.matches("[A-Z]{3}")){cb.done(null,"Invalid currency");return;}
        async(()->new JSONObject(read("https://api.frankfurter.dev/v2/rate/"+from+"/"+to)),cb);
    }
    public static void ptt(Callback<OilPriceParser.Feed> cb) {
        async(() -> PttOilService.fetchFeed(),cb);
    }
    private static String marketError(Exception ex) {
        String message=ex.getMessage()==null?"ไม่สามารถอ่านข้อมูลได้":ex.getMessage();
        if(message.contains("HTTP 402"))return "จำกัดสิทธิ์การเข้าถึง (402)";
        if(message.contains("HTTP 429"))return "เกินจำนวนครั้งที่อนุญาต (429)";
        if(message.contains("HTTP 403"))return "ถูกปฏิเสธการเข้าถึง (403)";
        return message.length()>100?message.substring(0,100):message;
    }
    public static final class ThaiGoldData {
        public final double buyBar,sellBar,buyOrnament,sellOrnament;
        public final String publishedAt;
        public final boolean fresh;
        public final int updateRound;
        public final String source;
        ThaiGoldData(double a,double b,double c,double d,String at,boolean fresh,int round){
            this(a,b,c,d,at,fresh,round,"ThaiGoldToday");
        }
        ThaiGoldData(double a,double b,double c,double d,String at,boolean fresh,int round,String source){
            this.buyBar=a;this.sellBar=b;this.buyOrnament=c;this.sellOrnament=d;
            this.publishedAt=at;this.fresh=fresh;this.updateRound=round;this.source=source;
        }
    }
    public static void thaiGold(Callback<ThaiGoldData> cb) {
        async(() -> {
            String originalFailure="";
            try { return GoldFallbackParser.parse(readRakhagold()); }
            catch(Exception e) { originalFailure=marketError(e); }
            // Alternate source may require access authorization.
            try { return parseThaiGold(read(THAI_GOLD_SOURCE)); }
            catch(Exception e) {
                throw new Exception("แหล่งราคาทองไทยไม่พร้อม — Rakhagold: "
                    +originalFailure+"; ThaiGoldToday: "+marketError(e));
            }
        },cb);
    }
    public static ThaiGoldData parseThaiGold(String json) throws Exception {
        JSONObject current=new JSONObject(json).optJSONObject("current");
        if(current==null)throw new Exception("ไม่มีข้อมูลราคาทองไทย");
        double buyBar=current.optDouble("buyBar",Double.NaN),sellBar=current.optDouble("sellBar",Double.NaN);
        double buyOrn=current.optDouble("buyOrnament",Double.NaN),sellOrn=current.optDouble("sellOrnament",Double.NaN);
        for(double p:new double[]{buyBar,sellBar,buyOrn,sellOrn}) {
            if(!Double.isFinite(p)||p<=0||p>1000000)throw new Exception("ค่าราคาทองผิดรูปแบบ");
        }
        String stamp=current.optString("updatedAt","");long ms=0;
        try {ms=Instant.parse(stamp).toEpochMilli();}
        catch(Exception ex){try{ms=java.time.OffsetDateTime.parse(stamp).toInstant().toEpochMilli();}catch(Exception ignored){}}
        boolean fresh=FreshnessPolicy.isRecent(ms,System.currentTimeMillis(),24L*60*60*1000);
        return new ThaiGoldData(buyBar,sellBar,buyOrn,sellOrn,stamp,fresh,current.optInt("updateRound",0),current.optString("source","ThaiGoldToday"));
    }
    public static final class FuelItem {
        public final String name; public final double price;
        FuelItem(String n,double p){name=n;price=p;}
    }
    public static final class FuelData {
        public final String dateString; public final String updateString;
        public final boolean fresh;
        public final ArrayList<FuelItem> items;
        FuelData(String d, String u, boolean f, ArrayList<FuelItem> i){dateString=d;updateString=u;fresh=f;items=i;}
    }
    public static final class MetalData {
        public final String symbol;public final double price;
        public final String currency;public final String updatedAt;public final boolean fresh;
        MetalData(String s,double p,String c,String d,boolean f){symbol=s;price=p;currency=c;updatedAt=d;fresh=f;}
    }
    public static void fuel(Callback<FuelData> cb){async(() -> parseFuel(read(FUEL_SOURCE)),cb);}
    public static void metal(String symbol, Callback<MetalData> cb){
        if(!("XAU".equals(symbol)||"XAG".equals(symbol)||"XPT".equals(symbol)||"XPD".equals(symbol))) {
            cb.done(null,"ไม่รองรับโลหะชนิดนี้");return;
        }
        async(() -> parseMetal(read(METAL_SOURCE+symbol),symbol),cb);
    }
    public static FuelData parseFuel(String json) throws Exception {
        JSONArray data=new JSONArray(json);
        if(data.length()==0)throw new Exception("แหล่งข้อมูลน้ำมันว่าง");
        JSONObject root=data.getJSONObject(0);
        String date=root.optString("OilDateNow", "");
        String pub=root.optString("OilPriceDate","");
        String time=root.optString("OilPriceTime", "");
        JSONArray list=new JSONArray(root.optString("OilList","[]"));
        ArrayList<FuelItem> prices=new ArrayList<>();
        for(int i=0;i<list.length();i++){
            JSONObject v=list.getJSONObject(i);
            String name=v.optString("OilName","").trim();double p=v.optDouble("PriceToday",Double.NaN);
            if(!name.isEmpty()&&Double.isFinite(p)&&p>0&&p<200) prices.add(new FuelItem(name,p));
        }
        if(prices.isEmpty())throw new Exception("ไม่มีราคาที่ตรวจสอบได้");
        // OilDateNow is the service's observation day, not the day the phone requested it.
        Date observed=parseThaiDate(date);
        long age=observed==null?Long.MAX_VALUE:Math.abs(System.currentTimeMillis()-observed.getTime());
        boolean fresh=age < 3L*24*60*60*1000; // date may be at local midnight; allow a 48–72 h tolerance
        return new FuelData(date, pub+(time.isEmpty()?"":" "+time), fresh, prices);
    }
    static Date parseThaiDate(String raw) {
        try {
            String[] parts=raw.trim().split("/");
            if(parts.length!=3) return null;
            int day=Integer.parseInt(parts[0]),month=Integer.parseInt(parts[1]),year=Integer.parseInt(parts[2]);
            if(year>2400)year-=543;
            SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy",Locale.US);
            format.setLenient(false);format.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));
            return format.parse(String.format(Locale.US,"%02d/%02d/%04d",day,month,year));
        }catch(Exception e){return null;}
    }
    public static MetalData parseMetal(String json,String expectedSymbol) throws Exception {
        JSONObject root=new JSONObject(json);
        String symbol=root.optString("symbol","");
        double price=root.optDouble("price",Double.NaN);
        String currency=root.optString("currency","USD");
        String stamp=root.optString("updatedAt","");
        if(!expectedSymbol.equals(symbol)||!Double.isFinite(price)||price<=0) throw new Exception("ราคาโลหะไม่ถูกต้อง");
        if(!"USD".equals(currency)) throw new Exception("แหล่งข้อมูลไม่ได้ส่งราคา USD");
        long updated=0;
        try {updated=Instant.parse(stamp).toEpochMilli();}
        catch(Exception e) {try{updated=java.time.OffsetDateTime.parse(stamp).toInstant().toEpochMilli();}catch(Exception ignored){}}
        boolean fresh=updated>0 && System.currentTimeMillis()-updated>=0 && System.currentTimeMillis()-updated<10*60*1000L;
        return new MetalData(symbol,price,currency,stamp,fresh);
    }
}
