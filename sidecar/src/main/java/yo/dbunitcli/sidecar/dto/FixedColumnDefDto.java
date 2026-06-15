package yo.dbunitcli.sidecar.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class FixedColumnDefDto {

    private List<ColumnDefSetting> columns;

    public List<ColumnDefSetting> getColumns() {
        return this.columns;
    }

    public void setColumns(final List<ColumnDefSetting> columns) {
        this.columns = columns;
    }

    @Serdeable
    public static class ColumnDefSetting {
        private String name;
        private int length;
        private String align;
        private String pad;

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public int getLength() {
            return this.length;
        }

        public void setLength(final int length) {
            this.length = length;
        }

        public String getAlign() {
            return this.align;
        }

        public void setAlign(final String align) {
            this.align = align;
        }

        public String getPad() {
            return this.pad;
        }

        public void setPad(final String pad) {
            this.pad = pad;
        }
    }
}
