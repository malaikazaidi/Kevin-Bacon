package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

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
            } else if (r.getRequestMethod().equals("GET")) {
            	handleGet(r);
            }
        } catch (Exception e) {
        	r.sendResponseHeaders(400, 0);
        }
		
	}

	public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("actorId") && deserialized.has("movieId")) {
        	this.actorId = deserialized.getString("actorId");
        	this.movieId = deserialized.getString("movieId");
        } else {
        	r.sendResponseHeaders(400, 0);
        }

	
		try (Session session = driver.session()){
			String match = String.format("MATCH (m:movie), (a:Actor) WHERE m.movieId = \"%s\" AND a.actorId = \"%s\" RETURN EXISTS((m)-[:hasRelationship]->(a))", this.movieId, this.actorId);
			StatementResult result = session.run(match);
			//check whether there is an existing record of movie before adding
			Record rec = result.single();
			if(rec.toString().contains("FALSE")) {
				//create movie
				String create = String.format("MATCH (m:movie), (a:Actor) WHERE m.movieId = \"%s\" AND a.actorId = \"%s\" CREATE (m)-[r:hasRelationship]->(a) RETURN r ", this.movieId, this.actorId);
				StatementResult res = session.run(create);
				r.sendResponseHeaders(200, 0);
			} else {
				r.sendResponseHeaders(404, 0);
				return;
			}
		}
		catch(Exception e) {
			r.sendResponseHeaders(500, 0);
		}
	}
	
	public void handleGet(HttpExchange r) throws IOException, JSONException {
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("actorId") && deserialized.has("movieId")) {
        	this.actorId = deserialized.getString("actorId");
        	this.movieId = deserialized.getString("movieId");
        } else {
        	r.sendResponseHeaders(400, 0);
        }
        
        try (Session session = driver.session()){
        	String matchMovie = String.format("MATCH (m:movie {movieId: \"%s\"}) RETURN m", this.movieId);
        	String matchActor = String.format("MATCH (a:Actor {actorId: \"%s\"}) RETURN a", this.actorId);
        	StatementResult result1 = session.run(matchMovie);
        	StatementResult result2 = session.run(matchActor);
        	
        	if (result1.hasNext() == false || result2.hasNext() == false) {
        		r.sendResponseHeaders(404, 0);
        		return;
        	} else {
        		String match = String.format("MATCH (m:movie), (a:Actor) WHERE m.movieId = \"%s\" AND a.actorId = \"%s\" RETURN EXISTS((m)-[:hasRelationship]->(a))", this.movieId, this.actorId);
    			StatementResult result = session.run(match);
    			//check whether there is an existing record of movie before adding
    			Record rec = result.single();
    			String s;
    			if(rec.toString().contains("FALSE")) {
    				s = String.format("{actorId: \"%s\", movieId: \"%s\", hasRelationship: false}", this.actorId, this.movieId);
    			} else {
    				s = String.format("{actorId: \"%s\", movieId: \"%s\", hasRelationship: true}", this.actorId, this.movieId);
    			}
    			OutputStream os = r.getResponseBody();
        		r.sendResponseHeaders(200, s.getBytes().length);
        		os.write(s.getBytes());
        		os.close();
        	}
			
		}
		catch(Exception e) {
			r.sendResponseHeaders(500, 0);
		}
	}
}