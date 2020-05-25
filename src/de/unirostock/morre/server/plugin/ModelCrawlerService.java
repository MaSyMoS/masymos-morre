package de.unirostock.morre.server.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.MetaInfServices;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.ServerPlugin;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.unirostock.morre.server.util.ManagerUtil;
import de.unirostock.sems.masymos.database.ModelLookup;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
@MetaInfServices( ServerPlugin.class )
@Path("/model_crawler_service")
@Description( "An extension to the Neo4j Server to test if model API is alive" )
public class ModelCrawlerService extends ServerPlugin
{
	
	final static Logger logger = Logger.getLogger(ModelCrawlerService.class);

	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/get_model_history" )
    public String getModelHistory( 	@Context GraphDatabaseService graphDbSevice,
    										 String jsonMap)
    {
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
    	
    	Map<String, String> parameterMap = new HashMap<String, String>();
    	java.lang.reflect.Type typeOfT = new TypeToken<Map<String, String>>(){}.getType();

    	try {
    		parameterMap = gson.fromJson(jsonMap, typeOfT);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
            return gson.toJson(s); 
		}
    	
    	if (parameterMap==null){
    		String[] s = {"Exception","no parameters provided!"};
    		return gson.toJson(s);
    	} 		

    	String fileID = parameterMap.get("fileId");
    	if (StringUtils.isEmpty(fileID)){
    		String[] s = {"Exception","no file id provided!"};
    		return gson.toJson(s);
    	} 
    	List<String> resultList = null;
    	try {
    		resultList = ModelLookup.getDocumentHistory(fileID);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
   		
    		 return gson.toJson(resultList);

   

    }
  
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path ( "/get_model_history" )
    public String getModelHistory(@Context GraphDatabaseService graphDbSevice)
    {
		//ManagerUtil.initManager(graphDbSevice); 
		//String s = "Retrieve models matching the provided keywords. The query is expanded to all indices.";
		String[] s = {"'fileid':'($id)'"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/get_model_version" )
    public String getModelVersion( 	@Context GraphDatabaseService graphDbSevice,
    										String jsonMap)
    {
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
    	
    	Map<String, String> parameterMap = new HashMap<String, String>();
    	java.lang.reflect.Type typeOfT = new TypeToken<Map<String, String>>(){}.getType();

    	try {
    		parameterMap = gson.fromJson(jsonMap, typeOfT);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
            return gson.toJson(s); 
		}
    	
    	if (parameterMap==null){
    		String[] s = {"Exception","no parameters provided!"};
    		return gson.toJson(s);
    	} 		

    	String fileId = parameterMap.get("fileId");
    	if (StringUtils.isEmpty(fileId)){
    		String[] s = {"Exception","no model id provided!"};
    		return gson.toJson(s);
    	} 
    	String version = parameterMap.get("versionId");
    	if (StringUtils.isEmpty(version)){
    		return gson.toJson("newest version of model"); //TODO
    	} 
    	Map<String, String> resultMap= null;
    	try {

    		resultMap = ModelLookup.getDocumentVersion(fileId, version);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
   		
    		 return gson.toJson(resultMap);

   

    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path ( "/get_model_version" )
    public String getModelVersion(@Context GraphDatabaseService graphDbSevice)
    {
		//ManagerUtil.initManager(graphDbSevice); 
		//String s = "Retrieve models matching the provided keywords. The query is expanded to all indices.";
		String[] s = {"'fileId':'($id)', 'versionId':'($id)"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/get_model" )
    public String getModel( 	@Context GraphDatabaseService graphDbSevice,
    										String jsonMap)
    {
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
    	
    	Map<String, String> parameterMap = new HashMap<String, String>();
    	java.lang.reflect.Type typeOfT = new TypeToken<Map<String, String>>(){}.getType();

    	try {
    		parameterMap = gson.fromJson(jsonMap, typeOfT);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
            return gson.toJson(s); 
		}
    	
    	if (parameterMap==null){
    		String[] s = {"Exception","no parameters provided!"};
    		return gson.toJson(s);
    	} 		

    	String fileId = parameterMap.get("fileId");
    	if (StringUtils.isEmpty(fileId)){
    		String[] s = {"Exception","no model id provided!"};
    		return gson.toJson(s);
    	} 
    	Map<String, String> resultMap= null;
    	try {

    		resultMap = ModelLookup.getDocument(fileId);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
   		
    		 return gson.toJson(resultMap);//TODO

   

    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path ( "/get_model" )
    public String getModel(@Context GraphDatabaseService graphDbSevice)
    {
		//ManagerUtil.initManager(graphDbSevice); 
		//String s = "Retrieve models matching the provided keywords. The query is expanded to all indices.";
		String[] s = {"'fileId':'($id)'"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
/*	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/add_model_version" )
    public String addModelVersion( 	@Context GraphDatabaseService graphDbSevice,
    										String jsonMap)
    {
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
    	
    	ModelDataHolder mdh; 
    	java.lang.reflect.Type typeOfT = new TypeToken<ModelDataHolder>(){}.getType();
    	try {
    		mdh = gson.fromJson(jsonMap, typeOfT);	
		} catch (JsonSyntaxException e) {
			String[] s = {"Exception","wrong parameters provided!","Stacktrace", e.getMessage()};
    		return gson.toJson(s);
		}
    	
    	

    	Long uID = Long.MIN_VALUE;
    	try {
    		uID = ModelInserter.addModelVersion(mdh.getFileId(), mdh.getVersionId(), mdh.getParentMap(), new URL(mdh.getXmldoc()), gson.toJson(mdh.getMetaMap()), mdh.getModelType());
		} catch (Exception e) {
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
   		HashMap<String,String> resultMap = new HashMap<String,String>();
   		resultMap.put("fileId", mdh.getFileId());
   		resultMap.put("versionId", mdh.getVersionId());
   		resultMap.put("ok", "true");
   		resultMap.put("uID", uID.toString());
    		
   		return gson.toJson(resultMap);

   

    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path ( "/add_model_version" )
    public String addModelVersion(@Context GraphDatabaseService graphDbSevice)
    {
		//ManagerUtil.initManager(graphDbSevice); 
		//String s = "Retrieve models matching the provided keywords. The query is expanded to all indices.";
		String[] s = {"'fileId':'($id)', 'versionId':'($id), 'xmldoc:'($PATH)', 'parents':{'($parentModelId)':[($parentVersionId)], 'modelType':(SBML|CELLML|SEDML)}"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/add_model" )
    public String addModel( 	@Context GraphDatabaseService graphDbSevice,
    										String jsonMap)
    {
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
       	Map<String, String> parameterMap = new HashMap<String, String>();
    	java.lang.reflect.Type typeOfT = new TypeToken<Map<String, String>>(){}.getType();
    	
    	try {
    		parameterMap = gson.fromJson(jsonMap, typeOfT);	
		} catch (JsonSyntaxException e) {
			String[] s = {"Exception","wrong parameters provided!","Stacktrace", e.getMessage()};
    		return gson.toJson(s);
		}    	

    	Long uID = Long.MIN_VALUE;
    	String fileId = null;
    	URL url = null;
    	String modelType = null;
    	try {
    	   	fileId = parameterMap.get("fileId");
        	url = new URL(parameterMap.get("url"));
        	modelType = parameterMap.get("modelType");
    		uID = ModelInserter.addModel(fileId, url, modelType);
		} catch (Exception e) {
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
   		HashMap<String,String> resultMap = new HashMap<String,String>();
   		resultMap.put("fileId", fileId);
   		resultMap.put("url", url.toString());
   		resultMap.put("ok", "true");
   		resultMap.put("uID", uID.toString());
    		
   		return gson.toJson(resultMap);
    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path ( "/add_model" )
    public String addModel(@Context GraphDatabaseService graphDbSevice)
    {
		//ManagerUtil.initManager(graphDbSevice); 
		//String s = "Retrieve models matching the provided keywords. The query is expanded to all indices.";
		String[] s = {"'fileId':'($id)', 'url':'($PATH)', 'modelType':(SBML|CELLML|SEDML)}"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/delete_model" )
    public String deleteModel( 	@Context GraphDatabaseService graphDbSevice,
    										String jsonMap)
    {
    	//"'fileId':'($id)', 'versionId':'($id) [optional], 'uID':'($id)'"
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
    	
       	Map<String, String> parameterMap = new HashMap<String, String>();
    	java.lang.reflect.Type typeOfT = new TypeToken<Map<String, String>>(){}.getType();

    	try {
    		parameterMap = gson.fromJson(jsonMap, typeOfT);
		} catch (Exception e) {
			String[] s = {"Exception",e.getMessage()};			
            return gson.toJson(s); 
		}
    	
    	Long uId = Long.MIN_VALUE;
    	String fileId = null;
    	String versionId = null;
    
    	try {
    		uId = Long.valueOf(parameterMap.get("uID"));	
    		fileId = parameterMap.get("fileId");
    		versionId = parameterMap.get("versionId");
		} catch (Exception e) {
			String[] s = {"Exception",e.getMessage()};			
            return gson.toJson(s); 
		}
    	
    	String res = "";
    	if (StringUtils.isNotBlank(fileId) && StringUtils.isNotBlank(versionId)) res = ModelDeleter.deleteDocument(fileId, versionId, uId);
    	if (StringUtils.isNotBlank(fileId) && StringUtils.isBlank(versionId)) res = ModelDeleter.deleteDocument(fileId, uId);
    	if (StringUtils.isBlank(fileId) && StringUtils.isBlank(versionId)) res = ModelDeleter.deleteDocument(uId);
    	return gson.toJson(res);
     	
    }	
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path ( "/delete_model" )
    public String deleteModel(@Context GraphDatabaseService graphDbSevice)
    {
		//ManagerUtil.initManager(graphDbSevice); 
		//String s = "Removes models matching the provided keywords. The query is expanded to all indices.";
		String[] s = {"'fileId':'($id) [optional]', 'versionId':'($id) [optional]', 'uID':'($id)'"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/create_annotation_index" )
    public String createAnnotationIndex( 	@Context GraphDatabaseService graphDbSevice,
    										String jsonMap)
    {
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
    	
    	Map<String, String> parameterMap = new HashMap<String, String>();
    	java.lang.reflect.Type typeOfT = new TypeToken<Map<String, String>>(){}.getType();

    	try {
    		parameterMap = gson.fromJson(jsonMap, typeOfT);
		} catch (Exception e) {
			String[] s = {"Exception",e.getMessage()};			
            return gson.toJson(s); 
		}
    	
    	Boolean dropExistingIndex = false;
    	try {
    		dropExistingIndex = Boolean.parseBoolean(parameterMap.get("dropExistingIndex"));	
		} catch (Exception e) {

		}
    	
    	try {
    		ModelInserter.buildIndex(dropExistingIndex);
		} catch (Exception e) {
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
   		HashMap<String,String> resultMap = new HashMap<String,String>();
   		resultMap.put("ok", "true");
    		
   		return gson.toJson(resultMap);

   

    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path ( "/create_annotation_index" )
    public String createAnnotationIndex(@Context GraphDatabaseService graphDbSevice)
    {
		//ManagerUtil.initManager(graphDbSevice); 
		//String s = "Retrieve models matching the provided keywords. The query is expanded to all indices.";
		String[] s = {"'dropExistingIndex':'($bool)'"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
*/  
}