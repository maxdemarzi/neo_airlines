package com.maxdemarzi;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Path("/sample")
public class Sample {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/createtestdata")
    public Response createTestData(@Context GraphDatabaseService db) throws IOException {
        final String AIRLINES_STATEMENT=
                new StringBuilder()
                        .append("CREATE (airlines:Metadata {name:'Airlines'})")
                        .append("CREATE (airline1:Airline {name:'Neo4j Airlines', code:'NA'})")
                        .append("CREATE (airline2:Airline {name:'Unamerican Airlines', code:'UA'})")
                        .append("CREATE (airline3:Airline {name:'Divided Airlines', code:'DA'})")
                        .append("CREATE (airlines)<-[:IS_AIRLINE]-(airline1)")
                        .append("CREATE (airlines)<-[:IS_AIRLINE]-(airline2)")
                        .append("CREATE (airlines)<-[:IS_AIRLINE]-(airline3)")
                        .toString();

        final String MODEL_STATEMENT =
                new StringBuilder()
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

        try (Transaction tx = db.beginTx()) {
            db.execute(AIRLINES_STATEMENT);
            db.execute(MODEL_STATEMENT);
            tx.success();
        }
        Map<String, String> results = new HashMap<String,String>(){{
            put("testdata","created");
        }};
        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    }
