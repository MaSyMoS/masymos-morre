package de.unirostock.morre.server.plugin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;




import org.kohsuke.MetaInfServices;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
import org.apache.log4j.Logger;

import de.unirostock.morre.server.util.ManagerUtil;
import de.unirostock.sems.masymos.database.Manager;
import de.unirostock.sems.masymos.query.types.AnnotationQuery;
import de.unirostock.sems.masymos.query.types.CellMLModelQuery;
import de.unirostock.sems.masymos.query.types.PersonQuery;
import de.unirostock.sems.masymos.query.types.PublicationQuery;
import de.unirostock.sems.masymos.query.types.SBMLModelQuery;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
//@Path("/diagnose")
@MetaInfServices( ServerPlugin.class )
@Description( "An extension to the Neo4j Server to test if model API is alive" )
public class Diagnose extends ServerPlugin{
	
	final  Logger logger = Logger.getLogger(Diagnose.class);
	
	
    @Name( "is_model_manager_alive" )
    @Description( "Get true if manager is alive else false" )
    @PluginTarget( GraphDatabaseService.class )
//    @GET
//    @Produces( MediaType.APPLICATION_JSON )
//    @Path( "/is_model_manager_alive" )
    public Boolean isModelManagerAlive( @Source GraphDatabaseService graphDbSevice )
    {
    	try {
			ManagerUtil.initManager(graphDbSevice);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
    	return true;
    }
    
    
    @Name( "list_index_available" )
    @Description( "Get a list of available indexes" )
    @PluginTarget( GraphDatabaseService.class )
    public List<String> listIndexAvailable( @Source GraphDatabaseService graphDbSevice )
    {
    	ManagerUtil.initManager(graphDbSevice);
    	List<String> l = new LinkedList<String>();
    	l.addAll(Manager.instance().getNodeIndexMap().keySet()); 
    	return l;
    	   	
    }
 
//    @Name( "is_database_empty" )
//    @Description( "True if only the reference node is present" )
//    @PluginTarget( GraphDatabaseService.class )
//    public Boolean isDatabaseEmpty( @Source GraphDatabaseService graphDbSevice )
//    {    	
//        return !graphDbSevice.getReferenceNode().getRelationships().iterator().hasNext();
//    }
    
    @Name( "cellml_model_query_features" )
    @Description( "Returns a set of all supported features to query CellMl models" )
    @PluginTarget( GraphDatabaseService.class )
    public List<String> cellmlModelQueryFeatrues( @Source GraphDatabaseService graphDbSevice )
    {    
    	ManagerUtil.initManager(graphDbSevice);    	
        return Arrays.asList(new CellMLModelQuery().getIndexedFields()); 
    }
    
    @Name( "sbml_model_query_features" )
    @Description( "Returns a set of all supported features to query SBML models" )
    @PluginTarget( GraphDatabaseService.class )
    public List<String> sbmlModelQueryFeatrues( @Source GraphDatabaseService graphDbSevice )
    {    
    	ManagerUtil.initManager(graphDbSevice);
    	return Arrays.asList(new SBMLModelQuery().getIndexedFields()); 
    }
    
    @Name( "annotation_query_features" )
    @Description( "Returns a set of all supported features to query model annotation" )
    @PluginTarget( GraphDatabaseService.class )
    public List<String> annotationQueryFeatrues( @Source GraphDatabaseService graphDbSevice )
    {    
    	ManagerUtil.initManager(graphDbSevice);      
        return Arrays.asList(new AnnotationQuery().getIndexedFields()); 
    }
    
    @Name( "person_query_features" )
    @Description( "Returns a set of all supported features to query persons related to a model" )
    @PluginTarget( GraphDatabaseService.class )
    public List<String> personQueryFeatrues( @Source GraphDatabaseService graphDbSevice )
    {    
    	ManagerUtil.initManager(graphDbSevice);
        return Arrays.asList(new PersonQuery().getIndexedFields()); 
    }
    
    @Name( "publication_query_features" )
    @Description( "Returns a set of all supported features to query publications related to a model" )
    @PluginTarget( GraphDatabaseService.class )
    public List<String> publicationQueryFeatrues( @Source GraphDatabaseService graphDbSevice )
    {    
    	ManagerUtil.initManager(graphDbSevice);
        return Arrays.asList(new PublicationQuery().getIndexedFields()); 
    }
}