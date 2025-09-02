# Open Railway Styles

Design a style for railways using [OpenStreetMap](https://www.openstreetmap.org/) data.

Most maps favour only roads. We want to display railway information as a base map.

Too many railway services use a general purpose OpenStreetMap base layer (or even worse, a proprietary map from a GAFAM company) and this project aims to change that.

As the [OpenRailwayMap](https://www.openrailwaymap.org/) project shows, OpenStreetMap has a lot of information that can be used to design a nice looking general purpose railway base map.

In this project we provide:
- a style that focuses on showing railway infrastructure,
- a script that extracts the needed railway data from OpenStreetMap (with certain specific tags) and generates a [PMTiles](https://github.com/protomaps/PMTiles),
- a demonstration webpage to explore the style.

## Background

This project has been initiated during the [Hack4Rail 2025](https://hack4rail.event.sbb.ch/en/), a joint hackathon organized by the railway companies SBB, ÖBB, and DB in partnership with the OpenRail Association.

Three teams tackled the challenge under different angles (focus on a specific zoom level, different use cases…). Their results can be found here:

* [Infra Viewers](https://openrail-playground.github.io/openrailwaystyle/infra_viewers.html)
* [European Train Spotter](https://openrail-playground.github.io/openrailwaystyle/european_train_spotter.html)
* [Openstreet Trainsformer](https://openrail-playground.github.io/openrailwaystyle/openstreet_trainsformer.html)

<p align="center">
  <img alt="Hack4Rail Logo" src="img/hack4rail-logo.jpg" width="220"/>
</p>

## How does it work?

Maps on the web are nowadays generally rendered on the browser with a library such as [MapLibre](https://github.com/maplibre/maplibre-gl-js).

The data is pre-processed into [Vector Tiles](https://wiki.openstreetmap.org/wiki/Vector_tiles) to only access to the needed data at a given coordinate and zoom level (when displaying Europe, we don’t need the position of every three).

Similarly, only some tags from OpenStreetMap are used. There are conventions such as [OpenMapTiles](https://openmaptiles.org/schema/) that define what tags are included.

However, for this project, the usual data schemes doesn’t have enough details for railway specific rendering and we

For this preprocessing, we use the tool [planetiler](https://github.com/onthegomap/planetiler). The file [ProcessRailway.java] contains all the specific configuration.

Read the [justfile] to see how to run the processing different steps.

## Going further

The project focuses on the physical representation of rail networks. However, travelers often need information about commercial lines.
The [publication of Patrick Brosi and Hannah Bast](https://ad-publications.informatik.uni-freiburg.de/Large-Scale_Generation_of_Transit_Maps_from_OpenStreetMap_Data.pdf) could be considered to transform the physical layer to have more information.


## License

The content of this repository is licensed under the [Apache 2.0 license](LICENSE).
