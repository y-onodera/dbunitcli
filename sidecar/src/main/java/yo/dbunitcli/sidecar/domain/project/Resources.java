package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.resource.FileResources;
import yo.dbunitcli.sidecar.dto.ResourcesDto;

import java.io.File;
import java.util.function.Function;

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
                    this.resourceFile(FileResources.RESOURCES_JDBC_PATH, FileResources::searchJdbc),
                    this.resourceFile(FileResources.RESOURCES_SETTING_PATH, FileResources::searchSetting),
                    this.resourceFile(FileResources.RESOURCES_TEMPLATE_PATH, FileResources::searchTemplate),
                    this.resourceFile(FileResources.RESOURCES_XLSX_SCHEMA_PATH, FileResources::searchXlsxSchema),
                    this.resourceFile(FileResources.RESOURCES_FIXED_COLUMN_DEF_PATH, FileResources::searchFixedColumnDef),
                    this.resourceFile(FileResources.RESOURCES_SQL_PATH, FileResources::searchSql),
                    this.resourceFile(FileResources.RESOURCES_TABLE_PATH, FileResources::searchTable),
                    this.resourceFile(FileResources.RESOURCES_CSVQ_PATH, FileResources::searchCsvq)
            );
        }

        public Builder workspace(final File workspace) {
            this.workspace = workspace;
            return this;
        }

        private ResourceFile resourceFile(final String path, final Function<String, File> search) {
            return new ResourceFile(new File(this.workspace, path), search);
        }
    }
}
