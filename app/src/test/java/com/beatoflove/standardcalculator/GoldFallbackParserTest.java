package com.beatoflove.standardcalculator;
import org.junit.Test;
import static org.junit.Assert.*;
public class GoldFallbackParserTest {
 // Synthetic prices, real field names from GitHub Diagnostics run 44673082816.
 @Test public void readsRakhagoldShape() throws Exception {
  PriceService.ThaiGoldData d=GoldFallbackParser.parse("{\"data\":{\"bar_buy\":\"59,800\",\"bar_sell\":59900,\"orn_buy\":58700.5,\"orn_sell\":60700,\"date\":\"2026-01-19\",\"as_time\":\"17:01\",\"seq\":36},\"generated_at\":\"2026-01-20T00:00:00Z\"}");
  assertEquals(59800,d.buyBar,0);assertEquals(59900,d.sellBar,0);assertEquals(58700.5,d.buyOrnament,0);assertEquals(60700,d.sellOrnament,0);
  assertEquals("2026-01-19T10:01:00Z",d.publishedAt);assertEquals(36,d.updateRound);assertEquals("Rakhagold",d.source);
 }
 @Test(expected=Exception.class) public void rejectsMissingTime() throws Exception {GoldFallbackParser.parse("{\"data\":{\"bar_buy\":59800,\"bar_sell\":59900,\"orn_buy\":58700,\"orn_sell\":60700,\"date\":\"2026-01-19\"}}");}
 @Test(expected=Exception.class) public void rejectsMissingPrice() throws Exception {GoldFallbackParser.parse("{\"data\":{\"bar_buy\":59800,\"bar_sell\":59900,\"orn_buy\":58700,\"date\":\"2026-01-19\",\"as_time\":\"17:01\"}}");}
}
