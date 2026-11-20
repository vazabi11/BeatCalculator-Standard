package com.beatoflove.standardcalculator;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Defensive PTT OR SOAP adapter. Does not invent or override any price. */
public final class OilPriceParser {
    private OilPriceParser() {}
    public static final class Item {
        public final String product, publishedAt;
        public final double price;
        public final long publishedMillis;
        public Item(String product, double price, String publishedAt, long publishedMillis) {
            this.product=product; this.price=price; this.publishedAt=publishedAt; this.publishedMillis=publishedMillis;
        }
        public boolean isRecent(long now) {
            return FreshnessPolicy.isRecent(publishedMillis, now, 72L*60*60*1000);
        }
    }
    public static final class Feed {
        public final List<Item> items;
        public Feed(List<Item> items) {this.items=Collections.unmodifiableList(new ArrayList<>(items));}
    }
    static Document parseXml(String xml) throws Exception {
        if(xml==null || xml.isEmpty() || xml.length()>500_000) throw new Exception("PTT: XML ว่างหรือมีขนาดไม่ถูกต้อง");
        String upper=xml.toUpperCase(Locale.ROOT);
        if(upper.contains("<!DOCTYPE") || upper.contains("<!ENTITY")) throw new Exception("PTT: XML มี entity ที่ไม่อนุญาต");
        DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        try {f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);} catch(javax.xml.parsers.ParserConfigurationException ignored) {}
        try {f.setFeature("http://xml.org/sax/features/external-general-entities",false);} catch(javax.xml.parsers.ParserConfigurationException ignored) {}
        try {f.setFeature("http://xml.org/sax/features/external-parameter-entities",false);} catch(javax.xml.parsers.ParserConfigurationException ignored) {}
        try {f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);} catch(javax.xml.parsers.ParserConfigurationException ignored) {}
        try {f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);} catch(javax.xml.parsers.ParserConfigurationException ignored) {}
        f.setExpandEntityReferences(false);
        try {f.setXIncludeAware(false);} catch(UnsupportedOperationException ignored) {}
        return f.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
    private static NodeList nodes(Document d,String name) {return d.getElementsByTagNameNS("*",name);}
    private static String child(Element e,String name) {
        NodeList ns=e.getElementsByTagName(name);
        if(ns.getLength()==0) ns=e.getElementsByTagNameNS("*",name);
        return ns.getLength()==0 ? "" : ns.item(0).getTextContent().trim();
    }
    private static String nestedXml(String text) {
        String x=text.trim();
        // SOAP XML escapes its inner XML; DOM usually decodes once. Some proxies double-escape it.
        for(int i=0;i<2 && (x.startsWith("&lt;") || x.startsWith("&amp;lt;"));i++) {
            x=x.replace("&amp;lt;","&lt;").replace("&amp;gt;","&gt;")
               .replace("&lt;","<").replace("&gt;",">").replace("&quot;","\"").replace("&apos;","'");
        }
        return x;
    }
    public static Feed parse(String soapXml) throws Exception {
        Document soap=parseXml(soapXml);
        NodeList faults=nodes(soap,"Fault");
        if(faults.getLength()>0) throw new Exception("PTT: บริการ SOAP ส่ง Fault กลับมา");
        NodeList results=nodes(soap,"CurrentOilPriceResult");
        if(results.getLength()==0) results=nodes(soap,"GetOilPriceResult");
        if(results.getLength()==0) throw new Exception("PTT: ไม่พบ CurrentOilPriceResult/GetOilPriceResult");
        String embedded=nestedXml(results.item(0).getTextContent());
        if(embedded.isEmpty()) throw new Exception("PTT: ผลตอบกลับไม่มีข้อมูลราคา");
        if(!embedded.startsWith("<")) throw new Exception("PTT: รูปแบบผลตอบกลับไม่ใช่ XML");
        Document table=parseXml(embedded);
        NodeList entries=table.getElementsByTagName("DataAccess");
        if(entries.getLength()==0) entries=nodes(table,"DataAccess");
        if(entries.getLength()==0) throw new Exception("PTT: ไม่พบรายการ DataAccess ในข้อมูลราคาที่ได้รับ");
        int total=0,emptyNames=0,emptyPrice=0,invalidPrice=0;
        ArrayList<Item> output=new ArrayList<>();
        for(int i=0;i<entries.getLength();i++) {
            Node node=entries.item(i); if(!(node instanceof Element)) continue;
            total++;
            Element e=(Element) node;
            String product=child(e,"PRODUCT"),rawPrice=child(e,"PRICE"),stamp=child(e,"PRICE_DATE");
            if(product.isEmpty()){emptyNames++;continue;}
            if(rawPrice.isEmpty()){emptyPrice++;continue;}
            try {
                double price=Double.parseDouble(rawPrice.replace(",","").trim());
                if(!Double.isFinite(price)||price<=0||price>=200){invalidPrice++;continue;}
                long ms=0;
                try {ms=OffsetDateTime.parse(stamp).toInstant().toEpochMilli();}catch(Exception ignored) {}
                output.add(new Item(product,price,stamp,ms));
            } catch(NumberFormatException ignored){invalidPrice++;}
        }
        if(output.isEmpty()) throw new Exception("PTT: ไม่มีราคาที่ใช้ได้ (รายการ="+total+", ชื่อว่าง="+emptyNames+", ราคาไม่มีค่า="+emptyPrice+", ราคาไม่ถูกต้อง="+invalidPrice+")");
        return new Feed(output);
    }
}
