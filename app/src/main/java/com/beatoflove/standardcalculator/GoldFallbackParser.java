package com.beatoflove.standardcalculator;

import org.json.JSONArray;
import org.json.JSONObject;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Iterator;

/** Strict independent adapter for documented public Thai-gold JSON endpoints.
 *  Never uses arbitrary numeric fields or a synthetic current timestamp. */
public final class GoldFallbackParser {
    private GoldFallbackParser() {}
    private static JSONObject nested(JSONObject root, String... names) {
        for (String n:names) {
            JSONObject x=root.optJSONObject(n);
            if(x!=null) return x;
        }
        return null;
    }
    private static double number(JSONObject o, String... names) {
        if(o==null)return Double.NaN;
        for(String key:names){
            Object v=o.opt(key);
            if(v==null || JSONObject.NULL.equals(v))continue;
            try {
                String str=v.toString().trim().replace(",", "");
                double x=Double.parseDouble(str);
                if(Double.isFinite(x)&&x>0&&x<1_000_000) return x;
            }catch(Exception ignored){}
        }
        return Double.NaN;
    }
    private static String stamp(JSONObject o) {
        if(o==null)return "";
        String[] keys={"updatedAt","updated_at","announcedAt","announced_at","timestamp","publishedAt","price_date"};
        for(String k:keys){String s=o.optString(k,"").trim();if(!s.isEmpty())return s;}
        return "";
    }
    private static boolean verifiedTime(String stamp) {
        try {return Instant.parse(stamp).toEpochMilli()>0;}
        catch(Exception e){try{return OffsetDateTime.parse(stamp).toInstant().toEpochMilli()>0;}catch(Exception ignored){return false;}}
    }
    private static PriceService.ThaiGoldData decode(JSONObject object, String origin) throws Exception {
        JSONObject bar=nested(object,"gold_bar","goldBar","bar","goldbar","gold_bar_965");
        JSONObject ornament=nested(object,"ornament","gold_ornament","goldOrnament","jewelry","gold_jewelry");
        double bb=bar==null?number(object,"buyBar","buy_bar","goldBarBuy","gold_bar_buy"):number(bar,"buy","buyPrice","bid","buy_price");
        double bs=bar==null?number(object,"sellBar","sell_bar","goldBarSell","gold_bar_sell"):number(bar,"sell","sellPrice","ask","sell_price");
        double ob=ornament==null?number(object,"buyOrnament","buy_ornament","ornamentBuy","ornament_buy"):number(ornament,"buy","buyPrice","bid","buy_price");
        double os=ornament==null?number(object,"sellOrnament","sell_ornament","ornamentSell","ornament_sell"):number(ornament,"sell","sellPrice","ask","sell_price");
        for(double n:new double[]{bb,bs,ob,os})if(!Double.isFinite(n))throw new Exception("ไม่พบราคาทองไทยครบทั้ง 4 ช่อง");
        if(bs<bb || os<ob)throw new Exception("ราคาซื้อขายกลับด้าน");
        String timestamp=stamp(object);
        if(timestamp.isEmpty()) throw new Exception("ไม่มีวันเวลาประกาศ");
        if(!verifiedTime(timestamp)) throw new Exception("เวลาประกาศไม่ใช่ ISO-8601 พร้อมเขตเวลา");
        long updated=PriceCache.epoch(timestamp);
        if(updated>System.currentTimeMillis()+10*60_000L)throw new Exception("เวลาประกาศเป็นอนาคต");
        boolean fresh=FreshnessPolicy.isRecent(updated,System.currentTimeMillis(),24L*60*60*1000L);
        int round=object.optInt("updateRound",object.optInt("announceNo",object.optInt("round",0)));
        return new PriceService.ThaiGoldData(bb,bs,ob,os,timestamp,fresh,round,origin);
    }
    public static PriceService.ThaiGoldData parse(String raw) throws Exception {
        if(raw==null || raw.length()>500_000)throw new Exception("ขนาดข้อมูลไม่ถูกต้อง");
        JSONObject root=new JSONObject(raw);
        String error="ยังไม่พบข้อมูลทองไทยที่เป็นตัวเลขครบ 4 ช่องพร้อมเวลา";
        // Explicit candidates only; never recurse into untrusted unrelated asset prices.
        JSONObject[] candidates={root, root.optJSONObject("current"),root.optJSONObject("data"),root.optJSONObject("latest"),root.optJSONObject("result")};
        for(JSONObject obj:candidates){
            if(obj==null)continue;
            try {
                String timestamp=stamp(obj);
                if(timestamp.isEmpty() && obj!=root && !stamp(root).isEmpty()) {
                    JSONObject copy=new JSONObject(obj.toString()); copy.put("updatedAt",stamp(root));obj=copy;
                }
                return decode(obj,"Rakhagold");
            }catch(Exception ex){error=ex.getMessage()==null?error:ex.getMessage();}
        }
        throw new Exception("รูปแบบราคาจาก API สำรองไม่ตรงเอกสารที่รองรับ: "+error);
    }
}
