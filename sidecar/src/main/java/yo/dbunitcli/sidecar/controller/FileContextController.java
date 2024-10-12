package yo.dbunitcli.sidecar.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.serde.ObjectMapper;
import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.Context;

import java.io.IOException;

@Controller("context")
public class FileContextController {

    @Get(uri = "paths", produces = MediaType.APPLICATION_JSON)
    public String paths() throws IOException {
        return ObjectMapper
                .getDefault()
                .writeValueAsString(new Context(
                        FileResources.baseDir().getPath()
                        , FileResources.datasetDir().getPath()
                        , FileResources.resultDir().getPath()
                ));
    }

}
