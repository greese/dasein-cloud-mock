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
import org.dasein.cloud.compute.VirtualMachine;
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
import java.util.Random;
import java.util.UUID;

/**
 * Implements mocked up image management services for the Dasein Cloud mock cloud.
 * <p>Created by George Reese: 10/18/12 12:45 PM</p>
 * @author George Reese
 * @version 2012.09 initial version
 * @since 2012.09
 */
public class MockImageSupport implements MachineImageSupport {
    private CloudProvider provider;

    static private final Map<String,Map<String,Collection<MachineImage>>> customImages = new HashMap<String, Map<String, Collection<MachineImage>>>();
    static private final Map<String,Map<String,Collection<MachineImage>>> publicImages = new HashMap<String, Map<String, Collection<MachineImage>>>();
    static private final Random random = new Random();

    static private void addCustomImage(@Nonnull String endpoint, @Nonnull String regionId, @Nonnull MachineImage img) {
        synchronized( customImages ) {
            Map<String,Collection<MachineImage>> cloud = customImages.get(endpoint);

            if( cloud == null ) {
                cloud = new HashMap<String, Collection<MachineImage>>();
                customImages.put(endpoint, cloud);
            }
            Collection<MachineImage> images = cloud.get(regionId);

            if( images == null ) {
                images = new ArrayList<MachineImage>();
                cloud.put(regionId, images);
            }
            images.add(img);
        }
    }

    static private @Nonnull Collection<MachineImage> getCustomImages(@Nonnull ProviderContext ctx) {
        synchronized( customImages ) {
            Map<String,Collection<MachineImage>> cloud = customImages.get(ctx.getEndpoint());

            if( cloud == null ) {
                cloud = new HashMap<String, Collection<MachineImage>>();
                customImages.put(ctx.getEndpoint(), cloud);
            }
            Collection<MachineImage> images = cloud.get(ctx.getRegionId());

            if( images == null ) {
                images = new ArrayList<MachineImage>();
                cloud.put(ctx.getRegionId(), images);
            }
            ArrayList<MachineImage> custom = new ArrayList<MachineImage>();

            for( MachineImage img : images ) {
                if( img.getProviderOwnerId() != null && img.getProviderOwnerId().equals(ctx.getAccountNumber()) ) {
                    custom.add(img);
                }
            }
            return custom;
        }
    }

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
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was provided for this request");
        }
        for( MachineImage img : getCustomImages(ctx) ) {
            if( img.getProviderMachineImageId().equals(machineImageId) ) {
                return img;
            }
        }

        for( MachineImage img : getPublicImages(ctx) ) {
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
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        final String endpoint = ctx.getEndpoint();
        final String regionId = ctx.getRegionId();

        if( endpoint == null || regionId == null ) {
            throw new CloudException("Both endpoint and region must be set for this request");
        }
        @SuppressWarnings("ConstantConditions") VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(vmId);

        if( vm == null ) {
            throw new CloudException("No such virtual machine: " + vmId);
        }
        final MachineImage image = new MachineImage();

        image.setArchitecture(Architecture.I64);
        image.setCurrentState(MachineImageState.PENDING);
        image.setDescription(description);
        image.setName(name);
        image.setPlatform(vm.getPlatform());
        image.setProviderMachineImageId(UUID.randomUUID().toString());
        image.setProviderOwnerId(ctx.getAccountNumber());
        image.setProviderRegionId(ctx.getRegionId());
        image.setSoftware("");
        image.setType(MachineImageType.VOLUME);

        final AsynchronousTask<String> task = new AsynchronousTask<String>();

        Thread t = new Thread() {
            public void run() {
                try { Thread.sleep(15000L + (random.nextInt(45) * 1000L)); }
                catch( InterruptedException ignore ) { }
                task.setPercentComplete(50);
                try { Thread.sleep(15000L + (random.nextInt(45) * 1000L)); }
                catch( InterruptedException ignore ) { }
                addCustomImage(endpoint, regionId, image);
                task.completeWithResult(image.getProviderMachineImageId());
                try { Thread.sleep(15000L + (random.nextInt(45) * 1000L)); }
                catch( InterruptedException ignore ) { }
                image.setCurrentState(MachineImageState.ACTIVE);
            }
        };

        t.setName("Image Builder");
        t.setDaemon(true);
        t.start();
        return task;
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
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        return listMachineImagesOwnedBy(ctx.getEffectiveAccountNumber());
    }

    @Override
    public @Nonnull Iterable<MachineImage> listMachineImagesOwnedBy(String accountId) throws CloudException, InternalException {
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was provided for this request");
        }
        if( accountId == null ) {
            return getPublicImages(ctx);
        }
        return getCustomImages(ctx);
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
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        synchronized( customImages ) {
            Map<String,Collection<MachineImage>> cloud = customImages.get(ctx.getEndpoint());

            if( cloud == null ) {
                cloud = new HashMap<String, Collection<MachineImage>>();
                customImages.put(ctx.getEndpoint(), cloud);
            }
            Collection<MachineImage> images = cloud.get(ctx.getRegionId());
            ArrayList<MachineImage> newImages = new ArrayList<MachineImage>();

            if( images == null ) {
                images = new ArrayList<MachineImage>();
                cloud.put(ctx.getRegionId(), images);
                throw new CloudException("No such image: " + machineImageId);
            }
            boolean found = false;

            for( MachineImage img : images ) {
                if( img.getProviderMachineImageId().equals(machineImageId) ) {
                    found = true;
                }
                else {
                    newImages.add(img);
                }
            }
            if( !found ) {
                MachineImage img = getMachineImage(machineImageId);

                if( img == null ) {
                    throw new CloudException("No such image: " + machineImageId);
                }
                if( ctx.getEffectiveAccountNumber().equals(img.getProviderOwnerId()) ) {
                    throw new CloudException("You do not own that image");
                }
                throw new CloudException("Removal failed for no discernable reason");
            }
            cloud.put(ctx.getRegionId(), newImages);
        }
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
        return true;
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
