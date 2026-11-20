package com.beatoflove.standardcalculator;

public final class PttParserDiagnosticsTest {
    private static int count=0;
    private static String wrap(String op,String payload){
        return "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'><soap:Body>"+
               "<"+op+"Response xmlns='http://www.pttor.com'><"+op+"Result>"+
               payload.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")+
               "</"+op+"Result></"+op+"Response></soap:Body></soap:Envelope>";
    }
    private static void check(boolean v,String msg){if(!v)throw new AssertionError(msg);count++;}
    public static void main(String[] args) throws Exception {
        String data="<PTT_DS><DataAccess><PRODUCT>Blue Gasohol E20</PRODUCT><PRICE>29.55</PRICE><PRICE_DATE>2026-11-20T05:00:00+07:00</PRICE_DATE></DataAccess>"+
                "<DataAccess><PRODUCT>Blue Diesel</PRODUCT><PRICE_DATE>2026-11-20T05:00:00+07:00</PRICE_DATE></DataAccess></PTT_DS>";
        OilPriceParser.Feed current=OilPriceParser.parse(wrap("CurrentOilPrice",data));
        check(current.items.size()==1,"unpriced row skipped");
        check(current.items.get(0).price==29.55,"valid price");
        check(current.items.get(0).publishedMillis>0,"timestamp");
        OilPriceParser.Feed history=OilPriceParser.parse(wrap("GetOilPrice",data));
        check(history.items.size()==1,"historical operation parsed");
        String noOffset=data.replace("+07:00","");
        OilPriceParser.Feed local=OilPriceParser.parse(wrap("CurrentOilPrice",noOffset));
        check(local.items.get(0).publishedMillis>0,"local Thai timezone timestamp parsed");
        String alternative="<PTT_DS><Table><PRODUCT_NAME>Gasohol 95</PRODUCT_NAME>"+
            "<RETAIL_PRICE>33.25</RETAIL_PRICE><PRICE_DATE>2026-11-20T05:00:00</PRICE_DATE></Table></PTT_DS>";
        OilPriceParser.Feed varied=OilPriceParser.parse(wrap("CurrentOilPrice",alternative));
        check(varied.items.get(0).price==33.25,"alternative field names");
        boolean diagnostic=false;
        try{OilPriceParser.parse(wrap("CurrentOilPrice","<PTT_DS><DataAccess><PRODUCT>Diesel</PRODUCT></DataAccess></PTT_DS>"));}
        catch(Exception ex){diagnostic=ex.getMessage().contains("ราคาไม่มีค่า=1");}
        check(diagnostic,"missing price diagnostic");
        boolean fault=false;
        try{OilPriceParser.parse("<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'><soap:Body><soap:Fault>bad request</soap:Fault></soap:Body></soap:Envelope>");}
        catch(Exception ex){fault=ex.getMessage().contains("Fault");}
        check(fault,"SOAP fault");
        System.out.println("PASS "+count+" PTT diagnostic tests");
    }
}
