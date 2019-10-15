# euregio-history-map
Kartenvisualisierung der Datensätze von https://euregio-history.net/de

Der [Datensatz wurde für codingDavinci in Dortmund 2019 hochgeladen](http://download.codingdavinci.de/index.php/s/5pimsCHErbWMfDs/download?path=%2F&files=content_export1567751077.csv).

Dem Datensatz fehlen Geodaten für die Visualisierung. Die Ortsnamen liegen nur als Literale vor, deshalb bedarf es einer Disambiguierung, wenn diese Ortsnamen in einer
Geodatenbank gesucht werden sollen, um an die Geodaten zu gelangen.

Ein erster Ansatz, die nominatim der OpenStreetMap zu verwenden, hatte zuviele
falsche Treffer geliefert.

Der zweite Ansatz, dem Nachschlagen in lobid-nwbib (und damit der
Eingrenzung des Suchraums auf Orte innerhalb von NRW) bringt lediglich [18 von
68 Matches](https://github.com/dr0i/euregio-history-map/blob/master/ortsnameLatLon.txt).

Der aktuelle Ansatz benutzt die wikidata API, um die Namen aufzulösen, und dann
GeoDaten zu bekommen aus der Wikidata. Dieses Ergebnis hat ein paar Fehlmatches.
Es gibt nun [44 von 67 Matches](https://github.com/dr0i/euregio-history-map/blob/master/ortsnameWDLatLon.txt).

Ein dritter Ansatz wäre z.B. Pelias zu verwenden.
Oder die Geodaten händisch zu recherchieren - so viele sind es ja nicht (68 -18 = 50).
