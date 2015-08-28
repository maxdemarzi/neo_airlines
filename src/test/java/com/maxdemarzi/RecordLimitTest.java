package com.maxdemarzi;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Fixtures.AirlineResourceFixtures.AIRLINES_STATEMENT;
import static org.junit.Assert.assertArrayEquals;

public class RecordLimitTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(AIRLINES_STATEMENT)
            .withFixture(MODEL_STATEMENT)
            .withExtension("/v1", Service.class);

    @Test
    public void shouldReturnZeroRecords() {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/service/query").toString(),
                QUERY_MAP0);
        ArrayList actual = response.content();
        assertArrayEquals(new String[0], actual.toArray());
    }

    @Test
    public void shouldReturnOneRecords() {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/service/query").toString(),
                QUERY_MAP1);
        ArrayList actual = response.content();
        assertArrayEquals(EXPECTED1.toArray(), actual.toArray());
    }

    @Test
    public void shouldReturnDefaultRecords() {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/service/query").toString(),
                QUERY_MAP2);
        ArrayList actual = response.content();
        assertArrayEquals(EXPECTED_DEFAULT.toArray(), actual.toArray());
    }

    public static final String MODEL_STATEMENT =
            new StringBuilder()
                    .append("CREATE (iah_20150901:AirportDay {key:'IAH-1441065600'})")
                    .append("CREATE (ord_20150901:AirportDay {key:'ORD-1441065600'})")
                    .append("CREATE (dst1:Destination {code:'ORD'})")
                    .append("CREATE (flight1:Flight {code:'UA-1', departs:1441108800, arrives:1441119600, distance:926})")
                    .append("CREATE (flight2:Flight {code:'UA-2', departs:1441108800, arrives:1441119600, distance:926})")
                    .append("CREATE (flight3:Flight {code:'UA-3', departs:1441108800, arrives:1441119600, distance:926})")
                    .append("CREATE (iah_20150901)-[:HAS_DESTINATION]->(dst1)")
                    .append("CREATE (dst1)-[:UA_FLIGHT]->(flight1)")
                    .append("CREATE (dst1)-[:UA_FLIGHT]->(flight2)")
                    .append("CREATE (dst1)-[:UA_FLIGHT]->(flight3)")
                    .append("CREATE (flight1)-[:UA_FLIGHT]->(ord_20150901)")
                    .append("CREATE (flight2)-[:UA_FLIGHT]->(ord_20150901)")
                    .append("CREATE (flight3)-[:UA_FLIGHT]->(ord_20150901)")
                    .toString();

    public static HashMap<String, Object> QUERY_MAP0 = new HashMap<String, Object>(){{
        put("from", new ArrayList<String>() {{ add("IAH");} });
        put("to", new ArrayList<String>() {{  add("ORD");} });
        put("day", 1441065600);
        put("record_limit", 0);
    }};

    public static HashMap<String, Object> QUERY_MAP1 = new HashMap<String, Object>(){{
        put("from", new ArrayList<String>() {{ add("IAH");} });
        put("to", new ArrayList<String>() {{  add("ORD");} });
        put("day", 1441065600);
        put("record_limit", 1);
    }};

    public static HashMap<String, Object> QUERY_MAP2 = new HashMap<String, Object>(){{
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

    static HashMap<String, Object> FLIGHT2_MAP = new HashMap<String, Object>(){{
        put("departs", 1441108800);
        put("code","UA-2");
        put("arrives", 1441119600);
        put("distance", 926);
    }};

    static HashMap<String, Object> FLIGHT3_MAP = new HashMap<String, Object>(){{
        put("departs", 1441108800);
        put("code","UA-3");
        put("arrives", 1441119600);
        put("distance", 926);
    }};

    static ArrayList<HashMap> FLIGHT_LIST1 = new ArrayList<HashMap>(){{
        add(FLIGHT1_MAP);
    }};

    static ArrayList<HashMap> FLIGHT_LIST2 = new ArrayList<HashMap>(){{
        add(FLIGHT2_MAP);
    }};

    static ArrayList<HashMap> FLIGHT_LIST3 = new ArrayList<HashMap>(){{
        add(FLIGHT3_MAP);
    }};

    static HashMap<String, Object> ANSWER_MAP1 = new HashMap<String, Object>(){{
        put("flights", FLIGHT_LIST1);
        put("score", 1.0);
        put("distance", 926);
    }};

    static HashMap<String, Object> ANSWER_MAP2 = new HashMap<String, Object>(){{
        put("flights", FLIGHT_LIST2);
        put("score", 1.0);
        put("distance", 926);
    }};

    static HashMap<String, Object> ANSWER_MAP3 = new HashMap<String, Object>(){{
        put("flights", FLIGHT_LIST3);
        put("score", 1.0);
        put("distance", 926);
    }};

    public static ArrayList<HashMap> EXPECTED1 = new ArrayList<HashMap>(){{
        add(ANSWER_MAP3);
    }};

    public static ArrayList<HashMap> EXPECTED_DEFAULT = new ArrayList<HashMap>(){{
        add(ANSWER_MAP1);
        add(ANSWER_MAP2);
        add(ANSWER_MAP3);
    }};
}
