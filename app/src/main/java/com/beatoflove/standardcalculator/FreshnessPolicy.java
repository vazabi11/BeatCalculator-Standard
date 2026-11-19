package com.beatoflove.standardcalculator;

/** Time policy is based on publisher timestamp, not request time. */
public final class FreshnessPolicy {
    private FreshnessPolicy() {}
    public static boolean isRecent(long publisherEpochMillis,long nowMillis,long maxAgeMillis) {
        long age=nowMillis-publisherEpochMillis;
        return publisherEpochMillis>0 && age>=-5*60*1000L && age<=maxAgeMillis;
    }
    /** Normalizes equivalent *regular* motor fuels only. Premium remains distinct. */
    public static String commonFuelType(String name) {
        if(name==null)return "";
        String s=name.toLowerCase(java.util.Locale.ROOT).replaceAll("[\\s_\\-]", "");
        if(s.contains("premium")||s.contains("vpower")||s.contains("superpower")||s.contains("hi-premium"))return "";
        if(s.contains("e85"))return "E85";
        if(s.contains("e20"))return "E20";
        if(s.contains("gasohol95")||s.contains("แก๊สโซฮอล์95")||s.contains("แก๊สโซฮอล95"))return "Gasohol 95";
        if(s.contains("gasohol91")||s.contains("แก๊สโซฮอล์91")||s.contains("แก๊สโซฮอล91"))return "Gasohol 91";
        if(s.contains("diesel")||s.contains("ดีเซล"))return "Diesel";
        return "";
    }
}
