package com.maxdemarzi;

import com.maxdemarzi.Evaluators.ReachedDestinationAtEvaluator;
import com.maxdemarzi.Evaluators.ReachedDestinationEvaluator;
import com.maxdemarzi.Expanders.DirectExpander;
import com.maxdemarzi.Expanders.NonStopExpander;
import com.maxdemarzi.Expanders.OneStopExpander;
import com.maxdemarzi.Expanders.TwoStopExpander;
import com.maxdemarzi.Helpers.FlightComparator;
import com.maxdemarzi.Helpers.Labels;
import com.maxdemarzi.Helpers.RelationshipTypes;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.maxdemarzi.Helpers.Validators.getValidQueryInput;

@Path("/service")
public class Service {
    public static final Integer DEFAULT_RECORD_LIMIT = 50;
    public static final Long DEFAULT_TIME_LIMIT = 2000L; // 2000 ms
    private static final FlightComparator FLIGHT_COMPARATOR = new FlightComparator();

    private static Set<RelationshipType> flightTypes;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/helloworld")
    public Response helloWorld() throws IOException {
        Map<String, String> results = new HashMap<String,String>(){{
            put("hello","world");
        }};
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @GET
    @Path("/migrate")
    public Response migrate(@Context GraphDatabaseService db) throws IOException {
        ArrayList<String> results = new ArrayList<>();
        boolean migrated;
        try (Transaction tx = db.beginTx()) {
            migrated = db.schema().getConstraints().iterator().hasNext();
        }

        if (migrated) {
            results.add("Already Migrated!");
        } else {
            // Perform Migration
            try (Transaction tx = db.beginTx()) {
                Schema schema = db.schema();
                schema.constraintFor(Labels.Metadata)
                        .assertPropertyIsUnique("name")
                        .create();
                schema.constraintFor(Labels.Airline)
                        .assertPropertyIsUnique("code")
                        .create();
                schema.constraintFor(Labels.AirportDay)
                        .assertPropertyIsUnique("key")
                        .create();
                tx.success();
            }
            // Wait for indexes to come online
            try (Transaction tx = db.beginTx()) {
                Schema schema = db.schema();
                schema.awaitIndexesOnline(1, TimeUnit.DAYS);
            }
            results.add("Migrated");
        }

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    /**
     * JSON formatted body requires:
     *  from: An Array of Departure Airports (ex. ["IAH","HOU"])
     *  to: An Array of Arrival Airports (ex. ["LGA","EWR"])
     *  day: A number representing linux epoch time seconds in GMT (ex. 1430784000 for 5/5/2015)
     *  airlines: An Array of high priority Airlines to traverse first
     *  record_limit: A number representing the maximum results to gather
     *  time_limit: A number representing the maximum time to gather results
     */
    @POST
    @Path("/query")
    public Response query(String body, @Context GraphDatabaseService db) throws IOException {
        ArrayList<HashMap> results = new ArrayList<>();

        // Validate our input or exit right away
        HashMap input = getValidQueryInput(body);

        // Set our available flight types if we haven't already.
        // If we add or remove an Airline we should call setFlightTypes again.
        if (flightTypes == null) {
            setFlightTypes(db);
        }

        // Add High Priority Airlines to be checked first, and then add the rest
        Set<RelationshipType> orderedFlightTypes = new ListOrderedSet<>();
        for ( String airline : (ArrayList<String>)input.get("airlines")) {
            orderedFlightTypes.add(DynamicRelationshipType.withName(airline + "_FLIGHT"));
        }

        // Adding full list of Airlines, duplicates do not affect original ordering
        orderedFlightTypes.addAll(flightTypes);

        // Create our Expanders which control the traversals
        NonStopExpander nonStopExpander = new NonStopExpander(
                (ArrayList<String>)input.get("to"),
                flightTypes.toArray(new RelationshipType[flightTypes.size()]),
                (long)input.get("time_limit")
        );

        DirectExpander directExpander = new DirectExpander(
                (ArrayList<String>)input.get("to"),
                flightTypes.toArray(new RelationshipType[flightTypes.size()]),
                (long)input.get("time_limit"),
                (ArrayList<String>)input.get("exclusions")
        );

        OneStopExpander oneStopExpander = new OneStopExpander(
                (ArrayList<String>)input.get("to"),
                flightTypes.toArray(new RelationshipType[flightTypes.size()]),
                (long)input.get("time_limit"),
                (ArrayList<String>)input.get("exclusions")
        );

        TwoStopExpander twoStopExpander = new TwoStopExpander(
                (ArrayList<String>)input.get("to"),
                flightTypes.toArray(new RelationshipType[flightTypes.size()]),
                (long)input.get("time_limit"),
                (ArrayList<String>)input.get("exclusions")
        );

        ReachedDestinationEvaluator reachedDestinationEvaluator = new ReachedDestinationEvaluator((ArrayList<String>)input.get("to"));
        ReachedDestinationAtEvaluator reachedDestinationAtEvaluator = new ReachedDestinationAtEvaluator((ArrayList<String>)input.get("to"), 4);
        ReachedDestinationAtEvaluator reachedDestinationAtTwoHopsEvaluator = new ReachedDestinationAtEvaluator((ArrayList<String>)input.get("to"), 7);

        Long minDistance = 25000L;

        try (Transaction tx = db.beginTx()) {
            for (String key : getAirportDayKeys(input)) {
                Node departureAirportDay = db.findNode(Labels.AirportDay, "key", key);

                if (!(departureAirportDay == null)) {
                    // Non-Stop Flights
                    TraversalDescription nonStopTraversalDescription = db.traversalDescription()
                            .depthFirst()
                            .expand(nonStopExpander)
                            .evaluator(reachedDestinationEvaluator)
                            .uniqueness(Uniqueness.RELATIONSHIP_PATH);

                    minDistance = collectFlights(results, departureAirportDay, nonStopTraversalDescription, 1.0, input, minDistance);

                    // Direct Flights
                    TraversalDescription directTraversalDescription = db.traversalDescription()
                            .depthFirst()
                            .expand(directExpander)
                            .evaluator(reachedDestinationEvaluator)
                            .uniqueness(Uniqueness.RELATIONSHIP_PATH);

                    minDistance = collectFlights(results, departureAirportDay, directTraversalDescription, 2.0, input, minDistance);

                    // One-Stop Flights
                    TraversalDescription oneStopTraversalDescription = db.traversalDescription()
                            .depthFirst()
                            .expand(oneStopExpander)
                            .evaluator(reachedDestinationAtEvaluator)
                            .uniqueness(Uniqueness.RELATIONSHIP_PATH);

                    minDistance = collectFlights(results, departureAirportDay, oneStopTraversalDescription, 2.5, input, minDistance);

                    // Two-Stop Flights
                    TraversalDescription twoStopTraversalDescription = db.traversalDescription()
                            .depthFirst()
                            .expand(twoStopExpander)
                            .evaluator(reachedDestinationAtTwoHopsEvaluator)
                            .uniqueness(Uniqueness.RELATIONSHIP_PATH);

                    minDistance = collectFlights(results, departureAirportDay, twoStopTraversalDescription, 3.0, input, minDistance);
                }
            }
        }

        // We now perform a final distance filter to make sure we don't recommend a flight from SFO to LAX via ORD.
        for (Iterator<HashMap> iterator = results.iterator(); iterator.hasNext();) {
            HashMap result = iterator.next();
            if ((Long)result.get("distance") > 2.5 * minDistance){
                iterator.remove();
            }
        }

        Collections.sort(results, FLIGHT_COMPARATOR);

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    private Long collectFlights(ArrayList<HashMap> results, Node departureAirportDay, TraversalDescription td, Double score, HashMap input, Long minDistance) {
        int recordLimit = (Integer)input.get("record_limit");
        // Stop collecting records if I already reached the requested limit
        if(results.size() >= recordLimit) {
            return minDistance;
        }

        for (org.neo4j.graphdb.Path position : td.traverse(departureAirportDay)) {
            // We check this twice because I want to stop collecting records before I even begin traversing
            // if I have reached the limit, or as soon as I reach the limit while traversing
            if(results.size() < recordLimit) {
                HashMap<String, Object> result = new HashMap<>();
                ArrayList<HashMap> flights = new ArrayList<>();
                Long distance = 0L;
                for (Node flight : position.nodes()) {
                    if (flight.hasLabel(Labels.Flight)) {
                        HashMap flightInfo = new HashMap();
                        for (String property : flight.getPropertyKeys()) {
                            flightInfo.put(property, flight.getProperty(property));
                        }
                        flights.add(flightInfo);
                        distance += ((Number) flight.getProperty("distance")).longValue();
                    }
                }
                // Update our minimum distance as we go along
                if (distance < minDistance) {
                    minDistance = distance;
                }

                // Add the flight to our result set for now, but it may be filtered out again if we find a
                // smaller minimum  distance while traversing
                if (distance < 2.5 * minDistance) {
                    result.put("flights", flights);
                    result.put("score", score);
                    result.put("distance", distance);
                    results.add(result);
                }
            } else {
                break;
            }
        }

        return minDistance;
    }

    private ArrayList<String> getAirportDayKeys(HashMap input) {
        ArrayList<String> departureAirportDayKeys = new ArrayList<>();
        for(String code : (ArrayList<String>)input.get("from")) {
            String key = code + "-" + input.get("day");
            departureAirportDayKeys.add(key);
        }
        return departureAirportDayKeys;
    }

    public static void setFlightTypes(@Context GraphDatabaseService db) {
        Set<RelationshipType> flightTypes = new HashSet<>();
        try (Transaction tx = db.beginTx()) {
            Node airlines = db.findNode(Labels.Metadata, "name", "Airlines");
            if (airlines!=null) {
                for (Relationship r : airlines.getRelationships(Direction.INCOMING, RelationshipTypes.IS_AIRLINE)) {
                    final Node carrier = r.getStartNode();
                    final String flightType = carrier.getProperty("code") + "_FLIGHT";
                    flightTypes.add(DynamicRelationshipType.withName(flightType));
                }
            }
            Service.flightTypes = flightTypes;
        }
    }

}
