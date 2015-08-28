package com.maxdemarzi.Helpers;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.maxdemarzi.Service.DEFAULT_RECORD_LIMIT;
import static com.maxdemarzi.Service.DEFAULT_TIME_LIMIT;

public class Validators {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static HashMap getValidAirlineInput(String body) throws IOException {
        HashMap input;

        // Parse the input
        try {
            input = objectMapper.readValue(body, HashMap.class);
        } catch (Exceptions e) {
            throw Exceptions.invalidInput;
        }
        // Make sure it has a name parameter
        if (!input.containsKey("name")) {
            throw Exceptions.missingNameParameter;
        }
        // Make sure it has a code parameter
        if (!input.containsKey("code")) {
            throw Exceptions.missingCodeParameter;
        }

        // Make sure the name is not blank
        if (input.get("name") == "") {
            throw Exceptions.invalidNameParameter;
        }
        // Make sure the code is not blank
        if (input.get("code") == "") {
            throw Exceptions.invalidCodeParameter;
        }

        return input;
    }

    public static HashMap getValidQueryInput(String body) throws IOException {
        HashMap input;

        // Parse the input
        try {
            input = objectMapper.readValue(body, HashMap.class);
        } catch (Exceptions e) {
            throw Exceptions.invalidInput;
        }

        if (!input.containsKey("from")) {
            throw Exceptions.missingFromParameter;
        }
        if (!input.containsKey("to")) {
            throw Exceptions.missingToParameter;
        }
        if (!input.containsKey("day")) {
            throw Exceptions.missingDayParameter;
        }
        if(!input.containsKey("airlines")) {
            input.put("airlines", new ArrayList<String>());
        }
        if (!input.containsKey("record_limit")) {
            input.put("record_limit", DEFAULT_RECORD_LIMIT);
        }
        if (!input.containsKey("time_limit")) {
            input.put("time_limit", DEFAULT_TIME_LIMIT);
        } else {
            // Avoid Integer vs Long nonsense
            input.put("time_limit", ((Number)input.get("time_limit")).longValue());
        }
        if(!input.containsKey("exclusions")) {
            input.put("exclusions", new ArrayList<String>());
        }

        if (input.get("from") == "") {
            throw Exceptions.invalidFromParameter;
        }
        if (input.get("to") == "") {
            throw Exceptions.invalidToParameter;
        }
        if (input.get("day") == "") {
            throw Exceptions.invalidDayParameter;
        }

        return input;
    }
}
