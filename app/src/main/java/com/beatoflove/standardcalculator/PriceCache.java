package com.beatoflove.standardcalculator;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;

/**
 * Disk-backed, last-known-good market prices.
 * Cache entries are written only after upstream parsing and value validation.
 * Retrieval never silently promotes a cached quote to a live quote.
 */
public final class PriceCache {
    private static final String STORE = "beat_calculator_market_cache_v1";
    private static final int SCHEMA = 1;
    private static final String PTT = "ptt_or";
    private static final String BANGCHAK = "bangchak";
    private static final String THAI_GOLD = "thai_gold";
    private PriceCache() {}

    public static final class Snapshot<T> {
        public final T data;
        /** Device time when the upstream response was last accepted. */
        public final long savedAtMillis;
        public final long publisherMillis;
        Snapshot(T value, long savedAt, long publishedAt) {
            data = value;
            savedAtMillis = savedAt;
            publisherMillis = publishedAt;
        }
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(STORE, Context.MODE_PRIVATE);
    }
    private static Snapshot<JSONObject> read(Context c, String key) {
        try {
            String raw = prefs(c).getString(key, null);
            if(raw == null || raw.length() > 100_000) return null;
            JSONObject wrapper = new JSONObject(raw);
            if(wrapper.optInt("schema") != SCHEMA) return null;
            long savedAt = wrapper.optLong("savedAt", 0);
            if(savedAt <= 0 || savedAt > System.currentTimeMillis() + 5*60_000L) return null;
            JSONObject data = wrapper.optJSONObject("payload");
            if(data == null) return null;
            return new Snapshot<>(data, savedAt, wrapper.optLong("publisherAt", 0));
        } catch (Exception ex) { return null; }
    }
    private static boolean write(Context c, String key, JSONObject payload, long publisherAt) {
        try {
            if (payload.toString().length() > 90_000) return false;
            Snapshot<JSONObject> previous = read(c, key);
            // Do not overwrite a known newer publisher quote with an older or undated one.
            if (previous != null &&
                    !CacheUpdatePolicy.shouldAccept(previous.publisherMillis, publisherAt)) return false;
            JSONObject wrapper = new JSONObject();
            wrapper.put("schema", SCHEMA);
            wrapper.put("savedAt", System.currentTimeMillis());
            wrapper.put("publisherAt", publisherAt);
            wrapper.put("payload", payload);
            // commit() provides durable completion before treating the quote as cached.
            return prefs(c).edit().putString(key, wrapper.toString()).commit();
        } catch (Exception ex) { return false; }
    }
    static long epoch(String value) {
        if(value == null || value.isEmpty()) return 0;
        try { return Instant.parse(value).toEpochMilli(); }
        catch (Exception ignored) {
            try { return OffsetDateTime.parse(value).toInstant().toEpochMilli(); }
            catch (Exception ex) { return 0; }
        }
    }

