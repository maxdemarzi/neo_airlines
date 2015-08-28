package com.maxdemarzi;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Fixtures.AirlineResourceFixtures.AIRLINES_STATEMENT;
import static org.junit.Assert.assertArrayEquals;

public class NonStopMultipleAirports {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(AIRLINES_STATEMENT)
            .withFixture(MODEL_STATEMENT)
            .withExtension("/v1", Service.class);

    @Test
    public void shouldFindNonStopRoutes() {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/service/query").toString(),
                QUERY_MAP);
        ArrayList actual = response.content();
        assertArrayEquals(EXPECTED.toArray(), actual.toArray());
    }

    public static final String MODEL_STATEMENT =
            new StringBuilder()
                    .append("CREATE (iah_20150901:AirportDay {key:'IAH-1441065600'})")
                    .append("CREATE (ewr_20150901:AirportDay {key:'EWR-1441065600'})")
                    .append("CREATE (lga_20150901:AirportDay {key:'LGA-1441065600'})")

                    .append("CREATE (dst1:Destination {code:'EWR'})")
                    .append("CREATE (dst2:Destination {code:'LGA'})")

                    .append("CREATE (flight1:Flight {code:'UA-1', departs:1441119600, arrives:1441126800, distance:718})")
                    .append("CREATE (flight2:Flight {code:'UA-2', departs:1441123200, arrives:1441130400, distance:732})")

                    .append("CREATE (iah_20150901)-[:HAS_DESTINATION]->(dst1)")
                    .append("CREATE (iah_20150901)-[:HAS_DESTINATION]->(dst2)")
                    .append("CREATE (dst1)-[:UA_FLIGHT]->(flight1)")
                    .append("CREATE (dst2)-[:UA_FLIGHT]->(flight2)")
                    .append("CREATE (flight1)-[:UA_FLIGHT]->(ewr_20150901)")
                    .append("CREATE (flight2)-[:UA_FLIGHT]->(lga_20150901)")

                    .toString();

    public static HashMap<String, Object> QUERY_MAP = new HashMap<String, Object>(){{
        put("from", new ArrayList<String>() {{ add("IAH"); add("HOU");} });
        put("to", new ArrayList<String>() {{  add("EWR"); add("LGA");} });
        put("day", 1441065600);
    }};

    static HashMap<String, Object> FLIGHT1_MAP = new HashMap<String, Object>(){{
        put("departs", 1441119600);
        put("code","UA-1");
        put("arrives", 1441126800);
        put("distance", 718);
    }};

    static HashMap<String, Object> FLIGHT2_MAP = new HashMap<String, Object>(){{
        put("departs", 1441123200);
        put("code","UA-2");
        put("arrives", 1441130400);
        put("distance", 732);
    }};

    static ArrayList<HashMap> FLIGHT_LIST1 = new ArrayList<HashMap>(){{
        add(FLIGHT1_MAP);
    }};

    static ArrayList<HashMap> FLIGHT_LIST2 = new ArrayList<HashMap>(){{
        add(FLIGHT2_MAP);
    }};

    static HashMap<String, Object> ANSWER_MAP = new HashMap<String, Object>(){{
        put("flights", FLIGHT_LIST1);
        put("score", 1.0);
        put("distance", 718);
    }};
    static HashMap<String, Object> ANSWER_MAP2 = new HashMap<String, Object>(){{
        put("flights", FLIGHT_LIST2);
        put("score", 1.0);
        put("distance", 732);
    }};

    public static ArrayList<HashMap> EXPECTED = new ArrayList<HashMap>(){{
        add(ANSWER_MAP);
        add(ANSWER_MAP2);
    }};
}
