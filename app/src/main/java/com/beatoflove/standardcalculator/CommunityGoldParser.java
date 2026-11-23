package com.beatoflove.standardcalculator;

import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Optional free community feed. All fields and original publisher time are mandatory. */
public final class CommunityGoldParser {
    private CommunityGoldParser() {}
    private static final Pattern CLOCK=Pattern.compile("(\\d{1,2}:\\d{2}(?::\\d{2})?)");
    private static final Pattern ROUND=Pattern.compile("ครั้งที่\\s*(\\d+)");
    private static double value(JSONObject obj,String key) throws Exception {
        if(obj==null || !obj.has(key) || obj.isNull(key))throw new Exception("Missing gold field: "+key);
        double n=Double.parseDouble(obj.get(key).toString().replace(",","").trim());
        if(!Double.isFinite(n) || n<=0 || n>1_000_000)throw new Exception("Invalid gold field: "+key);
        return n;
    }
    public static PriceService.ThaiGoldData parse(String raw) throws Exception {
        if(raw==null || raw.length()>500_000)throw new Exception("Invalid response size");
        JSONObject root=new JSONObject(raw);
        if(!"success".equalsIgnoreCase(root.optString("status","")))throw new Exception("Community feed not successful");
        JSONObject response=root.getJSONObject("response");
        JSONObject price=response.getJSONObject("price");
        JSONObject bar=price.getJSONObject("gold_bar");
        JSONObject ornaments=price.getJSONObject("gold");
        double bb=value(bar,"buy"),bs=value(bar,"sell");
        double ob=value(ornaments,"buy"),os=value(ornaments,"sell");
        if(bs<bb || os<ob)throw new Exception("Reversed buy/sell prices");
        String date=response.optString("update_date","");
        String time=response.optString("update_time","");
        Matcher clock=CLOCK.matcher(time);
        if(!clock.find())throw new Exception("Missing announced time");
        String stamp=MarketTime.normalize(date,clock.group(1));
        long published=PriceCache.epoch(stamp);
        long now=System.currentTimeMillis();
        if(!GoldSourcePolicy.validPublisher(published,now))throw new Exception("Invalid publisher timestamp");
        Matcher round=ROUND.matcher(time);
        int roundNumber=round.find()?Integer.parseInt(round.group(1)):0;
        return new PriceService.ThaiGoldData(bb,bs,ob,os,stamp,
            FreshnessPolicy.isRecent(published,now,24L*60*60*1000L),roundNumber,"Thai Gold API (Community)");
    }
}
