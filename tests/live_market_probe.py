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

soap='''<?xml version="1.0" encoding="utf-8"?>
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
<soap:Body><CurrentOilPrice xmlns="http://www.pttor.com"><Language>en</Language></CurrentOilPrice></soap:Body></soap:Envelope>'''.encode()
response=fetch('PTT OR SOAP','https://orapiweb.pttor.com/oilservice/OilPrice.asmx',soap,{
 'Content-Type':'text/xml; charset=utf-8',
 'SOAPAction':'"https://orapiweb.pttor.com/CurrentOilPrice"',
 'User-Agent':'BeatCalculator-V5-Diagnostic/1.0'})
if response:
    try:
        outer=ET.fromstring(response)
        results=[x for x in outer.iter() if x.tag.rsplit('}',1)[-1].endswith('OilPriceResult')]
        print('Result elements:',len(results))
        if results:
            inner=html.unescape(''.join(results[0].itertext()).strip())
            doc=ET.fromstring(inner)
            rows=[x for x in doc.iter() if x.tag.rsplit('}',1)[-1] in ('DataAccess','Table','OilPrice')]
            print('Row candidates:',len(rows))
            if rows:
                print('First row fields:',[x.tag.rsplit('}',1)[-1] for x in rows[0]][:35])
                prices=[x.text for row in rows for x in row if x.tag.rsplit('}',1)[-1].upper() in ('PRICE','RETAIL_PRICE','PRICETODAY')]
                print('Price fields:',len(prices),'nonempty:',sum(bool(str(x).strip()) for x in prices))
    except Exception as e:print('XML diagnostic:',type(e).__name__,str(e)[:150])
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
