# euregio-history-map
Hier steht beschrieben, wie die interaktive Karte der Geschichten, die euregio-history.net sammelt, erzeugt wurde.


Die Karte wurde produktiv geschaltet in [euregio-history](https://euregio-history.net/): dort auf die blauen Markierer klicken um eine Liste von Titeln mit Links zu den Texten zu erhalten.

Dieses Miniprojekt wurde für die [Coding Da Vinci Westfalen 2019](https://codingdavinci.de/events/westfalen-ruhrgebiet/) angemeldet.

# Ausgangslage: die Daten
Dem [originären Datensatz von Coding Da Vinci Westfalen 2019](http://download.codingdavinci.de/index.php/s/5pimsCHErbWMfDs/download?path=%2F&files=content_export1567751077.csv)
 fehlen Geodaten für die Visualisierung. Die Ortsnamen liegen nur als Literale vor, deshalb bedarf es einer Disambiguierung, wenn diese Ortsnamen in einer
Geodatenbank gesucht werden sollen, um an die Geodaten zu gelangen.

Der aktuelle Ansatz benutzt die Wikidata API, um die Namen aufzulösen, um dann Geo-Daten aus der Wikidata zu bekommen.

# Erzeugte Geodaten
Es gibt nun für 40 von 71 Orte GeoDaten.

# Verbesserungsmöglichkeiten
Dieses Ergebnis hat ein paar Fehlmatches.
Weiter Infos dazu in https://github.com/dr0i/euregio-history-map/issues/1.

Alle Daten und Code in diesem Repo sind, wenn nicht anders vermerkt, CC0 lizensiert.

# Benutzungsanleitung

Die Datei `map.html` und die `euregioHistory.geojson` müssen auf einem Webserver abgelegt werden.

# Build
Um die [Geojson-Daten](https://github.com/dr0i/euregio-history-map/blob/master/euregioHistory.geojson) aus den originären Daten zu erzeugen muss(te) folgendes getan werden:

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
