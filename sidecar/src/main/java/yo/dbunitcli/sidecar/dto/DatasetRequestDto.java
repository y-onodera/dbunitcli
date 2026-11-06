package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class DatasetRequestDto extends ResourceSaveRequest<DatasetSettingsDto> {
}
