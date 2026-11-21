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
 static final String[] URLS={"https://www.bangkokbank.com/en/personal/other-services/view-rates/foreign-exchange-rates","https://www.kasikornbank.com/en/rate/Pages/Foreign-Exchange.aspx","https://www.scb.co.th/en/personal-banking/foreign-exchange-rates/","https://krungthai.com/","https://www.krungsri.com/","https://www.ttbbank.com/en/rates/exchange-rates"};

 static void fetch(int bank,PriceService.Callback<JSONObject> cb){new Thread(()->{JSONObject data=null;String err=null;try{if(bank==3||bank==4)throw new Exception("No verified structured board adapter");Document doc=Jsoup.connect(URLS[bank]).userAgent("SirawitLabs-Calculator/5.3.1").timeout(18000).maxBodySize(2000000).get();if(!doc.location().startsWith("https://"))throw new Exception("Insecure redirect");data=parse(bank,doc);}catch(Exception e){err=e.getMessage();}final JSONObject out=data;final String problem=err;new Handler(Looper.getMainLooper()).post(()->cb.done(out,problem));},"BankBoardFetch").start();}
 static BigDecimal num(String s,int unit){try{String t=s.trim().replace(",","");if(!t.matches("[0-9]+(?:\\.[0-9]+)?"))return null;BigDecimal n=new BigDecimal(t);if(n.signum()<=0||n.compareTo(new BigDecimal("100000"))>=0)return null;return n.divide(BigDecimal.valueOf(unit),MathContext.DECIMAL128);}catch(Exception e){return null;}}
 static JSONObject parse(int bank,Document doc)throws Exception{
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
}
