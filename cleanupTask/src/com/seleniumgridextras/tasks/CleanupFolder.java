/**
 * This task help user to cleanup a specific folder on grid node.
 */

package com.seleniumgridextras.tasks;

import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.groupon.seleniumgridextras.tasks.ExecuteOSTask;
import com.groupon.seleniumgridextras.tasks.config.TaskDescriptions;
import com.groupon.seleniumgridextras.utilities.json.JsonCodec;
import com.groupon.seleniumgridextras.utilities.json.JsonResponseBuilder;

public class CleanupFolder extends ExecuteOSTask {
	public static final String CLEANUPFOLDER = "/cleanupfolder";
	public static final String CLEANUPFOLDER_DESC = "cleansup specified folder if exists";
	public static final String PATH = "path";
	public static final String CLEANUPFOLDER_BUTTONTEXT = "cleanupfolder";

	private static Logger logger = Logger.getLogger(CleanupFolder.class);

	public CleanupFolder() {
		setEndpoint(CleanupFolder.CLEANUPFOLDER);
		setDescription(CleanupFolder.CLEANUPFOLDER_DESC);
		JsonObject params = new JsonObject();
		params.addProperty(CleanupFolder.PATH, "Path of folder to clean");
		setAcceptedParams(params);
		setRequestType("GET");
		setResponseType("json");
		setClassname(this.getClass().getCanonicalName().toString());
		setCssClass(TaskDescriptions.UI.BTN_DANGER);
		setButtonText(CleanupFolder.CLEANUPFOLDER_BUTTONTEXT);
		setEnabledInGui(true);

	}

	

	@Override
	public JsonObject execute() {

		getJsonResponse().addKeyValues(JsonCodec.ERROR, "path is a required parameter");
		return getJsonResponse().getJson();
	}

	@Override
	public JsonObject execute(String parameter) {

		JsonObject response = new JsonResponseBuilder().getJson();
		deleteFilesUnderFolder(parameter);
		return response;
	}

	private void deleteFilesUnderFolder(String path) {
		logger.info("cleaningup " + path);
		File parent = new File(path);
		if (parent.exists() && parent.isDirectory()) {
			// get all children and recursively delete it
			File[] childrens = parent.listFiles();
			for (File child : childrens) {
				deleteFilesRecursive(child);
			}
		}

	}

	private void deleteFilesRecursive(File fileName) {
		try{
		if (fileName.isDirectory()) {
			// delete childs recursively
			File[] childrens = fileName.listFiles();
			for (File child : childrens) {
				deleteFilesRecursive(child);
			}
			// delete folder
			fileName.delete();
		} else {
			fileName.delete();
		}
		}catch (Exception e)
		{
			logger.error(e.getMessage());
		}
		
	}

	@Override
	public JsonObject execute(Map<String, String> parameter) {
		if (parameter.isEmpty() || !parameter.containsKey(CleanupFolder.PATH)) {

			return execute();
		} else {
			String folderToCleanup = String.valueOf(parameter.get(CleanupFolder.PATH));
			return execute(folderToCleanup);
		}
	}

	

}