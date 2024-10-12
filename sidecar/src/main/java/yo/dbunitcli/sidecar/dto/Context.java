package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Context(String workspace, String datasetBase, String resultBase) {
}
