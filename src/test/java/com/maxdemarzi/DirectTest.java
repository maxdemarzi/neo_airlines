package com.maxdemarzi;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Fixtures.AirlineResourceFixtures.AIRLINES_STATEMENT;
import static org.junit.Assert.assertArrayEquals;

public class DirectTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(AIRLINES_STATEMENT)
            .withFixture(MODEL_STATEMENT)
            .withExtension("/v1", Service.class);

    @Test
    public void shouldFindDirectRoute() {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/service/query").toString(),
                QUERY_MAP);
        ArrayList actual = response.content();
        assertArrayEquals(EXPECTED.toArray(), actual.toArray());
    }

    public static final String MODEL_STATEMENT =
            new StringBuilder()
                    .append("CREATE (iah_20150901:AirportDay {key:'IAH-1441065600'})")
                    .append("CREATE (ord_20150901:AirportDay {key:'ORD-1441065600'})")
                    .append("CREATE (ewr_20150901:AirportDay {key:'EWR-1441065600'})")

                    .append("CREATE (dst1:Destination {code:'ORD'})")
                    .append("CREATE (dst2:Destination {code:'EWR'})")

                    // Direct Flights have the same flight code, but make stops along the way
                    .append("CREATE (flight1:Flight {code:'UA-1', departs:1441108800, arrives:1441119600, distance:718})")
                    .append("CREATE (flight2:Flight {code:'UA-1', departs:1441123200, arrives:1441126800, distance:418})")

                    .append("CREATE (iah_20150901)-[:HAS_DESTINATION]->(dst1)")
                    .append("CREATE (ord_20150901)-[:HAS_DESTINATION]->(dst2)")

                    .append("CREATE (dst1)-[:UA_FLIGHT]->(flight1)")
                    .append("CREATE (dst2)-[:UA_FLIGHT]->(flight2)")

                    .append("CREATE (flight1)-[:UA_FLIGHT]->(ord_20150901)")
                    .append("CREATE (flight2)-[:UA_FLIGHT]->(ewr_20150901)")

                    .toString();

    public static HashMap<String, Object> QUERY_MAP = new HashMap<String, Object>(){{
        put("from", new ArrayList<String>() {{ add("IAH"); } });
        put("to", new ArrayList<String>() {{  add("EWR"); } });
        put("day", 1441065600);
    }};

    static HashMap<String, Object> LEG1_MAP = new HashMap<String, Object>(){{
        put("departs", 1441108800);
        put("code","UA-1");
        put("arrives", 1441119600);
        put("distance", 718);
    }};

    static HashMap<String, Object> LEG2_MAP = new HashMap<String, Object>(){{
        put("departs", 1441123200);
        put("code","UA-1");
        put("arrives", 1441126800);
        put("distance", 418);
    }};

    static ArrayList<HashMap> FLIGHT_LIST1 = new ArrayList<HashMap>(){{
        add(LEG1_MAP);
        add(LEG2_MAP);
    }};

    static HashMap<String, Object> ANSWER_MAP = new HashMap<String, Object>(){{
        put("flights", FLIGHT_LIST1);
        put("score", 2.0);
        put("distance", 1136);
    }};

    public static ArrayList<HashMap> EXPECTED = new ArrayList<HashMap>(){{
        add(ANSWER_MAP);
    }};
}
