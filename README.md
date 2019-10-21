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
