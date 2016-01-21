package com.gradleware.tooling.toolingmodel.repository.internal;

import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.ModelRequest;
import com.gradleware.tooling.toolingclient.ToolingClient;
import com.gradleware.tooling.toolingmodel.OmniEclipseWorkspace;
import com.gradleware.tooling.toolingmodel.repository.CompositeModelRepository;
import com.gradleware.tooling.toolingmodel.repository.Environment;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

public class DefaultCompositeModelRepository implements CompositeModelRepository {

    private ImmutableList<FixedRequestAttributes> requestAttributes;
    private ToolingClient toolingClient;
    private Environment environment;

    public DefaultCompositeModelRepository(List<FixedRequestAttributes> requestAttributes, ToolingClient toolingClient, Environment environment) {
        if (requestAttributes.size() != 1) {
            throw new IllegalArgumentException("Composite builds can currently contain exactly one project");
        }
        this.requestAttributes = ImmutableList.copyOf(requestAttributes);
        this.toolingClient = Preconditions.checkNotNull(toolingClient);
        this.environment = Preconditions.checkNotNull(environment);
    }

    @Override
    public OmniEclipseWorkspace fetchEclipseWorkspace(TransientRequestAttributes transientRequestAttributes, FetchStrategy fetchStrategy) {
        ModelRequest<EclipseProject> modelRequest = this.toolingClient.newModelRequest(EclipseProject.class);
        this.requestAttributes.get(0).apply(modelRequest);
        transientRequestAttributes.apply(modelRequest);
        EclipseProject eclipseProject = modelRequest.executeAndWait();
        return DefaultOmniEclipseWorkspace.from(DefaultOmniEclipseProject.from(eclipseProject, false));
    }

}
