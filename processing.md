# Download the raw OpenStreetMap data from Europe
wget https://download.geofabrik.de/europe-latest.osm.pbf

# Convert it to the o5m format that allows filtering
osmconvert europe-latest.osm.pbf -o=europe-latest.o5m

# Only keep railway related data
osmfilter europe-latest.o5m --keep="railway= razed:railway= public_transport= highway=bus_stop route="
-o=europe-railway.o5m

# Convert back to the railway.osm.pbf format
osmconvert europe-railway.o5m -o=europe-railway.osm.pbf

# Process the osm.pbf file to generate pmtiles
JAVA_TOOL_OPTIONS="-Xmx8g" java -cp planetiler.jar ProcessRailway.java
