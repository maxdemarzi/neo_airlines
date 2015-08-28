package com.maxdemarzi;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Fixtures.AirlineResourceFixtures.AIRLINES_STATEMENT;
import static org.junit.Assert.assertArrayEquals;

public class TwoStopTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(AIRLINES_STATEMENT)
            .withFixture(MODEL_STATEMENT)
            .withExtension("/v1", Service.class);

    @Test
    public void shouldFindTwoStopRoute() {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/service/query").toString(),
                QUERY_MAP);
        ArrayList actual = response.content();
        assertArrayEquals(EXPECTED.toArray(), actual.toArray());
    }

    public static final String MODEL_STATEMENT =
            new StringBuilder()
                    // Fly from Dallas to Haneda Airport in Tokyo
                    .append("CREATE (dfw_20150901:AirportDay {key:'DFW-1441065600'})")
                    .append("CREATE (iah_20150901:AirportDay {key:'IAH-1441065600'})")
                    .append("CREATE (ord_20150901:AirportDay {key:'ORD-1441065600'})")
                    .append("CREATE (ewr_20150901:AirportDay {key:'EWR-1441065600'})")
                    .append("CREATE (hnd_20150902:AirportDay {key:'HND-1441152000'})")

                    .append("CREATE (dst0:Destination {code:'IAH'})")
                    .append("CREATE (dst1:Destination {code:'ORD'})")
                    .append("CREATE (dst2:Destination {code:'EWR'})")
                    .append("CREATE (dst3:Destination {code:'HND'})")
                    .append("CREATE (dst4:Destination {code:'HND'})")

                    .append("CREATE (flight0:Flight {code:'UA-0', departs:1441101600, arrives:1441105200, distance:225})")
                    .append("CREATE (flight1:Flight {code:'UA-1', departs:1441108800, arrives:1441119600, distance:718})")
                    .append("CREATE (flight2:Flight {code:'UA-2', departs:1441108800, arrives:1441123200, distance:1416})")
                    .append("CREATE (flight3:Flight {code:'UA-3', departs:1441123200, arrives:1441177200, distance:6296})")
                    .append("CREATE (flight4:Flight {code:'UA-4', departs:1441130400, arrives:1441180800, distance:6731})")

                    .append("CREATE (dfw_20150901)-[:HAS_DESTINATION]->(dst0)")
                    .append("CREATE (iah_20150901)-[:HAS_DESTINATION]->(dst1)")
                    .append("CREATE (iah_20150901)-[:HAS_DESTINATION]->(dst2)")
                    .append("CREATE (ord_20150901)-[:HAS_DESTINATION]->(dst3)")
                    .append("CREATE (ewr_20150901)-[:HAS_DESTINATION]->(dst4)")

                    .append("CREATE (dst0)-[:UA_FLIGHT]->(flight0)")
                    .append("CREATE (dst1)-[:UA_FLIGHT]->(flight1)")
                    .append("CREATE (dst2)-[:UA_FLIGHT]->(flight2)")
                    .append("CREATE (dst3)-[:UA_FLIGHT]->(flight3)")
                    .append("CREATE (dst4)-[:UA_FLIGHT]->(flight4)")

                    .append("CREATE (flight0)-[:UA_FLIGHT]->(iah_20150901)")
                    .append("CREATE (flight1)-[:UA_FLIGHT]->(ord_20150901)")
                    .append("CREATE (flight2)-[:UA_FLIGHT]->(ewr_20150901)")
                    .append("CREATE (flight3)-[:UA_FLIGHT]->(hnd_20150902)")
                    .append("CREATE (flight4)-[:UA_FLIGHT]->(hnd_20150902)")

                    .toString();

    public static HashMap<String, Object> QUERY_MAP = new HashMap<String, Object>(){{
        put("from", new ArrayList<String>() {{ add("DFW"); } });
        put("to", new ArrayList<String>() {{  add("HND"); } });
        put("day", 1441065600);
    }};

    static HashMap<String, Object> LEG0_MAP = new HashMap<String, Object>(){{
        put("departs", 1441101600);
        put("code","UA-0");
        put("arrives", 1441105200);
        put("distance", 225);
    }};

    static HashMap<String, Object> LEG1_MAP = new HashMap<String, Object>(){{
        put("departs", 1441108800);
        put("code","UA-1");
        put("arrives", 1441119600);
        put("distance", 718);
    }};

    static HashMap<String, Object> LEG2_MAP = new HashMap<String, Object>(){{
        put("departs", 1441123200);
        put("code","UA-3");
        put("arrives", 1441177200);
        put("distance", 6296);
    }};

    static ArrayList<HashMap> FLIGHT_LIST1 = new ArrayList<HashMap>(){{
        add(LEG0_MAP);
        add(LEG1_MAP);
        add(LEG2_MAP);
    }};

    static HashMap<String, Object> ANSWER_MAP1 = new HashMap<String, Object>(){{
        put("flights", FLIGHT_LIST1);
        put("score", 3.0);
        put("distance", 7239);
    }};

    static HashMap<String, Object> LEG3_MAP = new HashMap<String, Object>(){{
        put("departs", 1441108800);
        put("code","UA-2");
        put("arrives", 1441123200);
        put("distance", 1416);
    }};

    static HashMap<String, Object> LEG4_MAP = new HashMap<String, Object>(){{
        put("departs", 1441130400);
        put("code","UA-4");
        put("arrives", 1441180800);
        put("distance", 6731);
    }};

    static ArrayList<HashMap> FLIGHT_LIST2 = new ArrayList<HashMap>(){{
        add(LEG0_MAP);
        add(LEG3_MAP);
        add(LEG4_MAP);
    }};

    static HashMap<String, Object> ANSWER_MAP2 = new HashMap<String, Object>(){{
        put("flights", FLIGHT_LIST2);
        put("score", 3.0);
        put("distance", 8372);
    }};

    public static ArrayList<HashMap> EXPECTED = new ArrayList<HashMap>(){{
        add(ANSWER_MAP1);
        add(ANSWER_MAP2);
    }};
}
