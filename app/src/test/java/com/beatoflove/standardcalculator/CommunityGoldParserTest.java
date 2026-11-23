package com.beatoflove.standardcalculator;

import org.junit.Test;
import static org.junit.Assert.*;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CommunityGoldParserTest {
    private static final ZoneId THAI=ZoneId.of("Asia/Bangkok");
    private static String payload(String date,String time,String bb,String bs,String ob,String os){
        return "{\"status\":\"success\",\"response\":{\"update_date\":\""+date+"\",\"update_time\":\""+time+"\",\"price\":{\"gold_bar\":{\"buy\":\""+bb+"\",\"sell\":\""+bs+"\"},\"gold\":{\"buy\":\""+ob+"\",\"sell\":\""+os+"\"}}}}";
    }
    @Test public void validThaiDateAndRound() throws Exception {
        ZonedDateTime now=ZonedDateTime.now(THAI).minusMinutes(1);
        String date=String.format(Locale.US,"%02d/%02d/%04d",now.getDayOfMonth(),now.getMonthValue(),now.getYear()+543);
        String time="เวลา "+now.format(DateTimeFormatter.ofPattern("HH:mm"))+" น. (ครั้งที่ 14)";
        PriceService.ThaiGoldData quote=CommunityGoldParser.parse(payload(date,time,"58,150.00","58,350.00","56,986.44","59,150.00"));
        assertEquals(58150.0,quote.buyBar,0.001);
        assertEquals(58350.0,quote.sellBar,0.001);
        assertEquals(56986.44,quote.buyOrnament,0.001);
        assertEquals(59150.0,quote.sellOrnament,0.001);
        assertEquals(14,quote.updateRound);
        assertEquals("Thai Gold API (Community)",quote.source);
    }
    @Test public void rejectsReversedPrice() throws Exception {
        String date="01/01/2569";
        try{CommunityGoldParser.parse(payload(date,"เวลา 10:23 น. (ครั้งที่ 1)","60,000","59,000","56,000","58,000"));fail("Reversed price accepted");}
        catch(Exception expected){assertTrue(expected.getMessage().contains("Reversed"));}
    }
    @Test public void rejectsMissingPublisherTime() throws Exception {
        try{CommunityGoldParser.parse(payload("23/11/2569","ครั้งที่ 9","58,150","58,350","56,986","59,150"));fail("Missing time accepted");}
        catch(Exception expected){assertTrue(expected.getMessage().contains("Missing announced time"));}
    }
}
