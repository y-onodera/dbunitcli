package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

/**
 * parameterizeのオプションが参照するファイル
 *
 * @param usedBy 同じファイルを参照する他のparameterizeのオプション名。空でなければ削除しない
 */
@Serdeable
public record ParameterizeFileDto(String option, String path, List<String> usedBy) {
}
