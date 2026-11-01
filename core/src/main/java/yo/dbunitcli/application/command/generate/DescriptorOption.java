package yo.dbunitcli.application.command.generate;

/**
 * 記述子producerの合成に使うオプション。GenerateOption（builtin駆動）とScaffoldOption（記述子dataset雛型）が実装し、
 * 同じ{@link DescriptorGenerateStrategy#wrapDescriptor}を共有する（現状はfixedColumnDefだけが参照する）
 */
public interface DescriptorOption {
    String fixedLength();

    String defaultLength();

    String align();
}
