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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dasein.cloud.InternalException;

/**
 * Created by Jeffrey Yan on 3/18/2015.
 * @author Jeffrey Yan
 * @since 2015.05.1
 */
public class MockCapabilitiesFactory {
    private MockCloud mockCloud;

    private Map<Class, AbstractMockCapabilities> capabilitieses;

    public MockCapabilitiesFactory(MockCloud mockCloud) {
        this.mockCloud = mockCloud;
        capabilitieses = new ConcurrentHashMap<Class, AbstractMockCapabilities>();
    }

    public <T extends AbstractMockCapabilities> T getCapabilities(Class<T> capabilitiesClz) throws InternalException {
        AbstractMockCapabilities capabilities = this.capabilitieses.get(capabilitiesClz);

        if (capabilities == null) {
            try {
                Constructor<T> constructor = capabilitiesClz.getConstructor(MockCloud.class);
                capabilities = constructor.newInstance(mockCloud);
                mockCloud.getConfigurationManager().configure("capabilities/" + capabilitiesClz.getName() + ".yaml", capabilities);
                this.capabilitieses.put(capabilitiesClz, capabilities);
            } catch (InstantiationException instantiationException) {
                throw new InternalException(instantiationException);
            } catch (IllegalAccessException illegalAccessException) {
                throw new InternalException(illegalAccessException);
            } catch (NoSuchMethodException noSuchMethodException) {
                throw new InternalException(noSuchMethodException);
            } catch (InvocationTargetException invocationTargetException) {
                throw new InternalException(invocationTargetException);
            }
        }
        //TODO, wrap to check lock before return
        return (T) capabilities;
    }
}
