package yo.dbunitcli.sidecar;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.Micronaut;
import io.micronaut.serde.annotation.SerdeImport;
import yo.dbunitcli.application.command.Parameterize;
import yo.dbunitcli.application.Option;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.domain.project.Workspace;

import java.util.Arrays;


@SerdeImport(value = Option.Arg.class)
@SerdeImport(value = Option.Attribute.class)
@Factory
public class Application {

    public static void main(final String[] args) {
        if (args.length > 0 && args[0].startsWith("-cli")) {
            Parameterize.main(Arrays.copyOfRange(args, 1, args.length));
        } else {
            Micronaut.run(Application.class, args);
        }
    }

    @Context
    public Workspace load(final ApplicationContext context) {
        final String workspace = context.getEnvironment()
                .get(FileResources.PROPERTY_WORKSPACE, String.class)
                .orElse(".");
        System.setProperty(FileResources.PROPERTY_WORKSPACE, workspace);
        context.getEnvironment()
                .get(FileResources.PROPERTY_DATASET_BASE, String.class)
                .filter(yo.dbunitcli.Strings::isNotEmpty)
                .ifPresent(v -> System.setProperty(FileResources.PROPERTY_DATASET_BASE, v));
        context.getEnvironment()
                .get(FileResources.PROPERTY_RESULT_BASE, String.class)
                .filter(yo.dbunitcli.Strings::isNotEmpty)
                .ifPresent(v -> System.setProperty(FileResources.PROPERTY_RESULT_BASE, v));
        return Workspace.builder().setPath(workspace).build();
    }
}