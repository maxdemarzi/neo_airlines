package com.maxdemarzi.Metadata;

import com.maxdemarzi.Helpers.Labels;
import com.maxdemarzi.Helpers.RelationshipTypes;
import com.maxdemarzi.Service;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Helpers.Validators.getValidAirlineInput;

@Path("/metadata")
public class Airlines {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GET
    @Path("/airlines")
    public Response getAirlines(@Context GraphDatabaseService db) throws IOException {
        ArrayList<HashMap> results = new ArrayList<>();

        try (Transaction tx = db.beginTx()){
            Node airlines = db.findNode(Labels.Metadata, "name", "Airlines");
            for(Relationship r: airlines.getRelationships(Direction.INCOMING, RelationshipTypes.IS_AIRLINE)){
                Node airline = r.getStartNode();
                HashMap<String, Object> airlineMap = new HashMap<>();
                airlineMap.put("name", airline.getProperty("name", ""));
                airlineMap.put("code", airline.getProperty("code", ""));
                results.add(airlineMap);
            }
        }

        return Response.ok().entity(objectMapper.writeValueAsString(results)).build();
    }

    @POST
    @Path("/airlines")
    public Response createOrUpdateCarrier(String body, @Context GraphDatabaseService db) throws IOException {
        HashMap input = getValidAirlineInput(body);
        boolean changed = false;

        try (Transaction tx = db.beginTx()) {
            Node airline = db.findNode(Labels.Airline, "code", input.get("code"));

            if (airline == null) {
                changed = true;
                airline = db.createNode(Labels.Airline);
                airline.setProperty("name", input.get("name"));
                airline.setProperty("code", input.get("code"));

                Node airlines = db.findNode(Labels.Metadata, "name", "Airlines");
                airline.createRelationshipTo(airlines, RelationshipTypes.IS_AIRLINE);
            } else {
                if (input.get("name") != airline.getProperty("name")){
                    airline.setProperty("name", input.get("name"));
                    changed = true;
                }
            }

            tx.success();
        }

        if(changed){
            Service.setFlightTypes(db);
        }

        return Response.status(Response.Status.CREATED).entity(objectMapper.writeValueAsString(input)).build();

    }
}
