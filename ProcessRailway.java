import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmRelationInfo;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ProcessRailway implements Profile {
    /*
     * The processing happens in 3 steps: 1. On the first pass through the input
     * file, store relevant information from OSM bike route relations 2. On the
     * second pass, emit linestrings for each OSM way contained in one of those
     * relations 3. Before storing each finished tile, Merge linestrings in each
     * tile with the same tags and touching endpoints
     */

    /*
     * Step 1)
     *
     * Planetiler processes the .osm.pbf input file in two passes. The first pass
     * stores node locations, and invokes preprocessOsmRelation for reach relation
     * and stores information the profile needs during the second pass when we emit
     * map feature for ways contained in that relation.
     */

    // Minimal container for data we extract from OSM bicycle route relations. This
    // is held in RAM so keep it small.
    private record RouteRelationInfo(
            // OSM ID of the relation (required):
            @Override long id,
            // Values for tags extracted from the OSM relation:
            String name, String ref, String route, String colour, String network) implements OsmRelationInfo {
    }

    private record Stop(@Override long id, String name) implements OsmRelationInfo {
    }

    @Override
    public List<OsmRelationInfo> preprocessOsmRelation(OsmElement.Relation relation) {
        // If this is a "route" relation ...
        if (relation.hasTag("type", "route")
                && relation.hasTag("route", "subway", "tram", "train", "light_rail", "railway"))
            // then store a RouteRelationInfo instance with tags we'll need later
            return List.of(new RouteRelationInfo(relation.id(), relation.getString("name"), relation.getString("ref"),
                    relation.getString("route"), relation.getString("colour"), relation.getString("network")));

        // for any other relation, return null to ignore
        return null;

    }

    /*
     * Step 2)
     *
     * On the second pass through the input .osm.pbf file, for each way in a
     * relation that we stored data about, emit a linestring map feature with
     * attributes derived from the relation.
     */

    @Override
    public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {
        // ignore nodes and ways that should only be treated as polygons
        if (sourceFeature.canBeLine()
                && sourceFeature.hasTag("railway", "rail", "subway", "light_rail", "construction", "funicular",
                        "abandoned", "disused", "narrow_gauge", "monorail", "tram")) {
            // get all the RouteRelationInfo instances we returned from
            // preprocessOsmRelation that this way belongs to
            String relation_ref = null;
            String relation_name = null;
            String relation_colour = null;
            String relation_network = null;
            for (var routeInfo : sourceFeature.relationInfo(RouteRelationInfo.class)) {
                // (routeInfo.role() also has the "role" of this relation member if needed)
                RouteRelationInfo relation = routeInfo.relation();
                if (relation.ref != null)
                    relation_ref = relation.ref;
                if (relation.name != null)
                    relation_name = relation.name;
                if (relation.colour != null)
                    relation_colour = relation.colour;
                if (relation_network != null)
                    relation_network = relation.network;
            }

            boolean construction = sourceFeature.hasTag("railway", "construction")
                    || sourceFeature.getTag("opening_date", "").toString().compareTo("2025-06") > 0;

            String railway = construction ? sourceFeature.getTag("construction", "").toString()
                    : sourceFeature.getTag("railway").toString();

            boolean service = sourceFeature.hasTag("service", "yard", "spur", "siding", "crossover");
            boolean branch = sourceFeature.hasTag("usage", "branch");
            boolean main = sourceFeature.hasTag("usage", "main");

            features.line(railway)
                    .setAttr("relation_name", relation_name)
                    .setAttr("relation_ref", relation_ref)
                    .setAttr("relation_colour", relation_colour)
                    .setAttr("relation_network", relation_network)
                    .setAttr("construction", construction)
                    .setAttr("service", service)
                    .setAttr("electrified", sourceFeature.getTag("electrified"))
                    .setAttr("tunnel", sourceFeature.getTag("tunnel"))
                    .setAttr("bridge", sourceFeature.getTag("bridge"))
                    .setAttr("voltage", sourceFeature.getTag("voltage"))
                    .setAttr("frequency", sourceFeature.getTag("frequency"))
                    .setAttr("maxspeed", sourceFeature.getTag("maxspeed"))
                    .setAttr("branch", branch)
                    .setAttr("mainline", main)
                    .setAttr("highspeed", sourceFeature.getTag("highspeed"))
                    .setAttr("subway", sourceFeature.getTag("subway"))
                    // don't filter out short line segments even at low zooms because the next step
                    // needs them
                    // to merge lines with the same tags where the endpoints are touching
                    .setMinPixelSize(0);
        }

        if (sourceFeature.isPoint() && sourceFeature.hasTag("railway", "signal", "switch", "crossing",
                "level_crossing", "railway_crossing", "tram_level_crossing", "tram_stop", "subway_entrance", "stop")) {
            features.point(sourceFeature.getTag("railway").toString()).setAttr("name", sourceFeature.getTag("name"));
        }

        if (sourceFeature.isPoint() && sourceFeature.hasTag("public_transport", "station")) {
            features.point("station").setAttr("name", sourceFeature.getTag("name"));
        }

        if (sourceFeature.hasTag("railway", "platform")) {
            if (sourceFeature.canBeLine() && !sourceFeature.canBePolygon()) {
                features.line("platforms_line").setAttr("subway", sourceFeature.getTag("subway"));
            } else if (sourceFeature.canBePolygon()) {
                features.polygon("platforms").setAttr("subway", sourceFeature.getTag("subway"));
            }
        }
    }

    /*
     * Step 3)
     *
     * Before writing tiles to the output, first merge linestrings where the
     * endpoints are touching that share the same tags to improve line and text
     * rendering in clients.
     */

    @Override
    public List<VectorTile.Feature> postProcessLayerFeatures(String layer, int zoom, List<VectorTile.Feature> items) {
        // FeatureMerge has several utilities for merging geometries in a layer that
        // share the same tags.
        // `mergeLineStrings` combines lines with the same tags where the endpoints
        // touch.
        // Tiles are 256x256 pixels and all FeatureMerge operations work in tile pixel
        // coordinates.
        return FeatureMerge.mergeLineStrings(items, 0.5, // after merging, remove lines that are still less than 0.5px
                                                         // long
                0.1, // simplify output linestrings using a 0.1px tolerance
                4 // remove any detail more than 4px outside the tile boundary
        );
    }

    /*
     * Main entrypoint for this example program
     */
    public static void main(String[] args) throws Exception {
        run(Arguments.fromArgsOrConfigFile(args));
    }

    static void run(Arguments args) throws Exception {
        String base = ".";
        Planetiler.create(args).setProfile(new ProcessRailway())
                // override this default with osm_path="path/to/data.osm.pbf"
                .addOsmSource("osm", Path.of(base, "data", "europe-railway.osm.pbf"), "geofabrik:europe")
                .overwriteOutput(Path.of(base, "data", "europe-railway.pmtiles")).run();
    }
}
