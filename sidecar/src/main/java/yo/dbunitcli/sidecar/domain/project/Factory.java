package yo.dbunitcli.sidecar.domain.project;

import io.micronaut.context.annotation.Context;

@io.micronaut.context.annotation.Factory
public class Factory {

    @Context
    public Workspace load() {
        return Workspace.builder().setPath("src/test/resources/workspace/sample").build();
    }

}
