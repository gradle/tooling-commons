package com.gradleware.tooling.toolingmodel.repository.internal;

import com.gradleware.tooling.toolingmodel.OmniEclipseLinkedResource;
import org.gradle.tooling.model.eclipse.EclipseLinkedResource;

/**
 * Default implementation of the {@link OmniEclipseLinkedResource} interface.
 */
public final class DefaultOmniEclipseLinkedResource implements OmniEclipseLinkedResource {

    private final String name;
    private final String type;
    private final String location;
    private final String locationUri;

    private DefaultOmniEclipseLinkedResource(String name, String type, String location, String locationUri) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.locationUri = locationUri;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getLocation() {
        return this.location;
    }

    @Override
    public String getLocationUri() {
        return this.locationUri;
    }

    public static DefaultOmniEclipseLinkedResource from(EclipseLinkedResource linkedResource) {
        return new DefaultOmniEclipseLinkedResource(
                linkedResource.getName(),
                linkedResource.getType(),
                linkedResource.getLocation(),
                linkedResource.getLocationUri());
    }

}
