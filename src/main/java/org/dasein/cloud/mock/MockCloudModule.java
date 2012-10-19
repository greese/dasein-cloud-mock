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