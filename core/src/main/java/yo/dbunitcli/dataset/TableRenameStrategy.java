package yo.dbunitcli.dataset;

import java.util.Optional;
import java.util.function.BiFunction;

public interface TableRenameStrategy {
    default TableRenameStrategy compose(final TableRenameStrategy composer) {
        return new Combinator(composer, this);
    }

    default TableRenameStrategy andThen(final TableRenameStrategy combiner) {
        return new Combinator(this, combiner);
    }

    default String apply(final String originName, final int splitNo) {
        return this.renameFunction().apply(originName, splitNo);
    }

    BiFunction<String, Integer, String> renameFunction();

    record Combinator(TableRenameStrategy composer, TableRenameStrategy combiner) implements TableRenameStrategy {
        @Override
        public BiFunction<String, Integer, String> renameFunction() {
            return (s, i) -> this.combiner.renameFunction().apply(this.composer.renameFunction().apply(s, i), i);
        }
    }

    record ReplaceFunction(String newName, String prefix, String suffix,
                           boolean isSplit) implements TableRenameStrategy {

        public ReplaceFunction(final Builder builder) {
            this(builder.getNewName(), builder.getPrefix(), builder.getSuffix(), builder.isSplit());
        }

        @Override
        public BiFunction<String, Integer, String> renameFunction() {
            if (this.isSplit()) {
                return (origin, no) -> this.format(this.prefix, no) + this.replace(origin, this.newName) + this.format(this.suffix, no);
            }
            return (origin, no) -> this.replace("", this.prefix) + this.replace(origin, this.newName) + this.replace("", this.suffix);
        }

        public Builder builder() {
            return new Builder()
                    .setPrefix(this.prefix)
                    .setNewName(this.newName)
                    .setSuffix(this.suffix)
                    .setSplit(this.isSplit);
        }

        private String format(final String prefix, final int no) {
            return Optional.of(prefix).filter(it -> !it.isEmpty()).map(it -> String.format(it, no)).orElse("");
        }

        private String replace(final String origin, final String newName) {
            return Optional.of(newName).filter(it -> !it.isEmpty()).orElse(origin);
        }

        public static class Builder {
            private String newName = "";
            private String prefix = "";
            private String suffix = "";
            private boolean isSplit;

            public String getNewName() {
                return this.newName;
            }

            public Builder setNewName(final String newName) {
                this.newName = newName;
                return this;
            }

            public String getPrefix() {
                return this.prefix;
            }

            public Builder setPrefix(final String prefix) {
                this.prefix = prefix;
                return this;
            }

            public String getSuffix() {
                return this.suffix;
            }

            public Builder setSuffix(final String suffix) {
                this.suffix = suffix;
                return this;
            }

            public boolean isSplit() {
                return this.isSplit;
            }

            public Builder setSplit(final boolean split) {
                this.isSplit = split;
                return this;
            }

            public TableRenameStrategy build() {
                return new ReplaceFunction(this);
            }
        }
    }
}
