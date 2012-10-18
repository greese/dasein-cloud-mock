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
package org.dasein.cloud.mock.compute.image;

import org.dasein.cloud.AsynchronousTask;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.CloudProvider;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.ProviderContext;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.MachineImage;
import org.dasein.cloud.compute.MachineImageFormat;
import org.dasein.cloud.compute.MachineImageState;
import org.dasein.cloud.compute.MachineImageSupport;
import org.dasein.cloud.compute.MachineImageType;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.identity.ServiceAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implements mocked up image management services for the Dasein Cloud mock cloud.
 * <p>Created by George Reese: 10/18/12 12:45 PM</p>
 * @author George Reese
 * @version 2012.09 initial version
 * @since 2012.09
 */
public class MockImageSupport implements MachineImageSupport {
    private CloudProvider provider;

    static private final Map<String,Map<String,Collection<MachineImage>>> publicImages = new HashMap<String, Map<String, Collection<MachineImage>>>();

    static private @Nonnull Collection<MachineImage> getPublicImages(@Nonnull ProviderContext ctx) {
        synchronized( publicImages ) {
            Map<String,Collection<MachineImage>> cloud = publicImages.get(ctx.getEndpoint());

            if( cloud == null ) {
                cloud = new HashMap<String, Collection<MachineImage>>();
                publicImages.put(ctx.getEndpoint(), cloud);
            }
            Collection<MachineImage> images = cloud.get(ctx.getRegionId());

            if( images == null ) {
                images = new ArrayList<MachineImage>();

                MachineImage img;

                img = new MachineImage();
                img.setProviderRegionId(ctx.getRegionId());
                img.setSoftware("");
                img.setType(MachineImageType.VOLUME);
                img.setArchitecture(Architecture.I64);
                img.setCurrentState(MachineImageState.ACTIVE);
                img.setDescription("An Ubunutu VM");
                img.setName("Ubuntu 10.04 x64");
                img.setPlatform(Platform.UBUNTU);
                img.setProviderMachineImageId(ctx.getRegionId() + "-1");
                img.setProviderOwnerId(null);
                img.setProviderRegionId(ctx.getRegionId());
                images.add(img);

                img = new MachineImage();
                img.setProviderRegionId(ctx.getRegionId());
                img.setSoftware("");
                img.setType(MachineImageType.VOLUME);
                img.setArchitecture(Architecture.I64);
                img.setCurrentState(MachineImageState.ACTIVE);
                img.setDescription("Windows VM");
                img.setName("Windows 2008 x64");
                img.setPlatform(Platform.WINDOWS);
                img.setProviderMachineImageId(ctx.getRegionId() + "-2");
                img.setProviderOwnerId(null);
                img.setProviderRegionId(ctx.getRegionId());
                images.add(img);

                cloud.put(ctx.getRegionId(), Collections.unmodifiableCollection(images));
            }
            return images;
        }
    }

    public MockImageSupport(CloudProvider provider) { this.provider = provider; }

    @Override
    public void downloadImage(@Nonnull String machineImageId, @Nonnull OutputStream toOutput) throws CloudException, InternalException {
        throw new OperationNotSupportedException("You cannot download images from this fake cloud");
    }

    @Override
    public MachineImage getMachineImage(@Nonnull String machineImageId) throws CloudException, InternalException {
        for( MachineImage img : searchMachineImages(null, null, null) ) {
            if( img.getProviderMachineImageId().equals(machineImageId) ) {
                return img;
            }
        }
        return null;
    }

    @Override
    public @Nonnull String getProviderTermForImage(@Nonnull Locale locale) {
        return "machine image";
    }

    @Override
    public boolean hasPublicLibrary() {
        return true;
    }

    @Override
    public @Nonnull AsynchronousTask<String> imageVirtualMachine(String vmId, String name, String description) throws CloudException, InternalException {
        throw new OperationNotSupportedException("Imaging not supported"); // TODO: change this
    }

