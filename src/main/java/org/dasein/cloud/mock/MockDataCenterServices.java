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

import org.dasein.cloud.CloudException;
import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.dc.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Implements mock data center services for a generic cloud.
 * <p>Created by George Reese: 8/23/12 7:26 PM</p>
 * @author George Reese
 * @version 2012.07
 * @since 2012.07
 */
public class MockDataCenterServices extends AbstractDataCenterServices<MockCloud> implements DataCenterServices {
    static private Map<String,List<DataCenter>> dataCenters;
    static private List<Region> regions;

    public MockDataCenterServices(MockCloud provider) {
        super(provider);

        if( regions == null ) {
            ArrayList<Region> list = new ArrayList<Region>();
            Region region;

            region = new Region();
            region.setActive(true);
            region.setAvailable(true);
            region.setJurisdiction("US");
            region.setName("US/MN");
            region.setProviderRegionId("usmn");
            list.add(region);

            region = new Region();
            region.setActive(true);
            region.setAvailable(true);
            region.setJurisdiction("US");
            region.setName("US/ME");
            region.setProviderRegionId("usme");
            list.add(region);

            region = new Region();
            region.setActive(true);
            region.setAvailable(false);
            region.setJurisdiction("EU");
            region.setName("GB/SCT");
            region.setProviderRegionId("gbsct");
            list.add(region);

            region = new Region();
            region.setActive(false);
            region.setAvailable(false);
            region.setJurisdiction("NZ");
            region.setName("NZ/AKL");
            region.setProviderRegionId("nzakl");
            list.add(region);

            if( regions == null ) {
                regions = Collections.unmodifiableList(list);
            }
        }
        if( dataCenters == null ) {
            HashMap<String,List<DataCenter>> map = new HashMap<String, List<DataCenter>>();

            for( Region region : regions ) {
                ArrayList<DataCenter> list = new ArrayList<DataCenter>();
                DataCenter dc;

                dc = new DataCenter();
                dc.setActive(region.isActive());
                dc.setAvailable(region.isAvailable());
                dc.setName(region.getName() + " (Zone 1)");
                dc.setProviderDataCenterId(region.getProviderRegionId() + "-1");
                dc.setRegionId(region.getProviderRegionId());
                list.add(dc);

                dc = new DataCenter();
                dc.setActive(region.isActive());
                dc.setAvailable(false);
                dc.setName(region.getName() + " (Zone 2)");
                dc.setProviderDataCenterId(region.getProviderRegionId() + "-2");
                dc.setRegionId(region.getProviderRegionId());
                list.add(dc);

                dc = new DataCenter();
                dc.setActive(false);
                dc.setAvailable(false);
                dc.setName(region.getName() + " (Zone 3)");
                dc.setProviderDataCenterId(region.getProviderRegionId() + "-3");
                dc.setRegionId(region.getProviderRegionId());
                list.add(dc);

                map.put(region.getProviderRegionId(), Collections.unmodifiableList(list));
            }
            if( dataCenters == null ) {
                dataCenters = map;
            }
        }
    }

    @Override
    public @Nullable DataCenter getDataCenter(@Nonnull String providerDataCenterId) throws InternalException, CloudException {
        for( Region region : listRegions() ) {
            for( DataCenter dc : listDataCenters(region.getProviderRegionId()) ) {
                if( providerDataCenterId.equals(dc.getProviderDataCenterId()) ) {
                    return dc;
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable Region getRegion(@Nonnull String providerRegionId) throws InternalException, CloudException {
        for( Region region : listRegions() ) {
            if( providerRegionId.equals(region.getProviderRegionId()) ) {
                return region;
            }
        }
        return null;
    }

    @Override
    public @Nonnull Collection<DataCenter> listDataCenters(@Nonnull String providerRegionId) throws InternalException, CloudException {
        if( dataCenters.containsKey(providerRegionId) ) {
            return dataCenters.get(providerRegionId);
        }
        return Collections.emptyList();
    }

    @Override
    public @Nonnull Collection<Region> listRegions() throws InternalException, CloudException {
        return regions;
    }

    @Nonnull
    @Override
    public DataCenterCapabilities getCapabilities() throws InternalException, CloudException {
        return new MockDataCenterCapabilities(getProvider());
    }
}
