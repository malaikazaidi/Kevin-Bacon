package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class getActor implements HttpHandler{

	private Driver driver;
	private String actorId;
	private String actorname;
	private JSONObject response = new JSONObject() ;

	public getActor(Driver driver) {
		this.driver = driver;
	}

	@Override
	public void handle(HttpExchange r) throws IOException {
		try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
                getactor(this.actorId, this.actorname, r);
            }
            else {
            	//Send 400 error
				r.sendResponseHeaders(400, 0);
				OutputStream os = r.getResponseBody();
		        os.close();
            }
        } catch (Exception e) {
        	//send 500 error
        	r.sendResponseHeaders(500, 0);
        	OutputStream os = r.getResponseBody();
	        os.close();
            e.printStackTrace();
        }
		
	}

	private void getactor(String actorId, String actorname, HttpExchange r) throws IOException, JSONException{
		try (Session session = driver.session()){
			String match = String.format("MATCH (a:actor {id: \"%s\"}) RETURN a.Name", this.actorId);
			StatementResult result = session.run(match);
			
			//if there is a record with the actorid then get the info
			if(result.hasNext()) {
				this.actorname = Utils.parseRecord(result.next().values().toString());
				
				String matchmovie = String.format("MATCH (a:actor {id:\"%s\"})--(m:movie) RETURN m.id", this.actorId);
				result = session.run(matchmovie);
				List<Record> movielist = result.list();
				
				JSONArray movies = new JSONArray();
				for(Record record: movielist ) {
					movies.put(Utils.parseRecord(record.values().toString()));
				}
				response.put("actorId", this.actorId);
				response.put("name", this.actorname);
				response.put("movies", movies);
		        r.sendResponseHeaders(200, response.toString().getBytes().length);
		   
		        
		        OutputStream os = r.getResponseBody();
		        os.write(response.toString().getBytes());
		        os.close();
			}
			else {
				//actor not found error
				r.sendResponseHeaders(404, 0);
				OutputStream os = r.getResponseBody();
		        os.close();
			}
		}
		catch(Exception e) {
			r.sendResponseHeaders(500, 0);
			OutputStream os = r.getResponseBody();
	        os.close();
		}
	}

	private void handleGet(HttpExchange r) throws IOException, JSONException {
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("actorId") && deserialized.length() == 1) {
        	this.actorId = deserialized.getString("actorId");
        }
        else {
        	r.sendResponseHeaders(400, 0);
        	OutputStream os = r.getResponseBody();
	        os.close();
        }
       
		
	}
	
	

    
}