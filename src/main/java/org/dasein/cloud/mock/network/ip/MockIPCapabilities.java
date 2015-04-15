package org.dasein.cloud.mock.network.ip;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.mock.AbstractMockCapabilities;
import org.dasein.cloud.mock.MockCloud;
import org.dasein.cloud.network.IPAddressCapabilities;
import org.dasein.cloud.network.IPVersion;

import java.util.Locale;

public class MockIPCapabilities extends AbstractMockCapabilities implements IPAddressCapabilities {

    private String providerTermForIpAddress;
    private Requirement vlanForVlanIPRequirement;
    private Requirement vlanForIPRequirement;
    private boolean isAssigned;
    private boolean canBeAssigned;
    private boolean isAssignablePostLaunch;
    private boolean isForwarding;
    private boolean isRequestable;
    private Iterable<IPVersion> supportedIPVersions;
    private boolean supportsVLANAddresses;

	public MockIPCapabilities (MockCloud provider) {
		super(provider);
	}

	@Override
	public String getProviderTermForIpAddress(Locale locale) {
		return providerTermForIpAddress;
	}

	@Override
	public Requirement identifyVlanForVlanIPRequirement()
			throws CloudException, InternalException {
		return vlanForVlanIPRequirement;
	}

	@Override
	public Requirement identifyVlanForIPRequirement() throws CloudException,
			InternalException {
		return vlanForIPRequirement;
	}

	@Override
	public boolean isAssigned(IPVersion version) throws CloudException,
			InternalException {
		return isAssigned;
	}

	@Override
	public boolean canBeAssigned(VmState vmState) throws CloudException,
			InternalException {
		return canBeAssigned;
	}

	@Override
	public boolean isAssignablePostLaunch(IPVersion version)
			throws CloudException, InternalException {
		return isAssignablePostLaunch;
	}

	@Override
	public boolean isForwarding(IPVersion version) throws CloudException,
			InternalException {
		return isForwarding;
	}

	@Override
	public boolean isRequestable(IPVersion version) throws CloudException,
			InternalException {
		return isRequestable;
	}

	@Override
	public Iterable<IPVersion> listSupportedIPVersions() throws CloudException,
			InternalException {
		return supportedIPVersions;
	}

	@Override
	public boolean supportsVLANAddresses(IPVersion ofVersion)
			throws InternalException, CloudException {
		return supportsVLANAddresses;
	}

}
