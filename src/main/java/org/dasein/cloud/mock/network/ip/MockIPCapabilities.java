package org.dasein.cloud.mock.network.ip;

import java.util.Arrays;
import java.util.Locale;

import org.dasein.cloud.AbstractCapabilities;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.mock.MockCloud;
import org.dasein.cloud.network.IPAddressCapabilities;
import org.dasein.cloud.network.IPVersion;

public class MockIPCapabilities extends AbstractCapabilities<MockCloud>
		implements IPAddressCapabilities {

	public MockIPCapabilities (MockCloud provider) {
		super(provider);
	}
	
	@Override
	public String getAccountNumber() {
		return this.getAccountNumber();
	}

	@Override
	public String getRegionId() {
		return this.getRegionId();
	}

	@Override
	public String getProviderTermForIpAddress(Locale locale) {
		return "IP Address";
	}

	@Override
	public Requirement identifyVlanForVlanIPRequirement()
			throws CloudException, InternalException {
		return Requirement.NONE;
	}

	@Override
	public Requirement identifyVlanForIPRequirement() throws CloudException,
			InternalException {
		return Requirement.NONE;
	}

	@Override
	public boolean isAssigned(IPVersion version) throws CloudException,
			InternalException {
		return true;
	}

	@Override
	public boolean canBeAssigned(VmState vmState) throws CloudException,
			InternalException {
		return true;
	}

	@Override
	public boolean isAssignablePostLaunch(IPVersion version)
			throws CloudException, InternalException {
		return true;
	}

	@Override
	public boolean isForwarding(IPVersion version) throws CloudException,
			InternalException {
		return false;
	}

	@Override
	public boolean isRequestable(IPVersion version) throws CloudException,
			InternalException {
		return true;
	}

	@Override
	public Iterable<IPVersion> listSupportedIPVersions() throws CloudException,
			InternalException {
		return Arrays.asList(IPVersion.IPV4, IPVersion.IPV6);
	}

	@Override
	public boolean supportsVLANAddresses(IPVersion ofVersion)
			throws InternalException, CloudException {
		return false;
	}

}
