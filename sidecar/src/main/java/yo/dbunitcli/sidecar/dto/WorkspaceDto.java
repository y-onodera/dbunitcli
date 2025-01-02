package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;
import yo.dbunitcli.resource.FileResources;

@Serdeable
public class WorkspaceDto {
    private ParametersDto parameterList = new ParametersDto();

    private ResourcesDto resources = new ResourcesDto();

    private FileResources.FileResourcesContext context = FileResources.getContext();

    public ParametersDto getParameterList() {
        return this.parameterList;
    }

    public void setParameterList(final ParametersDto parameterList) {
        this.parameterList = parameterList;
    }

    public ResourcesDto getResources() {
        return this.resources;
    }

    public void setResources(final ResourcesDto resources) {
        this.resources = resources;
    }

    public FileResources.FileResourcesContext getContext() {
        return this.context;
    }

    public void setContext(final FileResources.FileResourcesContext context) {
        this.context = context;
    }
}
