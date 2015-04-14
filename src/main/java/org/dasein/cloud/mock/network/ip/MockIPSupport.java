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

package org.dasein.cloud.mock.network.ip;

import org.dasein.cloud.*;
import org.dasein.cloud.compute.ComputeServices;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.compute.VirtualMachineSupport;
import org.dasein.cloud.identity.ServiceAction;
import org.dasein.cloud.mock.MockCloud;
import org.dasein.cloud.mock.MockObjectCacheManager;
import org.dasein.cloud.network.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;
import java.util.concurrent.Future;

/**
 * Mock support for public IP address management in the cloud, including IPv4
 * and IPv6 support.
 * <p>
 * Created by George Reese: 10/18/12 3:01 PM
 * </p>
 * 
 * @author George Reese
 * @version 2012.09 initial version
 * @since 2012.09
 */
public class MockIPSupport extends AbstractIpAddressSupport<MockCloud>
		implements IpAddressSupport {

	static private final byte[] allocatedIpsLock = new byte[0];

	static private int quad1 = 26;
	static private int quad2 = 0;
	static private int quad3 = 0;
	static private int quad4 = 0;

	static private final Random random = new Random();

	static private MockObjectCacheManager mockObjectCacheManager = new MockObjectCacheManager();

	static private @Nonnull String allocate(@Nonnull ProviderContext ctx,
			IPVersion version) throws CloudException {
		Set<String> allocatedIps = null;
		synchronized (allocatedIpsLock) {
			// TODO change endpoint to cloud name.
			allocatedIps = (Set<String>) mockObjectCacheManager
					.readObjectFromCache(ctx.getCloud().getCloudName(),
							ctx.getRegionId(), "allocatedIps");
			if (allocatedIps == null) {
				allocatedIps = new TreeSet<String>();
			}
			String ip;
			do {
				if (version.equals(IPVersion.IPV4)) {
					quad4++;
					if (quad4 > 253) {
						quad3++;
						if (quad3 > 253) {
							quad2++;
							if (quad2 > 253) {
								quad1++;
								if (quad1 == 10 || quad1 == 25 || quad1 == 127
										|| quad1 == 172 || quad1 == 187
										|| quad1 == 192 || quad1 == 207) {
									quad1++;
								}
								if (quad1 > 253) {
									throw new CloudException(
											"IPv4 address space exhausted");
								}
							}
						}
					}
					ip = (quad1 + "." + quad2 + "." + quad3 + "." + quad4);
				} else {
					StringBuilder str = new StringBuilder();

					str.append("2001");

					for (int i = 0; i < 7; i++) {
						str.append(":");
						str.append(Integer.toHexString(random.nextInt(65534)));
					}
					ip = str.toString();
				}
			} while (allocatedIps.contains(ip));
			allocatedIps.add(ip);

			HashMap<String, Map<String, Map<String, Collection<String>>>> allocations = (HashMap<String, Map<String, Map<String, Collection<String>>>>) mockObjectCacheManager
					.readObjectFromCache(ctx.getCloud().getCloudName(),
							ctx.getRegionId(), "allocations");
			if (allocations == null) {
				allocations = new HashMap<String, Map<String, Map<String, Collection<String>>>>();
			}
			Map<String, Map<String, Collection<String>>> cloud = allocations
					.get(ctx.getCloud().getEndpoint());

			if (cloud == null) {
				cloud = new HashMap<String, Map<String, Collection<String>>>();
				allocations.put(ctx.getCloud().getEndpoint(), cloud);
			}
			Map<String, Collection<String>> region = cloud.get(ctx
					.getRegionId());

			if (region == null) {
				region = new HashMap<String, Collection<String>>();
				cloud.put(ctx.getRegionId(), region);
			}
			Collection<String> account = region.get(ctx
					.getEffectiveAccountNumber());

			if (account == null) {
				account = new TreeSet<String>();
				region.put(ctx.getEffectiveAccountNumber(), account);
			}
			account.add(ip);
			mockObjectCacheManager.writeObjectToCache(ctx.getCloud()
					.getCloudName(), ctx.getRegionId(), "allocations",
					allocations);
			mockObjectCacheManager.writeObjectToCache(ctx.getCloud()
					.getCloudName(), ctx.getRegionId(), "allocatedIps",
					allocatedIps);
			return ip;
		}
	}

	static public void assignToVM(@Nonnull ProviderContext ctx,
			@Nonnull String ipAddress, @Nonnull VirtualMachine vm)
			throws CloudException {
		synchronized (allocatedIpsLock) {
			HashMap<String, Map<String, Map<String, Collection<String>>>> allocations = (HashMap<String, Map<String, Map<String, Collection<String>>>>) mockObjectCacheManager
					.readObjectFromCache(ctx.getCloud().getCloudName(),
							ctx.getRegionId(), "allocations");
			if (allocations == null) {
				allocations = new HashMap<String, Map<String, Map<String, Collection<String>>>>();
			}
			Map<String, Map<String, Collection<String>>> cloud = allocations
					.get(ctx.getCloud().getEndpoint());

			if (cloud == null) {
				cloud = new HashMap<String, Map<String, Collection<String>>>();
				allocations.put(ctx.getCloud().getEndpoint(), cloud);
			}
			Map<String, Collection<String>> region = cloud.get(ctx
					.getRegionId());

			if (region == null) {
				region = new HashMap<String, Collection<String>>();
				cloud.put(ctx.getRegionId(), region);
			}
			Collection<String> account = region.get(ctx
					.getEffectiveAccountNumber());

			if (account == null) {
				account = new TreeSet<String>();
				region.put(ctx.getEffectiveAccountNumber(), account);
			}
			if (!account.contains(ipAddress)) {
				throw new CloudException(
						"That IP address is not allocated to you");
			}

			String current = null;
			HashMap<String, String> vmAssignments = (HashMap<String, String>) mockObjectCacheManager
					.readObjectFromCache("vmAssignments");
			if (vmAssignments == null) {
				vmAssignments = new HashMap<String, String>();
			}
			for (Map.Entry<String, String> entry : vmAssignments.entrySet()) {
				if (entry.getKey().equals(ipAddress)) {
					throw new CloudException("IP address is already assigned");
				}
				if (entry.getValue().equals(vm.getProviderVirtualMachineId())) {
					current = entry.getKey();
				}
			}
			if (current == null) {
				HashMap<String, String> lbAssignments = (HashMap<String, String>) mockObjectCacheManager
						.readObjectFromCache("lbAssignments");
				if (lbAssignments != null
						&& lbAssignments.containsKey(ipAddress)) {
					throw new CloudException("IP address is already assigned");
				}
			}
			vmAssignments.put(ipAddress, vm.getProviderVirtualMachineId());
			if (current != null) {
				vmAssignments.remove(current);
			}
			mockObjectCacheManager.writeObjectToCache("vmAssignments",
					vmAssignments);
		}
	}

	static public @Nullable String getIPAddressForVM(@Nonnull String vmId) {
		synchronized (allocatedIpsLock) {
			HashMap<String, String> vmAssignments = (HashMap<String, String>) mockObjectCacheManager
					.readObjectFromCache("vmAssignments");
			if (vmAssignments != null) {
				for (Map.Entry<String, String> entry : vmAssignments.entrySet()) {
					if (entry.getValue().equals(vmId)) {
						return entry.getKey();
					}
				}
			}
		}
		return null;
	}

	static public @Nullable String getIPAddressForLB(@Nonnull String lbId) {
		synchronized (allocatedIpsLock) {
			HashMap<String, String> lbAssignments = (HashMap<String, String>) mockObjectCacheManager
					.readObjectFromCache("lbAssignments");
			if (lbAssignments != null) {
				for (Map.Entry<String, String> entry : lbAssignments.entrySet()) {
					if (entry.getValue().equals(lbId)) {
						return entry.getKey();
					}
				}
			}
		}
		return null;
	}

	public MockIPSupport(@Nonnull MockCloud provider) {
		super(provider);
	}

	@Override
	public void assign(@Nonnull String addressId, @Nonnull String serverId)
			throws InternalException, CloudException {
		ProviderContext ctx = getProvider().getContext();

		if (ctx == null) {
			throw new CloudException("No context was set for this request");
		}
		ComputeServices compute = getProvider().getComputeServices();

		if (compute == null) {
			throw new CloudException(
					"This cloud does not support compute services");
		}
		VirtualMachineSupport vmSupport = compute.getVirtualMachineSupport();

		if (vmSupport == null) {
			throw new CloudException(
					"This cloud does not support virtual machines");
		}
		VirtualMachine vm = vmSupport.getVirtualMachine(serverId);

		if (vm == null) {
			throw new CloudException("No such virtual machine: " + serverId);
		}
		assignToVM(ctx, addressId, vm);
	}

	@Override
	public void assignToNetworkInterface(@Nonnull String addressId,
			@Nonnull String nicId) throws InternalException, CloudException {
		throw new OperationNotSupportedException(
				"No support for network interfaces");
	}

	@Override
	public @Nonnull String forward(@Nonnull String addressId, int publicPort,
			@Nonnull Protocol protocol, int privatePort,
			@Nonnull String onServerId) throws InternalException,
			CloudException {
		throw new OperationNotSupportedException("No support for IP forwarding");
	}

	@Override
	public IpAddress getIpAddress(@Nonnull String addressId)
			throws InternalException, CloudException {
		for (IpAddress addr : listIpPool(IPVersion.IPV4, false)) {
			if (addr.getProviderIpAddressId().equals(addressId)) {
				return addr;
			}
		}
		for (IpAddress addr : listIpPool(IPVersion.IPV6, false)) {
			if (addr.getProviderIpAddressId().equals(addressId)) {
				return addr;
			}
		}
		return null;
	}

	@Override
	public boolean isSubscribed() throws CloudException, InternalException {
		return true;
	}

	@Override
	public @Nonnull Iterable<IpAddress> listIpPool(@Nonnull IPVersion version,
			boolean unassignedOnly) throws InternalException, CloudException {
		ArrayList<IpAddress> addresses = new ArrayList<IpAddress>();
		ProviderContext ctx = getProvider().getContext();

		if (ctx == null) {
			throw new CloudException("No context was set for this request");
		}
		synchronized (allocatedIpsLock) {
			HashMap<String, Map<String, Map<String, Collection<String>>>> allocations = (HashMap<String, Map<String, Map<String, Collection<String>>>>) mockObjectCacheManager
					.readObjectFromCache(ctx.getCloud().getCloudName(),
							ctx.getRegionId(), "allocations");
			if (allocations == null) {
				allocations = new HashMap<String, Map<String, Map<String, Collection<String>>>>();
			}
			Map<String, Map<String, Collection<String>>> cloud = allocations
					.get(ctx.getCloud().getEndpoint());

			if (cloud == null) {
				cloud = new HashMap<String, Map<String, Collection<String>>>();
				allocations.put(ctx.getCloud().getEndpoint(), cloud);
			}
			Map<String, Collection<String>> region = cloud.get(ctx
					.getRegionId());

			if (region == null) {
				region = new HashMap<String, Collection<String>>();
				cloud.put(ctx.getRegionId(), region);
			}
			Collection<String> account = region.get(ctx
					.getEffectiveAccountNumber());

			if (account == null) {
				account = new TreeSet<String>();
				region.put(ctx.getEffectiveAccountNumber(), account);
			}
			for (String ip : account) {
				boolean v4 = (ip.split("\\.").length == 4);

				if (v4 != version.equals(IPVersion.IPV4)) {
					continue;
				}

				HashMap<String, String> vmAssignments = (HashMap<String, String>) mockObjectCacheManager
						.readObjectFromCache("vmAssignments");
				HashMap<String, String> lbAssignments = (HashMap<String, String>) mockObjectCacheManager
						.readObjectFromCache("lbAssignments");
				if (unassignedOnly
						&& ((vmAssignments != null && vmAssignments
								.containsKey(ip)) || (lbAssignments != null && lbAssignments
								.containsKey(ip)))) {
					continue;
				}
				IpAddress address = new IpAddress();
				address.setAddress(ip);
				address.setAddressType(AddressType.PUBLIC);
				address.setForVlan(false);
				address.setIpAddressId(ip);
				address.setProviderLoadBalancerId(lbAssignments == null ? null
						: lbAssignments.get(ip));
				address.setProviderNetworkInterfaceId(null);
				// noinspection ConstantConditions
				address.setRegionId(ctx.getRegionId());
				address.setServerId(vmAssignments == null ? null
						: vmAssignments.get(ip));
				address.setVersion(version);
				addresses.add(address);
			}
		}
		return addresses;
	}

	@Override
	public @Nonnull Iterable<ResourceStatus> listIpPoolStatus(
			@Nonnull IPVersion version) throws InternalException,
			CloudException {
		ArrayList<ResourceStatus> status = new ArrayList<ResourceStatus>();

		for (IpAddress addr : listIpPool(version, false)) {
			status.add(new ResourceStatus(addr.getProviderIpAddressId(), !addr
					.isAssigned()));
		}
		return status;
	}

	@Override
	public @Nonnull Iterable<IpForwardingRule> listRules(
			@Nonnull String addressId) throws InternalException, CloudException {
		return Collections.emptyList();
	}

	@Override
	public void releaseFromPool(@Nonnull String ip) throws InternalException,
			CloudException {
		ProviderContext ctx = getProvider().getContext();

		if (ctx == null) {
			throw new CloudException("No context was set for this request");
		}
		synchronized (allocatedIpsLock) {
			HashMap<String, String> vmAssignments = (HashMap<String, String>) mockObjectCacheManager
					.readObjectFromCache("vmAssignments");
			HashMap<String, String> lbAssignments = (HashMap<String, String>) mockObjectCacheManager
					.readObjectFromCache("lbAssignments");
			if ((vmAssignments != null && vmAssignments.containsKey(ip))
					|| (lbAssignments != null && lbAssignments.containsKey(ip))) {
				throw new CloudException(
						"That IP is currently assigned to a resource");
			}

			HashMap<String, Map<String, Map<String, Collection<String>>>> allocations = (HashMap<String, Map<String, Map<String, Collection<String>>>>) mockObjectCacheManager
					.readObjectFromCache(ctx.getCloud().getCloudName(),
							ctx.getRegionId(), "allocations");
			if (allocations != null) {
				Map<String, Map<String, Collection<String>>> cloud = allocations
						.get(ctx.getCloud().getEndpoint());

				if (cloud == null) {
					cloud = new HashMap<String, Map<String, Collection<String>>>();
					allocations.put(ctx.getCloud().getEndpoint(), cloud);
				}
				Map<String, Collection<String>> region = cloud.get(ctx
						.getRegionId());

				if (region == null) {
					region = new HashMap<String, Collection<String>>();
					cloud.put(ctx.getRegionId(), region);
				}
				Collection<String> account = region.get(ctx
						.getEffectiveAccountNumber());

				if (account == null) {
					account = new TreeSet<String>();
					region.put(ctx.getEffectiveAccountNumber(), account);
				}
				account.remove(ip);
				mockObjectCacheManager.writeObjectToCache(ctx.getCloud()
						.getCloudName(), ctx.getRegionId(), "allocations",
						allocations);
			}
			Set<String> allocatedIps = (Set<String>) mockObjectCacheManager
					.readObjectFromCache(ctx.getCloud().getCloudName(),
							ctx.getRegionId(), "allocatedIps");
			if (allocatedIps != null) {
				allocatedIps.remove(ip);
				mockObjectCacheManager.writeObjectToCache(ctx.getCloud()
						.getCloudName(), ctx.getRegionId(), "allocatedIps",
						allocatedIps);
			}
		}
	}

	@Override
	public void releaseFromServer(@Nonnull String ip) throws InternalException,
			CloudException {
		ProviderContext ctx = getProvider().getContext();

		if (ctx == null) {
			throw new CloudException("No context was set for this request");
		}
		synchronized (allocatedIpsLock) {
			HashMap<String, String> vmAssignments = (HashMap<String, String>) mockObjectCacheManager
					.readObjectFromCache("vmAssignments");
			if (vmAssignments == null) {
				return;
			}
			if (!vmAssignments.containsKey(ip)) {
				throw new CloudException(
						"That IP is not currently assigned to a resource");
			}
			HashMap<String, Map<String, Map<String, Collection<String>>>> allocations = (HashMap<String, Map<String, Map<String, Collection<String>>>>) mockObjectCacheManager
					.readObjectFromCache(ctx.getCloud().getCloudName(),
							ctx.getRegionId(), "allocations");
			if (allocations != null) {
				Map<String, Map<String, Collection<String>>> cloud = allocations
						.get(ctx.getCloud().getEndpoint());
				if (cloud == null) {
					cloud = new HashMap<String, Map<String, Collection<String>>>();
					allocations.put(ctx.getCloud().getEndpoint(), cloud);
				}
				Map<String, Collection<String>> region = cloud.get(ctx
						.getRegionId());

				if (region == null) {
					region = new HashMap<String, Collection<String>>();
					cloud.put(ctx.getRegionId(), region);
				}
				Collection<String> account = region.get(ctx
						.getEffectiveAccountNumber());

				if (account == null) {
					account = new TreeSet<String>();
					region.put(ctx.getEffectiveAccountNumber(), account);
				}
				if (!account.contains(ip)) {
					throw new CloudException("Not your IP address");
				}
			}
			vmAssignments.remove(ip);
			mockObjectCacheManager.writeObjectToCache("vmAssignments",
					vmAssignments);
		}
	}

	@Override
	public @Nonnull String request(@Nonnull IPVersion version)
			throws InternalException, CloudException {
		ProviderContext ctx = getProvider().getContext();

		if (ctx == null) {
			throw new CloudException("No context was set for this request");
		}
		return allocate(ctx, version);
	}

	@Override
	public @Nonnull String requestForVLAN(@Nonnull IPVersion version)
			throws InternalException, CloudException {
		throw new OperationNotSupportedException(
				"VLAN IP addresses are not yet supported");
	}

	@Override
	public @Nonnull String requestForVLAN(@Nonnull IPVersion version,
			@Nonnull String vlanId) throws InternalException, CloudException {
		throw new OperationNotSupportedException(
				"No support for VLAN IP addresses");
	}

	@Override
	public void stopForward(@Nonnull String ruleId) throws InternalException,
			CloudException {
		throw new OperationNotSupportedException(
				"IP forwarding is not supported");
	}

	@Override
	public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
		return new String[0];
	}

	@Nonnull
	@Override
	public Future<Iterable<IpAddress>> listIpPoolConcurrently(
			@Nonnull IPVersion version, boolean unassignedOnly)
			throws InternalException, CloudException {
		return null;
	}

	@Nonnull
	@Override
	public IPAddressCapabilities getCapabilities() throws CloudException,
			InternalException {
		return new MockIPCapabilities(getProvider());
	}
}
