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
import org.dasein.cloud.Requirement;
import org.dasein.cloud.compute.AbstractImageSupport;
import org.dasein.cloud.compute.Architecture;
import org.dasein.cloud.compute.ImageClass;
import org.dasein.cloud.compute.ImageCreateOptions;
import org.dasein.cloud.compute.ImageFilterOptions;
import org.dasein.cloud.compute.MachineImage;
import org.dasein.cloud.compute.MachineImageFormat;
import org.dasein.cloud.compute.MachineImageState;
import org.dasein.cloud.compute.MachineImageType;
import org.dasein.cloud.compute.Platform;
import org.dasein.cloud.compute.VirtualMachine;
import org.dasein.cloud.identity.ServiceAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public class MockImageSupport extends AbstractImageSupport {
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
                img.setProviderOwnerId("--cloud--");
                img.setProviderRegionId(ctx.getRegionId());
                img.setImageClass(ImageClass.MACHINE);
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
                img.setImageClass(ImageClass.MACHINE);
                img.setProviderMachineImageId(ctx.getRegionId() + "-2");
                img.setProviderOwnerId("--cloud--");
                img.setProviderRegionId(ctx.getRegionId());
                images.add(img);

                cloud.put(ctx.getRegionId(), Collections.unmodifiableCollection(images));
            }
            return images;
        }
    }

    public MockImageSupport(CloudProvider provider) {
        super(provider);
        this.provider = provider;
    }

    @Override
    public void addImageShare(@Nonnull String providerImageId, @Nonnull String accountNumber) throws CloudException, InternalException {
        throw new OperationNotSupportedException("No sharing yet");
    }

    @Override
    public void addPublicShare(@Nonnull String providerImageId) throws CloudException, InternalException {
        throw new OperationNotSupportedException("No sharing yet");
    }

    @Override
    public @Nonnull String bundleVirtualMachine(@Nonnull String virtualMachineId, @Nonnull MachineImageFormat format, @Nonnull String bucket, @Nonnull String name) throws CloudException, InternalException {
        throw new OperationNotSupportedException("No bundling yet");
    }

    @Override
    public void bundleVirtualMachineAsync(@Nonnull String virtualMachineId, @Nonnull MachineImageFormat format, @Nonnull String bucket, @Nonnull String name, @Nonnull AsynchronousTask<String> trackingTask) throws CloudException, InternalException {
        throw new OperationNotSupportedException("No bundling yet");
    }

    @Override
    public MachineImage getImage(@Nonnull String providerImageId) throws CloudException, InternalException {
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was provided for this request");
        }
        for( MachineImage img : getCustomImages(ctx) ) {
            if( img.getProviderMachineImageId().equals(providerImageId) ) {
                return img;
            }
        }

        for( MachineImage img : getPublicImages(ctx) ) {
            if( img.getProviderMachineImageId().equals(providerImageId) ) {
                return img;
            }
        }
        return null;
    }

    @Override
    public @Nonnull String getProviderTermForImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
        return (cls.name().toLowerCase() + " image");
    }

    @Override
    public boolean hasPublicLibrary() {
        return true;
    }

    @Override
    public @Nonnull Requirement identifyLocalBundlingRequirement() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Override
    protected @Nonnull MachineImage capture(@Nonnull ImageCreateOptions options, @Nullable AsynchronousTask<MachineImage> optionalTask) throws CloudException, InternalException {
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        @SuppressWarnings("ConstantConditions") VirtualMachine vm = provider.getComputeServices().getVirtualMachineSupport().getVirtualMachine(options.getVirtualMachineId());

        if( vm == null ) {
            throw new CloudException("No such virtual machine: " + options.getVirtualMachineId());
        }
        MachineImage image = new MachineImage();
        String endpoint = ctx.getEndpoint();
        String regionId = ctx.getRegionId();

        if( endpoint == null || regionId == null ) {
            throw new CloudException("Both endpoint and region must be set for this request");
        }
        image.setArchitecture(Architecture.I64);
        image.setCurrentState(MachineImageState.PENDING);
        image.setDescription(options.getDescription());
        image.setName(options.getName());
        image.setPlatform(vm.getPlatform());
        image.setProviderMachineImageId(UUID.randomUUID().toString());
        image.setProviderOwnerId(ctx.getAccountNumber());
        image.setProviderRegionId(ctx.getRegionId());
        image.setSoftware("");
        image.setImageClass(ImageClass.MACHINE);
        image.setType(MachineImageType.VOLUME);

        try { Thread.sleep(15000L + (random.nextInt(45) * 1000L)); }
        catch( InterruptedException ignore ) { }
        if( optionalTask != null ) {
            optionalTask.setPercentComplete(50);
        }
        try { Thread.sleep(15000L + (random.nextInt(45) * 1000L)); }
        catch( InterruptedException ignore ) { }
        addCustomImage(endpoint, regionId, image);
        if( optionalTask != null ) {
            optionalTask.completeWithResult(image);
        }
        try { Thread.sleep(15000L + (random.nextInt(45) * 1000L)); }
        catch( InterruptedException ignore ) { }
        image.setCurrentState(MachineImageState.ACTIVE);
        return image;
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
    public @Nonnull Iterable<MachineImage> listImages(@Nullable ImageFilterOptions options) throws CloudException, InternalException {
        ImageClass cls = (options == null ? null : options.getImageClass());

        if( cls != null && !cls.equals(ImageClass.MACHINE) ) {
            return Collections.emptyList();
        }
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        String account = (options == null ? null : options.getAccountNumber());

        if( account == null ) {
            account = ctx.getAccountNumber();
        }
        return listMachineImagesOwnedBy(account);
    }

    @Override
    public @Nonnull Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
        return Collections.singletonList(MachineImageFormat.OVF);
    }

    @Override
    public @Nonnull Iterable<MachineImageFormat> listSupportedFormatsForBundling() throws CloudException, InternalException {
        return Collections.emptyList();
    }

    @Override
    public @Nonnull Iterable<String> listShares(@Nonnull String forMachineImageId) throws CloudException, InternalException {
        return Collections.emptyList();
    }

    @Override
    public @Nonnull Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
        return Collections.singletonList(ImageClass.MACHINE);
    }

    @Override
    public @Nonnull Iterable<MachineImageType> listSupportedImageTypes() throws CloudException, InternalException {
        return Collections.singletonList(MachineImageType.VOLUME);
    }

    @Override
    public void remove(@Nonnull String providerImageId, boolean checkState) throws CloudException, InternalException {
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
                throw new CloudException("No such image: " + providerImageId);
            }
            boolean found = false;

            for( MachineImage img : images ) {
                if( img.getProviderMachineImageId().equals(providerImageId) ) {
                    found = true;
                }
                else {
                    newImages.add(img);
                }
            }
            if( !found ) {
                MachineImage img = getImage(providerImageId);

                if( img == null ) {
                    throw new CloudException("No such image: " + providerImageId);
                }
                if( ctx.getEffectiveAccountNumber().equals(img.getProviderOwnerId()) ) {
                    throw new CloudException("You do not own that image");
                }
                throw new CloudException("Removal failed for no discernable reason");
            }
            cloud.put(ctx.getRegionId(), newImages);
        }
    }

    /*
    private boolean matches(MachineImage img, @Nullable String accountNumber, @Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture) {
        if( accountNumber != null && !accountNumber.equals(img.getProviderOwnerId()) ) {
            return false;
        }
        if( keyword != null ) {
            if( !img.getName().toLowerCase().contains(keyword) && !img.getDescription().toLowerCase().contains(keyword) ) {
                return false;
            }
        }
        if( platform != null && !platform.equals(Platform.UNKNOWN) ) {
            Platform mine = img.getPlatform();

            if( platform.isWindows() && !mine.isWindows() ) {
                return false;
            }
            if( platform.isUnix() && !mine.isUnix() ) {
                return false;
            }
            if( platform.isBsd() && !mine.isBsd() ) {
                return false;
            }
            if( platform.isLinux() && !mine.isLinux() ) {
                return false;
            }
            if( platform.equals(Platform.UNIX) ) {
                if( !mine.isUnix() ) {
                    return false;
                }
            }
            else if( !platform.equals(mine) ) {
                return false;
            }
        }
        return !(architecture != null && !img.getArchitecture().equals(architecture));
    }
    */

    @Override
    public @Nonnull Iterable<MachineImage> searchMachineImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture) throws CloudException, InternalException {
        HashMap<String,MachineImage> images = new HashMap<String, MachineImage>();

        for( MachineImage img : searchImages(null, keyword, platform, architecture, ImageClass.MACHINE) ) {
            images.put(img.getProviderMachineImageId(), img);
        }
        for( MachineImage img : searchPublicImages(keyword, platform, architecture, ImageClass.MACHINE) ) {
            images.put(img.getProviderMachineImageId(), img);
        }
        return images.values();
    }

    @Override
    public @Nonnull Iterable<MachineImage> searchImages(@Nullable String accountNumber, @Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture, @Nullable ImageClass... imageClasses) throws CloudException, InternalException {
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        if( imageClasses == null ) {
            imageClasses = ImageClass.values();
        }
        ArrayList<MachineImage> images = new ArrayList<MachineImage>();
        ImageFilterOptions options = null;

        if( accountNumber != null ) {
            ImageFilterOptions.getInstance().withAccountNumber(accountNumber);
        }
        Iterable<MachineImage> list = listImages(options);

        for( MachineImage img : list ) {
            if( img != null && matches( img, keyword, platform, architecture, imageClasses) ) {
                images.add(img);
            }
        }
        return images;
    }

    @Override
    public @Nonnull Iterable<MachineImage> searchPublicImages(@Nullable String keyword, @Nullable Platform platform, @Nullable Architecture architecture, @Nullable ImageClass... imageClasses) throws CloudException, InternalException {
        ProviderContext ctx = provider.getContext();

        if( ctx == null ) {
            throw new CloudException("No context was set for this request");
        }
        if( imageClasses == null || imageClasses.length < 1 ) {
            imageClasses = ImageClass.values();
        }
        ArrayList<MachineImage> images = new ArrayList<MachineImage>();

        for( MachineImage img : getPublicImages(ctx) ) {
            if( img != null &&  matches(img, keyword, platform, architecture, imageClasses) ) {
                images.add(img);
            }
        }
        return images;
    }


    @Override
    public boolean supportsCustomImages() {
        return true;
    }

    @Override
    public boolean supportsDirectImageUpload() throws CloudException, InternalException {
        return false;
    }

    @Override
    public boolean supportsImageCapture(@Nonnull MachineImageType type) throws CloudException, InternalException {
        return type.equals(MachineImageType.VOLUME);
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
    public boolean supportsPublicLibrary(@Nonnull ImageClass cls) throws CloudException, InternalException {
        return cls.equals(ImageClass.MACHINE);
    }

    @Override
    public @Nonnull String[] mapServiceAction(@Nonnull ServiceAction action) {
        return new String[0];
    }
}
