package org.adsync4j.demo;

import java.io.*;
import java.util.Map;
import java.util.Properties;

class Utils {
    @SuppressWarnings("unchecked")
    static Map<String, String> loadPropertiesFile(String propertiesFileName) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesFileName));
        return (Map) properties;
    }

    static void printClassPathResource(String resourceName) {
        try(InputStream stream = ADSync.class.getResourceAsStream(resourceName)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String line = in.readLine();
            while (line != null) {
                System.out.println(line);
                line = in.readLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
