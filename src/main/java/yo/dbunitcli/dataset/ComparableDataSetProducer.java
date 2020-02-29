package yo.dbunitcli.dataset;

import org.dbunit.dataset.stream.IDataSetProducer;

public interface ComparableDataSetProducer extends IDataSetProducer {

    default String getSrc(){
        return this.getParam().getSrc().getPath();
    }

    ComparableDataSetLoaderParam getParam();
}