    public static boolean putPtt(Context c, OilPriceParser.Feed feed) {
        if(feed == null || feed.items.isEmpty()) return false;
        try {
            JSONArray array = new JSONArray();long published = 0;
            for(OilPriceParser.Item item:feed.items) {
                if(item.product == null || item.product.isEmpty() || item.product.length()>150 ||
                        !Double.isFinite(item.price) || item.price<=0 || item.price>=200) return false;
                JSONObject obj = new JSONObject();
                obj.put("name", item.product);obj.put("price",item.price);
                obj.put("published",item.publishedAt);obj.put("millis",item.publishedMillis);
                array.put(obj);
                published = Math.max(published, item.publishedMillis);
            }
            return write(c,PTT,new JSONObject().put("items",array),published);
        } catch(Exception ex){return false;}
    }
    public static Snapshot<OilPriceParser.Feed> getPtt(Context c) {
        Snapshot<JSONObject> cached = read(c,PTT);
        if(cached == null)return null;
        try {
            JSONArray rows=cached.data.getJSONArray("items");
            if(rows.length()==0 || rows.length()>100)return null;
            ArrayList<OilPriceParser.Item> items=new ArrayList<>();
            for(int i=0;i<rows.length();i++){
                JSONObject r=rows.getJSONObject(i);
                String name=r.getString("name");double price=r.getDouble("price");
                if(name.isEmpty()||name.length()>150||!Double.isFinite(price)||price<=0||price>=200)return null;
                items.add(new OilPriceParser.Item(name,price,r.optString("published",""),r.optLong("millis",0)));
            }
            return new Snapshot<>(new OilPriceParser.Feed(items),cached.savedAtMillis,cached.publisherMillis);
        } catch(Exception ex){return null;}
    }
    public static boolean putBangchak(Context c, PriceService.FuelData fuel) {
        if(fuel==null || fuel.items.isEmpty())return false;
        try {
            JSONArray items=new JSONArray();
            for(PriceService.FuelItem x:fuel.items){
                if(x.name==null||x.name.isEmpty()||x.name.length()>150||!Double.isFinite(x.price)||x.price<=0||x.price>=200)return false;
                items.put(new JSONObject().put("name",x.name).put("price",x.price));
            }
            JSONObject body=new JSONObject().put("date",fuel.dateString).put("update",fuel.updateString).put("items",items);
            java.util.Date d=PriceService.parseThaiDate(fuel.dateString);
            return write(c,BANGCHAK,body,d==null?0:d.getTime());
        }catch(Exception ex){return false;}
    }
    public static Snapshot<PriceService.FuelData> getBangchak(Context c) {
        Snapshot<JSONObject> cached=read(c,BANGCHAK);if(cached==null)return null;
        try {
            String date=cached.data.optString("date","");
            String update=cached.data.optString("update","");
            JSONArray rows=cached.data.getJSONArray("items");
            if(rows.length()==0||rows.length()>100)return null;
            ArrayList<PriceService.FuelItem> items=new ArrayList<>();
            for(int i=0;i<rows.length();i++){
                JSONObject o=rows.getJSONObject(i);String name=o.getString("name");double price=o.getDouble("price");
                if(name.isEmpty()||name.length()>150||!Double.isFinite(price)||price<=0||price>=200)return null;
                items.add(new PriceService.FuelItem(name,price));
            }
            java.util.Date d=PriceService.parseThaiDate(date);
            boolean fresh=d!=null&&FreshnessPolicy.isRecent(d.getTime(),System.currentTimeMillis(),3L*24*60*60*1000);
            return new Snapshot<>(new PriceService.FuelData(date,update,fresh,items),cached.savedAtMillis,cached.publisherMillis);
        }catch(Exception ex){return null;}
    }
    public static boolean putThaiGold(Context c, PriceService.ThaiGoldData gold) {
        if(gold==null)return false;
        try {
            JSONObject raw=new JSONObject().put("current",new JSONObject()
                    .put("buyBar",gold.buyBar).put("sellBar",gold.sellBar)
                    .put("buyOrnament",gold.buyOrnament).put("sellOrnament",gold.sellOrnament)
                    .put("updatedAt",gold.publishedAt).put("updateRound",gold.updateRound)
                    .put("source",gold.source));
            // Reparse to validate all four fields before persisting.
            PriceService.parseThaiGold(raw.toString());
            return write(c,THAI_GOLD,raw,epoch(gold.publishedAt));
        }catch(Exception ex){return false;}
    }
    public static Snapshot<PriceService.ThaiGoldData> getThaiGold(Context c) {
        Snapshot<JSONObject> cached=read(c,THAI_GOLD);if(cached==null)return null;
        try {PriceService.ThaiGoldData value=PriceService.parseThaiGold(cached.data.toString());
            String source=cached.data.optJSONObject("current").optString("source","ThaiGoldToday");
            if(!"ThaiGoldToday".equals(source)) value=new PriceService.ThaiGoldData(
                    value.buyBar,value.sellBar,value.buyOrnament,value.sellOrnament,
                    value.publishedAt,value.fresh,value.updateRound,source);
            return new Snapshot<>(value,cached.savedAtMillis,cached.publisherMillis);}
        catch(Exception ex){return null;}
    }
    private static String metalKey(String symbol) {
        switch(symbol) {case "XAU": case "XAG": case "XPT": case "XPD":return "metal_"+symbol;default:return null;}
    }
    public static boolean putMetal(Context c, PriceService.MetalData metal) {
        if(metal==null||metalKey(metal.symbol)==null)return false;
        try {
            JSONObject raw=new JSONObject().put("symbol",metal.symbol).put("price",metal.price)
                    .put("currency",metal.currency).put("updatedAt",metal.updatedAt);
            PriceService.parseMetal(raw.toString(),metal.symbol);
            return write(c,metalKey(metal.symbol),raw,epoch(metal.updatedAt));
        }catch(Exception ex){return false;}
    }
    public static Snapshot<PriceService.MetalData> getMetal(Context c, String symbol) {
        if(metalKey(symbol)==null)return null;
        Snapshot<JSONObject> cached=read(c,metalKey(symbol));if(cached==null)return null;
        try {return new Snapshot<>(PriceService.parseMetal(cached.data.toString(),symbol),cached.savedAtMillis,cached.publisherMillis);}
        catch(Exception ex){return null;}
    }
}
