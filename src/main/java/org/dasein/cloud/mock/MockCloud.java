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

import com.google.inject.Inject;
import org.dasein.cloud.AbstractCloud;
import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.dc.DataCenterServices;
import org.dasein.cloud.network.NetworkServices;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Injectable Mock Cloud provider for unit testing.
 * <p>Created by George Reese: 8/23/12 5:19 PM</p>
 * @author George Reese
 * @version 2012.07
 * @since 2012.07
 */
public class MockCloud extends AbstractCloud {
    @Inject
    static private ComputeServices computeServices;

    @Inject
    static private DataCenterServices dataCenterServices;

    @Inject
    static private NetworkServices networkServices;

    public MockCloud() { }

    @Override
    public @Nonnull ComputeServices getComputeServices() {
        try {
            try {
                Constructor<? extends ComputeServices> c = computeServices.getClass().getDeclaredConstructor(CloudProvider.class);

                return c.newInstance(this);
            }
            catch( NoSuchMethodException e ) {
                return computeServices.getClass().newInstance();
            }
        }
        catch( InvocationTargetException e ) {
            throw new RuntimeException("Unable to construct compute services: " + e.getMessage());
        }
        catch( InstantiationException e ) {
            throw new RuntimeException("Unable to construct compute services: " + e.getMessage());
        }
        catch( IllegalAccessException e ) {
            throw new RuntimeException("Unable to construct compute services: " + e.getMessage());
        }
    }

    @Override
    public @Nonnull DataCenterServices getDataCenterServices() {
        try {
            try {
                Constructor<? extends DataCenterServices> c = dataCenterServices.getClass().getDeclaredConstructor(CloudProvider.class);

                return c.newInstance(this);
            }
            catch( NoSuchMethodException e ) {
                return dataCenterServices.getClass().newInstance();
            }
        }
        catch( InvocationTargetException e ) {
            throw new RuntimeException("Unable to construct data center services: " + e.getMessage());
        }
        catch( InstantiationException e ) {
            throw new RuntimeException("Unable to construct data center services: " + e.getMessage());
        }
        catch( IllegalAccessException e ) {
            throw new RuntimeException("Unable to construct data center services: " + e.getMessage());
        }
    }

    @Override
    public @Nonnull NetworkServices getNetworkServices() {
        try {
            try {
                Constructor<? extends NetworkServices> c = networkServices.getClass().getDeclaredConstructor(CloudProvider.class);

                return c.newInstance(this);
            }
            catch( NoSuchMethodException e ) {
                return networkServices.getClass().newInstance();
            }
        }
        catch( InvocationTargetException e ) {
            throw new RuntimeException("Unable to construct network services: " + e.getMessage());
        }
        catch( InstantiationException e ) {
            throw new RuntimeException("Unable to construct network services: " + e.getMessage());
        }
        catch( IllegalAccessException e ) {
            throw new RuntimeException("Unable to construct network services: " + e.getMessage());
        }
    }

    @Override
    public @Nonnull String getCloudName() {
        ProviderContext ctx = getContext();
        String name = (ctx == null ? null : ctx.getCloudName());

        return (name == null ? "Mock Cloud" : name);
    }

    @Override
    public @Nonnull String getProviderName() {
        ProviderContext ctx = getContext();
        String name = (ctx == null ? null : ctx.getProviderName());

        return (name == null ? "Dasein" : name);
    }

    @Override
    public String testContext() {
        ProviderContext ctx = getContext();

        if( ctx == null ) {
            return null;
        }
        try {
            String access = new String(ctx.getAccessPublic(), "utf-8");
            String secret = new String(ctx.getAccessPrivate(), "utf-8");

            if( !access.equals("6789") ) {
                return null;
            }
            if( !secret.equals("abcdefghijkl") ) {
                return null;
            }
            return "12345";
        }
        catch( UnsupportedEncodingException e ) {
            e.printStackTrace();
            return null;
        }
    }
}
