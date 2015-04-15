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

import junit.framework.Assert;
import org.dasein.cloud.VisibleScope;
import org.dasein.cloud.compute.ImageClass;
import org.dasein.cloud.compute.MachineImageFormat;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.mock.compute.vm.MockVMCapabilities;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

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

        Assert.assertEquals(1, capabilities.enumBooleanMap.size());
        Assert.assertTrue(capabilities.enumBooleanMap.get(VmState.PAUSED));
        Assert.assertEquals(1, capabilities.enumStringMap.size());
        Assert.assertEquals("machine image", capabilities.enumStringMap.get(ImageClass.MACHINE));
        Assert.assertEquals(VisibleScope.ACCOUNT_GLOBAL, capabilities.enumSingle);
        Assert.assertEquals(2, capabilities.enumList.size());
        Assert.assertEquals(MachineImageFormat.AWS, capabilities.enumList.get(0));
        Assert.assertTrue(capabilities.booleanSingle);
    }

    public class MockTestCapabilities extends AbstractMockCapabilities {

        protected Map<VmState, Boolean> enumBooleanMap;
        protected Map<ImageClass, String> enumStringMap;
        protected VisibleScope enumSingle;
        protected List<MachineImageFormat> enumList;
        protected boolean booleanSingle;

        public MockTestCapabilities() {
            super(null);
        }
    }
}
