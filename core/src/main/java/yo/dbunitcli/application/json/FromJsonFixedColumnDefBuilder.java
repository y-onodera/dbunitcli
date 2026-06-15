package yo.dbunitcli.application.json;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import yo.dbunitcli.dataset.converter.FixedColumnDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FromJsonFixedColumnDefBuilder {

    public List<FixedColumnDef> build(final File file) {
        try (final FileInputStream fis = new FileInputStream(file);
             final JsonReader reader = Json.createReader(fis)) {
            final JsonObject root = reader.readObject();
            final JsonArray columns = root.getJsonArray("columns");
            final List<FixedColumnDef> result = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                final JsonObject col = columns.getJsonObject(i);
                final String name = col.getString("name");
                final int length = col.getInt("length");
                final boolean leftAlign = !"right".equalsIgnoreCase(col.getString("align", "left"));
                final String pad = col.getString("pad", " ");
                result.add(new FixedColumnDef(name, length, leftAlign, pad));
            }
            return result;
        } catch (final IOException e) {
            throw new AssertionError("Failed to load fixed column def: " + file, e);
        }
    }
}
