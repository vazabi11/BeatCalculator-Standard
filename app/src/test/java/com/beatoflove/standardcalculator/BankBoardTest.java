package com.beatoflove.standardcalculator;
import org.junit.Test;
import org.jsoup.Jsoup;
import org.json.*;
import static org.junit.Assert.*;
public class BankBoardTest {
 static String header="<p>Date 20 November 2025 Time 11:36:38</p><table><tr><th>Currency</th><th>Export sight bill</th><th>Telex Transfer / T/T</th><th>Bank Buying Bank Notes</th><th>TT&amp;Draft</th><th>Bank Selling Bank Notes</th></tr>";
 static String row(String label,String a,String b,String c,String d,String e){return "<tr><td>"+label+"</td><td>"+a+"</td><td>"+b+"</td><td>"+c+"</td><td>"+d+"</td><td>"+e+"</td></tr>";}
 @Test public void kbankTiers() throws Exception {JSONObject j=BankRateService.parse(1,Jsoup.parse(header+row("USD 50-100 USD : 50-100","33.7","33.8","33.5","34.1","34.3")+row("JPY Yen","0.20","0.21","0.20","0.22","0.23")+"</table>"));JSONArray a=j.getJSONArray("rows");assertEquals("50-100",a.getJSONObject(0).getString("tier"));assertEquals("33.5",a.getJSONObject(0).getString("nb"));assertEquals("34.1",a.getJSONObject(0).getString("ts"));assertEquals("0.20",a.getJSONObject(1).getString("nb"));}
 @Test public void ttbUnits() throws Exception {JSONObject j=BankRateService.parse(5,Jsoup.parse(header+row("JPY : 100 Japanese Yen","20","21","20","22","23")+"</table>"));assertEquals(0.2,j.getJSONArray("rows").getJSONObject(0).getDouble("nb"),0.0000001);}
 @Test(expected=Exception.class) public void rejectMissingDate() throws Exception {BankRateService.parse(1,Jsoup.parse(header.substring(header.indexOf("<table>"))+row("USD 50-100","33","34","33","35","35")+"</table>"));}
 @Test(expected=Exception.class) public void rejectBlankBoard() throws Exception {BankRateService.parse(1,Jsoup.parse(header+"</table>"));}
 @Test public void krungsriUnits() throws Exception {String h="<p>Date 20/11/2025 Time 14:05 Round 3</p><table><tr><th>Currency</th><th>DENOM.</th><th>NOTES</th><th>SIGHT</th><th>T/T</th></tr>";String r="<tr><td>JAPAN</td><td>JPY</td><td>100</td><td>20</td><td>22</td><td>20.1</td><td>20.2</td><td>21.9</td></tr>";JSONObject j=BankRateService.parse(4,Jsoup.parse(h+r+"</table>"));assertEquals(0.2,j.getJSONArray("rows").getJSONObject(0).getDouble("nb"),1e-9);assertEquals(0.202,j.getJSONArray("rows").getJSONObject(0).getDouble("tb"),1e-9);}
 @Test public void krungthaiUnits() throws Exception {String h="<p>20 พฤศจิกายน 2025 รอบที่ 2 14:05</p><table><tr><th>CURRENCY</th><th>SIGHT BILL</th><th>T/T</th><th>FOREIGN BANK NOTE</th></tr>";JSONObject j=BankRateService.parse(3,Jsoup.parse(h+row("JPY Japanese Yen","0.201","0.202","0.212","0.203","0.215")+"</table>"));assertEquals(0.203,j.getJSONArray("rows").getJSONObject(0).getDouble("nb"),1e-9);}
}
