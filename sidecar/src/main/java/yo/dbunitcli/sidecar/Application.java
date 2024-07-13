package yo.dbunitcli.sidecar;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.Micronaut;
import yo.dbunitcli.sidecar.domain.project.Workspace;


@Factory
public class Application {

    public static void main(final String[] args) {
        Micronaut.run(Application.class, args);

    }

    @Context
    public Workspace load(final ApplicationContext context) {
        return Workspace.builder().setPath(context.getEnvironment()
                .get("yo.dbunit.cli.sidecar.workspace", String.class, ".")).build();
    }
}