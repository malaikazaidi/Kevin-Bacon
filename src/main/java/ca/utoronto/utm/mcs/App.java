package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import com.sun.net.httpserver.HttpServer;



public class App 
{
    static int PORT = 8080;
    Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j","1234"));
    Session session = driver.session();
    
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.start();
        System.out.printf("Server started on port %d...\n", PORT);
    }
}
