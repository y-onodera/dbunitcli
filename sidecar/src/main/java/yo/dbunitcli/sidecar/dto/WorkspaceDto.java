package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class WorkspaceDto {
    private ParametersDto parameterList = new ParametersDto();

    private ResourcesDto resources = new ResourcesDto();

    private DatasourceFilesDto datasourceFiles = new DatasourceFilesDto();

    private ContextDto context = new ContextDto();

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

    public DatasourceFilesDto getDatasourceFiles() {
        return this.datasourceFiles;
    }

    public void setDatasourceFiles(final DatasourceFilesDto datasourceFiles) {
        this.datasourceFiles = datasourceFiles;
    }

    public ContextDto getContext() {
        return this.context;
    }

    public void setContext(final ContextDto context) {
        this.context = context;
    }

}
