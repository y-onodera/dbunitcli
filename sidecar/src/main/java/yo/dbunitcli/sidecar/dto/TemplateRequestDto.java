package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class TemplateRequestDto extends ResourceSaveRequest<TemplateContentDto> {
}
