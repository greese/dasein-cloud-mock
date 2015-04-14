/*
 * *
 *  * Copyright (C) 2009-2015 Dell, Inc.
 *  * See annotations for authorship information
 *  *
 *  * ====================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  * ====================================================================
 *
 */

package org.dasein.cloud.mock.compute.image;

import org.dasein.cloud.*;
import org.dasein.cloud.compute.*;
import org.dasein.cloud.mock.AbstractMockCapabilities;
import org.dasein.cloud.mock.MockCloud;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Jeffrey Yan on 4/2/2015.
 *
 * @author Jeffrey Yan
 * @since 2015.05.1
 */
public class MockImageCapabilities extends AbstractMockCapabilities implements ImageCapabilities {

    private Map<VmState, Boolean> canBundle;
    private Map<VmState, Boolean> canImage;
    private Map<ImageClass, String> providerTermForImage;
    private Map<ImageClass, String> providerTermForCustomImage;
    private VisibleScope imageVisibleScope;
    private Requirement localBundlingRequirement;
    private List<MachineImageFormat> supportedFormats;
    private List<MachineImageFormat> supportedFormatsForBundling;
    private List<ImageClass> supportedImageClasses;
    private List<MachineImageType> supportedImageTypes;
    private boolean supportsDirectImageUpload;
    private Map<MachineImageType, Boolean> supportsImageCapture;
    private boolean supportsImageCopy;
    private boolean supportsImageSharing;
    private boolean supportsImageSharingWithPublic;
    private boolean supportsListingAllRegions;
    private Map<ImageClass, Boolean> supportsPublicLibrary;
    private boolean imageCaptureDestroysVM;

    public MockImageCapabilities(@Nonnull MockCloud provider) {
        super(provider);
    }

    @Override
    public boolean canBundle(@Nonnull VmState fromState) throws CloudException, InternalException {
        if (canBundle.containsKey(fromState)) {
            return canBundle.get(fromState);
        }
        return false;
    }

    @Override
    public boolean canImage(@Nonnull VmState fromState) throws CloudException, InternalException {
        if (canImage.containsKey(fromState)) {
            return canImage.get(fromState);
        }
        return false;
    }

    @Nonnull
    @Override
    public String getProviderTermForImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
        if (providerTermForImage != null && providerTermForImage.containsKey(cls)) {
            return providerTermForImage.get(cls);
        }
        return cls.name().toLowerCase() + "image";
    }

    @Nonnull
    @Override
    public String getProviderTermForCustomImage(@Nonnull Locale locale, @Nonnull ImageClass cls) {
        if (providerTermForCustomImage != null && providerTermForCustomImage.containsKey(cls)) {
            return providerTermForCustomImage.get(cls);
        }
        return cls.name().toLowerCase() + "image";
    }

    @Nullable
    @Override
    public VisibleScope getImageVisibleScope() {
        return imageVisibleScope;
    }

    @Nonnull
    @Override
    public Requirement identifyLocalBundlingRequirement() throws CloudException, InternalException {
        return localBundlingRequirement;
    }

    @Nonnull
    @Override
    public Iterable<MachineImageFormat> listSupportedFormats() throws CloudException, InternalException {
        return supportedFormats;
    }

    @Nonnull
    @Override
    public Iterable<MachineImageFormat> listSupportedFormatsForBundling() throws CloudException, InternalException {
        return supportedFormatsForBundling;
    }

    @Nonnull
    @Override
    public Iterable<ImageClass> listSupportedImageClasses() throws CloudException, InternalException {
        return supportedImageClasses;
    }

    @Nonnull
    @Override
    public Iterable<MachineImageType> listSupportedImageTypes() throws CloudException, InternalException {
        return supportedImageTypes;
    }

    @Override
    public boolean supportsDirectImageUpload() throws CloudException, InternalException {
        return supportsDirectImageUpload;
    }

    @Override
    public boolean supportsImageCapture(@Nonnull MachineImageType type) throws CloudException, InternalException {
        if (supportsImageCapture.containsKey(type)) {
            return supportsImageCapture.get(type);
        }
        return false;
    }

    @Override
    public boolean supportsImageCopy() throws CloudException, InternalException {
        return supportsImageCopy;
    }

    @Override
    public boolean supportsImageSharing() throws CloudException, InternalException {
        return supportsImageSharing;
    }

    @Override
    public boolean supportsImageSharingWithPublic() throws CloudException, InternalException {
        return supportsImageSharingWithPublic;
    }

    @Override
    public boolean supportsListingAllRegions() throws CloudException, InternalException {
        return supportsListingAllRegions;
    }

    @Override
    public boolean supportsPublicLibrary(@Nonnull ImageClass cls) throws CloudException, InternalException {
        if (supportsPublicLibrary.containsKey(cls)) {
            return supportsPublicLibrary.get(cls);
        }
        return false;
    }

    @Override
    public boolean imageCaptureDestroysVM() throws CloudException, InternalException {
        return imageCaptureDestroysVM;
    }
}
