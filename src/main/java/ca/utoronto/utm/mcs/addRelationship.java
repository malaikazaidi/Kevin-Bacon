package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class addRelationship implements HttpHandler {

	private String actorId;
	private String movieId;
	private Driver driver;


	public addRelationship(Driver driver) {
		this.driver = driver;
	}

	@Override
	public void handle(HttpExchange r) throws IOException {
		try {
			if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            } else {
            	r.sendResponseHeaders(400, 0);
            }
        } catch (Exception e) {
        	r.sendResponseHeaders(500, 0);
        }
		
	}

	public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("actorId") && deserialized.has("movieId") && deserialized.length() == 2) {
        	this.actorId = deserialized.getString("actorId");
        	this.movieId = deserialized.getString("movieId");
        } else {
        	r.sendResponseHeaders(400, 0);
        }

	
		try (Session session = driver.session()){
			String match = String.format("MATCH (m:movie), (a:Actor) WHERE m.movieId = \"%s\" AND a.actorId = \"%s\" RETURN EXISTS((m)-[r:ActedIn]->(a))", this.movieId, this.actorId);
			StatementResult result = session.run(match);
			
			Record rec = result.single();
			if(rec.toString().contains("FALSE")) {
				String create = String.format("MATCH (m:movie), (a:Actor) WHERE m.movieId = \"%s\" AND a.actorId = \"%s\" CREATE (m)-[r:ActedIn]->(a) RETURN r ", this.movieId, this.actorId);
				StatementResult res = session.run(create);
				r.sendResponseHeaders(200, 0);
				
			} else {
				r.sendResponseHeaders(404, 0);
			}
		}
		catch(Exception e) {
			r.sendResponseHeaders(500, 0);
		}
	}
	
}