/**
 * ========= CONFIDENTIAL =========
 *
 * Copyright (C) 2012 enStratus Networks Inc - ALL RIGHTS RESERVED
 *
 * ====================================================================
 *  NOTICE: All information contained herein is, and remains the
 *  property of enStratus Networks Inc. The intellectual and technical
 *  concepts contained herein are proprietary to enStratus Networks Inc
 *  and may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law. Dissemination
 *  of this information or reproduction of this material is strictly
 *  forbidden unless prior written permission is obtained from
 *  enStratus Networks Inc.
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
