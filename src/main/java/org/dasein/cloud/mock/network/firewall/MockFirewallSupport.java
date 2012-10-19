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
package org.dasein.cloud.mock.network.firewall;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.network.Direction;
import org.dasein.cloud.network.Firewall;
import org.dasein.cloud.network.FirewallRule;
import org.dasein.cloud.network.FirewallSupport;
import org.dasein.cloud.network.NetworkServices;
import org.dasein.cloud.network.Permission;
import org.dasein.cloud.network.Protocol;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Implements bi-directional mock firewall support.
 * <p>Created by George Reese: 10/19/12 12:30 PM</p>
 * @author George Reese
 * @version 2012.09 initial version
 * @since 2012.09
 */
public class MockFirewallSupport implements FirewallSupport {
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

    static private @Nonnull String toRuleId(@Nonnull FirewallRule rule) {
        Direction d = rule.getDirection();
        Protocol p = rule.getProtocol();

        if( d == null ) {
            d = Direction.INGRESS;
        }
        if( p == null ) {
            p = Protocol.TCP;
        }
        return (rule.getFirewallId() + "_:_" + rule.getPermission().toString() + "_:_" + d.toString() + "_:_" + p.toString() + "_:_" + rule.getCidr() + "_:_" + rule.getStartPort() + ":" + rule.getEndPort());
    }

    private CloudProvider provider;

    public MockFirewallSupport(CloudProvider provider) { this.provider = provider; }

    @Override
    public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
        return authorize(firewallId, Direction.INGRESS, cidr, protocol, beginPort,  endPort);
    }

    @Override
    public @Nonnull String authorize(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
        if( getFirewall(firewallId) == null ) {
            throw new CloudException("No such firewall: " + firewallId);
        }

        FirewallRule rule = new FirewallRule();

        rule.setCidr(cidr);
        rule.setDirection(direction);
        rule.setEndPort(endPort);
        rule.setFirewallId(firewallId);
        rule.setPermission(Permission.ALLOW);
        rule.setProtocol(protocol);
        rule.setStartPort(beginPort);
        rule.setProviderRuleId(toRuleId(rule));
        synchronized( firewalls ) {
            Collection<FirewallRule> list = rules.get(firewallId);

            if( list == null ) {
                list = new ArrayList<FirewallRule>();
                rules.put(firewallId, list);
            }
            list.add(rule);
        }
        return rule.getProviderRuleId();
    }

    @Override
    public @Nonnull String create(@Nonnull String name, @Nonnull String description) throws InternalException, CloudException {
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was specified for this request");
        }
        String regionId = ctx.getRegionId();

        if( regionId == null ) {
            throw new CloudException("No region was specified for this request");
        }
        Firewall fw = new Firewall();

        fw.setActive(true);
        fw.setAvailable(true);
        fw.setDescription(description);
        fw.setName(name);
        fw.setProviderFirewallId(UUID.randomUUID().toString());
        fw.setRegionId(regionId);
        synchronized( firewalls ) {
            Map<String,Map<String,Collection<Firewall>>> cloud = firewalls.get(ctx.getEndpoint());

            if( cloud == null ) {
                cloud = new HashMap<String, Map<String, Collection<Firewall>>>();
                firewalls.put(ctx.getEndpoint(), cloud);
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
        //noinspection ConstantConditions
        return copy(fw).getProviderFirewallId();
    }

    @Override
    public @Nonnull String createInVLAN(@Nonnull String name, @Nonnull String description, @Nonnull String providerVlanId) throws InternalException, CloudException {
        throw new OperationNotSupportedException("VLANs not yet supported");
    }

    @Override
    public void delete(@Nonnull String firewallId) throws InternalException, CloudException {
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was specified for this request");
        }
        String regionId = ctx.getRegionId();

        if( regionId == null ) {
            throw new CloudException("No region was specified for this request");
        }
        synchronized( firewalls ) {
            ComputeServices compute = provider.getComputeServices();

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
            Map<String,Map<String,Collection<Firewall>>> cloud = firewalls.get(ctx.getEndpoint());

            if( cloud == null ) {
                cloud = new HashMap<String, Map<String, Collection<Firewall>>>();
                firewalls.put(ctx.getEndpoint(), cloud);
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

    @Override
    public Firewall getFirewall(@Nonnull String firewallId) throws InternalException, CloudException {
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        String regionId = ctx.getRegionId();

        if( regionId == null ) {
            throw new CloudException("No region was set for this request");
        }
        synchronized( firewalls ) {
            Map<String,Map<String,Collection<Firewall>>> cloud = firewalls.get(ctx.getEndpoint());

            if( cloud == null ) {
                return null;
            }
            Map<String,Collection<Firewall>> region = cloud.get(regionId);

            if( region == null ) {
                return null;
            }
            Collection<Firewall> account = region.get(ctx.getAccountNumber());

            if( account == null ) {
                return null;
            }
            for( Firewall fw : account ) {
                if( firewallId.equals(fw.getProviderFirewallId()) ) {
                    return fw;
                }
            }
            return null;
        }
    }

    @Override
    public @Nonnull String getProviderTermForFirewall(@Nonnull Locale locale) {
        return "firewall";
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
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        String regionId = ctx.getRegionId();

        if( regionId == null ) {
            throw new CloudException("No region was set for this request");
        }
        synchronized( firewalls ) {
            Map<String,Map<String,Collection<Firewall>>> cloud = firewalls.get(ctx.getEndpoint());

            if( cloud == null ) {
                return Collections.emptyList();
            }
            Map<String,Collection<Firewall>> region = cloud.get(regionId);

            if( region == null ) {
                return Collections.emptyList();
            }
            Collection<Firewall> account = region.get(ctx.getAccountNumber());

            if( account == null ) {
                return Collections.emptyList();
            }
            return Collections.unmodifiableCollection(account);
        }
    }

    @Override
    public void revoke(@Nonnull String firewallId, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
        revoke(firewallId, Direction.INGRESS, cidr, protocol, beginPort, endPort);
    }

    @Override
    public void revoke(@Nonnull String firewallId, @Nonnull Direction direction, @Nonnull String cidr, @Nonnull Protocol protocol, int beginPort, int endPort) throws CloudException, InternalException {
        FirewallRule rule = new FirewallRule();

        rule.setCidr(cidr);
        rule.setDirection(direction);
        rule.setEndPort(endPort);
        rule.setFirewallId(firewallId);
        rule.setPermission(Permission.ALLOW);
        rule.setProtocol(protocol);
        rule.setStartPort(beginPort);
        rule.setProviderRuleId(toRuleId(rule));
        synchronized( firewalls ) {
            Collection<FirewallRule> list = rules.get(firewallId);

            if( list == null ) {
                return;
            }
            ArrayList<FirewallRule> replacement = new ArrayList<FirewallRule>();

            for( FirewallRule r : list ) {
                if( !r.getProviderRuleId().equals(rule.getProviderRuleId()) ) {
                    replacement.add(r);
                }
            }
            rules.put(firewallId, replacement);
        }
    }

    @Override
    public boolean supportsRules(@Nonnull Direction direction, boolean inVlan) throws CloudException, InternalException {
        return !inVlan;
    }

    @Override
    public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
        return new String[0];
    }
}
