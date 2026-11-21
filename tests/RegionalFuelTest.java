package com.beatoflove.standardcalculator;
public class RegionalFuelTest {
 public static void main(String[] args)throws Exception{
  String data="<NewDataSet><Oil><LOCATION>Thung Song</LOCATION><PRODUCT>Gasohol 95</PRODUCT><PRICE>35.50</PRICE><PRICE_DATE>2026-11-20T05:00:00</PRICE_DATE></Oil></NewDataSet>";
  String current="<Envelope><Body><CurrentOilPriceProvincialResult><![CDATA["+data+"]]></CurrentOilPriceProvincialResult></Body></Envelope>";
  String historic=current.replace("CurrentOilPriceProvincialResult","GetOilPriceProvincialResult");
  if(PttOilService.regional(current,"CurrentOilPriceProvincial").items.get(0).price!=35.5)throw new AssertionError("current");
  if(PttOilService.regional(historic,"GetOilPriceProvincial").items.size()!=1)throw new AssertionError("dated");
  try{PttOilService.regional(current.replace("Provincial",""),"CurrentOilPriceProvincial");throw new AssertionError("wrong wrapper accepted");}catch(Exception expected){}
  try{PttOilService.regional(current.replace("35.50","0"),"CurrentOilPriceProvincial");throw new AssertionError("zero accepted");}catch(Exception expected){}
  if(!PttOilService.regional(current,"CurrentOilPriceProvincial").items.get(0).location.equals("Thung Song"))throw new AssertionError("location");
  if(!ProvinceNames.english("สุราษฎร์ธานี").equals("Surat Thani"))throw new AssertionError("province mapping");
  System.out.println("RegionalFuelTest: 6 passed");
 }
}
