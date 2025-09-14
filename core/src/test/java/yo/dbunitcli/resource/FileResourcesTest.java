package yo.dbunitcli.resource;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
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
            Assertions.assertEquals(new File("src\\main"), result);
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
            Assertions.assertEquals(new File("src\\main"), result);
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
            Assertions.assertEquals(new File("src\\main"), result);
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
        @Test
        void searchWorkspaceWithUserProfile() {
            final String userProfile = System.getenv("USERPROFILE");
            final File result = FileResources.searchWorkspace("%USERPROFILE%\\test");
            final File expected = new File(userProfile + "\\test");
            Assertions.assertEquals(expected, result);
        }

        @Test
        void searchDatasetBaseWithUserProfile() {
            final String userProfile = System.getenv("USERPROFILE");
            final File result = FileResources.searchDatasetBase("%USERPROFILE%\\data");
            final File expected = new File(userProfile + "\\data");
            Assertions.assertEquals(expected, result);
        }

        @Test
        void resultDirWithUserProfile() {
            final String userProfile = System.getenv("USERPROFILE");
            final File result = FileResources.resultDir("%USERPROFILE%\\result");
            final File expected = new File(userProfile + "\\result");
            Assertions.assertEquals(expected, result);
        }

        @Test
        void baseDirWithUserProfileProperty() {
            final String userProfile = System.getenv("USERPROFILE");
            final Properties newProperty = new Properties();
            newProperty.putAll(FileResourcesTest.backup);
            newProperty.put(FileResources.PROPERTY_WORKSPACE, "%USERPROFILE%\\workspace");
            System.setProperties(newProperty);

            final File result = FileResources.baseDir();
            final File expected = new File(userProfile + "\\workspace");
            Assertions.assertEquals(expected, result);

            FileResourcesTest.restore();
        }
    }
}