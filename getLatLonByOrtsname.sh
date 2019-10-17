#!/bin/bash
# Lookup lobid/nwbib by an Ortsname and get its lat and lon
# date: 2019-10-14
# author: dr0i
echo "ort, wd, latitude,longitude" > ortsnameWDLatLon.txt
echo '{
  "type": "FeatureCollection",
  "features": [
' > ortsnameWDLatLon.geojson

declare -A wikidata
exit
getLatLonByOrtViaWikidata () {
IFS=\
;
ORT=$1
WD=$2
TARGET=$3
DATA=$(curl -L  https://www.wikidata.org/entity/$WD)
LAT=$(echo $DATA | jq . |grep latitude | sed 's#.*"latitude": ##g'| sed 's#,##g#')
LON=$(echo $DATA | jq . |grep longitude | sed 's#.*"longitude": ##g'| sed 's#,##g#')
if [ ! -z $LAT ]; then
printf "$ORT,$WD,$LAT,$LON\n" >> ortsnameWDLatLon.txt
printf '{
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [' >> ortsnameWDLatLon.geojson
printf "$LON,$LAT" >> ortsnameWDLatLon.geojson
printf ' ]
      },
      "properties": {
        "ort": "' >> ortsnameWDLatLon.geojson
printf "$ORT" >> ortsnameWDLatLon.geojson
printf '",
        "wd": "' >>ortsnameWDLatLon.geojson
printf "$WD" >>ortsnameWDLatLon.geojson
printf '",
        "target": "' >>ortsnameWDLatLon.geojson
printf "$TARGET" >>ortsnameWDLatLon.geojson
printf '" }
    },
' >>ortsnameWDLatLon.geojson
fi
}

getWikidataEntityAndLatLOn () {
ORT=$1
TARGET=$2
WD=$(curl -G  https://www.wikidata.org/w/api.php --data-urlencode "action=wbgetentities" --data-urlencode "sites=dewiki" --data-urlencode "titles=$ORT" --data-urlencode "props=descriptions" --data-urlencode "languages=de" --data-urlencode "format=json"  |jq .|grep id|sed  's#.*"id": "##g' | sed 's#",##g' );
getLatLonByOrtViaWikidata $ORT $WD $TARGET
}
##
# main
##

# get the data at http://download.codingdavinci.de/index.php/s/5pimsCHErbWMfDs:
# curl "http://download.codingdavinci.de/index.php/s/5pimsCHErbWMfDs/download?path=%2F&files=content_export1567751077.csv" > content_export1567751077.csv

csvtool drop 1 content_export1567751077.csv > euregioHistoryWithoutFirstRow.csv
IFS="
";
for i in $(csvtool col 13,17 euregioHistoryWithoutFirstRow.csv | sed -s 's#"##g' | sed 's#, #\n#g' | sed 's#; #\n#g'| sed 's#-##g' | sort); do
	ort=$(echo "$i"|csvtool col 1 -)
	target=$(echo "$i"|csvtool col 2 -)
	if [ ! -z "$ort" -a ! -z "$target" ]; then
		#getLatLonByOrt $i
		echo $ort,$target
		getWikidataEntityAndLatLOn $ort $target
	fi
done
