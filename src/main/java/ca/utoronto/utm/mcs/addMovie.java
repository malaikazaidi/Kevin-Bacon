package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class addMovie implements HttpHandler {

	private String name;
	private String movieId;
	private Driver driver;
	private JSONObject response = new JSONObject();


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
        	r.sendResponseHeaders(400, 0);
        }
		
	}

	public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("name") && deserialized.has("movieId")) {
        	this.name = deserialized.getString("name");
        	this.movieId = deserialized.getString("movieId");
        } else {
        	r.sendResponseHeaders(400, 0);
        }

	
		try (Session session = driver.session()){
			String match = String.format("MATCH (m:movie {movieId: \"%s\"}) RETURN m", this.movieId);
			StatementResult result = session.run(match);
			
			//check whether there is an existing record of movie before adding
			
			if(result.hasNext() == false) {
				//create movie
				String create = String.format("CREATE (m:movie {name: \"%s\", movieId: \"%s\"})", this.name, this.movieId);
				StatementResult res = session.run(create);
				r.sendResponseHeaders(200, 0);
			} else {
				r.sendResponseHeaders(404, 0);
				return;
			}
		}
		catch(Exception e) {
			r.sendResponseHeaders(500, 0);
			return;
		}
	}
	
	public void handleGet(HttpExchange r) throws IOException, JSONException{
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if (deserialized.has("movieId")) {
        	this.movieId = deserialized.getString("movieId");
        } else {
        	r.sendResponseHeaders(400, 0);
        	return;
        }
        
        try (Session session = driver.session()) {
        	
        	String match = String.format("MATCH (m:movie {movieId: \"%s\"}) RETURN m.name", this.movieId);
        	StatementResult result = session.run(match);
        	
        	if (result.hasNext() == true) {
        		this.name = Utils.parseRecord(result.next().values().toString());
        		
        		String query = String.format("MATCH (m:movie {movieId:\"%s\"})-[:hasRelationship]->(a:Actor) RETURN a.actorId", this.movieId);
        		result = session.run(query);
				List<Record> actorList = result.list();
				
				JSONArray actors = new JSONArray();
				
				for(Record record: actorList ) {
					actors.put(Utils.parseRecord(record.values().toString()));
				}
				this.response.put("movieId:", this.movieId);
				this.response.put("name:", this.name);
				this.response.put("actors:", actors);
        		
        		OutputStream os = r.getResponseBody();
        		r.sendResponseHeaders(200, response.toString().getBytes().length);
        		os.write(response.toString().getBytes());
        		os.close();
        	} else {
        		r.sendResponseHeaders(404, 0);
        		return;
        	}
        }
        catch(Exception e) {
        	r.sendResponseHeaders(500, 0);
        	return;
        }
            
	}
	
	
}