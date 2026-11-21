package com.beatoflove.standardcalculator;
import java.math.*;import java.time.*;
/** THB-based institutional reference quote; dates and direction are validated before display. */
final class ReferenceRate {
 final String date; final BigDecimal unitsPerThb;
 private ReferenceRate(String d,BigDecimal q){date=d;unitsPerThb=q;}
 static ReferenceRate parse(String base,String quote,String expected,String date,String raw,LocalDate today){
  if(!"THB".equals(base)||!expected.equals(quote)||!quote.matches("[A-Z]{3}")||"THB".equals(quote))throw new IllegalArgumentException("Wrong currency pair");
  LocalDate d=LocalDate.parse(date);if(d.isAfter(today)||d.isBefore(LocalDate.of(1999,1,1)))throw new IllegalArgumentException("Invalid publisher date");
  BigDecimal q=new BigDecimal(raw);if(q.signum()<=0||q.precision()>80||Math.abs((long)q.scale())>60)throw new IllegalArgumentException("Invalid rate");return new ReferenceRate(d.toString(),q);
 }
 String thb(){return BigDecimal.ONE.divide(unitsPerThb,MathContext.DECIMAL64).round(new MathContext(8,RoundingMode.HALF_UP)).stripTrailingZeros().toPlainString();}
}
