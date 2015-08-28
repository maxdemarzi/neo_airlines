package com.maxdemarzi.Metadata;

import com.maxdemarzi.Service;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Fixtures.AirlineResourceFixtures.AIRLINES_STATEMENT;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AirlinesTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule()
            .withFixture(AIRLINES_STATEMENT)
            .withExtension("/v1", Service.class);

    @Test
    public void shouldGetAirlines() {
        HTTP.Response response = HTTP.GET(neo4j.httpURI().resolve("/v1/metadata/airlines").toString());
        ArrayList actual = response.content();
        assertArrayEquals(EXPECTED.toArray(), actual.toArray());
    }

    @Test
    public void shouldCreateAirline() throws IOException {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/v1/metadata/airlines").toString(), HARD_JET_AIRLINES_MAP);
        assertEquals(201, response.status());
        HashMap actual = response.content();
        assertEquals(HARD_JET_AIRLINES_MAP, actual);
    }

    public static HashMap<String, Object> DIVIDED_AIRLINES_MAP = new HashMap<String, Object>(){{
        put("name","Divided Airlines");
        put("code","DA");
    }};

    public static HashMap<String, Object> UNAMERICAN_AIRLINES_MAP = new HashMap<String, Object>(){{
        put("name","Unamerican Airlines");
        put("code","UA");
    }};

    public static HashMap<String, Object> NEO4J_AIRLINES_MAP = new HashMap<String, Object>(){{
        put("name", "Neo4j Airlines");
        put("code", "NA");
    }};

    public static HashMap<String, Object> HARD_JET_AIRLINES_MAP = new HashMap<String, Object>(){{
        put("name", "HardJet Airlines");
        put("code", "HJ");
    }};
    public static ArrayList<HashMap> EXPECTED = new ArrayList<HashMap>(){{
        add(DIVIDED_AIRLINES_MAP);
        add(UNAMERICAN_AIRLINES_MAP);
        add(NEO4J_AIRLINES_MAP);
    }};
}
