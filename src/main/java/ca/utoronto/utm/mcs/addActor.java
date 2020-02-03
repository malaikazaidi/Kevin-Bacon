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
                addactor(this.actorId, this.actorname, r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
	}
	


	public void addactor(String actorId, String actorname, HttpExchange r) {
		try (Session session = driver.session()){
			String match = String.format("MATCH (a:Actor {id: \"%s\"}) RETURN a", actorId);
			StatementResult result = session.run(match);
			
			
			//check whether there is an existing record of person before adding
			
			if(result.hasNext() == false ) {
				//create person
				String create = String.format("CREATE (:Actor {name: \"%s\", actorId: \"%s\"}) ", actorname, actorId);
				StatementResult res = session.run(create);
				r.sendResponseHeaders(200, 0);
			}
			else {
				return;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("actorId") && deserialized.has("name")) {
        	actorId = deserialized.getString("actorId");
        	actorname = deserialized.getString("name");
        }
        else {
        	r.sendResponseHeaders(400, 0);
        }
       
    }

	
	
	
	
	
	
	
}