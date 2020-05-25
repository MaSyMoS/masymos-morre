package de.unirostock.morre.server.plugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.unirostock.morre.server.util.ManagerUtil;
import de.unirostock.sems.masymos.configuration.RankAggregationType;
import de.unirostock.sems.masymos.configuration.RankAggregationType.Types;
import de.unirostock.sems.masymos.query.IQueryInterface;
import de.unirostock.sems.masymos.query.QueryAdapter;
import de.unirostock.sems.masymos.query.aggregation.GroupVersions;
import de.unirostock.sems.masymos.query.aggregation.RankAggregation;
import de.unirostock.sems.masymos.query.enumerator.AnnotationFieldEnumerator;
import de.unirostock.sems.masymos.query.enumerator.CellMLModelFieldEnumerator;
import de.unirostock.sems.masymos.query.enumerator.PersonFieldEnumerator;
import de.unirostock.sems.masymos.query.enumerator.PublicationFieldEnumerator;
import de.unirostock.sems.masymos.query.enumerator.SBMLModelFieldEnumerator;
import de.unirostock.sems.masymos.query.enumerator.SedmlFieldEnumerator;
import de.unirostock.sems.masymos.query.results.AnnotationResultSet;
import de.unirostock.sems.masymos.query.results.ModelResultSet;
import de.unirostock.sems.masymos.query.results.VersionResultSet;
import de.unirostock.sems.masymos.query.results.PersonResultSet;
import de.unirostock.sems.masymos.query.results.PublicationResultSet;
import de.unirostock.sems.masymos.query.results.SedmlResultSet;
import de.unirostock.sems.masymos.query.types.AnnotationQuery;
import de.unirostock.sems.masymos.query.types.CellMLModelQuery;
import de.unirostock.sems.masymos.query.types.PersonQuery;
import de.unirostock.sems.masymos.query.types.PublicationQuery;
import de.unirostock.sems.masymos.query.types.SBMLModelQuery;
import de.unirostock.sems.masymos.query.types.SedmlQuery;
import de.unirostock.sems.masymos.util.RankAggregationUtil;
import de.unirostock.sems.masymos.util.ResultSetUtil;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
//@MetaInfServices( ServerPlugin.class )
@Path("/query")
public class Query{
	
