package com.beatoflove.standardcalculator;
public class MarketTimeTest {
 public static void main(String[] args){
  String expected="2026-11-19T10:01:00Z";
  String[][] cases={{"2026-11-19","17:01"},{"19/11/2569","17:01 น."},{"2026-11-19","2026-11-19 17:01:00"},{"","2026-11-19T17:01:00+07:00"}};
  for(String[] a:cases)if(!expected.equals(MarketTime.normalize(a[0],a[1])))throw new AssertionError(java.util.Arrays.toString(a));
  if(!MarketTime.normalize("2026-11-19","").isEmpty())throw new AssertionError("missing time");
  if(!MarketTime.normalize("2026-02-30","17:01").isEmpty())throw new AssertionError("invalid date");
  System.out.println("PASS 6 publisher time tests");
 }
}
