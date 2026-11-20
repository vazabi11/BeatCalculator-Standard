#!/usr/bin/env python3
"""Manual diagnostic of public market endpoints. Never prints tokens or full responses."""
import json, urllib.request, urllib.error, xml.etree.ElementTree as ET, html

def fetch(name,url,body=None,headers=None):
    print('\n===',name,'===')
    req=urllib.request.Request(url,data=body,headers=headers or {'User-Agent':'BeatCalculator-V5-Diagnostic/1.0'},method='POST' if body else 'GET')
    try:
        with urllib.request.urlopen(req,timeout=18) as r:
            raw=r.read(500000)
            print('HTTP:',r.status,'bytes:',len(raw),'content-type:',r.headers.get('Content-Type','?'))
            return raw
    except urllib.error.HTTPError as e:
        print('HTTP:',e.code,'reason:',e.reason)
    except Exception as e:
        print('NETWORK:',type(e).__name__,str(e)[:150])
    return None

from datetime import datetime, timedelta
from zoneinfo import ZoneInfo
from collections import Counter

def local(tag): return tag.rsplit('}',1)[-1]
def inspect_ptt(operation,lang,date=None):
    extra='' if date is None else f'<DD>{date.day}</DD><MM>{date.month}</MM><YYYY>{date.year}</YYYY>'
    body=f'<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"><soap:Body><{operation} xmlns="http://www.pttor.com"><Language>{lang}</Language>{extra}</{operation}></soap:Body></soap:Envelope>'.encode()
    response=fetch(f'PTT {operation} {lang} {date or ""}','https://orapiweb.pttor.com/oilservice/OilPrice.asmx',body,{'Content-Type':'text/xml; charset=utf-8','SOAPAction':f'"https://orapiweb.pttor.com/{operation}"','User-Agent':'SirawitLabs-Calculator/5.1 Diagnostics'})
    if not response:return False
    try:
        outer=ET.fromstring(response)
        results=[x for x in outer.iter() if local(x.tag)==operation+'Result']
        print('Result elements:',len(results))
        if not results:return False
        node=results[0]
        if len(node):doc=node
        else:
            inner=(node.text or '').strip()
            for _ in range(2):
                if inner.startswith('&lt;') or inner.startswith('&amp;lt;'):inner=html.unescape(inner)
            doc=ET.fromstring(inner)
        print('Root:',local(doc.tag),'element tags:',dict(Counter(local(x.tag) for x in doc.iter()).most_common(35)))
        valid=0
        for row in doc.iter():
            fields={local(c.tag).upper():c.text for c in row}
            name=next((fields[k] for k in ('PRODUCT','PRODUCT_NAME','OILNAME','OIL_NAME','NAME','PRODUCTNAME') if fields.get(k)),None)
            price=next((fields[k] for k in ('PRICE','PRICE_TODAY','PRICETODAY','RETAIL_PRICE','RETAILPRICE','PRICE_VALUE','SELLINGPRICE') if fields.get(k)),None)
            if name and price:
                try:
                    if 0<float(price.replace(',',''))<200:valid+=1;print('Valid row:',str(name)[:90],price,fields.get('PRICE_DATE',fields.get('PRICEDATE','')))
                except ValueError:pass
        print('Valid price rows:',valid)
        return valid>0
    except Exception as e:print('XML diagnostic:',type(e).__name__,str(e)[:150]);return False
ptt_ok=inspect_ptt('CurrentOilPrice','EN') or inspect_ptt('CurrentOilPrice','TH')
if not ptt_ok:
    today=datetime.now(ZoneInfo('Asia/Bangkok')).date()
    for i in range(4):
        if inspect_ptt('GetOilPrice','EN',today-timedelta(days=i)):ptt_ok=True;break
print('PTT price rows available:',ptt_ok)

for title,url in [('ThaiGoldToday','https://www.thaigoldtoday.com/api/gold-price'),
                  ('Rakhagold','https://rakhagold.com/wp-json/priceradar/v1/gold/latest')]:
    response=fetch(title,url)
    if response:
        try:
            data=json.loads(response)
            print('JSON root keys:',list(data)[:30] if isinstance(data,dict) else 'array')
            if isinstance(data,dict):
                for key in ('current','data','latest','result'):
                    if isinstance(data.get(key),dict): print(key,'keys:',list(data[key])[:24])
        except Exception as e:print('JSON diagnostic:',type(e).__name__,str(e)[:100])

# The public oil API's response schema has not yet been verified; record structure only.
# Avoid calling both Rakhagold endpoints within one minute (provider rate limit).
print('Rakhagold oil endpoint documented at https://rakhagold.com/api-docs/; not enabled until payload is verified.')
