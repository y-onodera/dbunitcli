package yo.dbunitcli.resource;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

class FileResourcesTest {

    private static final Properties backup = new Properties();

    @BeforeAll
    public static void setUp() throws UnsupportedEncodingException {
        FileResourcesTest.backup.putAll(System.getProperties());
    }

    @AfterAll
    static void restore() {
        System.setProperties(FileResourcesTest.backup);
    }

    @Nested
    class NoSystemPropertyTest {
        @Test
        void resultDir() {
            Assertions.assertEquals(new File("."), FileResources.resultDir());
        }

        @Test
        void datasetDir() {
            Assertions.assertEquals(new File("."), FileResources.datasetDir());
        }

        @Test
        void baseDir() {
            Assertions.assertEquals(new File("."), FileResources.baseDir());
        }
    }

    @Nested
    class SetWorkspaceTest {
        @BeforeAll
        static void setProperty() {
            final Properties newProperty = new Properties();
            newProperty.putAll(FileResourcesTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "src");
            System.setProperties(newProperty);
        }

        @AfterAll
        static void restore() {
            FileResourcesTest.restore();
        }

        @Test
        void searchInOrderDatasetBase() {
            final File result = FileResources.searchDatasetBase("main");
            Assertions.assertEquals(new File("src/main"), result);
        }

        @Test
        void searchInOrderDatasetBaseNotFoundWorkspace() {
            final File result = FileResources.searchDatasetBase("src");
            Assertions.assertEquals(new File("src"), result);
        }

        @Test
        void searchInOrderDatasetBaseAbsolutePath() {
            final File result = FileResources.searchDatasetBase("C:/test");
            Assertions.assertEquals(new File("C:/test"), result);
        }

        @Test
        void searchInOrderWorkspace() {
            final File result = FileResources.searchWorkspace("main");
            Assertions.assertEquals(new File("src/main"), result);
        }

        @Test
        void searchInOrderWorkspaceNotFoundWorkspace() {
            final File result = FileResources.searchWorkspace("src");
            Assertions.assertEquals(new File("src"), result);
        }

        @Test
        void searchInOrderWorkspaceAbsolutePath() {
            final File result = FileResources.searchWorkspace("C:/test");
            Assertions.assertEquals(new File("C:/test"), result);
        }

        @Test
        void resultDir() {
            Assertions.assertEquals(new File("src"), FileResources.resultDir());
        }

        @Test
        void datasetDir() {
            Assertions.assertEquals(new File("src"), FileResources.datasetDir());
        }

        @Test
        void baseDir() {
            Assertions.assertEquals(new File("src"), FileResources.baseDir());
        }
    }

    @Nested
    class SetDatasetBaseTest {
        @BeforeAll
        static void setProperty() {
            final Properties newProperty = new Properties();
            newProperty.putAll(FileResourcesTest.backup);
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "src");
            System.setProperties(newProperty);
        }

        @AfterAll
        static void restore() {
            FileResourcesTest.restore();
        }


        @Test
        void searchInOrderDatasetBase() {
            final File result = FileResources.searchDatasetBase("main");
            Assertions.assertEquals(new File("src/main"), result);
        }

        @Test
        void searchInOrderDatasetBaseNotFoundWorkspace() {
            final File result = FileResources.searchDatasetBase("src");
            Assertions.assertEquals(new File("src"), result);
        }

        @Test
        void searchInOrderDatasetBaseAbsolutePath() {
            final File result = FileResources.searchDatasetBase("C:/test");
            Assertions.assertEquals(new File("C:/test"), result);
        }

        @Test
        void searchInOrderWorkspace() {
            final File result = FileResources.searchWorkspace("main");
            Assertions.assertEquals(new File("main"), result);
        }

        @Test
        void searchInOrderWorkspaceNotFoundWorkspace() {
            final File result = FileResources.searchWorkspace("src");
            Assertions.assertEquals(new File("src"), result);
        }

        @Test
        void searchInOrderWorkspaceAbsolutePath() {
            final File result = FileResources.searchWorkspace("C:\\test");
            Assertions.assertEquals(new File("C:\\test"), result);
        }

        @Test
        void resultDir() {
            Assertions.assertEquals(new File("."), FileResources.resultDir());
        }

        @Test
        void datasetDir() {
            Assertions.assertEquals(new File("src"), FileResources.datasetDir());
        }

