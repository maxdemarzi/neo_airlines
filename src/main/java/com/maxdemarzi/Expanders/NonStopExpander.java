package com.maxdemarzi.Expanders;

import com.maxdemarzi.Helpers.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.ArrayList;
import java.util.Collections;

public class NonStopExpander extends BaseExpander {
    public NonStopExpander(ArrayList<String> destinations,  RelationshipType[] relationshipTypes, long stopTime) {
        super(destinations, relationshipTypes, stopTime);
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState state) {
        if (System.currentTimeMillis() < stopTime) {
            switch (path.length()) {
                case 0:
                    return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_DESTINATION);
                case 1: {
                    Node lastNode = path.endNode();
                    if (destinations.contains((String) lastNode.getProperty("code"))) {
                        return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                    } else {
                        return Collections.emptyList();
                    }
                }
                 case 2:
                 case 3: {
                     return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                 }
                default:
                    return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public PathExpander reverse() {
        return null;
    }
}
