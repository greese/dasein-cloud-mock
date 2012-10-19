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
package org.dasein.cloud.mock.network;

import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.mock.network.firewall.MockFirewallSupport;
import org.dasein.cloud.mock.network.ip.MockIPSupport;
import org.dasein.cloud.network.AbstractNetworkServices;
import org.dasein.cloud.network.FirewallSupport;
import org.dasein.cloud.network.IpAddressSupport;

import javax.annotation.Nonnull;

/**
 * Implements mock network services for Dasein Cloud support.
 * <p>Created by George Reese: 10/18/12 3:00 PM</p>
 * @author George Reese
 * @version 2012.09 initial version
 * @since 2012.09
 */
public class MockNetworkServices extends AbstractNetworkServices {
    private CloudProvider provider;

    public MockNetworkServices() { }

    public MockNetworkServices(CloudProvider provider) { this.provider = provider; }

    @Override
    public @Nonnull FirewallSupport getFirewallSupport() {
        return new MockFirewallSupport(provider);
    }

    @Override
    public @Nonnull IpAddressSupport getIpAddressSupport() {
        return new MockIPSupport(provider);
    }
}
