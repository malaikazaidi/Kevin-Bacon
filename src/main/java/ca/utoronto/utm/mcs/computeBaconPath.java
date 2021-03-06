package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.*;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Path.Segment;
import org.neo4j.driver.v1.util.Pair;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class computeBaconPath implements HttpHandler{

	private Driver driver;
	private String actorId;
	private String baconNumber;
	private JSONObject response = new JSONObject();
	private JSONObject bresponse = new JSONObject();
	
	public computeBaconPath(Driver driver) {
		this.driver = driver;
	}

	@Override
	public void handle(HttpExchange r) throws IOException {
		try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
                getBaconPath(this.actorId, r);
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

	private void getBaconPath(String actorId, HttpExchange r) throws IOException, JSONException {
		try (Session session = driver.session()){
			String match = String.format("MATCH (a:actor {id: \"%s\"}) RETURN a.Name", this.actorId);
			StatementResult result = session.run(match);
			
			//if there is a record with the actorid then get the info
			if(result.hasNext()) {
				if((this.actorId).equals("nm0000102")) {
					//if it is Kevin Bacon
					String matchmovie = String.format("MATCH (a:actor {id:\"%s\"})--(m:movie) RETURN m.id", this.actorId);
					result = session.run(matchmovie);
					List<Record> movielist = result.list();
					JSONArray array = new JSONArray();
					bresponse.put("actorId", this.actorId);
					bresponse.put("movieId", Utils.parseRecord(movielist.get(0).values().toString())  );
					array.put(bresponse);
					response.put("baconNumber", "0");
					response.put("baconPath", array);
				    r.sendResponseHeaders(200, response.toString().getBytes().length);
				   
				        
				    OutputStream os = r.getResponseBody();
				    os.write(response.toString().getBytes());
				    os.close();
				}
				else {
					String baconpath = String.format("MATCH p=shortestPath((a:actor {id:\"%s\"})-[*]-(b:actor {id:\"nm0000102\"})) RETURN p", this.actorId);
					result = session.run(baconpath);
					//count the relationships {} and divide by 2 to get the bacon number
					if(result.hasNext()) {
						Path path = result.single().get(0).asPath();
						int lengthofpath = path.length();
						this.baconNumber = String.valueOf((lengthofpath/2));
				
						response.put("baconNumber", this.baconNumber);
						JSONArray array = new JSONArray();
						for (Segment i:path) {
							String checkstart = String.format("MATCH (r) WHERE ID(r) = %d RETURN labels(r) ", i.start().id());
							StatementResult result2 = session.run(checkstart);
							JSONObject responsepath = new JSONObject();
							String s = new String("actor");
							String x = result2.single().get(0).toString();
							//if first node is actor
							if(x.contains(s)) {
								responsepath.put("actorId", Utils.removequotation(i.start().get("id").toString()));
								responsepath.put("movieId", Utils.removequotation(i.end().get("id").toString()));
								array.put(responsepath);
							}
							
							//first node is movie
							else {
								responsepath.put("actorId", Utils.removequotation(i.end().get("id").toString()));
								responsepath.put("movieId", Utils.removequotation(i.start().get("id").toString()));
								array.put(responsepath);
							}
					
							
						}
						
						
						JSONObject responsepath = new JSONObject();
							
						response.put("baconPath", array);
					    r.sendResponseHeaders(200, response.toString().getBytes().length);
					    OutputStream os = r.getResponseBody();
					    os.write(response.toString().getBytes());
					    os.close();
					        
		
					}
					else {
						//there is not path with this actor
						r.sendResponseHeaders(404, 0);
						OutputStream os = r.getResponseBody();
				        os.close();
					}
						
				}
				
				
			}
			else {
				//actor not found error
				r.sendResponseHeaders(400, 0);
				OutputStream os = r.getResponseBody();
		        os.close();
			}
			
			
		}
		catch(Exception e) {
			System.out.print("hhhheh");
			r.sendResponseHeaders(500, 0);
			OutputStream os = r.getResponseBody();
	        os.close();
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
        	OutputStream os = r.getResponseBody();
	        os.close();
        }
	}
	
}