	final static Logger logger = Logger.getLogger(Query.class);

    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/model_query" )
    public String modelQuery( 	@Context GraphDatabaseService graphDbSevice,
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

    	String keyword = parameterMap.get("keyword");
    	if (StringUtils.isEmpty(keyword)){
    		String[] s = {"Exception","no keywords provided!"};
    		return gson.toJson(s);
    	} 
    	String topns = parameterMap.get("topn");
    	Integer topn = Integer.MAX_VALUE;
    	if (!StringUtils.isEmpty(topns) && StringUtils.isNumeric(topns)){
    		topn = Integer.valueOf(topns);
    	}
    	List<VersionResultSet> results = null;
    	
       	CellMLModelQuery cq = new CellMLModelQuery();
    	cq.addQueryClause(CellMLModelFieldEnumerator.NONE, keyword);
    	SBMLModelQuery sq = new SBMLModelQuery();
    	sq.addQueryClause(SBMLModelFieldEnumerator.NONE, keyword);
    	PersonQuery persq = new PersonQuery();
    	persq.addQueryClause(PersonFieldEnumerator.NONE, keyword);
    	PublicationQuery pubq = new PublicationQuery();
    	pubq.addQueryClause(PublicationFieldEnumerator.NONE, keyword);
    	AnnotationQuery aq = new AnnotationQuery();
    	aq.addQueryClause(AnnotationFieldEnumerator.NONE, keyword);
    	
    	List<IQueryInterface> qL = new LinkedList<IQueryInterface>();
		qL.add(cq);
		qL.add(sq);
		qL.add(persq);
		qL.add(pubq);
		qL.add(aq);
    	try {
    		results = QueryAdapter.executeMultipleQueriesForModels(qL);
    		results = ResultSetUtil.collateModelResultSetByModelId(results);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
    	if ((results!=null) && !results.isEmpty()) {
 
    		results = results.subList(0, Math.min(topn, results.size()));
    		return gson.toJson(results);
    		
    	} else {
    		String[] s = {"#Results","0"};
    		
    		 return gson.toJson(s);

    	}
    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/model_query" )
    public String modelQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		//String s = "Retrieve models matching the provided keywords. The query is expanded to all indices.";
		String[] s = {"keyword"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
	
	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/simple_cellml_model_query" )
    public String simpleCellMLModelQuery( 	@Context GraphDatabaseService graphDbSevice,
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

    	String keyword = parameterMap.get("keyword");
    	if (StringUtils.isEmpty(keyword)){
    		String[] s = {"Exception","no keywords provided!"};
    		return gson.toJson(s);
    	} 
    	String topns = parameterMap.get("topn");
    	Integer topn = Integer.MAX_VALUE;
    	if (!StringUtils.isEmpty(topns) && StringUtils.isNumeric(topns)){
    		topn = Integer.valueOf(topns);
    	}
    	List<VersionResultSet> results = null;
    	
       	CellMLModelQuery cq = new CellMLModelQuery();
    	cq.addQueryClause(CellMLModelFieldEnumerator.NONE, keyword);     	
    	List<IQueryInterface> qL = new LinkedList<IQueryInterface>();
		qL.add(cq);
    	try {
    		results = QueryAdapter.executeMultipleQueriesForModels(qL);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
    	if ((results!=null) && !results.isEmpty()) {

    		results = results.subList(0, Math.min(topn, results.size()));
    		return gson.toJson(results);
    		
    	} else {
    		String[] s = {"#Results","0"};
    		
    		 return gson.toJson(s);

    	}
    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/simple_cellml_model_query" )
    public String simpleCellMLModelQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 		
		//String s = "Retrieve CellML models matching the provided keywords.";
		String[] s = {"keyword"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }    
    

	@SuppressWarnings("unchecked")
	@POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/cellml_model_query" )
    public String cellMLModelQuery( @Context GraphDatabaseService graphDbSevice,
    								String jsonMap)
    {
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
    	
    	Map<String, Object> parameterMap = new HashMap<String, Object>();
    	java.lang.reflect.Type typeOfT = new TypeToken<Map<String, Object>>(){}.getType();

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
    	
    	Integer topn = Integer.MAX_VALUE;
    	List<String> features = null;
    	List<String> keywords = null;
    	Object o;
    	o = parameterMap.get("features");
    	if (o instanceof List<?>){
    		features = (List<String>) o; ;
    	}
    	if ((features==null)) {
    		String[] s = {"Exception","No features provided"};
    		return gson.toJson(s);
    	}
    	
    	o =  parameterMap.get("keywords");
    	if (o instanceof List<?>){
    		keywords = (List<String>) o; ;
    	}
    	if ((keywords==null)) {
    		String[] s = {"Exception","No keywords provided"};
    		return gson.toJson(s);
    	}
    	
    	o = parameterMap.get("topn");    	
    	if (o instanceof String){
    		String topns = (String) parameterMap.get("topn");
    		if (StringUtils.isNumeric(topns)){
    			topn = Integer.valueOf(topns);
    		}
    	}
    	if (o instanceof Integer){
    		topn = (Integer) o;
    	}
    	   	
    	CellMLModelQuery cq = new CellMLModelQuery();
      	
    	int noOfPairs = Math.min(features.size(), keywords.size());    	
    	for (int i = 0; i < noOfPairs; i++) {
			String keyword = keywords.get(i);
			String feature = features.get(i);
			CellMLModelFieldEnumerator field;
			try {
				field = CellMLModelFieldEnumerator.valueOf(feature.trim().toUpperCase(Locale.ENGLISH));
			} catch (Exception e) {
				logger.warn("The feature " + feature + " is not available in CellMLModelQuery");
				logger.warn(e.getMessage());
				//feature is not available
				continue;
			}			
			cq.addQueryClause(field, keyword);
		}
    	   	
    	List<VersionResultSet> results = null;
       	List<IQueryInterface> qL = new LinkedList<IQueryInterface>();
    		qL.add(cq);
        	try {
        		results = QueryAdapter.executeMultipleQueriesForModels(qL);
    		} catch (Exception e) {
    			logger.error(e.getMessage());
    			String[] s = {"Exception",e.getMessage()};			
    			return gson.toJson(s);
    		}
        	if ((results!=null) && !results.isEmpty()) {
        		results = results.subList(0, Math.min(topn, results.size()));
        		return gson.toJson(results);
        	} else {
        		String[] s = {"#Results","0"};
    			return gson.toJson(s);
        	}
    }
	
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/cellml_model_query" )
    public String cellMLModelQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		
//		StringBuilder sb = new StringBuilder();
//	    String[] fields = (new CellMLModelQuery().getIndexedFields());
//	    for (String field : fields) {
//			sb.append(field);
//			sb.append(", ");
//		} 
//		String s = "Retrieve CellML models matching the provided (feature : keywords) pairs." +
//	  		  "Ensure that position of a keyword in keywords parameter matches the" + 
//		      " position of the feature in features parameter.\r\n" +
//	  		  "Features is a list of " + StringUtils.removeEnd(sb.toString(), ", ") + "\r\n" +
//		      "Keywords are the terms to search for.";
		
		Gson gson = new Gson();
		return gson.toJson(new CellMLModelQuery().getIndexedFields());
    }
	

    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/simple_sbml_model_query" )
    public String simpleSBMLModelQuery(	@Context GraphDatabaseService graphDbSevice,
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

    	String keyword = parameterMap.get("keyword");
    	if (StringUtils.isEmpty(keyword)){
    		String[] s = {"Exception","no keywords provided!"};
    		return gson.toJson(s);
    	} 
    	String topns = parameterMap.get("topn");
    	Integer topn = Integer.MAX_VALUE;
    	if (!StringUtils.isEmpty(topns) && StringUtils.isNumeric(topns)){
    		topn = Integer.valueOf(topns);
    	}
    	List<VersionResultSet> results = null;
    	
       	SBMLModelQuery sq = new SBMLModelQuery();
    	sq.addQueryClause(SBMLModelFieldEnumerator.NONE, keyword);     	
    	List<IQueryInterface> sL = new LinkedList<IQueryInterface>();
		sL.add(sq);
    	try {
    		results = QueryAdapter.executeMultipleQueriesForModels(sL);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
    	if ((results!=null) && !results.isEmpty()) {
    		results = results.subList(0, Math.min(topn, results.size()));
    		return gson.toJson(results);
    		
    	} else {
    		String[] s = {"#Results","0"};
    		
    		 return gson.toJson(s);

    	}
    }	
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/simple_sbml_model_query" )
    public String simpleSBMLModelQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		
//		String s = "Retrieve SBML models matching the provided keywords.";
		String[] s = {"keyword"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
	
	@SuppressWarnings("unchecked")
	@POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/sbml_model_query" )
    public String sbmlModelQuery( 	@Context GraphDatabaseService graphDbSevice,
    								String jsonMap)
    {
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
    	
    	Map<String, Object> parameterMap = new HashMap<String, Object>();
    	java.lang.reflect.Type typeOfT = new TypeToken<Map<String, Object>>(){}.getType();

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
    	
    	Integer topn = Integer.MAX_VALUE;
    	List<String> features = null;
    	List<String> keywords = null;
    	Object o;
    	o = parameterMap.get("features");
    	if (o instanceof List<?>){
    		features = (List<String>) o; ;
    	}
    	if ((features==null)) {
    		String[] s = {"Exception","No features provided"};
    		return gson.toJson(s);
    	}
    	
    	o =  parameterMap.get("keywords");
    	if (o instanceof List<?>){
    		keywords = (List<String>) o; ;
    	}
    	if ((keywords==null)) {
    		String[] s = {"Exception","No keywords provided"};
    		return gson.toJson(s);
    	}
    	
    	o = parameterMap.get("topn");    	
    	if (o instanceof String){
    		String topns = (String) parameterMap.get("topn");
    		if (StringUtils.isNumeric(topns)){
    			topn = Integer.valueOf(topns);
    		}
    	}
    	if (o instanceof Integer){
    		topn = (Integer) o;
    	}
    	   	
    	SBMLModelQuery sq = new SBMLModelQuery();
      	
    	int noOfPairs = Math.min(features.size(), keywords.size());    	
    	for (int i = 0; i < noOfPairs; i++) {
			String keyword = keywords.get(i);
			String feature = features.get(i);
			SBMLModelFieldEnumerator field;
			try {
				field = SBMLModelFieldEnumerator.valueOf(feature.trim().toUpperCase(Locale.ENGLISH));
			} catch (Exception e) {				
				logger.warn("The featrue " + feature + " is not avialable in SBMLModelQuery");
				logger.warn(e.getMessage());
				continue;
			}			
			sq.addQueryClause(field, keyword);
		}
    	   	
    	List<VersionResultSet> results = null;
       	List<IQueryInterface> sL = new LinkedList<IQueryInterface>();
    		sL.add(sq);
        	try {
        		results = QueryAdapter.executeMultipleQueriesForModels(sL);
    		} catch (Exception e) {
    			logger.error(e.getMessage());
    			String[] s = {"Exception",e.getMessage()};			
    			return gson.toJson(s);
    		}
        	if ((results!=null) && !results.isEmpty()) {
        		results = results.subList(0, Math.min(topn, results.size()));
        		return gson.toJson(results);
        	} else {
        		String[] s = {"#Results","0"};
    			return gson.toJson(s);
        	}
    }
	
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/sbml_model_query" )
    public String sbmlModelQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		
//		StringBuilder sb = new StringBuilder();
//	    String[] fields = (new SBMLModelQuery().getIndexedFields());
//	    for (String field : fields) {
//			sb.append(field);
//			sb.append(", ");
//		} 
//		String s = "Retrieve SBML models matching the provided (feature : keywords) pairs." +
//	  		  "Ensure that position of a keyword in keywords parameter matches the" + 
//		      " position of the feature in features parameter.\r\n" +
//	  		  "Features is a list of " + StringUtils.removeEnd(sb.toString(), ", ") + "\r\n" +
//		      "Keywords are the terms to search for.";
		Gson gson = new Gson();
		return gson.toJson(new SBMLModelQuery().getIndexedFields());
	
    }
	
	@POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/publication_query" )
    public String publicationQuery( @Context GraphDatabaseService graphDbSevice,
    								String jsonMap)
    {
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
    	
    	Map<String, List<String>> parameterMap = new HashMap<String, List<String>>();
    	java.lang.reflect.Type typeOfT = new TypeToken<Map<String, List<String>>>(){}.getType();
    	
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
    	
    	List<String> features = parameterMap.get("features");
    	List<String> keywords = parameterMap.get("keywords");
    	if ((keywords==null)) {
    		String[] s = {"Exception","No keywords provided"};
    		return gson.toJson(s);
    	}    	
    	if ((features==null)) {
    		String[] s = {"Exception","No features provided"};
    		return gson.toJson(s);
    	}
    	   	
    	PublicationQuery pq = new PublicationQuery();
      	
    	int noOfPairs = Math.min(features.size(), keywords.size());    	
    	for (int i = 0; i < noOfPairs; i++) {
			String keyword = keywords.get(i);
			String feature = features.get(i);
			PublicationFieldEnumerator field;
			try {
				field = PublicationFieldEnumerator.valueOf(feature.trim().toUpperCase(Locale.ENGLISH));
			} catch (Exception e) {			
				logger.warn("The featrue " + feature + " is not available in PublicationQuery.");
				logger.warn(e.getMessage());
				continue;
			}			
			pq.addQueryClause(field, keyword);
		}
    	   	
    	List<PublicationResultSet> results = null;
           	try {
        		results = QueryAdapter.executePublicationQuery(pq);
    		} catch (Exception e) {
    			logger.error(e.getMessage());
    			String[] s = {"Exception",e.getMessage()};			
    			return gson.toJson(s);
    		}
        	if ((results!=null) && !results.isEmpty()) {      		
        		return gson.toJson(results);       		
        	} else {
        		String[] s = {"#Results","0"};
    			return gson.toJson(s);
        	}
    }
	
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/publication_query" )
    public String publicationQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		
//		StringBuilder sb = new StringBuilder();
//	    String[] fields = (new PublicationQuery().getIndexedFields());
//	    for (String field : fields) {
//			sb.append(field);
//			sb.append(", ");
//		} 
//		String s = "Retrieve publications defined by (feature : keywords) pairs." +
//	  		  "Ensure that position of a keyword in keywords parameter matches the" + 
//		      " position of the feature in features parameter.\r\n" +
//	  		  "Features is a list of " + StringUtils.removeEnd(sb.toString(), ", ") + "\r\n" +
//		      "Keywords are the terms to search for.";
		Gson gson = new Gson();
		return gson.toJson(new PublicationQuery().getIndexedFields());
    }
	
	@POST
	@Produces( MediaType.APPLICATION_JSON )
	@Consumes( MediaType.APPLICATION_JSON ) 
	@Path( "/publication_model_query" )
	public String publicationModelQuery( @Context GraphDatabaseService graphDbSevice,
									String jsonMap)
	{
		ManagerUtil.initManager(graphDbSevice); 	
		
		Gson gson = new Gson();
		
		Map<String, List<String>> parameterMap = new HashMap<String, List<String>>();
		java.lang.reflect.Type typeOfT = new TypeToken<Map<String, List<String>>>(){}.getType();

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
		
		List<String> features = parameterMap.get("features");
		List<String> keywords = parameterMap.get("keywords");
		if ((keywords==null)) {
			String[] s = {"Exception","No keywords provided"};
			return gson.toJson(s);
		}    	
		if ((features==null)) {
			String[] s = {"Exception","No features provided"};
			return gson.toJson(s);
		}
		   	
		PublicationQuery pq = new PublicationQuery();
	  	
		int noOfPairs = Math.min(features.size(), keywords.size());    	
		for (int i = 0; i < noOfPairs; i++) {
			String keyword = keywords.get(i);
			String feature = features.get(i);
			PublicationFieldEnumerator field;
			try {
				field = PublicationFieldEnumerator.valueOf(feature.trim().toUpperCase(Locale.ENGLISH));
			} catch (Exception e) {		
				logger.warn("The feature " + feature + " is not available in PublicationQuery");
				logger.warn(e.getMessage());
				//feature is not available
				continue;
			}			
			pq.addQueryClause(field, keyword);
		}
		   	
		List<VersionResultSet> results = null;
	   	List<IQueryInterface> qL = new LinkedList<IQueryInterface>();
			qL.add(pq);
	    	try {
	    		results = QueryAdapter.executeMultipleQueriesForModels(qL);
			} catch (Exception e) {
				logger.error(e.getMessage());
				String[] s = {"Exception",e.getMessage()};			
				return gson.toJson(s);
			}
	    	if ((results!=null) && !results.isEmpty()) {      		
	    		return gson.toJson(results);       		
	    	} else {
	    		String[] s = {"#Results","0"};
				return gson.toJson(s);
	    	}
	}

	@GET
	@Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
	@Path( "/publication_model_query" )
	public String publicationModelQuery(@Context GraphDatabaseService graphDbSevice)
	{
		ManagerUtil.initManager(graphDbSevice); 
		
//		StringBuilder sb = new StringBuilder();
//	    String[] fields = (new PublicationQuery().getIndexedFields());
//	    for (String field : fields) {
//			sb.append(field);
//			sb.append(", ");
//		} 
//		String s = "Retrieve models related to provided persons. Person are defined by (feature : keywords) pairs." +
//	  		  "Ensure that position of a keyword in keywords parameter matches the" + 
//		      " position of the feature in features parameter.\r\n" +
//	  		  "Features is a list of " + StringUtils.removeEnd(sb.toString(), ", ") + "\r\n" +
//		      "Keywords are the terms to search for.";
		Gson gson = new Gson();
		return gson.toJson(new PublicationQuery().getIndexedFields());
	}

	@POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/person_model_query" )
    public String personModelQuery( @Context GraphDatabaseService graphDbSevice,
    								String jsonMap)
    {
    	ManagerUtil.initManager(graphDbSevice); 	
    	
    	Gson gson = new Gson();
    	
    	Map<String, List<String>> parameterMap = new HashMap<String, List<String>>();
    	java.lang.reflect.Type typeOfT = new TypeToken<Map<String, List<String>>>(){}.getType();

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
    	
    	List<String> features = parameterMap.get("features");
    	List<String> keywords = parameterMap.get("keywords");
    	if ((keywords==null)) {
    		String[] s = {"Exception","No keywords provided"};
    		return gson.toJson(s);
    	}    	
    	if ((features==null)) {
    		String[] s = {"Exception","No features provided"};
    		return gson.toJson(s);
    	}
    	   	
    	PersonQuery pq = new PersonQuery();
      	
    	int noOfPairs = Math.min(features.size(), keywords.size());    	
    	for (int i = 0; i < noOfPairs; i++) {
			String keyword = keywords.get(i);
			String feature = features.get(i);
			PersonFieldEnumerator field;
			try {
				field = PersonFieldEnumerator.valueOf(feature.trim().toUpperCase(Locale.ENGLISH));
			} catch (Exception e) {			
				logger.warn("The feature " + feature + " is not available in PersonQuery");
				logger.warn(e.getMessage());
				//feature is not available
				continue;
			}			
			pq.addQueryClause(field, keyword);
		}
    	   	
    	List<VersionResultSet> results = null;
       	List<IQueryInterface> qL = new LinkedList<IQueryInterface>();
    		qL.add(pq);
        	try {
        		results = QueryAdapter.executeMultipleQueriesForModels(qL);
    		} catch (Exception e) {
    			logger.error(e.getMessage());
    			String[] s = {"Exception",e.getMessage()};			
    			return gson.toJson(s);
    		}
        	if ((results!=null) && !results.isEmpty()) {      		
        		return gson.toJson(results);       		
        	} else {
        		String[] s = {"#Results","0"};
    			return gson.toJson(s);
        	}
    }
	
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/person_model_query" )
    public String personModelQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		
//		StringBuilder sb = new StringBuilder();
//	    String[] fields = (new PersonQuery().getIndexedFields());
//	    for (String field : fields) {
//			sb.append(field);
//			sb.append(", ");
//		} 
//		String s = "Retrieve models related to provided persons. Person are defined by (feature : keywords) pairs." +
//	  		  "Ensure that position of a keyword in keywords parameter matches the" + 
//		      " position of the feature in features parameter.\r\n" +
//	  		  "Features is a list of " + StringUtils.removeEnd(sb.toString(), ", ") + "\r\n" +
//		      "Keywords are the terms to search for.";
		Gson gson = new Gson();
		return gson.toJson(new PersonQuery().getIndexedFields());
    }
	
	@POST
	@Produces( MediaType.APPLICATION_JSON )
	@Consumes( MediaType.APPLICATION_JSON ) 
	@Path( "/person_query" )
	public String personQuery( @Context GraphDatabaseService graphDbSevice,
									String jsonMap)
	{
		ManagerUtil.initManager(graphDbSevice); 	
		
		Gson gson = new Gson();
		
		Map<String, List<String>> parameterMap = new HashMap<String, List<String>>();
		java.lang.reflect.Type typeOfT = new TypeToken<Map<String, List<String>>>(){}.getType();

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
		
		List<String> features = parameterMap.get("features");
		List<String> keywords = parameterMap.get("keywords");
		if ((keywords==null)) {
			String[] s = {"Exception","No keywords provided"};
			return gson.toJson(s);
		}    	
		if ((features==null)) {
			String[] s = {"Exception","No features provided"};
			return gson.toJson(s);
		}
		   	
		PersonQuery pq = new PersonQuery();
	  	
		int noOfPairs = Math.min(features.size(), keywords.size());    	
		for (int i = 0; i < noOfPairs; i++) {
			String keyword = keywords.get(i);
			String feature = features.get(i);
			PersonFieldEnumerator field;
			try {
				field = PersonFieldEnumerator.valueOf(feature.trim().toUpperCase(Locale.ENGLISH));
			} catch (Exception e) {			
				logger.warn("The featrue " + feature + " is not avialable in PersonQuery");
				logger.warn(e.getMessage());
				//feature is not available
				continue;
			}			
			pq.addQueryClause(field, keyword);
		}
		   	
		List<PersonResultSet> results = null;
	   	   	try {
	    		results = QueryAdapter.executePersonQuery(pq);
			} catch (Exception e) {
				logger.error(e.getMessage());
				String[] s = {"Exception",e.getMessage()};			
				return gson.toJson(s);
			}
	    	if ((results!=null) && !results.isEmpty()) {      		
	    		return gson.toJson(results);       		
	    	} else {
	    		String[] s = {"#Results","0"};
				return gson.toJson(s);
	    	}
	}

	@GET
	@Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
	@Path( "/person_query" )
	public String personQuery(@Context GraphDatabaseService graphDbSevice)
	{
		ManagerUtil.initManager(graphDbSevice); 
		
//		StringBuilder sb = new StringBuilder();
//	    String[] fields = (new PersonQuery().getIndexedFields());
//	    for (String field : fields) {
//			sb.append(field);
//			sb.append(", ");
//		} 
//		String s = "Retrieve persons defined by (feature : keywords) pairs." +
//	  		  "Ensure that position of a keyword in keywords parameter matches the" + 
//		      " position of the feature in features parameter.\r\n" +
//	  		  "Features is a list of " + StringUtils.removeEnd(sb.toString(), ", ") + "\r\n" +
//		      "Keywords are the terms to search for.";
		Gson gson = new Gson();
		return gson.toJson(new PersonQuery().getIndexedFields());
	}
	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/annotation_model_query" )
    public String annotationModelQuery( 	@Context GraphDatabaseService graphDbSevice,
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

    	String keyword = parameterMap.get("keyword");
    	if (StringUtils.isEmpty(keyword)){
    		String[] s = {"Exception","no keywords provided!"};
    		return gson.toJson(s);
    	} 
    	String topns = parameterMap.get("topn");
    	Integer topn = Integer.MAX_VALUE;
    	if (!StringUtils.isEmpty(topns) && StringUtils.isNumeric(topns)){
    		topn = Integer.valueOf(topns);
    	}
    	List<VersionResultSet> results = null;
    	
       	AnnotationQuery cq = new AnnotationQuery();
    	cq.addQueryClause(AnnotationFieldEnumerator.NONE, keyword);     	
    	List<IQueryInterface> qL = new LinkedList<IQueryInterface>();
		qL.add(cq);
    	try {
    		results = QueryAdapter.executeMultipleQueriesForModels(qL);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
    	if ((results!=null) && !results.isEmpty()) {
    		results = results.subList(0, Math.min(topn, results.size()));
    		return gson.toJson(results);
    		
    	} else {
    		String[] s = {"#Results","0"};
    		
    		 return gson.toJson(s);

    	}
    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/annotation_model_query" )
    public String annotationModelQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		
//		String s = "Retrieve models by annotation keywords.";	  		 
		String[] s = {"keyword"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/annotation_query" )
    public String annotationQuery( 	@Context GraphDatabaseService graphDbSevice,
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

    	String keyword = parameterMap.get("keyword");
    	if (StringUtils.isEmpty(keyword)){
    		String[] s = {"Exception","no keywords provided!"};
    		return gson.toJson(s);
    	} 
    	String topns = parameterMap.get("topn");
    	Integer topn = Integer.MAX_VALUE;
    	if (!StringUtils.isEmpty(topns) && StringUtils.isNumeric(topns)){
    		topn = Integer.valueOf(topns);
    	}
    	List<AnnotationResultSet> results = null;
    	
       	AnnotationQuery cq = new AnnotationQuery();
    	cq.addQueryClause(AnnotationFieldEnumerator.NONE, keyword);     	
    	try {
    		results = QueryAdapter.executeAnnotationQuery(cq);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
    	if ((results!=null) && !results.isEmpty()) {
    		results = results.subList(0, Math.min(topn, results.size()));
    		return gson.toJson(results);
    		
    	} else {
    		String[] s = {"#Results","0"};
    		
    		 return gson.toJson(s);

    	}
    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/annotation_query" )
    public String annotationQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		
//		String s = "Retrieve annotations by annotation keywords.";
		String[] s = {"keyword"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
	@POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/simple_sedml_query" )
    public String simpleSedmlQuery( @Context GraphDatabaseService graphDbSevice,
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

    	String keyword = parameterMap.get("keyword");
    	if (StringUtils.isEmpty(keyword)){
    		String[] s = {"Exception","no keywords provided!"};
    		return gson.toJson(s);
    	} 
    	String topns = parameterMap.get("topn");
    	Integer topn = Integer.MAX_VALUE;
    	if (!StringUtils.isEmpty(topns) && StringUtils.isNumeric(topns)){
    		topn = Integer.valueOf(topns);
    	}
    	List<SedmlResultSet> results = null;
    	
    	
    	SedmlQuery sedq = new SedmlQuery();
    	sedq.addQueryClause(SedmlFieldEnumerator.NONE, keyword);
    	
    	try {
    	results = QueryAdapter.executeSedmlQuery(sedq);
    	//results = ResultSetUtil.collateModelResultSetByModelId(results);
    	} catch (Exception e) {
    		logger.error(e.getMessage());
    	String[] s = {"Exception",e.getMessage()};			
    	
    	return gson.toJson(s); 
    	}
    			
    	if ((results!=null) && !results.isEmpty()) {
    		results = results.subList(0, Math.min(topn, results.size()));
       		return gson.toJson(results);
    		
    	} else {
    		String[] s = {"#Results","0"};
    		
    		 return gson.toJson(s);

    	}
    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
	@Path( "/simple_sedml_query" )
    public String simpleSedmlQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		//String s = "Retrieve models matching the provided keywords. The query is expanded to all indices.";
		String[] s = {"keyword"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/aggregated_model_query" )
    public String aggregatedModelQuery( 	@Context GraphDatabaseService graphDbSevice,
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

    	String keyword = parameterMap.get("keyword");
    	if (StringUtils.isEmpty(keyword)){
    		String[] s = {"Exception","no keywords provided!"};
    		return gson.toJson(s);
    	} 
    	String topns = parameterMap.get("topn");
    	Integer topn = Integer.MAX_VALUE;
    	if (!StringUtils.isEmpty(topns) && StringUtils.isNumeric(topns)){
    		topn = Integer.valueOf(topns);
    	}   	
    	String aggregationTypeString = parameterMap.get("aggregationType");
    	RankAggregationType.Types aggregationType;
    	if (StringUtils.isEmpty(aggregationTypeString)){
    		aggregationType = Types.DEFAULT;  
    	} else aggregationType = RankAggregationType.stringToRankAggregationType(aggregationTypeString);
    	String rankersWeightsString = parameterMap.get("rankersWeights");
    	int rankersWeights = Integer.parseInt(rankersWeightsString); //possible exception if not provided
    	
    	List<VersionResultSet> results = null;
    	List<VersionResultSet> initialAggregateRanker = null;
    	
       	CellMLModelQuery cq = new CellMLModelQuery();
    	cq.addQueryClause(CellMLModelFieldEnumerator.NONE, keyword);
    	SBMLModelQuery sq = new SBMLModelQuery();
    	sq.addQueryClause(SBMLModelFieldEnumerator.NONE, keyword);
    	PersonQuery persq = new PersonQuery();
    	persq.addQueryClause(PersonFieldEnumerator.NONE, keyword);
    	PublicationQuery pubq = new PublicationQuery();
    	pubq.addQueryClause(PublicationFieldEnumerator.NONE, keyword);
    	AnnotationQuery aq = new AnnotationQuery();
    	aq.addQueryClause(AnnotationFieldEnumerator.NONE, keyword);
    	
    	List<IQueryInterface> qL = new LinkedList<IQueryInterface>();
		qL.add(cq);
		qL.add(sq);
		qL.add(persq);
		qL.add(pubq);
		qL.add(aq);
    	try {
    		results = QueryAdapter.executeMultipleQueriesForModels(qL);
    		    		
    		initialAggregateRanker = ResultSetUtil.collateModelResultSetByModelId(results);
    		List<List<VersionResultSet>> splitResults = RankAggregationUtil.splitModelResultSetByIndex(results);
    		results = RankAggregation.aggregate(splitResults, initialAggregateRanker, aggregationType, rankersWeights);
		} catch (Exception e) {
			logger.error(e.getMessage());
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
    	
    	if ((results!=null) && !results.isEmpty()) {
    		results = results.subList(0, Math.min(topn, results.size()));
    		return gson.toJson(results);
    		
    	}
    	else {
    		String[] s = {"#Results","0"};
    		
    		 return gson.toJson(s);

    	}
    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/aggregated_model_query" )
    public String aggregatedModelQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		//String s = "Retrieve models matching the provided keywords. The query is expanded to all indices. Results from different indices are aggregated according to the chosen aggregation";
		String[] s = {"keyword","aggregationType:["+RankAggregationType.Types.values()+"]"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
	
	@POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON ) 
    @Path( "/grouped_aggregated_model_query" )
    public String groupedAggregatedModelQuery( 	@Context GraphDatabaseService graphDbSevice,
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
    	
    	if (parameterMap==null){
    		String[] s = {"Exception","no parameters provided!"};
    		return gson.toJson(s);
    	} 		

    	String keyword = parameterMap.get("keyword");
    	if (StringUtils.isEmpty(keyword)){
    		String[] s = {"Exception","no keywords provided!"};
    		return gson.toJson(s);
    	} 
    	String topns = parameterMap.get("topn");
    	Integer topn = Integer.MAX_VALUE;
    	if (!StringUtils.isEmpty(topns) && StringUtils.isNumeric(topns)){
    		topn = Integer.valueOf(topns);
    	}   	
    	String aggregationTypeString = parameterMap.get("aggregationType");
    	RankAggregationType.Types aggregationType;
    	if (StringUtils.isEmpty(aggregationTypeString)){
    		aggregationType = Types.DEFAULT;  
    	} else aggregationType = RankAggregationType.stringToRankAggregationType(aggregationTypeString);
    	String rankersWeightsString = parameterMap.get("rankersWeights");
    	int rankersWeights = Integer.parseInt(rankersWeightsString);
    	
    	List<VersionResultSet> results = null;
    	List<VersionResultSet> initialAggregateRanker = null;
		List<ModelResultSet> groupedResults = new LinkedList<ModelResultSet>();
    	
       	CellMLModelQuery cq = new CellMLModelQuery();
    	cq.addQueryClause(CellMLModelFieldEnumerator.NONE, keyword);
    	SBMLModelQuery sq = new SBMLModelQuery();
    	sq.addQueryClause(SBMLModelFieldEnumerator.NONE, keyword);
    	PersonQuery persq = new PersonQuery();
    	persq.addQueryClause(PersonFieldEnumerator.NONE, keyword);
    	PublicationQuery pubq = new PublicationQuery();
    	pubq.addQueryClause(PublicationFieldEnumerator.NONE, keyword);
    	AnnotationQuery aq = new AnnotationQuery();
    	aq.addQueryClause(AnnotationFieldEnumerator.NONE, keyword);
    	
    	List<IQueryInterface> qL = new LinkedList<IQueryInterface>();
		qL.add(cq);
		qL.add(sq);
		qL.add(persq);
		qL.add(pubq);
		qL.add(aq);
    	try {
    		results = QueryAdapter.executeMultipleQueriesForModels(qL);
    		    		
    		initialAggregateRanker = ResultSetUtil.collateModelResultSetByModelId(results);
    		List<List<VersionResultSet>> splitResults = RankAggregationUtil.splitModelResultSetByIndex(results);
    		results = RankAggregation.aggregate(splitResults, initialAggregateRanker, aggregationType, rankersWeights);
    		groupedResults = GroupVersions.groupVersions(results);
		} catch (Exception e) {
			String[] s = {"Exception",e.getMessage()};			
			
            return gson.toJson(s); 
		}
    	
    	if ((groupedResults!=null) && !groupedResults.isEmpty()) {
    		groupedResults = groupedResults.subList(0, Math.min(topn, groupedResults.size()));
    		return gson.toJson(groupedResults);
    	}
    	else {
    		String[] s = {"#Results","0"};
    		
    		 return gson.toJson(s);

    	}
    }
    
	@GET
    @Produces( MediaType.APPLICATION_JSON ) 
	@Consumes(MediaType.TEXT_PLAIN) 
    @Path( "/grouped_aggregated_model_query" )
    public String groupedAggregatedModelQuery(@Context GraphDatabaseService graphDbSevice)
    {
		ManagerUtil.initManager(graphDbSevice); 
		//String s = "Retrieve models matching the provided keywords. The query is expanded to all indices. Results from different indices are aggregated according to the chosen aggregation";
		String[] s = {"keyword","aggregationType:["+RankAggregationType.Types.values()+"]"};
		Gson gson = new Gson();
		return gson.toJson(s);
    }
    
}
