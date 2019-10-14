# Lookup lobid/nwbib by an Ortsname and get its lat and lon
# date: 2019-10-14
# author: dr0i
rm ortsnameLatLon.txt

getLatLonByOrt () {
IFS=\
;
ORT=$1
DATA=$(curl "https://lobid.org/resources/search?q=spatial.label%3A%22$ORT%22+AND+inCollection.id%3A%22http%3A%2F%2Flobid.org%2Fresources%2FHT014176012%23!%22&from=1&size=1&format=json" 2>/dev/null)
LAT=$(echo $DATA | grep '"lat" : ' |cut -d " " -f13|tr -d ','|head -n1)
LON=$(echo $DATA | grep '"lon" : ' |cut -d " " -f13|tr -d ','|head -n1)
printf "$ORT: \"$LAT,$LON\"\n" >> ortsnameLatLon.txt
}

##
# main
##

# debug:
# getLatLonByOrt $1; exit

# get the data at http://download.codingdavinci.de/index.php/s/5pimsCHErbWMfDs:
# curl "http://download.codingdavinci.de/index.php/s/5pimsCHErbWMfDs/download?path=%2F&files=content_export1567751077.csv" > content_export1567751077.csv

csvtool drop 1 content_export1567751077.csv > euregioHistoryWithoutFirstRow.csv
IFS="
";
for i in $(csvtool col 13 euregioHistoryWithoutFirstRow.csv | sed -s 's#"##g' | sed 's#, #\n#g' | sed 's#; #\n#g'| sed 's#-##g' | sort -u); do
	if [ ! -z "$i" ]; then
		getLatLonByOrt $i
	fi
done
