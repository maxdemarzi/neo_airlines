package com.maxdemarzi;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Fixtures.AirlineResourceFixtures.AIRLINES_STATEMENT;
import static org.junit.Assert.assertArrayEquals;

public class NonStopTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(AIRLINES_STATEMENT)
            .withFixture(MODEL_STATEMENT)
            .withExtension("/v1", Service.class);

    @Test
    public void shouldFindNonStopRoute() {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/service/query").toString(),
                QUERY_MAP);
        ArrayList actual = response.content();
        assertArrayEquals(EXPECTED.toArray(), actual.toArray());
    }

    public static final String MODEL_STATEMENT =
            new StringBuilder()
                    .append("CREATE (iah_20150901:AirportDay {key:'IAH-1441065600'})")
                    .append("CREATE (ord_20150901:AirportDay {key:'ORD-1441065600'})")
                    .append("CREATE (dst1:Destination {code:'ORD'})")
                    .append("CREATE (flight1:Flight {code:'UA-1', departs:1441108800, arrives:1441119600, distance:926})")
                    .append("CREATE (iah_20150901)-[:HAS_DESTINATION]->(dst1)")
                    .append("CREATE (dst1)-[:UA_FLIGHT]->(flight1)")
                    .append("CREATE (flight1)-[:UA_FLIGHT]->(ord_20150901)")
                    .toString();

    public static HashMap<String, Object> QUERY_MAP = new HashMap<String, Object>(){{
        put("from", new ArrayList<String>() {{ add("IAH");} });
        put("to", new ArrayList<String>() {{  add("ORD");} });
        put("day", 1441065600);
    }};

    static HashMap<String, Object> FLIGHT1_MAP = new HashMap<String, Object>(){{
        put("departs", 1441108800);
        put("code","UA-1");
        put("arrives", 1441119600);
        put("distance", 926);
    }};

    static ArrayList<HashMap> FLIGHT_LIST = new ArrayList<HashMap>(){{
        add(FLIGHT1_MAP);
    }};

    static HashMap<String, Object> ANSWER_MAP = new HashMap<String, Object>(){{
        put("flights", FLIGHT_LIST);
        put("score", 1.0);
        put("distance", 926);
    }};

    public static ArrayList<HashMap> EXPECTED = new ArrayList<HashMap>(){{
        add(ANSWER_MAP);
    }};
}
