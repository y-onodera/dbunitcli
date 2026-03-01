package yo.dbunitcli.application;

import java.util.stream.Stream;

public interface CompositeDto {

    Stream<Object> dto();
}
