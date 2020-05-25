package de.unirostock.morre.server.util;

import org.neo4j.graphdb.GraphDatabaseService;

import de.unirostock.sems.masymos.configuration.Config;
import de.unirostock.sems.masymos.database.Manager;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/
public class ManagerUtil {
	
	public static void initManager(GraphDatabaseService graphDbSevice){
		if (!Config.instance().isWebSeverInstance()){
    		Config.instance().setDb(graphDbSevice);
    		Manager.instance();
    	}   
	}

}
