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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

public class MockObjectCacheManager {
	
	private static Logger logger = Logger.getLogger(MockObjectCacheManager.class);
	
	private static byte[] lock = new byte[0];
	
	private static Yaml yaml = new Yaml();
	private static String prefix = "target/cache/" + (new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")).format(new Date()) + "/";
	private static Map<String, byte[]> lockMap = new HashMap<String, byte[]>();
	
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
	
	public byte[] getLock(String key) {
		byte[] flock = lockMap.get(key);
		if (flock == null) {
			lockMap.put(key, new byte[0]);
		}
		return lockMap.get(key);
	}

	public void writeObjectToCache(String cloud, String region, String datacenter, String name, Object object) {
		
		String fname = genFilePath(cloud, region, datacenter, name);
		File file = new File(fname);
		if (!file.isDirectory() && !file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		Writer writer = null;
		synchronized (getLock(fname)) {
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
	}

	public Object readObjectFromCache(String cloud, String region,
			String datacenter, String name) {
		String fname = genFilePath(cloud, region, datacenter, name);
		File file = new File(fname);
		Reader reader = null;
		synchronized (getLock(fname)) {
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
