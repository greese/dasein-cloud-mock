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

package org.dasein.cloud.mock.network.firewall;

import org.dasein.cloud.*;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.mock.MockCloud;
import org.dasein.cloud.network.*;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.*;

/**
 * Implements bi-directional mock firewall support.
 * <p>Created by George Reese: 10/19/12 12:30 PM</p>
 * @author George Reese
 * @version 2012.09 initial version
 * @since 2012.09
 */
public class MockFirewallSupport extends AbstractFirewallSupport<MockCloud> implements FirewallSupport {
    static private final Map<String,Map<String,Map<String,Collection<Firewall>>>> firewalls = new HashMap<String, Map<String, Map<String, Collection<Firewall>>>>();
    static private final Map<String,Collection<FirewallRule>>                     rules     = new HashMap<String, Collection<FirewallRule>>();
    static private final Map<String,Collection<String>>                           vmMap     = new HashMap<String, Collection<String>>();

    @SuppressWarnings("ConstantConditions")
    static private @Nonnull Firewall copy(@Nonnull Firewall fw) {
        Firewall copy = new Firewall();

        copy.setActive(fw.isActive());
        copy.setAvailable(fw.isAvailable());
        copy.setDescription(fw.getDescription());
        copy.setName(fw.getName());
        copy.setProviderFirewallId(fw.getProviderFirewallId());
        copy.setProviderVlanId(fw.getProviderVlanId());
        copy.setRegionId(fw.getRegionId());
        return copy;
    }

    static public void vmTerminated(@Nonnull String vmId) {
        synchronized( firewalls ) {
            vmMap.remove(vmId);
        }
    }

    static public @Nonnull Collection<String> getFirewallsForVM(@Nonnull String vmId) {
        Collection<String> ids;

        synchronized( firewalls ) {
            ids = vmMap.get(vmId);
        }
        if( ids == null ) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(ids);
    }

    static public void saveFirewallsForVM(@Nonnull CloudProvider provider, @Nonnull String vmId, @Nonnull String ... firewallIds) throws CloudException, InternalException {
        NetworkServices network = provider.getNetworkServices();

        if( network == null ) {
            throw new CloudException("No firewall services supported in this cloud");
        }
        FirewallSupport support = network.getFirewallSupport();

        if( support == null ) {
            throw new CloudException("No firewall services supported in this cloud");
        }
        if( support instanceof MockFirewallSupport ) {
            ArrayList<String> flist= new ArrayList<String>();

            for( String id : firewallIds ) {
                Firewall fw = support.getFirewall(id);

                if( fw == null ) {
                    throw new CloudException("No such firewall: " + id);
                }
                flist.add(id);
            }
            synchronized( firewalls ) {
                vmMap.put(vmId, flist);
            }
        }
    }

    /*
    static private @Nonnull String toRuleId(@Nonnull FirewallRule rule) {
        return (rule.getFirewallId() + "_:_" + rule.getPermission().toString() + "_:_" + rule.getDirection().toString() + "_:_" + rule.getPermission().toString() + "_:_" + rule.getSourceEndpoint() + "_:_" + rule.getDestinationEndpoint() + "_:_" + rule.getStartPort() + ":" + rule.getEndPort());
    }
    */


    public MockFirewallSupport(MockCloud provider) {
        super(provider);
        capabilities = new MockFirewallCapabilities(provider);
    }


