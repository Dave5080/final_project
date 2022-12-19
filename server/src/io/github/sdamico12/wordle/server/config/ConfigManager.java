package io.github.sdamico12.wordle.server.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class ConfigManager {

	private static Properties defaultProp = null;

	private static Properties getDefaultProp(){
		if(defaultProp != null) return defaultProp;
		defaultProp = new Properties();
		for(CEntries e : CEntries.values())
			defaultProp.setProperty(e.getName(), e.getDef());
		return defaultProp;
	}

	private static ConfigManager instance = null;

	public static ConfigManager getManager() throws IOException {
		return instance == null? (instance = new ConfigManager()) : instance;
	}

	private ConfigManager() throws IOException {
		propFile = new File(propPath);
		properties = new Properties();
		defaultIf();
	}

	public static void setConfigPath(String path){
		propPath = path;
	}

	private static String propPath = "server.properties";

	private Properties properties;
	private final File propFile;

	public Properties load() throws IOException {
		FileInputStream fin = new FileInputStream(propFile);
		properties.load(fin);
		for(CEntries c : CEntries.values())
			if(!c.checkConfigCondition(this))
				throw new InvalidPropertiesFormatException(c.getName());
		return properties;
	}

	public void store() throws IOException {
		FileOutputStream fout = new FileOutputStream(propFile);
		properties.store(fout, null);
	}

	public String getVal(CEntries e){
		return properties.getProperty(e.getName(), e.getDef());
	}

	public int getIntVal(CEntries e){
		return Integer.parseInt(getVal(e));
	}

	private void defaultIf() throws IOException {
		if(!propFile.exists()){
			propFile.createNewFile();
			properties = getDefaultProp();
			store();
		} else
			load();
	}
}
