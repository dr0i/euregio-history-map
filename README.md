# euregio-history-map
Kartenvisualisierung der Datensätze von https://euregio-history.net/de

Der [Datensatz wurde für codingDavinci in Dortmund 2019 hochgeladen](http://download.codingdavinci.de/index.php/s/5pimsCHErbWMfDs/download?path=%2F&files=content_export1567751077.csv).

Zum [aktuellen Stand der Visualisierung](http://lobid.org/download/tmp/euregio-history/map.html).
Auf die blauen Markierer klicken um eine Liste von Titeln mit Links zu den Texten zu erhalten. 

Dem Datensatz fehlen Geodaten für die Visualisierung. Die Ortsnamen liegen nur als Literale vor, deshalb bedarf es einer Disambiguierung, wenn diese Ortsnamen in einer
Geodatenbank gesucht werden sollen, um an die Geodaten zu gelangen.

Der aktuelle Ansatz benutzt die wikidata API, um die Namen aufzulösen, und dann
GeoDaten zu bekommen aus der Wikidata.
Es gibt nun für 40 von 71 Orte GeoDaten.
Dieses Ergebnis hat ein paar Fehlmatches.
Weiter Infos dazu in https://github.com/dr0i/euregio-history-map/issues/1.

Alle Daten und Code in diesem Repo sind, wenn nicht anders vermerkt, CC0 lizensiert.

# Build

Prerequisites: Java 8, Maven 3; verify with `mvn -version`.

```bash
mkdir ~/git; cd ~/git; git clone https://github.com/dr0i/euregio-history-map.git; cd euregio-history-map
```
Get the data:
```bash
curl -L  "http://download.codingdavinci.de/index.php/s/5pimsCHErbWMfDs/download?path=%2F&files=content_export1567751077.csv" > content_export1567751077.csv
```
Run:
```bash
 mvn clean install; mvn exec:java -Dexec.mainClass=CreateGeoJson >out.log
```
See the geojson output:
```bash
cat euregioHistory.geojson
```
Create the "missingGeoData.txt" and "missingWdEntity.txt" files:
```bash
bash getMissingWDEntityOrGeoData.sh
```

