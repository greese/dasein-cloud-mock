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
package org.dasein.cloud.mock;

import com.google.inject.AbstractModule;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.dc.DataCenterServices;
import org.dasein.cloud.mock.compute.MockComputeServices;
import org.dasein.cloud.mock.network.MockNetworkServices;
import org.dasein.cloud.network.NetworkServices;

/**
 * Module for binding a complete mock cloud with all services supported.
 * <p>Created by George Reese: 8/23/12 7:47 PM</p>
 * @author George Reese
 * @version 2012.07
 * @since 2012.07
 */
public class MockCloudModule extends AbstractModule {
    @Override
    public void configure() {
        bind(DataCenterServices.class).to(MockDataCenterServices.class);
        bind(ComputeServices.class).to(MockComputeServices.class);
        bind(NetworkServices.class).to(MockNetworkServices.class);
        requestStaticInjection(MockCloud.class);
    }
}