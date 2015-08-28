package com.maxdemarzi.Helpers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Exceptions  extends WebApplicationException {

    public Exceptions(int code, String error)  {
        super(new Throwable(error), Response.status(code)
                .entity("{\"error\":\"" + error + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build());

    }

    public static Exceptions invalidInput = new Exceptions(400, "Invalid Input");

    public static Exceptions missingNameParameter = new Exceptions(400, "Missing name Parameter.");
    public static Exceptions invalidNameParameter = new Exceptions(400, "Invalid name Parameter.");

    public static Exceptions missingCodeParameter = new Exceptions(400, "Missing code Parameter.");
    public static Exceptions invalidCodeParameter = new Exceptions(400, "Invalid code Parameter.");

    public static Exceptions missingPriorityParameter = new Exceptions(400, "Missing priority Parameter.");
    public static Exceptions invalidPriorityParameter = new Exceptions(400, "Invalid priority Parameter.");

    public static Exceptions missingFromParameter = new Exceptions(400, "Missing from Parameter.");
    public static Exceptions invalidFromParameter = new Exceptions(400, "Invalid from Parameter.");

    public static Exceptions missingToParameter = new Exceptions(400, "Missing to Parameter.");
    public static Exceptions invalidToParameter = new Exceptions(400, "Invalid to Parameter.");

    public static Exceptions missingDayParameter = new Exceptions(400, "Missing day Parameter.");
    public static Exceptions invalidDayParameter = new Exceptions(400, "Invalid day Parameter.");

}
