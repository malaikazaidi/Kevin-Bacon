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

public class hasRelationship implements HttpHandler {
	private String actorId;
	private String movieId;
	private Driver driver;
	private JSONObject response = new JSONObject();
	
	public hasRelationship(Driver driver) {
		this.driver = driver;
	}
	
	@Override
	public void handle(HttpExchange r) throws IOException {
		try {
			if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            } else {
            	r.sendResponseHeaders(400, 0);
            	OutputStream os = r.getResponseBody();
        		os.close();
            }
        } catch (Exception e) {
        	r.sendResponseHeaders(500, 0);
        	OutputStream os = r.getResponseBody();
    		os.close();
        }
		
	}
	
	public void handleGet(HttpExchange r) throws IOException, JSONException {
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("actorId") && deserialized.has("movieId") && deserialized.length() == 2) {
        	this.actorId = deserialized.getString("actorId");
        	this.movieId = deserialized.getString("movieId");
        } else {
        	r.sendResponseHeaders(400, 0);
        	OutputStream os = r.getResponseBody();
    		os.close();
        }
        
        try (Session session = driver.session()){
        	String matchMovie = String.format("MATCH (m:movie {id: \"%s\"}) RETURN m", this.movieId);
        	String matchActor = String.format("MATCH (a:actor {id: \"%s\"}) RETURN a", this.actorId);
        	StatementResult result1 = session.run(matchMovie);
        	StatementResult result2 = session.run(matchActor);
        	
        	if (result1.hasNext() == false || result2.hasNext() == false) {
        		r.sendResponseHeaders(404, 0);
        		OutputStream os = r.getResponseBody();
        		os.close();
        	} else {
        		String match = String.format("MATCH (m:movie), (a:actor) WHERE m.id = \"%s\" AND a.id = \"%s\" RETURN EXISTS((a)--(m))", this.movieId, this.actorId);
    			StatementResult result = session.run(match);
    			//check whether there is an existing record of movie before adding
    			Record rec = result.single();
    			String s;
    			if(rec.toString().contains("FALSE")) {
    				this.response.put("actorId", this.actorId);
    			    this.response.put("movieId", this.movieId);
    			    this.response.put("hasRelationship", false);
    			} else {
    				this.response.put("actorId", this.actorId);
    			    this.response.put("movieId", this.movieId);
    			    this.response.put("hasRelationship", true);
    			}
    	
    			OutputStream os = r.getResponseBody();
        		r.sendResponseHeaders(200, response.toString().getBytes().length);
        		os.write(response.toString().getBytes());
        		os.close();
        	}
			
		}
		catch(Exception e) {
			r.sendResponseHeaders(500, 0);
			OutputStream os = r.getResponseBody();
    		os.close();
		}
	}
}
