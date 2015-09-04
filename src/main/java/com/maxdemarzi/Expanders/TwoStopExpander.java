package com.maxdemarzi.Expanders;

import com.maxdemarzi.Helpers.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class TwoStopExpander extends BaseExpander {
    public TwoStopExpander(ArrayList<String> destinations, RelationshipType[] orderedFlightTypes, long stopTime, ArrayList<String> exclusions) {
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
                        return Collections.emptyList();
                    } else {
                        return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                    }
                }
                case 2: {
                    return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                }
                case 3:
                case 6: {
                    return path.endNode().getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_DESTINATION);
                }
                // Skip One Stop Routes
                case 4: {
                    Node lastNode = path.endNode();
                    if (destinations.contains(((String) lastNode.getProperty("code")))) {
                        Collections.emptyList();
                    } else {
                        return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                    }
                }
                // Accept Two Stop Routes
                case 7 : {
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

                    if (((Long) lastFlight.getProperty("arrives") + minimumConnectTime) > (Long) lastNode.getProperty("departs")) {
                        return Collections.emptyList();
                    }

                    if (lastNode.getProperty("code").equals(lastFlight.getProperty("code"))) {
                        return Collections.emptyList();
                    } else {
                        return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
                    }
                }
                case 8: {
                    Node lastNode = path.endNode();
                    Iterator<Node> nodes = path.nodes().iterator();
                    nodes.next();
                    nodes.next();
                    nodes.next();
                    nodes.next();
                    nodes.next();
                    Node lastFlight = nodes.next();

                    if (((Long) lastFlight.getProperty("arrives") + minimumConnectTime) > (Long) lastNode.getProperty("departs")) {
                        return Collections.emptyList();
                    } else {
                        return path.endNode().getRelationships(Direction.OUTGOING, relationshipTypes);
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
