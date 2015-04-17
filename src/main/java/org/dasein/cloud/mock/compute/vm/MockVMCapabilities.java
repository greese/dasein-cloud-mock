/**
 * Copyright (C) 2009-2015 Dell, Inc.
 * See annotations for authorship information
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
 *
 */
package org.dasein.cloud.mock.compute.vm;

import org.dasein.cloud.*;
import org.dasein.cloud.compute.*;
import org.dasein.cloud.mock.AbstractMockCapabilities;
import org.dasein.cloud.mock.MockCloud;
import org.dasein.cloud.network.NetworkServices;
import org.dasein.cloud.util.NamingConstraints;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Map;

/**
 * @author Colin Ke.
 * @since 2015.05.1
 */
public class MockVMCapabilities extends AbstractMockCapabilities implements VirtualMachineCapabilities {

    private Map<VmState, Boolean> canClone;
    private Map<VmState, Boolean> canAlter;
    private Map<VmState, Boolean> canPause;
    private Map<VmState, Boolean> canReboot;
    private Map<VmState, Boolean> canResume;
    private Map<VmState, Boolean> canStart;
    private Map<VmState, Boolean> canStop;
    private Map<VmState, Boolean> canSuspend;
    private Map<VmState, Boolean> canTerminate;
    private Map<VmState, Boolean> canUnpause;

    private int costFactor;
    private int maximumVirtualMachineCount;

    private String providerTermForVirtualMachine;
    private VMScalingCapabilities verticalScalingCapabilities;
    private NamingConstraints virtualMachineNamingConstraints;
    private VisibleScope virtualMachineVisibleScope;
    private VisibleScope virtualMachineProductVisibleScope;
    private Requirement dataCenterLaunchRequirement;
    private Map<ImageClass, Requirement> imageRequirement;
    private Map<Platform, Requirement> passwordRequirement;
    private Requirement rootVolumeRequirement;
    private Map<Platform, Requirement> shellKeyRequirement;
    private Requirement staticIPRequirement;
    private Requirement subnetRequirement;

    private Iterable<Architecture> supportedArchitectures;

    private boolean isAPITerminationPreventable;
    private boolean isBasicAnalyticsSupported;
    private boolean isExtendedAnalyticsSupported;
    private boolean isUserDataSupported;
    private boolean isUserDefinedPrivateIPSupported;
    private boolean supportsSpotVirtualMachines;
    private boolean supportsClientRequestToken;
    private boolean supportsCloudStoredShellKey;
    private boolean isVMProductDCConstrained;
    private boolean supportsAlterVM;
    private boolean supportsClone;
    private boolean supportsPause;
    private boolean supportsReboot;
    private boolean supportsResume;
    private boolean supportsStart;
    private boolean supportsStop;
    private boolean supportsSuspend;
    private boolean supportsTerminate;
    private boolean supportsUnPause;



    public MockVMCapabilities(@Nonnull MockCloud provider) {
        super(provider);
    }

    @Override
    public boolean canAlter(@Nonnull VmState fromState) throws CloudException, InternalException {
        if(canAlter.containsKey(fromState))
            return canAlter.get(fromState);
        return false;

    }

    @Override
    public boolean canClone(@Nonnull VmState fromState) throws CloudException, InternalException {
        if(canClone.containsKey(fromState))
            return canClone.get(fromState);
        return false;
    }

    @Override
    public boolean canPause(@Nonnull VmState fromState) throws CloudException, InternalException {
        if(canPause.containsKey(fromState))
            return canPause.get(fromState);
        return true;
    }

    @Override
    public boolean canReboot(@Nonnull VmState fromState) throws CloudException, InternalException {
        if(canReboot.containsKey(fromState))
            return canReboot.get(fromState);
        return true;
    }

    @Override
    public boolean canResume(@Nonnull VmState fromState) throws CloudException, InternalException {
        if(canResume.containsKey(fromState))
            return canResume.get(fromState);
        return true;
    }

    @Override
    public boolean canStart(@Nonnull VmState fromState) throws CloudException, InternalException {
        if(canStart.containsKey(fromState))
            return canStart.get(fromState);
        return true;
    }

    @Override
    public boolean canStop(@Nonnull VmState fromState) throws CloudException, InternalException {
        if(canStop.containsKey(fromState))
            return canStop.get(fromState);
        return true;

    }

    @Override
    public boolean canSuspend(@Nonnull VmState fromState) throws CloudException, InternalException {
        if(canSuspend.containsKey(fromState))
            return canSuspend.get(fromState);
        return true;
    }

    @Override
    public boolean canTerminate(@Nonnull VmState fromState) throws CloudException, InternalException {
        if(canTerminate.containsKey(fromState))
            return canTerminate.get(fromState);
        return true;
    }

    @Override
	public boolean canUnpause(@Nonnull VmState fromState) throws CloudException, InternalException {
        if(canUnpause.containsKey(fromState))
            return canUnpause.get(fromState);
        return false;
	}

