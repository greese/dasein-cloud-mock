/**
 * Copyright (C) 2009-2012 enStratus Networks Inc.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.dasein.cloud.mock.compute;

import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.compute.AbstractComputeServices;
import org.dasein.cloud.compute.MachineImageSupport;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.mock.compute.image.MockImageSupport;
import org.dasein.cloud.mock.compute.vm.MockVMSupport;

import javax.annotation.Nonnull;

/**
 * Provides access into various mock compute services.
 * <p>Created by George Reese: 10/17/12 6:04 PM</p>
 * @author George Reese
 * @version 2012.09
 * @since 2012.09
 */
public class MockComputeServices extends AbstractComputeServices {
    private CloudProvider provider;

    public MockComputeServices() { }

    public MockComputeServices(CloudProvider provider) { this.provider = provider; }

    @Override
    public @Nonnull MachineImageSupport getImageSupport() {
        return new MockImageSupport(provider);
    }

    @Override
    public @Nonnull VirtualMachineSupport getVirtualMachineSupport() {
        return new MockVMSupport(provider);
    }
}