    @Override
    public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull Permission permission, @Nonnull RuleTarget sourceEndpoint, @Nonnull Protocol protocol, @Nonnull RuleTarget destinationEndpoint, int beginPort, int endPort, @Nonnegative int precedence) throws CloudException, InternalException {
        if( getFirewall(firewallId) == null ) {
            throw new CloudException("No such firewall: " + firewallId);
        }

        FirewallRule rule = FirewallRule.getInstance(null, firewallId, sourceEndpoint, direction, protocol, permission, destinationEndpoint, beginPort, endPort);

        synchronized( firewalls ) {
            Collection<FirewallRule> list = rules.get(firewallId);

            if( list == null ) {
                list = new ArrayList<FirewallRule>();
                rules.put(firewallId, list);
            } else {
                for(FirewallRule r : list){
                    if(r.getProviderRuleId().equals(rule.getProviderRuleId()))
                        throw new CloudException("trying to add a duplicated rule");
                }

            }

            list.add(rule);
        }
        return rule.getProviderRuleId();
    }

    @Override
    public @Nonnull String create(@Nonnull FirewallCreateOptions options) throws InternalException, CloudException {
        ProviderContext ctx = getProvider().getContext();


        if( ctx == null ) {
            throw new CloudException("No context was specified for this request");
        }

        if(!getCapabilities().supportsFirewallCreation(options.getProviderVlanId() != null)){
            String msg = "";
            if(options.getProviderVlanId() != null)
                msg = "in vlan";
            else
                msg = "not in vlan";
            throw new OperationNotSupportedException("not supported firewall creation " + msg);
        }

        String regionId = ctx.getRegionId();

        if( regionId == null ) {
            throw new CloudException("No region was specified for this request");
        }
        Firewall fw = new Firewall();
        String id = UUID.randomUUID().toString();
        String description = options.getDescription();
        String name = options.getName();
        description = description == null ? "" : description;
        name = name == null ? "MockedFireWall" : name;

        fw.setActive(true);
        fw.setAvailable(true);
        fw.setDescription(description);
        fw.setName(name);
        fw.setProviderFirewallId(id);
        fw.setRegionId(regionId);

        synchronized( firewalls ) {
            Map<String,Map<String,Collection<Firewall>>> cloud = firewalls.get(ctx.getCloud().getEndpoint());

            if( cloud == null ) {
                cloud = new HashMap<String, Map<String, Collection<Firewall>>>();
                firewalls.put(ctx.getCloud().getEndpoint(), cloud);
            }
            Map<String,Collection<Firewall>> region = cloud.get(regionId);

            if( region == null ) {
                region = new HashMap<String, Collection<Firewall>>();
                cloud.put(regionId, region);
            }
            Collection<Firewall> account = region.get(ctx.getAccountNumber());

            if( account == null ) {
                account = new ArrayList<Firewall>();
                region.put(ctx.getAccountNumber(), account);
            }

            account.add(fw);
        }

        if(null != options.getInitialRules()){
            for(FirewallRuleCreateOptions option : options.getInitialRules()){
                option.build(getProvider(), id);
            }
        }
        //noinspection ConstantConditions
        return copy(fw).getProviderFirewallId();
    }

    @Override
    public void delete(@Nonnull String firewallId) throws InternalException, CloudException {
        ProviderContext ctx = getProvider().getContext();

        if( ctx == null ) {
            throw new CloudException("No context was specified for this request");
        }
        String regionId = ctx.getRegionId();

        if( regionId == null ) {
            throw new CloudException("No region was specified for this request");
        }
        synchronized( firewalls ) {
            ComputeServices compute = getProvider().getComputeServices();

            if( compute != null ) {
                VirtualMachineSupport vmSupport = compute.getVirtualMachineSupport();

                if( vmSupport != null ) {
                    for( Map.Entry<String,Collection<String>> entry : vmMap.entrySet() ) {
                        for( String id : entry.getValue() ) {
                            if( firewallId.equals(id) ) {
                                VirtualMachine vm = vmSupport.getVirtualMachine(entry.getKey());

                                if( vm != null && !vm.getCurrentState().equals(VmState.TERMINATED) ) {
                                    throw new CloudException("Firewall " + firewallId + " is currently in use");
                                }
                            }
                        }
                    }
                }
            }
            Map<String,Map<String,Collection<Firewall>>> cloud = firewalls.get(ctx.getCloud().getEndpoint());

            if( cloud == null ) {
                cloud = new HashMap<String, Map<String, Collection<Firewall>>>();
                firewalls.put(ctx.getCloud().getEndpoint(), cloud);
            }
            Map<String,Collection<Firewall>> region = cloud.get(regionId);

            if( region == null ) {
                region = new HashMap<String, Collection<Firewall>>();
                cloud.put(regionId, region);
            }
            Collection<Firewall> account = region.get(ctx.getAccountNumber());

            if( account == null ) {
                return;
            }
            ArrayList<Firewall> replacement = new ArrayList<Firewall>();

            for( Firewall fw : account ) {
                if( !firewallId.equals(fw.getProviderFirewallId()) ) {
                    replacement.add(fw);
                }
            }
            region.put(ctx.getAccountNumber(), replacement);
            rules.remove(firewallId);
        }
    }

    FirewallCapabilities capabilities;

    @Nonnull
    @Override
    public FirewallCapabilities getCapabilities() throws CloudException, InternalException {
        return capabilities;
    }

    @Override
    public @Nonnull Collection<FirewallRule> getRules(@Nonnull String firewallId) throws InternalException, CloudException {
        Firewall fw = getFirewall(firewallId);

        if( fw == null ) {
            throw new CloudException("No such firewall: " + firewallId);
        }
        synchronized( firewalls ) {
            Collection<FirewallRule> matches = rules.get(firewallId);

            if( matches == null ) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableCollection(matches);
        }
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        return true;
    }

    @Override
    public @Nonnull Collection<Firewall> list() throws InternalException, CloudException {
        ProviderContext ctx = getProvider().getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        String regionId = ctx.getRegionId();

        if( regionId == null ) {
            throw new CloudException("No region was set for this request");
        }
        synchronized( firewalls ) {
            Map<String,Map<String,Collection<Firewall>>> cloud = firewalls.get(ctx.getCloud().getEndpoint());

            if( cloud == null ) {
                create("default", "Default Firewall");
                cloud = firewalls.get(ctx.getCloud().getEndpoint());
                if( cloud == null ) {
                    return Collections.emptyList();
                }
            }
            Map<String,Collection<Firewall>> region = cloud.get(regionId);

            if( region == null ) {
                create("default", "Default Firewall");
                region = cloud.get(regionId);
                if( region == null ) {
                    return Collections.emptyList();
                }
            }
            Collection<Firewall> account = region.get(ctx.getAccountNumber());

            if( account == null || account.isEmpty() ) {
                create("default", "Default Firewall");
                account = region.get(ctx.getAccountNumber());
                if( account == null ) {
                    return Collections.emptyList();
                }
            }
            return Collections.unmodifiableCollection(account);
        }
    }

    @Override
    public void revoke(@Nonnull String providerFirewallRuleId) throws InternalException, CloudException {
        synchronized( firewalls ) {
            for( String fwId : rules.keySet() ) {
                Collection<FirewallRule> list = rules.get(fwId);

                if( list == null ) {
                    continue;
                }
                ArrayList<FirewallRule> replacement = new ArrayList<FirewallRule>();

                for( FirewallRule r : list ) {
                    if( !r.getProviderRuleId().equals(providerFirewallRuleId) ) {
                        replacement.add(r);
                    }
                }
                rules.put(fwId, replacement);
            }
        }
    }

    @Override
    public boolean supportsFirewallSources() throws CloudException, InternalException {
        return true;
    }
}