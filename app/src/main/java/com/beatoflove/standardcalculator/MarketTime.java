package com.beatoflove.standardcalculator;
import java.time.*;
import java.time.format.*;
import java.util.Locale;
/** Convert only explicit publisher dates/times; never substitute device time. */
public final class MarketTime {
 private MarketTime(){}
 public static String normalize(String date,String time){
  date=date==null?"":date.trim();time=time==null?"":time.trim();
  for(String v:new String[]{time,date}){
   try{return Instant.parse(v).toString();}catch(Exception ignored){}
   try{return OffsetDateTime.parse(v).toInstant().toString();}catch(Exception ignored){}
   try{return LocalDateTime.parse(v.replace(' ','T')).atZone(ZoneId.of("Asia/Bangkok")).toInstant().toString();}catch(Exception ignored){}
  }
  try {
   LocalDate d=null;
   for(String pattern:new String[]{"uuuu-MM-dd","d/M/uuuu","d-M-uuuu"})try{d=LocalDate.parse(date,DateTimeFormatter.ofPattern(pattern,Locale.US).withResolverStyle(ResolverStyle.STRICT));break;}catch(Exception ignored){}
   if(d==null)return "";if(d.getYear()>2400)d=d.minusYears(543);
   time=time.replace("น.","").trim(); LocalTime t=null;
   for(String pattern:new String[]{"H:mm:ss","H:mm"})try{t=LocalTime.parse(time,DateTimeFormatter.ofPattern(pattern,Locale.US).withResolverStyle(ResolverStyle.STRICT));break;}catch(Exception ignored){}
   if(t==null)return "";return d.atTime(t).atZone(ZoneId.of("Asia/Bangkok")).toInstant().toString();
  }catch(Exception e){return "";}
 }
}
