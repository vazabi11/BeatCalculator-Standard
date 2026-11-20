package com.beatoflove.standardcalculator;

import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Independent offline expression engine. Double precision, not a symbolic algebra system. */
public final class FormulaEngine {
    public enum Angle { DEG, RAD, GRAD }
    public enum Format { NORM, NORM2, FIX, SCI, ENG, FRACTION, MIXED, DMS }
    public Angle angle=Angle.DEG;
    public Format format=Format.NORM;
    public int digits=6;
    public double ans=0;
    public final Map<String,Double> variables=new LinkedHashMap<>();
    public final Map<String,Double> statistics=new LinkedHashMap<>();
    public final List<Double> statementResults=new ArrayList<>();
    public FormulaEngine(){for(char c:"ABCDEFXYM".toCharArray())variables.put(""+c,0d);}
    static double finite(double x){if(!Double.isFinite(x))throw new IllegalArgumentException("Math error: ผลลัพธ์อยู่นอกขอบเขต");return x==0?0:x;}
    static void require(boolean b,String m){if(!b)throw new IllegalArgumentException(m);}
    public double evaluate(String expression){
        require(expression!=null && expression.length()<=2000,"สมการยาวเกินไป");
        Map<String,Double> previous=new HashMap<>(variables);double old=ans;
        List<Double> next=new ArrayList<>();
        try { for(String s:expression.split(":",-1)){Parser p=new Parser(s);double x=p.sum();p.space();require(p.i==p.s.length(),"Syntax error ที่ตำแหน่ง "+(p.i+1));ans=finite(x);next.add(ans);}statementResults.clear();statementResults.addAll(next);return ans; }
        catch(RuntimeException e){variables.putAll(previous);ans=old;throw e;}
    }
    public double radians(double x){return angle==Angle.DEG?Math.toRadians(x):angle==Angle.GRAD?x*Math.PI/200:x;}
    public double fromRadians(double x){return angle==Angle.DEG?Math.toDegrees(x):angle==Angle.GRAD?x*200/Math.PI:x;}
    static double factorial(double x){require(x==Math.rint(x)&&x>=0&&x<=170,"Factorial: จำนวนเต็ม 0–170");double v=1;for(int i=2;i<=x;i++)v*=i;return finite(v);}
    static double combin(double n,double r,boolean permutation){require(n==Math.rint(n)&&r==Math.rint(r)&&n>=0&&r>=0&&r<=n&&n<=100000,"nCr/nPr: ต้องเป็นจำนวนเต็ม 0 ≤ r ≤ n ≤ 100000"); if(!permutation)r=Math.min(r,n-r);double v=1;for(int i=1;i<=r;i++){v*=permutation?n-i+1:(n-r+i)/i;finite(v);}return v;}
    static double root(double n,double x){require(n!=0,"รากลำดับศูนย์ไม่ได้");if(x<0){require(n==Math.rint(n)&&Math.abs(n%2)==1,"รากของจำนวนลบต้องเป็นลำดับคี่");return -Math.pow(-x,1/n);}return Math.pow(x,1/n);}
    static double tan(double r){require(Math.abs(Math.cos(r))>1e-15,"tan ไม่กำหนด");return Math.tan(r);}
    double call(String f,List<Double> a){int n=a.size();double x=n>0?a.get(0):0,y=n>1?a.get(1):0,z=n>2?a.get(2):0;
        int expected=Arrays.asList("ncr","npr","root","pol","rec","logbase","pctchange","pctadd","pctsub","mod","pctrate").contains(f)?2:Arrays.asList("dms","mixed").contains(f)?3:f.equals("ran")?0:1;
        require(n==expected,"จำนวนอาร์กิวเมนต์ไม่ถูกต้อง: "+f);
        switch(f){
            case "sin":return Math.sin(radians(x));case "cos":return Math.cos(radians(x));case "tan":return tan(radians(x));
            case "asin":return fromRadians(Math.asin(x));case "acos":return fromRadians(Math.acos(x));case "atan":return fromRadians(Math.atan(x));
            case "sinh":return Math.sinh(x);case "cosh":return Math.cosh(x);case "tanh":return Math.tanh(x);
            case "asinh":return Math.copySign(Math.log1p(Math.abs(x)+Math.abs(x)*(Math.abs(x)/(1+Math.hypot(x,1)))),x);case "acosh":return Math.log(x+Math.sqrt(x-1)*Math.sqrt(x+1));case "atanh":return .5*(Math.log1p(x)-Math.log1p(-x));
            case "sqrt":return Math.sqrt(x);case "cbrt":return Math.cbrt(x);case "root":return root(x,y);
            case "abs":return Math.abs(x);case "inv":return 1/x;case "fact":return factorial(x);case "ncr":return combin(x,y,false);case "npr":return combin(x,y,true);
            case "log":return Math.log10(x);case "ln":return Math.log(x);case "logbase":require(x>0&&x!=1&&y>0,"ฐาน log ไม่ถูกต้อง");return Math.log(y)/Math.log(x);
            case "exp":return Math.exp(x);case "pow10":return Math.pow(10,x);
            case "dms":require(y>=0&&y<60&&z>=0&&z<60,"ลิปดา/พิลิปดา 0–59");return Math.copySign(Math.abs(x)+y/60+z/3600,x);
            case "mixed":require(z!=0,"ตัวส่วนเป็นศูนย์");return Math.copySign(Math.abs(x)+Math.abs(y/z),x);
            case "deg":return fromRadians(Math.toRadians(x));case "rad":return fromRadians(x);case "grad":return fromRadians(x*Math.PI/200);
            case "pol":{double r=finite(Math.hypot(x,y)); variables.put("X",r);variables.put("Y",fromRadians(Math.atan2(y,x)));return r;}
            case "rec":{require(x>=0,"รัศมีต้องไม่ติดลบ");double u=finite(x*Math.cos(radians(y)));variables.put("X",u);variables.put("Y",finite(x*Math.sin(radians(y))));return u;}
            case "ran":return Math.floor(Math.random()*1000)/1000;case "rnd":return format==Format.FIX?BigDecimal.valueOf(x).setScale(digits,RoundingMode.HALF_UP).doubleValue():format==Format.SCI?BigDecimal.valueOf(x).round(new java.math.MathContext(Math.max(1,digits),RoundingMode.HALF_UP)).doubleValue():x;
            case "mod":return x%y;case "pctrate":return x/y*100;case "pctadd":return x+x*y/100;case "pctsub":return x-x*y/100;case "pctchange":return (y-x)/x*100;
            default:throw new IllegalArgumentException("ไม่รู้จักฟังก์ชัน "+f);
        }
    }
    private final class Parser {
        final String s;int i=0,depth=0;
        Parser(String s){this.s=s.replace("×","*").replace("÷","/").replace("−","-").replace("π","pi").replace("√","sqrt").replace("²","^2").replace("³","^3");}
        void space(){while(i<s.length()&&Character.isWhitespace(s.charAt(i)))i++;}
        boolean take(char c){space();if(i<s.length()&&s.charAt(i)==c){i++;return true;}return false;}
        char peek(){space();return i<s.length()?s.charAt(i):'\0';}
        double sum(){double x=term();while(true){if(take('+'))x=finite(x+term());else if(take('-'))x=finite(x-term());else return x;}}
        double term(){double x=unary();while(true){if(take('*'))x=finite(x*unary());else if(take('/'))x=finite(x/unary());else if(peek()=='('||Character.isLetter(peek()))x=finite(x*unary());else return x;}}
        double unary(){require(++depth<=100,"สมการซ้อนลึกเกินไป");try {if(take('+'))return unary();if(take('-'))return -unary();return power();}finally{depth--;}}
        double power(){double x=postfix();if(take('^'))x=finite(Math.pow(x,unary()));return x;}
        double postfix(){double x=atom();while(true){if(take('!'))x=factorial(x);else if(take('%'))x/=100;else return x;}}
        double atom(){space();if(take('(')){double v=sum();require(take(')'),"วงเล็บไม่ครบ");return v;}
            if(Character.isLetter(peek())){int start=i;while(i<s.length()&&(Character.isLetterOrDigit(s.charAt(i))||s.charAt(i)=='_'))i++;String id=s.substring(start,i);String f=id.toLowerCase(Locale.ROOT);
                if(take('(')){List<Double> args=new ArrayList<>();if(!take(')')){do{args.add(sum());}while(take(','));require(take(')'),"วงเล็บฟังก์ชันไม่ครบ");}return finite(call(f,args));}
                if(f.equals("pi"))return Math.PI;if(id.equals("e"))return Math.E;if(f.equals("ans"))return ans;if(variables.containsKey(id.toUpperCase(Locale.ROOT)))return variables.get(id.toUpperCase(Locale.ROOT));if(statistics.containsKey(f))return finite(statistics.get(f));throw new IllegalArgumentException("ไม่รู้จักตัวแปร "+id);}
            int start=i;while(i<s.length()&&(Character.isDigit(s.charAt(i))||s.charAt(i)=='.'))i++;require(i>start,"ต้องการตัวเลขที่ตำแหน่ง "+(i+1));if(i<s.length()&&(s.charAt(i)=='E'||s.charAt(i)=='e')){int old=i++;if(i<s.length()&&(s.charAt(i)=='+'||s.charAt(i)=='-'))i++;int d=i;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;if(i==d)i=old;}return finite(Double.parseDouble(s.substring(start,i)));}
    }
    public String display(double x){if(!Double.isFinite(x))return "—";switch(format){case FIX:return String.format(Locale.US,"%."+digits+"f",x);case SCI:return String.format(Locale.US,"%."+Math.max(0,digits-1)+"E",x);case ENG:if(x==0)return "0";int exp=3*(int)Math.floor(Math.log10(Math.abs(x))/3);return compact(x/Math.pow(10,exp))+"E"+exp;case FRACTION:return fraction(x,false);case MIXED:return fraction(x,true);case DMS:return dms(x);case NORM2:return norm(x,1e-9);default:return norm(x,1e-2);}}
    static String norm(double x,double low){if(x!=0&&(Math.abs(x)<low||Math.abs(x)>=1e10))return String.format(Locale.US,"%.9E",x).replaceFirst("\\.?0+E","E");return compact(x);}
    public static String compact(double x){if(!Double.isFinite(x))return "—";if(x==0)return "0";return BigDecimal.valueOf(x).round(new java.math.MathContext(12)).stripTrailingZeros().toString();}
    static String dms(double x){if(Math.abs(x)>1e12)return compact(x);long sec=Math.round(Math.abs(x)*3600*1000);return (x<0?"−":"")+(sec/3600000)+"° "+(sec/60000%60)+"′ "+compact((sec%60000)/1000d)+"″";}
    static String fraction(double x,boolean mixed){if(Math.abs(x)>1e9)return compact(x);double v=Math.abs(x);long h0=0,h1=1,k0=1,k1=0;for(int i=0;i<32;i++){long a=(long)Math.floor(v);if(a>1_000_000_000L)break;long h=a*h1+h0,k=a*k1+k0;if(k>1000000||k<=0)break;h0=h1;h1=h;k0=k1;k1=k;if(Math.abs((double)h/k-Math.abs(x))<1e-12||v==a)break;v=1/(v-a);}if(k1==0||Math.abs((double)h1/k1-Math.abs(x))>1e-10)return compact(x);String sign=x<0?"−":"";if(k1==1)return sign+h1;if(mixed&&h1>=k1)return sign+(h1/k1)+" "+(h1%k1)+"/"+k1;return sign+h1+"/"+k1;}
}
