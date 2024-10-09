package yo.dbunitcli.sidecar;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.Micronaut;
import io.micronaut.serde.annotation.SerdeImport;
import yo.dbunitcli.application.option.Option;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.Workspace;


@SerdeImport(value = Option.Arg.class)
@SerdeImport(value = Option.Attribute.class)
@Factory
public class Application {

    public static void main(final String[] args) {
        Micronaut.run(Application.class, args);
    }

    @Context
    public Workspace load(final ApplicationContext context) {
        return Workspace.builder().setPath(context.getEnvironment()
                .get(FileResources.PROPERTY_WORKSPACE, String.class, ".")).build();
    }
}