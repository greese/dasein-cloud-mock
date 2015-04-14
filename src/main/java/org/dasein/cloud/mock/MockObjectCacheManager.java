package org.dasein.cloud.mock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class MockObjectCacheManager {
	
	private static Logger logger = Logger.getLogger(MockObjectCacheManager.class);
	
	private static Yaml yaml = new Yaml();
	private static String prefix = "target/cache/" + (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date()) + "/";
	
	public static String parseCloud (String endpoint) {
		if (endpoint.contains("://")) {
			return endpoint.substring(endpoint.lastIndexOf("://"), endpoint.length());
		} else {
			return endpoint;
		}
	}
	
	public void writeObjectToCache(String name, Object object) {
		writeObjectToCache(null, name, object);
	}

	public void writeObjectToCache(String cloud, String name,
			Object object) {
		writeObjectToCache(cloud, null, name, object);
	}

	public void writeObjectToCache(String cloud, String region,
			String name, Object object) {
		writeObjectToCache(cloud, region, null, name, object);
	}

	public void writeObjectToCache(String cloud, String region, String datacenter, String name, Object object) {
		
		File file = new File(genFilePath(cloud, region, datacenter, name));
		if (!file.isDirectory() && !file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		Writer writer = null;
		try {
			writer = new FileWriter(file);
			yaml.dump(object, writer);
		} catch (IOException e) {
			logger.error("Write object to file " + file.getAbsolutePath() + " failed!");
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				logger.warn("Close writer for file " + file.getAbsolutePath() + " failed!");
			}
		}
	}

	public Object readObjectFromCache(String cloud, String region,
			String datacenter, String name) {
		File file = new File(genFilePath(cloud, region, datacenter, name));
		Reader reader = null;
		try {
			if (file != null) {
				reader = new FileReader(file);
				return yaml.load(reader);
			}
		} catch (FileNotFoundException e) {
			logger.error("Read object cache file " + file.getAbsolutePath() + " failed!", e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				logger.warn("Close reader for file " + file.getAbsolutePath() + " failed!", e);
			}
		}
		return null;
	}

	public Object readObjectFromCache(String cloud, String region,
			String name) {
		return readObjectFromCache(cloud, region, null, name);
	}

	public Object readObjectFromCache(String cloud, String name) {
		return readObjectFromCache(cloud, null, name);
	}

	public Object readObjectFromCache(String name) {
		return readObjectFromCache(null, name);
	}

	private String genFilePath(String cloud, String region,
			String datacenter, String name) {
		String filePath = ""; 
		if (cloud != null && cloud.contains("/")) {
			cloud = cloud.substring(cloud.lastIndexOf("/"), cloud.length());
		}
		filePath = (cloud == null) ? filePath : filePath.concat(cloud);
		filePath = (region == null) ? filePath
				: ((filePath.length() == 0) ? filePath.concat(region)
						: filePath.concat("/").concat(region));
		filePath = (datacenter == null) ? filePath
				: ((filePath.length() == 0) ? filePath.concat(datacenter)
						: filePath.concat("/").concat(datacenter));
		filePath = (filePath.length() == 0) ? filePath.concat(name).concat(
				".yaml") : filePath.concat("/").concat(name).concat(".yaml");
		filePath = prefix.concat(filePath);
		return filePath;
	}
}
