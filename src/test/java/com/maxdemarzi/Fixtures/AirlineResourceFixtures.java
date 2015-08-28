package com.maxdemarzi.Fixtures;

public class AirlineResourceFixtures {
    public static final String AIRLINES_STATEMENT=
            new StringBuilder()
                    .append("CREATE (airlines:Metadata {name:'Airlines'})")
                    .append("CREATE (airline1:Airline {name:'Neo4j Airlines', code:'NA'})")
                    .append("CREATE (airline2:Airline {name:'Unamerican Airlines', code:'UA'})")
                    .append("CREATE (airline3:Airline {name:'Divided Airlines', code:'DA'})")
                    .append("CREATE (airlines)<-[:IS_AIRLINE]-(airline1)")
                    .append("CREATE (airlines)<-[:IS_AIRLINE]-(airline2)")
                    .append("CREATE (airlines)<-[:IS_AIRLINE]-(airline3)")
                    .toString();
}
