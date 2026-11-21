package com.beatoflove.standardcalculator;
import org.junit.Test;import org.junit.Assume;import org.jsoup.Jsoup;import org.json.*;import java.net.*;import java.io.*;import java.nio.charset.StandardCharsets;import static org.junit.Assert.*;
/** Opt-in actual endpoint checks. No network calls from normal APK builds. */
public class LiveConnectivityTest {
 void enabled(){Assume.assumeTrue("Run only in explicit diagnostics", "1".equals(System.getenv("LIVE_MARKET_TESTS")));}
 @Test public void suratThani()throws Exception{enabled();checkProvince("สุราษฎร์ธานี");}
 @Test public void chonBuri()throws Exception{enabled();checkProvince("ชลบุรี");}
 void checkProvince(String province)throws Exception{OilPriceParser.Feed f=PttOilService.fetchProvince(province);assertFalse(f.items.isEmpty());for(OilPriceParser.Item i:f.items)System.out.println(province+" | "+i.location+" | "+i.product+" | "+i.price+" | "+i.publishedAt);}
 @Test public void kbank()throws Exception{enabled();board(1);}
 @Test public void ttb()throws Exception{enabled();board(5);}
 @Test public void bangkokBank()throws Exception{enabled();board(0);}
 @Test public void scb()throws Exception{enabled();board(2);}
 void board(int i)throws Exception{JSONObject j=BankRateService.parse(i,Jsoup.connect(BankRateService.URLS[i]).userAgent("SirawitLabs-Calculator/5.3.1").timeout(18000).maxBodySize(2000000).get());assertTrue(j.getJSONArray("rows").length()>0);System.out.println(j.getString("bank")+" published="+j.getString("stamp")+" rows="+j.getJSONArray("rows").length());}
 @Test public void biquoteForex()throws Exception{enabled();tick("EURUSD");}
 @Test public void biquoteGold()throws Exception{enabled();tick("XAUUSD");}
 void tick(String s)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL("https://biquote.io/api/"+s).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(15000);try{assertEquals(200,c.getResponseCode());try(InputStream in=c.getInputStream()){JSONObject j=new JSONObject(new String(in.readAllBytes(),StandardCharsets.UTF_8));assertEquals(s,j.getString("symbol"));assertTrue(j.getDouble("bid")>0);assertTrue(j.getDouble("ask")>=j.getDouble("bid"));java.time.Instant.parse(j.getString("time"));System.out.println(s+" timestamp="+j.getString("time"));}}finally{c.disconnect();}}
}
