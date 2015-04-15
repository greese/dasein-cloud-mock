/**
 * Copyright (C) 2009-2015 Dell, Inc.
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
package org.dasein.cloud.mock.network.firewall;

import org.dasein.cloud.*;
import org.dasein.cloud.mock.MockCloud;
import org.dasein.cloud.mock.AbstractMockCapabilities;
import org.dasein.cloud.network.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * @author Colin Ke.
 * @since 2015.05.1
 */
public class MockFirewallCapabilities extends AbstractMockCapabilities implements FirewallCapabilities {

    private String providerTermForFirewall;
    private VisibleScope firewallVisibleScope;
    private Map<Boolean, Requirement> precedenceRequirement;
    private boolean isZeroPrecedenceHighest;
    private Map<Boolean, Iterable<RuleTargetType>> supportedDestinationTypes;
    private Map<Boolean, Iterable<Direction>> supportedDirections;
    private Map<Boolean, Iterable<Permission>> supportedPermissions;
    private Map<Boolean, Iterable<Protocol>> supportedProtocols;
    private Map<Boolean, Iterable<RuleTargetType>> supportedSourceTypes;
    private boolean requiresRulesOnCreation;
    private Requirement requiresVLAN;
    private Map<Boolean, Boolean> supportsFirewallCreation;
    private boolean supportsFirewallDeletion;

    public MockFirewallCapabilities(@Nonnull MockCloud provider) {
        super(provider);
    }

    @Nonnull
    @Override
    public FirewallConstraints getFirewallConstraintsForCloud() throws InternalException, CloudException {
        //TODO
        return FirewallConstraints.getInstance();
    }

    @Nonnull
    @Override
    public String getProviderTermForFirewall(@Nonnull Locale locale) {
        return providerTermForFirewall;
    }

    @Nullable
    @Override
    public VisibleScope getFirewallVisibleScope() {
        return firewallVisibleScope;
    }

    @Nonnull
    @Override
    public Requirement identifyPrecedenceRequirement(boolean inVlan) throws InternalException, CloudException {
        if(null != precedenceRequirement)
            return precedenceRequirement.get(inVlan);
        return null;
    }

    @Override
    public boolean isZeroPrecedenceHighest() throws InternalException, CloudException {
        return isZeroPrecedenceHighest;
    }

    @Nonnull
    @Override
    public Iterable<RuleTargetType> listSupportedDestinationTypes(boolean inVlan) throws InternalException, CloudException {
        if(null != supportedDestinationTypes)
            return supportedDestinationTypes.get(inVlan);
        return null;
    }

    @Nonnull
    @Override
    public Iterable<Direction> listSupportedDirections(boolean inVlan) throws InternalException, CloudException {
        if(null != supportedDirections)
            return supportedDirections.get(inVlan);
        return null;
    }

    @Nonnull
    @Override
    public Iterable<Permission> listSupportedPermissions(boolean inVlan) throws InternalException, CloudException {
        if(null != supportedPermissions)
            return supportedPermissions.get(inVlan);
        return null;
    }

    @Nonnull
    @Override
    public Iterable<Protocol> listSupportedProtocols(boolean inVlan) throws InternalException, CloudException {
        if(null != supportedProtocols)
            return supportedProtocols.get(inVlan);
        return null;
    }

    @Nonnull
    @Override
    public Iterable<RuleTargetType> listSupportedSourceTypes(boolean inVlan) throws InternalException, CloudException {
        if(null != supportedSourceTypes)
            return supportedSourceTypes.get(inVlan);
        return null;
    }

    @Override
    public boolean requiresRulesOnCreation() throws CloudException, InternalException {
        return requiresRulesOnCreation;
    }

    @Nonnull
    @Override
    public Requirement requiresVLAN() throws CloudException, InternalException {
        return requiresVLAN;
    }

    @Override
    public boolean supportsRules(@Nonnull Direction direction, @Nonnull Permission permission, boolean inVlan) throws CloudException, InternalException {
        //TODO
        return (!inVlan && permission.equals(Permission.ALLOW));
    }

    @Override
    public boolean supportsFirewallCreation(boolean inVlan) throws CloudException, InternalException {
        if(null != supportsFirewallCreation)
            return supportsFirewallCreation.get(inVlan);
        return false;
    }

    @Override
    public boolean supportsFirewallDeletion() throws CloudException, InternalException {
        return supportsFirewallDeletion;
    }

    @Override
    public Iterable<RuleTargetType> listSupportedDestinationTypes(boolean inVlan, Direction direction)
            throws InternalException, CloudException {
        //TODO
        return Collections.emptyList();
    }

    @Override
    public Iterable<RuleTargetType> listSupportedSourceTypes(boolean inVlan, Direction direction)
            throws InternalException, CloudException {
        //TODO
        return Collections.emptyList();
    }
}