        @Test
        void baseDir() {
            Assertions.assertEquals(new File("."), FileResources.baseDir());
        }
    }

    @Nested
    class SetResultBaseTest {
        @BeforeAll
        static void setProperty() {
            final Properties newProperty = new Properties();
            newProperty.putAll(FileResourcesTest.backup);
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "result");
            System.setProperties(newProperty);
        }

        @AfterAll
        static void restore() {
            FileResourcesTest.restore();
        }

        @Test
        void searchInOrderDatasetBase() {
            final File result = FileResources.searchDatasetBase("main");
            Assertions.assertEquals(new File("main"), result);
        }

        @Test
        void searchInOrderDatasetBaseNotFoundWorkspace() {
            final File result = FileResources.searchDatasetBase("src");
            Assertions.assertEquals(new File("src"), result);
        }

        @Test
        void searchInOrderDatasetBaseAbsolutePath() {
            final File result = FileResources.searchDatasetBase("C:\\test");
            Assertions.assertEquals(new File("C:\\test"), result);
        }

        @Test
        void searchInOrderWorkspace() {
            final File result = FileResources.searchWorkspace("main");
            Assertions.assertEquals(new File("main"), result);
        }

        @Test
        void searchInOrderWorkspaceNotFoundWorkspace() {
            final File result = FileResources.searchWorkspace("src");
            Assertions.assertEquals(new File("src"), result);
        }

        @Test
        void searchInOrderWorkspaceAbsolutePath() {
            final File result = FileResources.searchWorkspace("C:\\test");
            Assertions.assertEquals(new File("C:\\test"), result);
        }

        @Test
        void resultDir() {
            Assertions.assertEquals(new File("result"), FileResources.resultDir());
        }

        @Test
        void datasetDir() {
            Assertions.assertEquals(new File("."), FileResources.datasetDir());
        }

        @Test
        void baseDir() {
            Assertions.assertEquals(new File("."), FileResources.baseDir());
        }
    }

    @Nested
    class SetWorkspaceAndResultBaseTest {
        @BeforeAll
        static void setProperty() {
            final Properties newProperty = new Properties();
            newProperty.putAll(FileResourcesTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "workspace");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "result");
            System.setProperties(newProperty);
        }

        @AfterAll
        static void restore() {
            FileResourcesTest.restore();
        }

        @Test
        void resultDir() {
            Assertions.assertEquals(new File("result"), FileResources.resultDir());
        }

        @Test
        void datasetDir() {
            Assertions.assertEquals(new File("workspace"), FileResources.datasetDir());
        }

        @Test
        void baseDir() {
            Assertions.assertEquals(new File("workspace"), FileResources.baseDir());
        }
    }

    @Nested
    class SetWorkspaceAndDatasetBaseTest {
        @BeforeAll
        static void setProperty() {
            final Properties newProperty = new Properties();
            newProperty.putAll(FileResourcesTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "src/main");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "src/test");
            System.setProperties(newProperty);
        }

        @AfterAll
        static void restore() {
            FileResourcesTest.restore();
        }

        @Test
        void searchInOrderDatasetBase() {
            final File result = FileResources.searchDatasetBase("java");
            Assertions.assertEquals(new File("src/test/java"), result);
        }

        @Test
        void searchInOrderDatasetBaseNotFoundWorkspace() {
            final File result = FileResources.searchDatasetBase("src");
            Assertions.assertEquals(new File("src"), result);
        }

        @Test
        void searchInOrderDatasetBaseAbsolutePath() {
            final File result = FileResources.searchDatasetBase("C:\\test");
            Assertions.assertEquals(new File("C:\\test"), result);
        }

        @Test
        void searchInOrderWorkspace() {
            final File result = FileResources.searchWorkspace("java");
            Assertions.assertEquals(new File("src/main/java"), result);
        }

        @Test
        void searchInOrderWorkspaceNotFoundWorkspace() {
            final File result = FileResources.searchWorkspace("src");
            Assertions.assertEquals(new File("src"), result);
        }

        @Test
        void searchInOrderWorkspaceAbsolutePath() {
            final File result = FileResources.searchWorkspace("C:\\test");
            Assertions.assertEquals(new File("C:\\test"), result);
        }

        @Test
        void resultDir() {
            Assertions.assertEquals(new File("src/main"), FileResources.resultDir());
        }

        @Test
        void datasetDir() {
            Assertions.assertEquals(new File("src/test"), FileResources.datasetDir());
        }

        @Test
        void baseDir() {
            Assertions.assertEquals(new File("src/main"), FileResources.baseDir());
        }
    }

    @Nested
    class SetAllPropertyTest {
        @BeforeAll
        static void setProperty() {
            final Properties newProperty = new Properties();
            newProperty.putAll(FileResourcesTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "workspace");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "data");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "result");
            System.setProperties(newProperty);
        }

        @AfterAll
        static void restore() {
            FileResourcesTest.restore();
        }

        @Test
        void resultDir() {
            Assertions.assertEquals(new File("result"), FileResources.resultDir());
        }

        @Test
        void datasetDir() {
            Assertions.assertEquals(new File("data"), FileResources.datasetDir());
        }

        @Test
        void baseDir() {
            Assertions.assertEquals(new File("workspace"), FileResources.baseDir());
        }
    }

    @Nested
    class EnvironmentVariableTest {
        private static final String HOME_VAR = System.getenv("USERPROFILE") != null ? "USERPROFILE" : "HOME";
        private static final String HOME_VALUE = System.getenv(HOME_VAR);

        @Test
        void searchWorkspaceWithUserProfile() {
            final File result = FileResources.searchWorkspace("%" + HOME_VAR + "%/test");
            final File expected = new File(HOME_VALUE + "/test");
            Assertions.assertEquals(expected, result);
        }

        @Test
        void searchDatasetBaseWithUserProfile() {
            final File result = FileResources.searchDatasetBase("%" + HOME_VAR + "%/data");
            final File expected = new File(HOME_VALUE + "/data");
            Assertions.assertEquals(expected, result);
        }

        @Test
        void resultDirWithUserProfile() {
            final File result = FileResources.resultDir("%" + HOME_VAR + "%/result");
            final File expected = new File(HOME_VALUE + "/result");
            Assertions.assertEquals(expected, result);
        }

        @Test
        void baseDirWithUserProfileProperty() {
            final Properties newProperty = new Properties();
            newProperty.putAll(FileResourcesTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "%" + HOME_VAR + "%/workspace");
            System.setProperties(newProperty);

            final File result = FileResources.baseDir();
            final File expected = new File(HOME_VALUE + "/workspace");
            Assertions.assertEquals(expected, result);

            FileResourcesTest.restore();
        }
    }

    @Nested
    class SearchParameterizeTemplateTest {
        @TempDir
        Path workspace;

        @BeforeEach
        void setProperty() throws IOException {
            final Properties newProperty = new Properties();
            newProperty.putAll(FileResourcesTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, this.workspace.toString());
            System.setProperties(newProperty);
            Files.createDirectories(this.workspace.resolve(FileResources.OPTION_PARAMETERIZE_TEMPLATE_PATH));
            Files.createDirectories(this.workspace.resolve(FileResources.RESOURCES_TEMPLATE_PATH));
        }

        @AfterEach
        void restore() {
            FileResourcesTest.restore();
        }

        @Test
        void searchParameterizeTemplateDir() throws IOException {
            final Path expected = Files.createFile(this.workspace.resolve(FileResources.OPTION_PARAMETERIZE_TEMPLATE_PATH).resolve("param.txt"));
            Files.createFile(this.workspace.resolve(FileResources.RESOURCES_TEMPLATE_PATH).resolve("param.txt"));
            Assertions.assertEquals(expected.toFile(), FileResources.searchParameterizeTemplate("param.txt"));
        }

        @Test
        void searchTemplateDirIfNotFoundParameterizeTemplateDir() throws IOException {
            final Path expected = Files.createFile(this.workspace.resolve(FileResources.RESOURCES_TEMPLATE_PATH).resolve("param.txt"));
            Assertions.assertEquals(expected.toFile(), FileResources.searchParameterizeTemplate("param.txt"));
        }

        @Test
        void searchWorkspaceIfNotFoundTemplateDir() throws IOException {
            final Path expected = Files.createFile(this.workspace.resolve("param.txt"));
            Assertions.assertEquals(expected.toFile(), FileResources.searchParameterizeTemplate("param.txt"));
        }

        @Test
        void emptyPath() {
            Assertions.assertNull(FileResources.searchParameterizeTemplate(""));
        }
    }

    @Nested
    class RunInContextTest {
        @BeforeEach
        void setProperty() {
            final Properties newProperty = new Properties();
            newProperty.putAll(FileResourcesTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "src");
            newProperty.put(FileResources.PROPERTY_DATASET_BASE, "src/main");
            newProperty.put(FileResources.PROPERTY_RESULT_BASE, "target");
            System.setProperties(newProperty);
        }

        @AfterEach
        void restore() {
            FileResourcesTest.restore();
        }

        @Test
        void keepContextWhenSystemPropertyChanged() {
            FileResources.runInContext(() -> {
                System.setProperty(FileResources.PROPERTY_WORKSPACE, "changed");
                System.setProperty(FileResources.PROPERTY_DATASET_BASE, "changed/dataset");
                System.setProperty(FileResources.PROPERTY_RESULT_BASE, "changed/result");
                System.setProperty(FileResources.PROPERTY_ENV, "changed.properties");
                Assertions.assertEquals(new File("src"), FileResources.baseDir());
                Assertions.assertEquals(new File("src/main"), FileResources.datasetDir());
                Assertions.assertEquals(new File("target"), FileResources.resultDir());
                Assertions.assertEquals(new File("src", FileResources.DEFAULT_ENV_PATH), FileResources.envFile());
                Assertions.assertEquals(new File("src/resources/setting"), FileResources.settingDir());
            });
            Assertions.assertEquals(new File("changed"), FileResources.baseDir());
        }

        @Test
        void nestedRunInheritsOuterContext() {
            FileResources.runInContext(() -> {
                System.setProperty(FileResources.PROPERTY_WORKSPACE, "changed");
                FileResources.runInContext(() -> Assertions.assertEquals(new File("src"), FileResources.baseDir()));
            });
        }
    }
}
