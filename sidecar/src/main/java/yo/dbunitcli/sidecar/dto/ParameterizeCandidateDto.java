package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

/** parameterizeの対象に選べるオプションとその保存値 */
@Serdeable
public record ParameterizeCandidateDto(String key, String value) {
}
