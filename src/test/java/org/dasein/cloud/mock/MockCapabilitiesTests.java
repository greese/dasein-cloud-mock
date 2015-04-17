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

import org.dasein.cloud.VisibleScope;
import org.dasein.cloud.compute.ImageClass;
import org.dasein.cloud.compute.MachineImageFormat;
import org.dasein.cloud.compute.VmState;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Jeffrey Yan on 4/15/2015.
 *
 * @author Jeffrey Yan
 * @since 2015.05.1
 */
public class MockCapabilitiesTests {

    private ConfigurationManager configurationManager;
    @Before
    public void configure() {
        configurationManager = new ConfigurationManager();
    }

    @Test
    public void testLoadConfiguration() {
        MockTestCapabilities capabilities = new MockTestCapabilities();
        configurationManager
                .configure("capabilities/" + "org.dasein.cloud.mock.MockCapabilitiesTests.MockTestCapabilities.yaml",
                        capabilities);

        assertEquals(1, capabilities.enumBooleanMap.size());
        assertTrue(capabilities.enumBooleanMap.get(VmState.PAUSED));
        assertEquals(1, capabilities.enumStringMap.size());
        assertEquals("machine image", capabilities.enumStringMap.get(ImageClass.MACHINE));
        assertEquals(VisibleScope.ACCOUNT_GLOBAL, capabilities.enumValue);
        assertEquals(2, capabilities.enumList.size());
        assertEquals(MachineImageFormat.AWS, capabilities.enumList.get(0));
        assertTrue(capabilities.booleanValue);

        assertEquals(1, capabilities.hierarchyMap.size());
        assertEquals(1, capabilities.hierarchyMap.get(VmState.PAUSED).size());
        assertEquals(Boolean.TRUE, capabilities.hierarchyMap.get(VmState.PAUSED).get(ImageClass.MACHINE));
        assertEquals(2, capabilities.hierarchyList.size());
        assertEquals(Boolean.FALSE, capabilities.hierarchyList.get(1).get(ImageClass.RAMDISK));

        assertEquals("This is a String", capabilities.innerCapabilities.stringValue);
        assertEquals("stringValue", capabilities.innerCapabilities.stringStringMap.get("stringKey"));
        assertNull(capabilities.innerCapabilities2);
    }

    public static class MockTestCapabilities extends AbstractMockCapabilities {

        protected Map<VmState, Boolean> enumBooleanMap;
        protected Map<ImageClass, String> enumStringMap;
        protected VisibleScope enumValue;
        protected List<MachineImageFormat> enumList;
        protected boolean booleanValue;

        protected Map<VmState, Map<ImageClass, Boolean>> hierarchyMap;
        protected List<Map<ImageClass, Boolean>> hierarchyList;

        protected MockTestInnerCapabilities innerCapabilities;

        protected MockTestInnerCapabilities innerCapabilities2;

        public MockTestCapabilities() {
            super(null);
        }
    }

    public static class MockTestInnerCapabilities {
        protected String stringValue;
        protected Map<String, String> stringStringMap;
    }
}
