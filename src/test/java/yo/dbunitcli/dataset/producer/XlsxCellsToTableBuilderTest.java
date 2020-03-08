package yo.dbunitcli.dataset.producer;

import org.junit.Test;

import static org.junit.Assert.*;

public class XlsxCellsToTableBuilderTest {

    @Test
    public void test() {
        assertEquals("A1", "sheet1!$A$1".replaceAll(".+!", "")
                .replaceAll("\\$", ""));
        assertEquals("A1", "$A$1".replaceAll(".+!", "")
                .replaceAll("\\$", ""));
        assertEquals("A1", "A1".replaceAll(".+!", "")
                .replaceAll("\\$", ""));
    }
}