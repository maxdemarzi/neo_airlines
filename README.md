# neo_airlines
Modeling and Querying Flight Data

# Instructions

1. Build it:

        mvn clean package

2. Copy target/airlines-1.0.jar to the plugins/ directory of your Neo4j server.

3. Download and copy additional jar to the plugins/ directory of your Neo4j server. 

        curl -O http://central.maven.org/maven2/org/apache/commons/commons-collections4/4.0/commons-collections4-4.0.jar

4. Configure Neo4j by adding a line to conf/neo4j-server.properties:

        dbms.unmanaged_extension_classes=com.maxdemarzi=/v1

        
5. Start Neo4j server.

6. Check that it is installed correctly over HTTP:

        :GET /v1/service/helloworld

7. Run the migration:
        
        :GET /v1/service/migrate

8. Create test data:
        
        :GET /v1/sample/createtestdata
        
9. Query the database:
        
        :POST /v1/service/query {"from":["DFW"], "to":["IAH"], "day":1441065600}
        :POST /v1/service/query {"from":["DFW"], "to":["ORD"], "day":1441065600}
        :POST /v1/service/query {"from":["DFW", "IAH"], "to":["ORD"], "day":1441065600}
        :POST /v1/service/query {"from":["EWR"], "to":["HND"], "day":1441065600}
        :POST /v1/service/query {"from":["IAH"], "to":["HND"], "day":1441065600}        
        :POST /v1/service/query {"from":["DFW"], "to":["HND"], "day":1441065600}        
