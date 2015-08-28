package com.maxdemarzi.Expanders;

import com.maxdemarzi.Helpers.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.ArrayList;
import java.util.Iterator;

public class OneStopExpander extends BaseExpander {
    public OneStopExpander(ArrayList<String> destinations, RelationshipType[] orderedFlightTypes, long stopTime, ArrayList<String> exclusions) {
        super(destinations, orderedFlightTypes, stopTime, exclusions);
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState branchState) {
        if (System.currentTimeMillis() < stopTime) {

            Long minimumConnectTime = 45L * 60L; // 45 minutes

            switch (path.length()) {
                case 0:
                    return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_DESTINATION);
                case 1: {
                    Node lastNode = path.endNode();
                    if (destinations.contains(((String) lastNode.getProperty("code"))) ||
                            exclusions.contains((String) lastNode.getProperty("code"))) {
                        return path.endNode().getRelationships(RelationshipTypes.DOES_NOT_EXIST);
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
                        return path.endNode().getRelationships(RelationshipTypes.DOES_NOT_EXIST);
                    }
                }
                case 5: {
                    Node lastNode = path.endNode();
                    Iterator<Node> nodes = path.nodes().iterator();
                    nodes.next();
                    nodes.next();
                    Node lastFlight = nodes.next();

                    if (((Long) lastFlight.getProperty("arrives") + minimumConnectTime) > (Long) lastNode.getProperty("departs")) {
                        return path.endNode().getRelationships(RelationshipTypes.DOES_NOT_EXIST);
                    }

                    if (lastNode.getProperty("code").equals(lastFlight.getProperty("code"))) {
                        return path.endNode().getRelationships(RelationshipTypes.DOES_NOT_EXIST);
                    } else {
                        return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                    }
                }
                default:
                    return path.endNode().getRelationships(RelationshipTypes.DOES_NOT_EXIST);
            }
        } else {
            return path.endNode().getRelationships(RelationshipTypes.DOES_NOT_EXIST);
        }
    }

    @Override
    public PathExpander reverse() {
        return null;
    }
}
