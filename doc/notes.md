<http://aers.data2semantics.org/yasgui/>

# BAG data from Bart
http://data.resc.info/bag/sparql

```
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
SELECT * WHERE {
  ?sub <http://www.w3.org/ns/locn#postCode> "1055HB".
  ?sub <http://www.w3.org/ns/locn#locatorDesignator> "20"^^xsd:decimal.
  ?sub <http://www.w3.org/ns/locn#locatorDesignator> "3".
  ?sub <http://data.resc.info/bag/resource/vocab/adres_adresseerbaarobject> ?obj.
  ?obj <http://data.resc.info/bag/resource/vocab/verblijfsobjectactueel_pand> ?pand.
  ?pand <http://data.resc.info/bag/resource/vocab/pandactueel_bouwjaar> ?b.
} LIMIT 10
```

# Data sources
BAG : http://lod.geodan.nl/BAG/
TOP10 : http://plod.nieuwland.nl/top10nl/

http://ep-online.nl/EnergieLabel.aspx
(example 9415RD, 2)

http://geodata.nationaalgeoregister.nl/geocoder/Geocoder?zoekterm=Amsterdam+Trouringhstraat+20+1055HB

# Weird thing with the Churches
http://pilod-huiskluis.appspot.com/?postcode=1012AD&number=73

