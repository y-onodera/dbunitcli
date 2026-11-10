package yo.dbunitcli.sidecar.domain.project;

import yo.dbunitcli.application.option.EncodingOption;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * defaultCharsetはencodingを指定しない読み書きで使う文字コード
 */
public record ResourceFile(File baseDir, Set<String> files, Function<String, Optional<Path>> pathResolver, Charset defaultCharset) {

    private static Function<String, Optional<Path>> toPath(final Function<String, File> toFile) {
        return it -> {
            File file = toFile.apply(it);
            if (file!=null && file.exists()) {
                return Optional.of(file.toPath());
            }
            return Optional.empty();
        };
    }

    public ResourceFile(final File parentDir, final Function<String, File> toFile, final Charset defaultCharset) {
        this(parentDir, new TreeSet<>(), toPath(toFile), defaultCharset);
        this.reload();
    }

    public static ResourceFile of(final File workspace, final String path, final Function<String, File> toFile, final Charset defaultCharset) {
        return new ResourceFile(new File(workspace, path), toFile, defaultCharset);
    }

    public Optional<String> read(final String name, final String encoding) {
        return this.select(name).map(it -> {
            try {
                return Files.readString(it, this.charsetOf(encoding));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Optional<Path> select(String target) {
        return this.files.stream().filter(it -> it.equals(target)).findFirst()
                .map(it -> new File(this.baseDir, it).toPath())
                .or(() -> this.pathResolver.apply(target));
    }

    public List<String> list() {
        return this.files.stream().toList();
    }

    public Stream<String> stream() {
        return this.files.stream();
    }

    public Stream<Path> pathStream() {
        return this.files.stream().map(it -> new File(this.baseDir, it).toPath());
    }

    public Path add(final String name, final String contents) throws IOException {
        final Path saveTo = this.prepareUpdate(this.getUniqueName(name));
        Files.writeString(saveTo, contents, this.defaultCharset);
        return saveTo;
    }

    public void update(final String name, final String contents) throws IOException {
        this.update(name, contents, "");
    }

    public void update(final String name, final String contents, final String encoding) throws IOException {
        final Path saveTo = this.prepareUpdate(name);
        Files.writeString(saveTo, contents, this.charsetOf(encoding));
    }

    /**
     * overwriteなら読込（{@link #read}）と同じ探索で見つかったファイルへ書き、そうでなければ{@link #update}と同じく保存する
     */
    public void save(final String name, final String contents, final String encoding, final boolean overwrite) throws IOException {
        if (!overwrite) {
            this.update(name, contents, encoding);
            return;
        }
        final Path saveTo = this.select(name).orElseThrow(() -> new IOException("File not found: " + name));
        Files.writeString(saveTo, contents, this.charsetOf(encoding));
    }

    /**
     * 読込と同じ探索で見つかったファイルの場所。見つからなければempty
     */
    public Optional<Location> locate(final String name) {
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        return this.select(name)
                .filter(Files::isRegularFile)
                .map(it -> {
                    final Path file = it.toAbsolutePath().normalize();
                    final Path defaultFolder = this.baseDir.toPath().toAbsolutePath().normalize();
                    final String fileName = file.getFileName().toString();
                    return new Location(file.getParent().toString(), defaultFolder.toString(), fileName,
                            defaultFolder.equals(file.getParent()), this.files.contains(fileName));
                });
    }

    /**
     * folder: 見つかったファイルのフォルダ、defaultFolder: 新規保存先のフォルダ、fileName: ファイル名、
     * inDefaultFolder: 見つかったファイルがdefaultFolder直下か、defaultFolderHasFile: defaultFolder直下に同名のファイルがあるか
     */
    public record Location(String folder, String defaultFolder, String fileName, boolean inDefaultFolder,
                           boolean defaultFolderHasFile) {
    }

    public void update(final String name, final byte[] contents) throws IOException {
        Files.write(this.prepareUpdate(name), contents);
    }

    /** バイナリ等、既存ファイルの内容をそのまま取り込む（同名は上書き） */
    public void updateFrom(final String name, final Path source) throws IOException {
        final Path saveTo = this.prepareUpdate(name);
        Files.copy(source, saveTo, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * baseDir直下のファイルなら削除して一覧からも外す
     *
     * @return baseDir直下のファイルだったか
     */
    public boolean deleteIfContains(final File file) throws IOException {
        final Path parent = file.toPath().toAbsolutePath().normalize().getParent();
        if (!this.baseDir.toPath().toAbsolutePath().normalize().equals(parent)) {
            return false;
        }
        this.delete(file.getName());
        return true;
    }

    public void delete(final String name) throws IOException {
        final Path filePath = new File(this.baseDir, name).toPath();
        if (!filePath.toFile().exists()) {
            throw new IOException("File not found: " + name);
        }
        Files.deleteIfExists(filePath);
        this.files.removeIf(path -> path.equals(name));
    }

    /** 文字コードの分からないファイルもあるため、内容をデコードせずにそのまま複製する */
    public void copy(final String source) throws IOException {
        final Optional<Path> path = this.select(source);
        if (path.isPresent()) {
            this.updateFrom(this.getUniqueName(source), path.get());
        }
    }

    public void rename(final String current, final String newName) throws IOException {
        Optional<Path> path = this.select(current);
        if (path.isPresent()) {
            String toUnique = this.getUniqueName(newName);
            Path newPath = new File(this.baseDir, toUnique).toPath();
            Files.move(path.get(), newPath, StandardCopyOption.REPLACE_EXISTING);
            this.files.remove(current);
            this.files.add(toUnique);
        }
    }

    private String getUniqueName(final String name) {
        if (!this.files.contains(name)) {
            return name;
        }
        final int dotIndex = name.lastIndexOf('.');
        final String base = dotIndex >= 0 ? name.substring(0, dotIndex):name;
        final String ext = dotIndex >= 0 ? name.substring(dotIndex):"";
        return IntStream.iterate(1, i -> i + 1)
                .mapToObj(i -> base + "(" + i + ")" + ext)
                .filter(candidate -> !this.files.contains(candidate))
                .findFirst()
                .orElse("");
    }

    private void reload() {
        if (this.baseDir.exists() && this.baseDir.isDirectory()) {
            this.files.clear();
            try (final Stream<Path> pathStream = Files.walk(this.baseDir.toPath(), 1)) {
                this.files.addAll(
                        pathStream.filter(it -> it.toFile().isFile()).map(it -> it.getFileName().toString()).toList());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Charset charsetOf(final String encoding) {
        return Charset.forName(EncodingOption.orDefault(encoding, this.defaultCharset));
    }

    private Path create(final File file) throws IOException {
        final File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            Files.createDirectories(parentFile.toPath());
        }
        final Path saveTo = file.toPath();
        if (!saveTo.toFile().exists()) {
            Files.createFile(saveTo);
            // 一覧はbaseDir直下のファイル名だけを持つ（サブディレクトリや絶対パスの保存先は名前で引けない）
            if (this.baseDir.toPath().toAbsolutePath().normalize()
                    .equals(saveTo.toAbsolutePath().normalize().getParent())) {
                this.files.add(saveTo.getFileName().toString());
            }
        }
        return saveTo;
    }

    private Path prepareUpdate(final String name) throws IOException {
        final File file = new File(name);
        if (file.isAbsolute()) {
            return this.create(file);
        }
        return this.create(new File(this.baseDir, name));
    }

}