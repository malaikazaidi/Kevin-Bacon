package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class addMovie implements HttpHandler {

	private String name;
	private String movieId;
	private Driver driver;


	public addMovie(Driver driver) {
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
            e.printStackTrace();
        }
		
	}

	public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("name") && deserialized.has("movieId")) {
        	name = deserialized.getString("name");
        	movieId = deserialized.getString("movieId");
        }

	
		try (Session session = driver.session()){
			String match = String.format("MATCH (m:movie {movieId: \"%s\"}) RETURN m", movieId);
			StatementResult result = session.run(match);
			
			//check whether there is an existing record of movie before adding
			
			if(result.hasNext() == false) {
				//create movie
				String create = String.format("CREATE (m:movie {name: \"%s\", movieId: \"%s\"})", name, movieId);
				StatementResult res = session.run(create);
				r.sendResponseHeaders(200, 0);
			} else {
				r.sendResponseHeaders(400, 0);
				return;
			}
		}
		catch(Exception e) {
			r.sendResponseHeaders(500, 0);
		}
	}
	
	public void handleGet(HttpExchange r) throws IOException, JSONException{
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if (deserialized.has("name") && deserialized.has("movieId")) {
        	this.name = deserialized.getString("name");
        	this.movieId = deserialized.getString("movieId");
        }
        
        try (Session session = driver.session()) {
        	String match = String.format("MATCH (m:movie {movieId: \"%s\"}) RETURN m", movieId);
        	StatementResult result = session.run(match);
        	//need to send 400 bad request somehow
        	
        	if (result.hasNext() == true) {
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