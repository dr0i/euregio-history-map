# euregio-history-map
Kartenvisualisierung der Datensätze von https://euregio-history.net/de

Dem Datensatz fehlen Geodaten für die Visualisierung. Die Ortsnamen liegen nur als Literale vor, deshalb bedarf es einer Disambiguierung, wenn diese Ortsnamen in einer
Geodatenbank gesucht werden sollen, um an die Geodaten zu gelangen.

Ein erster Ansatz, die nominatim der OpenStreetMap zu verwenden, hatte zuviele
falsche Treffer geliefert.

Der zweite, aktuelle Ansatz, dem Nachschlagen in lobid-nwbib (und damit der
Eingrenzung des Suchraums auf Orte innerhalb von NRW) bringt lediglich 18 von
68 Matches.

Ein dritter Ansatz wäre z.B. Pelias zu verwenden.
Oder die Geodaten händisch zu recherchieren - so viele sind es ja nicht (68 -18 = 50).
