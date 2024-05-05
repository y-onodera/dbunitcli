package yo.dbunitcli.application.dto;

import java.util.stream.Stream;

public interface CompositeDto {

    Stream<Object> dto();
}
