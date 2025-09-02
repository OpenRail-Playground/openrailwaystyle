# Open Railway Styles

Design a style for railways using [OpenStreetMap](https://www.openstreetmap.org/) data.

Most maps favour only roads. We want to display railway information as a base map.

Too many railway services use a general purpose OpenStreetMap base layer (or even worse, a proprietary map from a GAFAM company) and this project aims to change that.

As the [OpenRailwayMap](https://www.openrailwaymap.org/) project shows, OpenStreetMap has a lot of information that can be used.

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


## License

The content of this repository is licensed under the [Apache 2.0 license](LICENSE).
