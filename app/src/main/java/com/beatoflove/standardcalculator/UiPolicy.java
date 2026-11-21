package com.beatoflove.standardcalculator;
import java.util.*;
final class UiPolicy {
 static int swipe(int[] nav,int current,float dx,float dy,float threshold){if(Math.abs(dx)<threshold||Math.abs(dx)<1.6*Math.abs(dy))return current;for(int i=0;i<nav.length;i++)if(nav[i]==current){int n=i+(dx<0?1:-1);return n>=0&&n<nav.length?nav[n]:current;}return current;}
 static String groupNumber(String raw){if(raw==null||!raw.matches("[+\\-−]?[0-9]+(?:\\.[0-9]+)?"))return raw;int point=raw.indexOf('.');String tail=point<0?"":raw.substring(point);String whole=point<0?raw:raw.substring(0,point);return whole.replaceAll("(?<=\\d)(?=(?:\\d{3})+$)",",")+tail;}
 static String defaultDistrict(Collection<String> names){for(String n:names){String s=n.toLowerCase(Locale.ROOT).trim();if(s.matches("^(?:mueang|muang|m[uü]ang|เมือง)(?:.*)$"))return n;}return "";}
}
