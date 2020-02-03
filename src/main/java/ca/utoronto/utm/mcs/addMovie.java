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

	private String moveId;
	private String name;
	private Driver driver;


	public addMovie(Driver driver) {
		this.driver = driver;
	}

	@Override
	public void handle(HttpExchange r) throws IOException {
		try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
                addmovie(this.moveId, this.name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
	


	public void addmovie(String moveId, String movieName) {
		try (Session session = driver.session()){
			String match = String.format("MATCH (m:movie {moveId: %moveId}) RETURN m", moveId);
			StatementResult result = session.run(match);
			
			//check whether there is an existing record of movie before adding
			
			if(!result.hasNext()) {
				//create movie
				String create = String.format("CREATE (m:movie {moveId: %moveId, name: %movieName} RETURN m)", moveId, movieName);
				StatementResult res = session.run(create);
			}
		}
		
	}

	public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("moveId") && deserialized.has("name")) {
        	moveId = deserialized.getString("moveId");
        	name = deserialized.getString("name");
        }
       
    }

	
	
	
	
	
	
	
}