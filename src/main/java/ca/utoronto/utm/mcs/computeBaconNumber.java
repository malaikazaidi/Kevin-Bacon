package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.types.Path;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class computeBaconNumber implements HttpHandler{

	private Driver driver;
	private String actorId;
	private String baconNumber;
	private JSONObject response = new JSONObject();

	public computeBaconNumber(Driver driver) {
		this.driver=driver;
	}

	@Override
	public void handle(HttpExchange r) throws IOException {
		try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
                getBaconNumber(this.actorId, r);
                r.sendResponseHeaders(200, 0);
            }
            else {
            	//Send 400 error
				r.sendResponseHeaders(400, 0);
            }
        } catch (Exception e) {
        	//send 500 error
        	r.sendResponseHeaders(500, 0);
            e.printStackTrace();
        }
		
	}

	private void getBaconNumber(String actorId, HttpExchange r) {
		try (Session session = driver.session()){
			String match = String.format("MATCH (a:Actor {actorId: \"%s\"}) RETURN a.name", actorId);
			StatementResult result = session.run(match);
			
			//if there is a record with the actorid then get the info
			if(result.hasNext()) {
				
				String baconpath = String.format("MATCH p=shortestPath((bacon:Actor {actorId:\"nm0000102\"})-[*]-(a:Actor {actorId:\"%s\"})) RETURN p", this.actorId);
				result = session.run(baconpath);
				
				//count the relationships {} and divide by 2 to get the bacon number
				if(result.hasNext()) {
					Path path = result.single().get(0).asPath();
					
					int lengthofpath = path.length();
					this.baconNumber = String.valueOf((lengthofpath/2));
						
						
					response.put("baconNumber:", this.baconNumber);
				    r.sendResponseHeaders(200, response.toString().getBytes().length);
				   
				        
				    OutputStream os = r.getResponseBody();
				    os.write(response.toString().getBytes());
				    os.close();
						
				}
				else {
						//there is not path with this actor
						r.sendResponseHeaders(404, 0);
				}
				
			}
			else {
				//actor not found error
				r.sendResponseHeaders(400, 0);
			}
			
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void handleGet(HttpExchange r) throws IOException, JSONException {
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        
        if(deserialized.has("actorId") && deserialized.length() == 1) {
        	actorId = deserialized.getString("actorId");
        }
        else {
        	r.sendResponseHeaders(400, 0);
        }
	}
	
}