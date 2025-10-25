package yo.dbunitcli.dataset.producer;

import yo.dbunitcli.common.Parameter;
import yo.dbunitcli.common.Source;
import yo.dbunitcli.dataset.ComparableDataSetProducer;
import yo.dbunitcli.resource.st4.TemplateRender;

import java.util.stream.Stream;

public interface QueryDataSetProducer extends ComparableDataSetProducer {

    Parameter getParameter();

    default TemplateRender getTemplateLoader() {
        return this.getParam().templateRender();
    }

    @Override
    default Stream<Source> getSourceStream() {
        return ComparableDataSetProducer.super.getSourceStream();
    }
}
