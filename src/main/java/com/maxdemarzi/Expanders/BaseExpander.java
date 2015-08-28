package com.maxdemarzi.Expanders;

import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.RelationshipType;

import java.util.ArrayList;

public abstract class BaseExpander implements PathExpander {
    protected ArrayList<String> destinations;
    protected RelationshipType[] relationshipTypes;
    protected ArrayList<String> exclusions;
    protected long stopTime;

    public BaseExpander(){
        this.destinations = new ArrayList<>();
        this.relationshipTypes = new RelationshipType[0];
        this.stopTime = System.currentTimeMillis();
        this.exclusions = new ArrayList<>();
    }

    public BaseExpander(ArrayList<String> destinations, RelationshipType[] relationshipTypes, long stopTime) {
        this.destinations = destinations;
        this.relationshipTypes = relationshipTypes;
        this.stopTime = System.currentTimeMillis() + stopTime;
        this.exclusions = new ArrayList<>();
    }

    public BaseExpander(ArrayList<String> destinations, RelationshipType[] relationshipTypes, long stopTime, ArrayList<String> exclusions) {
        this.destinations = destinations;
        this.relationshipTypes = relationshipTypes;
        this.stopTime = System.currentTimeMillis() + stopTime;
        this.exclusions = exclusions;
    }

}