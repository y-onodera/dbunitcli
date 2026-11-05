package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.ResourcesDto;

import java.io.File;

public record Resources(
        ResourceFile jdbc
        , ResourceFile datasetSetting
        , ResourceFile template
        , ResourceFile xlsxSchema
        , ResourceFile fixedColumnDef
        , ResourceFile sql
        , ResourceFile table
        , ResourceFile csvq) {

    public static Builder builder() {
        return new Builder();
    }

    public ResourcesDto toDto() {
        final ResourcesDto result = new ResourcesDto();
        result.setDatasetSettings(this.datasetSetting().list());
        result.setJdbcFiles(this.jdbc().list());
        result.setTemplateFiles(this.template().list());
        result.setXlsxSchemas(this.xlsxSchema().list());
        result.setFixedColumnDefs(this.fixedColumnDef().list());
        result.setSqlFiles(this.sql().list());
        result.setTableFiles(this.table().list());
        result.setCsvqFiles(this.csvq().list());
        return result;
    }

    public static class Builder {
        private File workspace;

        public Resources build() {
            return new Resources(
                    ResourceFile.of(this.workspace, FileResources.RESOURCES_JDBC_PATH, FileResources::searchJdbc),
                    ResourceFile.of(this.workspace, FileResources.RESOURCES_SETTING_PATH, FileResources::searchSetting),
                    ResourceFile.of(this.workspace, FileResources.RESOURCES_TEMPLATE_PATH, FileResources::searchTemplate),
                    ResourceFile.of(this.workspace, FileResources.RESOURCES_XLSX_SCHEMA_PATH, FileResources::searchXlsxSchema),
                    ResourceFile.of(this.workspace, FileResources.RESOURCES_FIXED_COLUMN_DEF_PATH, FileResources::searchFixedColumnDef),
                    ResourceFile.of(this.workspace, FileResources.RESOURCES_SQL_PATH, FileResources::searchSql),
                    ResourceFile.of(this.workspace, FileResources.RESOURCES_TABLE_PATH, FileResources::searchTable),
                    ResourceFile.of(this.workspace, FileResources.RESOURCES_CSVQ_PATH, FileResources::searchCsvq)
            );
        }

        public Builder workspace(final File workspace) {
            this.workspace = workspace;
            return this;
        }
    }
}
