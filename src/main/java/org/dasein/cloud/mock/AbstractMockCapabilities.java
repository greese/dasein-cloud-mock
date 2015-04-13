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

import org.dasein.cloud.AbstractCapabilities;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Jeffrey Yan on 3/19/2015.
 *
 * @author Jeffrey Yan
 * @since 2015.05.1
 */
public abstract class AbstractMockCapabilities extends AbstractCapabilities<MockCloud> implements Configurable {

    protected ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public AbstractMockCapabilities(@Nonnull MockCloud provider) {
        super(provider);
    }

    public void configure(String content) {
        try {
            readWriteLock.writeLock().lockInterruptibly();
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            return;
        }
        //write locked
        try {
            Yaml yaml = new Yaml();
            List<Map<String, ?>> configYaml = (List<Map<String, ?>>) yaml.load(content);

            for (Map<String, ?> configs : configYaml) {
                for (Map.Entry<String, ?> configEntry : configs.entrySet()) {
                    Field field = ReflectionUtils.getField(this.getClass(), configEntry.getKey());
                    ReflectionUtils.setField(this, field, configEntry.getValue());
                }
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}
