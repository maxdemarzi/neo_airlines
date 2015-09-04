package com.maxdemarzi.Expanders;

import com.maxdemarzi.Helpers.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class DirectExpander extends BaseExpander {

    public DirectExpander(ArrayList<String> destinations, RelationshipType[] orderedFlightTypes, long stopTime, ArrayList<String> exclusions) {
        super(destinations, orderedFlightTypes, stopTime, exclusions);
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState state) {
        if (System.currentTimeMillis() < stopTime) {
            switch (path.length()) {
                case 0:
                    return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_DESTINATION);
                case 1: {
                    Node lastNode = path.endNode();
                    if (destinations.contains(((String) lastNode.getProperty("code"))) ||
                            exclusions.contains((String) lastNode.getProperty("code"))) {
                        return Collections.emptyList();
                    } else {
                        return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                    }
                }
                case 2: {
                    return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                }
                case 3: {
                    return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_DESTINATION);
                }
                case 4: {
                    Node lastNode = path.endNode();
                    if (destinations.contains(((String) lastNode.getProperty("code")))) {
                        return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                    } else {
                        return Collections.emptyList();
                    }
                }
                case 5: {
                    Node lastNode = path.endNode();
                    Iterator<Node> nodes = path.nodes().iterator();
                    nodes.next();
                    nodes.next();
                    Node lastFlight = nodes.next();
                    if (lastNode.getProperty("code").equals(lastFlight.getProperty("code"))) {
                        return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                    } else {
                        return Collections.emptyList();
                    }
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