    @Override
    public int getMaximumVirtualMachineCount() throws CloudException, InternalException {
        return maximumVirtualMachineCount;
    }

    @Override
    public int getCostFactor(@Nonnull VmState state) throws CloudException, InternalException {
        return costFactor;
    }

    @Nonnull
    @Override
    public String getProviderTermForVirtualMachine(@Nonnull Locale locale) throws CloudException, InternalException {
        return providerTermForVirtualMachine;
    }

    @Nullable
    @Override
    public VMScalingCapabilities getVerticalScalingCapabilities() throws CloudException, InternalException {
        return verticalScalingCapabilities;
    }

    @Nonnull
    @Override
    public NamingConstraints getVirtualMachineNamingConstraints() throws CloudException, InternalException {
        return virtualMachineNamingConstraints;
    }

    @Nullable
    @Override
    public VisibleScope getVirtualMachineVisibleScope() {
        return virtualMachineVisibleScope;
    }

    @Nullable
    @Override
    public VisibleScope getVirtualMachineProductVisibleScope() {
        return virtualMachineProductVisibleScope;
    }

    @Nonnull
    @Override
    public Requirement identifyDataCenterLaunchRequirement() throws CloudException, InternalException {
        return dataCenterLaunchRequirement;
    }

    @Nonnull
    @Override
    public Requirement identifyImageRequirement(@Nonnull ImageClass cls) throws CloudException, InternalException {
        if(imageRequirement.containsKey(cls))
            return imageRequirement.get(cls);
        return Requirement.NONE;
    }

    @Nonnull
    @Override
    public Requirement identifyPasswordRequirement(Platform platform) throws CloudException, InternalException {
        if(passwordRequirement.containsKey(platform))
            return passwordRequirement.get(platform);
        return Requirement.OPTIONAL;
    }

    @Nonnull
    @Override
    public Requirement identifyRootVolumeRequirement() throws CloudException, InternalException {
        return rootVolumeRequirement;
    }

    @Nonnull
    @Override
    public Requirement identifyShellKeyRequirement(Platform platform) throws CloudException, InternalException {
        if(shellKeyRequirement.containsKey(platform))
            return shellKeyRequirement.get(platform);
        return Requirement.OPTIONAL;
    }

    @Nonnull
    @Override
    public Requirement identifyStaticIPRequirement() throws CloudException, InternalException {
        return staticIPRequirement;
    }

    @Nonnull
    @Override
    public Requirement identifySubnetRequirement() throws CloudException, InternalException {
        return subnetRequirement;
    }

    @Nonnull
    @Override
    public Requirement identifyVlanRequirement() throws CloudException, InternalException {
        NetworkServices network = getProvider().getNetworkServices();

        if (network == null) {
            return Requirement.NONE;
        }
        if (network.hasVlanSupport()) {
            return Requirement.REQUIRED;
        }
        return Requirement.NONE;
    }

    @Override
    public boolean isAPITerminationPreventable() throws CloudException, InternalException {
        return isAPITerminationPreventable;
    }

    @Override
    public boolean isBasicAnalyticsSupported() throws CloudException, InternalException {
        return isBasicAnalyticsSupported;
    }

    @Override
    public boolean isExtendedAnalyticsSupported() throws CloudException, InternalException {
        return isExtendedAnalyticsSupported;
    }

    @Override
    public boolean isUserDataSupported() throws CloudException, InternalException {
        return isUserDataSupported;
    }

    @Override
    public boolean isUserDefinedPrivateIPSupported() throws CloudException, InternalException {
        return isUserDefinedPrivateIPSupported;
    }

    @Nonnull
    @Override
    public Iterable<Architecture> listSupportedArchitectures() throws InternalException, CloudException {
        return supportedArchitectures;
    }

    @Override
    public boolean supportsSpotVirtualMachines() throws InternalException, CloudException {
        return supportsSpotVirtualMachines;
    }

    @Override
    public boolean supportsClientRequestToken() throws InternalException, CloudException {
        return supportsClientRequestToken;
    }

    @Override
    public boolean supportsCloudStoredShellKey() throws InternalException, CloudException {
        return supportsCloudStoredShellKey;
    }

    @Override
    public boolean isVMProductDCConstrained() throws InternalException, CloudException {
        return isVMProductDCConstrained;
    }

    @Override
    public boolean supportsAlterVM() {
        return supportsAlterVM;
    }

    @Override
    public boolean supportsClone() {
        return supportsClone;
    }

    @Override
    public boolean supportsPause() {
        return supportsPause;
    }

    @Override
    public boolean supportsReboot() {
        return supportsReboot;
    }

    @Override
    public boolean supportsResume() {
        return supportsResume;
    }

    @Override
    public boolean supportsStart() {
        return supportsStart;
    }

    @Override
    public boolean supportsStop() {
        return supportsStop;
    }

    @Override
    public boolean supportsSuspend() {
        return supportsSuspend;
    }

    @Override
    public boolean supportsTerminate() {
        return supportsTerminate;
    }

    @Override
    public boolean supportsUnPause() {
        return supportsUnPause;
    }
}
