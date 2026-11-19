package com.beatoflove.standardcalculator;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** PTT OR official SOAP CurrentOilPrice XML adapter. Never creates prices locally. */
public final class OilPriceParser {
    private OilPriceParser() {}
    public static final class Item {
        public final String product, publishedAt;
        public final double price;
        public final long publishedMillis;
        public Item(String product,double price,String publishedAt,long publishedMillis) {
            this.product=product;this.price=price;this.publishedAt=publishedAt;this.publishedMillis=publishedMillis;
        }
        public boolean isRecent(long now) {return FreshnessPolicy.isRecent(publishedMillis, now, 72L*60*60*1000);}
    }
    public static final class Feed {
        public final List<Item> items;
        public Feed(List<Item> items) {this.items=Collections.unmodifiableList(new ArrayList<>(items));}
    }
    static Document parseXml(String xml) throws Exception {
        if(xml==null || xml.length()>500_000) throw new Exception("XML invalid size");
        // Android XML factories vary by API level. Reject declarations before parsing,
        // and disable external entities for implementations that support those features.
        String upper=xml.toUpperCase(java.util.Locale.ROOT);
        if(upper.contains("<!DOCTYPE") || upper.contains("<!ENTITY"))
            throw new Exception("XML document type and entities are not allowed");
        DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        try{f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);}catch(javax.xml.parsers.ParserConfigurationException ignored){}
        try{f.setFeature("http://xml.org/sax/features/external-general-entities",false);}catch(javax.xml.parsers.ParserConfigurationException ignored){}
        try{f.setFeature("http://xml.org/sax/features/external-parameter-entities",false);}catch(javax.xml.parsers.ParserConfigurationException ignored){}
        try{f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);}catch(javax.xml.parsers.ParserConfigurationException ignored){}
        try{f.setXIncludeAware(false);}catch(UnsupportedOperationException ignored){}
        f.setExpandEntityReferences(false);
        try{f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);}catch(javax.xml.parsers.ParserConfigurationException ignored){}
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
    private static NodeList nodes(Document d,String tag){return d.getElementsByTagNameNS("*",tag);}
    private static String child(Element e,String name){
        NodeList ns=e.getElementsByTagName(name);
        if(ns.getLength()==0) ns=e.getElementsByTagNameNS("*",name);
        return ns.getLength()==0?"":ns.item(0).getTextContent().trim();
    }
    public static Feed parse(String soapXml) throws Exception {
        Document soap=parseXml(soapXml);
        NodeList results=nodes(soap,"CurrentOilPriceResult");
        if(results.getLength()==0) throw new Exception("SOAP response missing CurrentOilPriceResult");
        String embedded=results.item(0).getTextContent().trim();
        if(embedded.length()==0) throw new Exception("PTT returned empty price data");
        Document table=parseXml(embedded);
        NodeList entries=table.getElementsByTagName("DataAccess");
        if(entries.getLength()==0)entries=nodes(table,"DataAccess");
        ArrayList<Item> output=new ArrayList<>();
        for(int i=0;i<entries.getLength();i++){
            Node n=entries.item(i); if(!(n instanceof Element))continue;
            Element e=(Element)n;
            String product=child(e,"PRODUCT"), rawPrice=child(e,"PRICE"), stamp=child(e,"PRICE_DATE");
            if(product.isEmpty()||rawPrice.isEmpty())continue; // no price != 0 price
            try{
                double value=Double.parseDouble(rawPrice.replace(",",""));
                if(!Double.isFinite(value)||value<=0||value>=200)continue;
                long ms=0;
                try{ms=OffsetDateTime.parse(stamp).toInstant().toEpochMilli();}
                catch(Exception ignored){}
                output.add(new Item(product,value,stamp,ms));
            } catch(NumberFormatException ignored){}
        }
        if(output.isEmpty())throw new Exception("PTT feed contains no valid price values");
        return new Feed(output);
    }
}
