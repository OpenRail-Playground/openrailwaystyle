#!/usr/bin/env just --justfile

# Display available recipes
help:
    @just --list --unsorted

# Get OSM data
download-osm:
    # Download the raw OpenStreetMap data from Europe
    wget https://download.geofabrik.de/europe-latest.osm.pbf -O data/europe-latest.osm.pbf

# Keep only railway data
prepare-osm:
    # Convert it to the o5m format that allows filtering
    osmconvert data/europe-latest.osm.pbf -o=data/europe-latest.o5m

    # Only keep railway related data
    osmfilter data/europe-latest.o5m --keep="railway= razed:railway= public_transport= highway=bus_stop route=" -o=data/europe-railway.o5m

    # Convert back to the railway.osm.pbf format
    osmconvert data/europe-railway.o5m -o=data/europe-railway.osm.pbf

generate-railway-pmtiles:
    # Process the osm.pbf file to generate pmtiles
    JAVA_TOOL_OPTIONS="-Xmx8g" java -cp planetiler.jar ProcessRailway.java

