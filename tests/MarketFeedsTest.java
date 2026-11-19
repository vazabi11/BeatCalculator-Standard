package com.beatoflove.standardcalculator;

public final class MarketFeedsTest {
    private static int count;
    private static void verify(boolean b,String m){if(!b)throw new AssertionError(m);count++;}
    private static final String PAYLOAD="<PTT_DS><DataAccess><PRODUCT>Blue Gasohol E20</PRODUCT>"+
      "<PRICE>29.55</PRICE><PRICE_DATE>2026-11-19T05:00:00+07:00</PRICE_DATE></DataAccess>"+
      "<DataAccess><PRODUCT>Diesel</PRODUCT><PRICE_DATE>2026-11-19T05:00:00+07:00</PRICE_DATE></DataAccess>"+
      "<DataAccess><PRODUCT>Gasohol 95</PRODUCT><PRICE>33.20</PRICE>"+
      "<PRICE_DATE>2026-11-19T05:00:00+07:00</PRICE_DATE></DataAccess></PTT_DS>";
    private static String SOAP="<?xml version=\"1.0\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
      "<soap:Body><CurrentOilPriceResponse xmlns=\"http://www.pttor.com\"><CurrentOilPriceResult>"+
      PAYLOAD.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")+
      "</CurrentOilPriceResult></CurrentOilPriceResponse></soap:Body></soap:Envelope>";
    public static void main(String[] args) throws Exception {
      OilPriceParser.Feed result=OilPriceParser.parse(SOAP);
      verify(result.items.size()==2,"skip unpriced rows");
      verify(result.items.get(0).product.equals("Blue Gasohol E20"),"PTT product");
      verify(result.items.get(0).price==29.55,"price");
      verify(result.items.get(0).publishedMillis>0,"timestamp parsed");
      verify(FreshnessPolicy.commonFuelType("Blue Gasohol E20").equals("E20"),"E20");
      verify(FreshnessPolicy.commonFuelType("แก๊สโซฮอล์ 95").equals("Gasohol 95"),"Thai grade");
      verify(FreshnessPolicy.commonFuelType("Super Power Gasohol 95").isEmpty(),"premium separated");
      verify(FreshnessPolicy.isRecent(1000,1000+60_000,90_000),"fresh");
      verify(!FreshnessPolicy.isRecent(1000,1000+120_000,90_000),"stale");
      verify(!FreshnessPolicy.isRecent(0,1000,90_000),"missing time");
      boolean reject=false;try { OilPriceParser.parse("<!DOCTYPE x [<!ENTITY boom SYSTEM 'file:///etc/passwd'>]><x>&boom;</x>"); }
      catch(Exception expected){reject=true;}
      verify(reject,"reject XXE/invalid SOAP");
      System.out.println("PASS "+count+" market feed tests");
    }
}