    @Override
    public @Nonnull AsynchronousTask<String> imageVirtualMachineToStorage(String vmId, String name, String description, String directory) throws CloudException, InternalException {
        throw new OperationNotSupportedException("No ability to image virtual machine to a storage location");
    }

    @Override
    public @Nonnull String installImageFromUpload(@Nonnull MachineImageFormat format, @Nonnull InputStream imageStream) throws CloudException, InternalException {
        throw new OperationNotSupportedException("No ability to install image from an upload");
    }

    @Override
    public boolean isImageSharedWithPublic(@Nonnull String machineImageId) throws CloudException, InternalException {
        return true;
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        return true;
    }

    @Override
    public @Nonnull Iterable<MachineImage> listMachineImages() throws CloudException, InternalException {
        return Collections.emptyList();
    }

    @Override
    public @Nonnull Iterable<MachineImage> listMachineImagesOwnedBy(String accountId) throws CloudException, InternalException {
        if( accountId == null ) {
            ProviderContext ctx = provider.getContext();

            if( ctx == null ) {
                throw new CloudException("No context was provided for this request");
            }
            return getPublicImages(ctx);
        }
        return Collections.emptyList();
    }

    @Override
    public @Nonnull Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
        return Collections.singletonList(MachineImageFormat.OVF);
    }

    @Override
    public @Nonnull Iterable<String> listShares(@Nonnull String forMachineImageId) throws CloudException, InternalException {
        return Collections.emptyList();
    }

    @Override
    public @Nonnull String registerMachineImage(String atStorageLocation) throws CloudException, InternalException {
        throw new OperationNotSupportedException("No ability to register machine images in storage");
    }

    @Override
    public void remove(@Nonnull String machineImageId) throws CloudException, InternalException {
        throw new OperationNotSupportedException("No removal support yet"); // TODO: implement me
    }

    @Override
    public @Nonnull Iterable<MachineImage> searchMachineImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture) throws CloudException, InternalException {
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        ArrayList<MachineImage> images = new ArrayList<MachineImage>();

        for( MachineImage img : getPublicImages(ctx) ) {
            if( img != null ) {
                if( keyword != null ) {
                    if( !img.getName().toLowerCase().contains(keyword) && !img.getDescription().toLowerCase().contains(keyword) ) {
                        continue;
                    }
                }
                if( platform != null && !platform.equals(Platform.UNKNOWN) ) {
                    Platform mine = img.getPlatform();

                    if( platform.isWindows() && !mine.isWindows() ) {
                        continue;
                    }
                    if( platform.isUnix() && !mine.isUnix() ) {
                        continue;
                    }
                    if( platform.isBsd() && !mine.isBsd() ) {
                        continue;
                    }
                    if( platform.isLinux() && !mine.isLinux() ) {
                        continue;
                    }
                    if( platform.equals(Platform.UNIX) ) {
                        if( !mine.isUnix() ) {
                            continue;
                        }
                    }
                    else if( !platform.equals(mine) ) {
                        continue;
                    }
                }
                if( architecture != null && !img.getArchitecture().equals(architecture) ) {
                    continue;
                }
                images.add(img);
            }
        }
        return images;
    }

    @Override
    public void shareMachineImage(@Nonnull String machineImageId, @Nonnull String withAccountId, boolean allow) throws CloudException, InternalException {
        throw new OperationNotSupportedException("No ability to share a machine image with other accounts");
    }

    @Override
    public boolean supportsCustomImages() {
        return false;
    }

    @Override
    public boolean supportsImageSharing() {
        return false;
    }

    @Override
    public boolean supportsImageSharingWithPublic() {
        return false;
    }

    @Override
    public @Nonnull String transfer(@Nonnull CloudProvider fromCloud, @Nonnull String machineImageId) throws CloudException, InternalException {
        throw new OperationNotSupportedException("Cannot transfer machine images");
    }

    @Override
    public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
        return new String[0];
    }
}
