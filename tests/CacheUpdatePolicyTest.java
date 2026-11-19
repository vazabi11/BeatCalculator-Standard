package com.beatoflove.standardcalculator;

public final class CacheUpdatePolicyTest {
    private static int assertions;
    private static void check(boolean condition, String text) {
        if (!condition) throw new AssertionError(text);
        assertions++;
    }
    public static void main(String[] args) {
        check(CacheUpdatePolicy.shouldAccept(0,0),"first undated price accepted after validation");
        check(CacheUpdatePolicy.shouldAccept(0,100),"first dated price accepted");
        check(CacheUpdatePolicy.shouldAccept(100,101),"newer publisher update accepted");
        check(CacheUpdatePolicy.shouldAccept(100,100),"same announcement revision accepted");
        check(!CacheUpdatePolicy.shouldAccept(101,100),"older announcement rejected");
        check(!CacheUpdatePolicy.shouldAccept(101,0),"unknown date cannot replace known newer price");
        check(CacheUpdatePolicy.isCachedDisplay(false,false),"offline cached data not live");
        check(CacheUpdatePolicy.isCachedDisplay(false,true),"failed refresh cached data not live");
        check(CacheUpdatePolicy.isCachedDisplay(true,true),"refresh failure not live");
        check(!CacheUpdatePolicy.isCachedDisplay(true,false),"successful network refresh not cache label");
        System.out.println("PASS " + assertions + " cache policy tests");
    }
}
