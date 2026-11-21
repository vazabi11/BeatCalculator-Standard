package com.beatoflove.standardcalculator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import java.util.*;
import java.util.regex.*;
import java.math.*;
import java.time.*;
import java.time.format.*;
import org.json.*;
import android.os.*;

/** Read public bank rate boards, never substitute reference rates or infer missing prices. */
final class BankRateService {
 static final String[] BANKS={"Bangkok Bank","Kasikornbank","SCB","Krungthai Bank","Krungsri","ttb"};
 static final String[] URLS={"https://www.bangkokbank.com/en/personal/other-services/view-rates/foreign-exchange-rates","https://www.kasikornbank.com/en/rate/Pages/Foreign-Exchange.aspx","https://www.scb.co.th/en/personal-banking/foreign-exchange-rates/","https://exchangerate.krungthai.com/counter-rate","https://www.krungsri.com/en/exchangerate/todayrates","https://www.ttbbank.com/en/rates/exchange-rates"};

 static void fetch(int bank,PriceService.Callback<JSONObject> cb){new Thread(()->{JSONObject data=null;String err=null;try{Document doc=Jsoup.connect(URLS[bank]).userAgent("SirawitLabs-SiraCalc/5.3.2").timeout(12000).maxBodySize(2000000).get();if(!doc.location().startsWith("https://"))throw new Exception("Insecure redirect");data=parse(bank,doc);}catch(Exception e){err=e.getMessage();}final JSONObject out=data;final String problem=err;new Handler(Looper.getMainLooper()).post(()->cb.done(out,problem));},"BankBoardFetch").start();}
 static BigDecimal num(String s,int unit){try{String t=s.trim().replace(",","");if(!t.matches("[0-9]+(?:\\.[0-9]+)?"))return null;BigDecimal n=new BigDecimal(t);if(n.signum()<=0||n.compareTo(new BigDecimal("100000"))>=0)return null;return n.divide(BigDecimal.valueOf(unit),MathContext.DECIMAL128);}catch(Exception e){return null;}}
 static JSONObject parse(int bank,Document doc)throws Exception{
  if(bank==3||bank==4)return parseRegionalBoard(bank,doc);
  // Require an actual rate header and a bank-issued date, not only a successful HTTP response.
  String page=doc.text();Matcher dm=Pattern.compile("(?i)(?:Date|Update as of)\\s*:?\\s*(\\d{1,2} [A-Za-z]+ \\d{4})(?:\\s*(?:Time)?\\s*:?\\s*(\\d{1,2}:\\d{2}(?::\\d{2})?))?").matcher(page);
  if(!dm.find())throw new Exception("Bank publication time not found");
  LocalDate day=LocalDate.parse(dm.group(1),DateTimeFormatter.ofPattern("d MMMM uuuu",Locale.ENGLISH));
  if(day.isAfter(LocalDate.now(ZoneId.of("Asia/Bangkok"))))throw new Exception("Future bank date");
  String stamp=day.toString()+(dm.group(2)==null?"":" "+dm.group(2));
  JSONArray rows=new JSONArray();Set<String> seen=new HashSet<>();
  for(Element table:doc.select("table")){
   String hdr=table.select("th").text().toLowerCase(Locale.ROOT);if(hdr.isEmpty())hdr=table.text().toLowerCase(Locale.ROOT);
   if(!hdr.contains("bank notes")&&!hdr.contains("banknotes")&&!hdr.contains("notes"))continue;
   if(bank==1&&!hdr.contains("telex transfer"))continue;
   if(bank==5&&(!hdr.contains("t/t")||!hdr.contains("bank buying")))continue;
   if(bank==0&&(!hdr.contains("sight bill")||!hdr.contains("tt")))continue;
   if(bank==2&&(!hdr.contains("bank sells")||!hdr.contains("bank buys")))continue;
   for(Element tr:table.select("tr")){
    Elements cells=tr.select("> td");int start=bank==0?2:1;int required=bank==2?7:start+5;if(cells.size()<required)continue;
    String label=cells.get(0).text().trim();if(label.length()<3)continue;String cc=label.substring(0,3);if(!cc.matches("[A-Z]{3}")||cc.equals("THB"))continue;
    String tail=label.substring(3);if(bank==0)tail+=" "+cells.get(1).text();
    String tier="";if(cc.equals("USD")){if(tail.matches("(?s).*50\\s*[-–]\\s*100.*"))tier="50-100";else if(tail.matches("(?s).*5\\s*[-–]\\s*20.*"))tier="5-20";else if(tail.matches("(?s).*1\\s*[-–]\\s*2.*")||tail.matches("(?s)^\\s*1(?:USD.*|\\s*$)"))tier="1-2";}
    int unit=1;if(bank==5){Matcher u=Pattern.compile("^\\s*:\\s*(1000|100)\\b").matcher(tail);if(u.find())unit=Integer.parseInt(u.group(1));else if(cc.equals("JPY")||cc.equals("KRW")||cc.equals("IDR")||cc.equals("VND"))continue;}
    // SCB's rendered board units are not yet confirmed for minor currencies.
    if(bank==2&&!Arrays.asList("USD","EUR","GBP","AUD","CAD","CHF","SGD","HKD","CNY","NZD").contains(cc))continue;
    BigDecimal nb,ns,tb,ts;
    if(bank==0){nb=num(cells.get(2).text(),unit);ns=num(cells.get(3).text(),unit);tb=num(cells.get(5).text(),unit);ts=num(cells.get(6).text(),unit);}
    else if(bank==2){ts=num(cells.get(1).text(),unit);ns=num(cells.get(2).text(),unit);tb=num(cells.get(3).text(),unit);nb=num(cells.get(6).text(),unit);}
    else{tb=num(cells.get(2).text(),unit);nb=num(cells.get(3).text(),unit);ts=num(cells.get(4).text(),unit);ns=num(cells.get(5).text(),unit);}
    if(cc.equals("USD")&&tier.isEmpty()){nb=null;ns=null;} // never compare unidentified denominations
    if(nb!=null&&ns!=null&&nb.compareTo(ns)>0){nb=null;ns=null;}if(tb!=null&&ts!=null&&tb.compareTo(ts)>0){tb=null;ts=null;}
    if(nb==null&&ns==null&&tb==null&&ts==null)continue;
    if(!seen.add(cc+":"+tier))continue;
    JSONObject r=new JSONObject().put("ccy",cc).put("tier",tier);if(nb!=null)r.put("nb",nb.toPlainString());if(ns!=null)r.put("ns",ns.toPlainString());if(tb!=null)r.put("tb",tb.toPlainString());if(ts!=null)r.put("ts",ts.toPlainString());rows.put(r);
   }
  }
  if(rows.length()==0)throw new Exception("No valid published rate rows; board may require JavaScript");
  return new JSONObject().put("bank",BANKS[bank]).put("stamp",stamp).put("rows",rows);
 }
 static JSONObject parseRegionalBoard(int bank,Document doc)throws Exception{
  String page=doc.text();String date,time;LocalDate day;
  if(bank==4){Matcher m=Pattern.compile("(?i)\\bDate\\s+(\\d{1,2}/\\d{1,2}/\\d{4})\\s+Time\\s+(\\d{1,2}:\\d{2}(?::\\d{2})?)\\s+Round\\b").matcher(page);if(!m.find())throw new Exception("Krungsri publication date absent");date=m.group(1);time=m.group(2);day=LocalDate.parse(date,DateTimeFormatter.ofPattern("d/M/uuuu",Locale.US));}
  else{Matcher m=Pattern.compile("(\\d{1,2})\\s+(มกราคม|กุมภาพันธ์|มีนาคม|เมษายน|พฤษภาคม|มิถุนายน|กรกฎาคม|สิงหาคม|กันยายน|ตุลาคม|พฤศจิกายน|ธันวาคม)\\s+(\\d{4})\\s+รอบที่\\s*\\d+\\s+(\\d{1,2}:\\d{2}(?::\\d{2})?)").matcher(page);if(!m.find())throw new Exception("Krungthai publication date absent");List<String> months=Arrays.asList("มกราคม","กุมภาพันธ์","มีนาคม","เมษายน","พฤษภาคม","มิถุนายน","กรกฎาคม","สิงหาคม","กันยายน","ตุลาคม","พฤศจิกายน","ธันวาคม");int yr=Integer.parseInt(m.group(3));if(yr>=2400)yr-=543;day=LocalDate.of(yr,months.indexOf(m.group(2))+1,Integer.parseInt(m.group(1)));time=m.group(4);}
  if(day.isAfter(LocalDate.now(ZoneId.of("Asia/Bangkok"))))throw new Exception("Future bank date");String stamp=day+" "+LocalTime.parse(time,DateTimeFormatter.ofPattern("H:mm[:ss]")).toString();JSONArray rows=new JSONArray();Set<String> seen=new HashSet<>();
  for(Element table:doc.select("table")){String hdr=table.select("th").text().toLowerCase(Locale.ROOT);if(bank==4?!(hdr.contains("denom")&&hdr.contains("notes")&&hdr.contains("sight")&&hdr.contains("t/t")):!(hdr.contains("sight bill")&&hdr.contains("t/t")&&hdr.contains("foreign bank note")))continue;
   for(Element tr:table.select("tr")){Elements cells=tr.select("> td");int n=cells.size();if(n<(bank==4?7:6))continue;int start=n-5;String cc="",denom="";if(bank==4){cc=cells.get(start-2).text().trim().replace("*","");denom=cells.get(start-1).text().trim();}else{StringBuilder prefix=new StringBuilder();for(int k=0;k<start;k++)prefix.append(cells.get(k).text()).append(' ');Matcher m=Pattern.compile("\\b([A-Z]{3})\\b").matcher(prefix.toString());if(!m.find())continue;cc=m.group(1);denom=prefix.toString().substring(m.end());}if(!cc.matches("[A-Z]{3}")||cc.equals("THB"))continue;
   String tier="";int unit=1;if(cc.equals("USD")){if(denom.matches("(?s).*50\\s*[-–]\\s*100.*"))tier="50-100";else if(denom.matches("(?s).*5\\s*[-–]\\s*20.*"))tier="5-20";else if(denom.matches("(?s).*1\\s*[-–]\\s*2.*"))tier="1-2";else if(denom.equals("1"))tier="1 only";}
   else if(bank==4){if(denom.matches("100|1000"))unit=Integer.parseInt(denom);else if(!denom.isEmpty()&&!denom.equals("1"))continue;}
   BigDecimal nb,ns,tb,ts;if(bank==4){nb=num(cells.get(start).text(),unit);ns=num(cells.get(start+1).text(),unit);tb=num(cells.get(start+3).text(),unit);ts=num(cells.get(start+4).text(),unit);}else{tb=num(cells.get(start+1).text(),unit);ts=num(cells.get(start+2).text(),unit);nb=num(cells.get(start+3).text(),unit);ns=num(cells.get(start+4).text(),unit);}
   if(cc.equals("USD")&&tier.isEmpty()||bank==3&&!cc.equals("USD")&&denom.contains("$")){nb=null;ns=null;}if(nb!=null&&ns!=null&&nb.compareTo(ns)>0){nb=null;ns=null;}if(tb!=null&&ts!=null&&tb.compareTo(ts)>0){tb=null;ts=null;}if(nb==null&&ns==null&&tb==null&&ts==null)continue;
   if(!seen.add(cc+":"+tier))continue;JSONObject r=new JSONObject().put("ccy",cc).put("tier",tier);if(nb!=null)r.put("nb",nb.toPlainString());if(ns!=null)r.put("ns",ns.toPlainString());if(tb!=null)r.put("tb",tb.toPlainString());if(ts!=null)r.put("ts",ts.toPlainString());rows.put(r);
   }
  }if(rows.length()==0)throw new Exception("Published board schema unavailable");return new JSONObject().put("bank",BANKS[bank]).put("stamp",stamp).put("rows",rows);
 }

}
