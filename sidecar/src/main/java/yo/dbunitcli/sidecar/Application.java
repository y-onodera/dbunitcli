package yo.dbunitcli.sidecar;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.Micronaut;
import io.micronaut.serde.annotation.SerdeImport;
import yo.dbunitcli.application.*;
import yo.dbunitcli.application.dto.*;
import yo.dbunitcli.sidecar.domain.project.Workspace;


@SerdeImport(value = CommandDto.class)
@SerdeImport(value = ParameterizeDto.class)
@SerdeImport(value = ConvertDto.class)
@SerdeImport(value = CompareDto.class)
@SerdeImport(value = RunDto.class)
@SerdeImport(value = GenerateDto.class)
@SerdeImport(value = TemplateRenderDto.class)
@SerdeImport(value = DataSetLoadDto.class)
@SerdeImport(value = DataSetConverterDto.class)
@SerdeImport(value = JdbcDto.class)
@SerdeImport(value = ImageCompareDto.class)
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