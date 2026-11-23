package com.beatoflove.standardcalculator;

/** No network/Android dependencies: deterministic publisher-time comparison tests. */
public final class GoldSourcePolicyTest {
    private static int count;
    private static void check(boolean value, String note) {
        if (!value) throw new AssertionError(note);
        count++;
    }
    public static void main(String[] unused) {
        long now=1_795_428_000_000L;
        long today=now-30_000L, old=now-4L*24*60*60*1000;
        check(GoldSourcePolicy.choose(today,old,now)==0,"fresh primary wins");
        check(GoldSourcePolicy.choose(old,today,now)==1,"newer fallback wins");
        check(GoldSourcePolicy.choose(today,today,now)==0,"tie favors primary");
        check(GoldSourcePolicy.choose(0,today,now)==1,"primary unavailable");
        check(GoldSourcePolicy.choose(today,0,now)==0,"fallback unavailable");
        check(GoldSourcePolicy.choose(0,0,now)==-1,"no source");
        check(GoldSourcePolicy.choose(now+11*60_000L,today,now)==1,"future primary rejected");
        check(GoldSourcePolicy.choose(today,now+11*60_000L,now)==0,"future fallback rejected");
        check(GoldSourcePolicy.choose(old,old+1,now)==1,"both stale: newest publisher timestamp wins");
        check(!GoldSourcePolicy.validPublisher(-1,now),"negative timestamps invalid");
        System.out.println("PASS "+count+" gold source policy tests");
    }
}
