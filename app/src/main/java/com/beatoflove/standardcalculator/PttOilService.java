package com.beatoflove.standardcalculator;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;

/** PTT OR CurrentOilPrice, then documented GetOilPrice by date if current data is unusable. */
public final class PttOilService {
    private PttOilService() {}
    private static final String URL="https://orapiweb.pttor.com/oilservice/OilPrice.asmx";
    private static final String ACTION="https://orapiweb.pttor.com/";
    private static final ZoneId BANGKOK=ZoneId.of("Asia/Bangkok");

    private static String escapeXml(String raw) {
        return raw.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
    private static String call(String operation, String parameters) throws Exception {
        String xml="<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<soap:Body><"+operation+" xmlns=\"http://www.pttor.com\">"+
            "<Language>thai</Language>"+parameters+"</"+operation+"></soap:Body></soap:Envelope>";
        HttpURLConnection conn=(HttpURLConnection)new URL(URL).openConnection();
        conn.setRequestMethod("POST");conn.setDoOutput(true);
        conn.setConnectTimeout(10000);conn.setReadTimeout(12000);
        conn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
        conn.setRequestProperty("SOAPAction","\""+ACTION+operation+"\"");
        conn.setRequestProperty("User-Agent","BeatCalculator/4.2 Android");
        try {
            byte[] bytes=xml.getBytes(StandardCharsets.UTF_8);
            try(java.io.OutputStream out=conn.getOutputStream()) {out.write(bytes);}
            int status=conn.getResponseCode();
            if(status!=200) throw new Exception("PTT HTTP "+status);
            try(InputStream in=conn.getInputStream();ByteArrayOutputStream result=new ByteArrayOutputStream()) {
                byte[] buf=new byte[4096];int n;
                while((n=in.read(buf))!=-1){result.write(buf,0,n); if(result.size()>500000)throw new Exception("PTT: response too large");}
                return new String(result.toByteArray(),StandardCharsets.UTF_8);
            }
        }finally{conn.disconnect();}
    }
    private static String getByDate(LocalDate day) throws Exception {
        String p="<DD>"+day.getDayOfMonth()+"</DD><MM>"+day.getMonthValue()+"</MM><YYYY>"+day.getYear()+"</YYYY>";
        return call("GetOilPrice",p);
    }
    public static OilPriceParser.Feed fetchFeed() throws Exception {
        // A connection or HTTP failure must not trigger a sequence of extra requests.
        String currentResponse=call("CurrentOilPrice","");
        String currentError="";
        try {
            return OilPriceParser.parse(currentResponse);
        } catch(Exception ex) {
            currentError=ex.getMessage()==null?"unknown":ex.getMessage();
        }
        // Try previous days only when current lookup was unsuccessful; retain upstream timestamps.
        // Avoid frequent requests (the UI polls at its normal interval, not here).
        LocalDate now=LocalDate.now(BANGKOK);
        for(int daysBack=1;daysBack<=3;daysBack++) {
            try {
                OilPriceParser.Feed result=OilPriceParser.parse(getByDate(now.minusDays(daysBack)));
                if(result!=null && !result.items.isEmpty()) return result;
            }catch(Exception ignored) { /* Continue to earlier day; never invent missing prices. */ }
        }
        throw new Exception("PTT OR: ไม่มีราคาในบริการปัจจุบันหรือย้อนหลัง 3 วัน ("+currentError+")");
    }
}
