package heartnews.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertyManager {

	public static String get(String fileName, String key) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(fileName));
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return props.getProperty(key);
	}

}
