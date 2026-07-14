package yo.dbunitcli.dataset.producer;

import org.junit.jupiter.api.Test;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.ComparableDataSetParam;
import yo.dbunitcli.dataset.ComparableTable;
import yo.dbunitcli.resource.jdbc.DatabaseConnectionLoader;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComparableJdbcMetaDataProducerVerifyTest {

    @Test
    void withJdbcConnection_includesRemarks() throws Exception {
        final String url = "jdbc:h2:mem:metaverify;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE PERSON (ID INT PRIMARY KEY, NAME VARCHAR(50) NOT NULL)");
                st.execute("COMMENT ON TABLE PERSON IS 'people table'");
                st.execute("COMMENT ON COLUMN PERSON.NAME IS 'person name'");
            }

            final Properties props = new Properties();
            props.put("url", url);
            props.put("user", "sa");
            props.put("pass", "");

            final File tableListFile = File.createTempFile("tables", ".txt");
            tableListFile.deleteOnExit();
            Files.writeString(tableListFile.toPath(), "PERSON");

            final ComparableDataSetParam param = ComparableDataSetParam.builder()
                    .setSrc(tableListFile)
                    .setEncoding("UTF-8")
                    .setDatabaseConnectionLoader(new DatabaseConnectionLoader(props))
                    .build();

            final ComparableDBDataSetProducer delegate = new ComparableDBDataSetProducer(param);
            final ComparableJdbcMetaDataProducer producer = new ComparableJdbcMetaDataProducer(delegate);
            final ComparableDataSet dataSet = producer.loadDataSet();
            final ComparableTable table = dataSet.getTable("PERSON");
            assertEquals(2, table.getRowCount());

            final var nameRow = table.getRowToMap(1);
            assertEquals("PERSON", nameRow.get("TABLE_NAME"));
            assertEquals("people table", nameRow.get("TABLE_REMARKS"));
            assertEquals("NAME", nameRow.get("COLUMN_NAME"));
            assertEquals("person name", nameRow.get("REMARKS"));
            assertEquals(Boolean.FALSE, nameRow.get("NULLABLE"));

            final var idRow = table.getRowToMap(0);
            assertEquals("ID", idRow.get("COLUMN_NAME"));
            assertEquals(Boolean.TRUE, idRow.get("IS_PK"));
            assertEquals("", idRow.get("PACKAGE"));
        }
    }

    @Test
    void withoutJdbcConnection_metadataOnlyFromCsv() {
        final ComparableDataSetParam csvParam = ComparableDataSetParam.builder()
                .setSrc(new File("src/test/resources/yo/dbunitcli/application/src/file/csv.csv"))
                .setEncoding("UTF-8")
                .build();
        final ComparableCsvDataSetProducer csvProducer = new ComparableCsvDataSetProducer(csvParam);
        final ComparableJdbcMetaDataProducer producer = new ComparableJdbcMetaDataProducer(csvProducer);
        final ComparableDataSet dataSet = producer.loadDataSet();
        final ComparableTable table = dataSet.getTable("csv");
        assertEquals(2, table.getRowCount());

        final var col0 = table.getRowToMap(0);
        assertEquals("csv", col0.get("TABLE_NAME"));
        assertEquals("", col0.get("TABLE_REMARKS"));
        assertEquals("ヘッダ1", col0.get("COLUMN_NAME"));
        assertEquals("", col0.get("REMARKS"));
        assertEquals(null, col0.get("PK_NAME"));
    }
}
