package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * 読み込まれるテーブルと、その行の読込元（ファイルパス・setting適用前のテーブル名）の組
 *
 * @param tableName       読み込まれるテーブルの名前
 * @param filePath        読込元のファイルのパス。srcがディレクトリならsrcからの相対パス（区切りは/）
 * @param matchPath       -regInclude・-regExcludeを当てるパス
 * @param sourceTableName 読込元のテーブル名（-regTableInclude・-regTableExcludeを当てる名前）
 */
@Serdeable
public record DatasetTableSourceDto(String tableName, String filePath, String matchPath, String sourceTableName) {
}
