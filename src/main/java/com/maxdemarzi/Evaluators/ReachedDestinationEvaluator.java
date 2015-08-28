package com.maxdemarzi.Evaluators;

import com.maxdemarzi.Helpers.Labels;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

import java.util.ArrayList;

public class ReachedDestinationEvaluator implements Evaluator {
    private ArrayList<String> destinations;

    public ReachedDestinationEvaluator(ArrayList<String> destinations) {
        this.destinations = destinations;
    }

    @Override
    public Evaluation evaluate(Path path) {
        Node lastNode = path.endNode();
        if (!lastNode.hasLabel(Labels.AirportDay)){
            return Evaluation.EXCLUDE_AND_CONTINUE;
        } else if (destinations.contains( ((String)lastNode.getProperty("key")).substring(0,3) )) {
            return Evaluation.INCLUDE_AND_PRUNE;
        }
        return Evaluation.EXCLUDE_AND_CONTINUE;
    }
}