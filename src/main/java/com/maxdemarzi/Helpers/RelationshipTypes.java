package com.maxdemarzi.Helpers;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    DOES_NOT_EXIST,
    HAS_DESTINATION,
    IS_AIRLINE
}
