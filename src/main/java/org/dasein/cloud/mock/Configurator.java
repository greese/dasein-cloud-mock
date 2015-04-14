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
public class Configurator {
    private MockCloud mockCloud;

    private Map<String, Configuration> configurations;

    public Configurator(MockCloud mockCloud) {
        this.mockCloud = mockCloud;
        configurations = new HashMap<String, Configuration>();
    }

    public void configure(String name, Configurable configurable, String path) {
        Configuration configuration = configurations.get(name);
        if (configuration == null) {
            configuration = newConfiguration(name, path, configurable);
            configurations.put(name, configuration);
        }
    }

    private Configuration newConfiguration(String name, String path, Configurable configurable) {
        String configurationPath = (String) mockCloud.getContext().getConfigurationValue("configurationPath"); //TODO, change to Field type
        if (configurationPath != null) {
            File configurationDir = new File(configurationPath);
            File configurationFie = new File(configurationDir, path);
            if (configurationFie.exists()) {
                return new Configuration(name, configurationFie.getAbsolutePath(), configurable);
            }
        }

        return new Configuration(name, this.getClass().getClassLoader().getResourceAsStream(path), configurable);
    }

    private class Configuration {

        private String name;

        //either configFilePath or configInputStream must be set
        //configFilePath has higher priority than configInputStream
        private String sourceFilePath;
        private InputStream sourceInputStream;

        private Configurable configurable;

        private String content;
        private Date refreshTime;

        public Configuration(@Nonnull String name, @Nonnull String configFilePath, @Nonnull Configurable configurable) {
            this.name = name;
            this.sourceFilePath = configFilePath;
            this.configurable = configurable;
            refresh();
        }

        public Configuration(@Nonnull String name, @Nonnull InputStream configInputStream, @Nonnull Configurable configurable) {
            this.name = name;
            this.sourceInputStream = configInputStream;
            this.configurable = configurable;
            refresh();
        }

        private void refresh() {
            if (sourceFilePath != null) {
                content = readFileAsString(sourceFilePath);
            } else {
                content = readStreamAsString(sourceInputStream);
            }
            configurable.configure(content);

            refreshTime = new Date();
        }

        private String readFileAsString(String filePath)  {
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
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }


    }

}
