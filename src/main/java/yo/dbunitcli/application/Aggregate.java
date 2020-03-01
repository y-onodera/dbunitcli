package yo.dbunitcli.application;

import com.google.common.collect.Lists;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import yo.dbunitcli.dataset.ComparableDataSet;
import yo.dbunitcli.dataset.Parameter;
import yo.dbunitcli.writer.IDataSetWriter;

import java.util.List;
import java.util.Map;

public class Aggregate {
    public static void main(String[] args) throws Exception {
        ParameterizeOption options = new ParameterizeOption();
        try {
            options.parse(args);
        } catch (Exception exp) {
            throw new AssertionError("option parse failed.", exp);
        }
        List<IDataSet> dataSets = Lists.newArrayList();
        int row = 1;
        for (Map<String, Object> param : options.loadParams()) {
            final Parameter param1 = new Parameter(row, param);
            ConvertOption exp = new ConvertOption(param1);
            exp.parse(options.createArgs(param1));
            ComparableDataSet dataSet = exp.targetDataSet();
            dataSets.add(dataSet);
            row++;
        }
        CompositeDataSet composite = new CompositeDataSet(dataSets.toArray(new IDataSet[dataSets.size()]));
        IDataSetWriter writer = options.writer();
        writer.open("result");
        for (String tableName : composite.getTableNames()) {
            writer.write(composite.getTable(tableName));
        }
        writer.close();
    }
}
