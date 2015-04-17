/*
 * *
 *  * Copyright (C) 2009-2015 Dell, Inc.
 *  * See annotations for authorship information
 *  *
 *  * ====================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  * ====================================================================
 *
 */

package org.dasein.cloud.mock;

import org.dasein.cloud.Capabilities;
import sun.security.krb5.Config;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jeffrey Yan on 3/23/2015.
 *
 * @author Jeffrey Yan
 * @since 2015.05.1
 */
public class ConfigurationManager {
    private String configurationPath;

    public ConfigurationManager() {
        this.configurationPath = null;
    }

    public ConfigurationManager(String configurationPath) {
        this.configurationPath = configurationPath;
    }

    public void configure(String path, Configurable configurable) {
        String content = readContent(path);
        configurable.configure(content);
    }

    private String readContent(String path) {
        if (configurationPath != null) {
            File configurationDir = new File(configurationPath);
            File configurationFile = new File(configurationDir, path);
            if (configurationFile.exists()) {
                return readFileAsString(configurationFile.getAbsolutePath());
            }
        }
        return readStreamAsString(this.getClass().getClassLoader().getResourceAsStream(path));
    }

    private String readFileAsString(String filePath) {
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            return readStreamAsString(bufferedInputStream);
        } catch (FileNotFoundException fileNotFoundException) {
            throw new IllegalArgumentException(fileNotFoundException);
        }
    }

    private String readStreamAsString(InputStream inputStream) {
        BufferedReader reader = null;
        try {
            StringBuffer bufferData = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            char[] buf = new char[2048];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                bufferData.append(readData);
            }

            return bufferData.toString();
        } catch (IOException ioException) {
            throw new IllegalArgumentException(ioException);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
