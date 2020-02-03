package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class addActor implements HttpHandler{

	private String actorId;
	private String actorname;
	private Driver driver;


	public addActor(Driver driver) {
		this.driver = driver;
	}

	@Override
	public void handle(HttpExchange r) throws IOException {
		try {
            if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
                addactor(this.actorId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
	


	public void addactor(String actorId) {
		try (Session session = driver.session()){
			String match = String.format("MATCH (a:actor {actorId: %actorId}) RETURN a", this.actorId);
			StatementResult result = session.run(match);
			
			//check whether there is an existing record of person before adding
			
			if(!result.hasNext()) {
				//create person
				String create = 
			}
		}
		
	}

	public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("actorId") && deserialized.has("name")) {
        	actorId = deserialized.getString("actorId");
        	actorname = deserialized.getString("name");
        }
       
    }

	
	
	
	
	
	
	
}