package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;




public class App 
{
    static int PORT = 8080;
    static Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j","1234"));

    
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);

        server.createContext("/api/v1/addActor", new addActor(driver));
        server.createContext("/api/v1/getActor", new getActor(driver));
        server.createContext("/api/v1/computeBaconNumber", new computeBaconNumber(driver));
        server.createContext("/api/v1/computeBaconPath", new computeBaconPath(driver));
        
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
