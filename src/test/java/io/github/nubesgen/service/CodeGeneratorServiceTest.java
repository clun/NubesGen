package io.github.nubesgen.service;

import io.github.nubesgen.configuration.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests NubesGen with the default options.
 */
@SpringBootTest
class CodeGeneratorServiceTest {

    private final Logger log = LoggerFactory.getLogger(CodeGeneratorServiceTest.class);

    private final CodeGeneratorService codeGeneratorService;
    private final TemplateListService templateListService;

    @Autowired
    public CodeGeneratorServiceTest(CodeGeneratorService codeGeneratorService, TemplateListService templateListService) {
        this.codeGeneratorService = codeGeneratorService;
        this.templateListService = templateListService;
    }

    @Test
    void generateDefaultConfiguration() throws IOException {
        NubesgenConfiguration properties = new NubesgenConfiguration();
        properties.setApplicationName("nubesgen-testapp");
        properties.setRegion("westeurope");

        Map<String, String> configuration = this.codeGeneratorService.generateAzureConfiguration(properties);

        testGeneratedFiles(properties, "default", configuration, this.templateListService.listMainTemplates(),
                this.templateListService.listAppServiceTemplates());
    }


    @Test
    void generateCosmosDbMongoDbConfiguration() throws IOException {
        NubesgenConfiguration properties = new NubesgenConfiguration();
        properties.setApplicationName("nubesgen-testapp-mongodb");
        properties.setRegion("westeurope");
        properties.setDatabaseConfiguration(new DatabaseConfiguration(DatabaseType.NONE, Tier.BASIC));
        List<AddonConfiguration> addons = new ArrayList<>();
        addons.add(new AddonConfiguration(AddonType.COSMOSDB_MONGODB, Tier.FREE));
        properties.setAddons(addons);

        Map<String, String> configuration = this.codeGeneratorService.generateAzureConfiguration(properties);

        testGeneratedFiles(properties, "cosmosdb-mongodb", configuration, this.templateListService.listMainTemplates(),
                this.templateListService.listAppServiceTemplates(), this.templateListService.listCosmosdbMongodbTemplates());

    }

    @Test
    void generateFunctionMysqlConfiguration() throws IOException {
        NubesgenConfiguration properties = new NubesgenConfiguration();
        properties.setApplicationName("nubesgen-testapp-function");
        properties.setRegion("westeurope");
        properties.setApplicationConfiguration(new ApplicationConfiguration(ApplicationType.FUNCTION, Tier.CONSUMPTION));
        properties.setDatabaseConfiguration(new DatabaseConfiguration(DatabaseType.MYSQL, Tier.BASIC));

        Map<String, String> configuration = this.codeGeneratorService.generateAzureConfiguration(properties);

        testGeneratedFiles(properties, "function-mysql", configuration, this.templateListService.listMainTemplates(),
                this.templateListService.listFunctionTemplates(), this.templateListService.listMysqlTemplates());

    }

    @Test
    void generateMysqlConfiguration() throws IOException {
        NubesgenConfiguration properties = new NubesgenConfiguration();
        properties.setApplicationName("nubesgen-testapp-mysql");
        properties.setRegion("westeurope");
        properties.setDatabaseConfiguration(new DatabaseConfiguration(DatabaseType.MYSQL, Tier.BASIC));

        Map<String, String> configuration = this.codeGeneratorService.generateAzureConfiguration(properties);

        testGeneratedFiles(properties, "mysql", configuration, this.templateListService.listMainTemplates(),
                this.templateListService.listAppServiceTemplates(), this.templateListService.listMysqlTemplates());

    }

    @Test
    void generatePostgreSQLConfiguration() throws IOException {
        NubesgenConfiguration properties = new NubesgenConfiguration();
        properties.setApplicationName("nubesgen-testapp-postgresql");
        properties.setRegion("westeurope");
        properties.setDatabaseConfiguration(new DatabaseConfiguration(DatabaseType.POSTGRESQL, Tier.BASIC));

        Map<String, String> configuration = this.codeGeneratorService.generateAzureConfiguration(properties);

        testGeneratedFiles(properties, "postgresql", configuration, this.templateListService.listMainTemplates(),
                this.templateListService.listAppServiceTemplates(), this.templateListService.listPostgresqlTemplates());

    }

    @Test
    void generateRedisConfiguration() throws IOException {
        NubesgenConfiguration properties = new NubesgenConfiguration();
        properties.setApplicationName("nubesgen-testapp-redis");
        properties.setRegion("westeurope");
        properties.setDatabaseConfiguration(new DatabaseConfiguration(DatabaseType.NONE, Tier.BASIC));
        List<AddonConfiguration> addons = new ArrayList<>();
        addons.add(new AddonConfiguration(AddonType.REDIS, Tier.BASIC));
        properties.setAddons(addons);

        Map<String, String> configuration = this.codeGeneratorService.generateAzureConfiguration(properties);

        testGeneratedFiles(properties, "redis", configuration, this.templateListService.listMainTemplates(),
                this.templateListService.listAppServiceTemplates(), this.templateListService.listRedisTemplates());

    }

    @Test
    void generateSqlServerConfiguration() throws IOException {
        NubesgenConfiguration properties = new NubesgenConfiguration();
        properties.setApplicationName("nubesgen-testapp-sql-server");
        properties.setRegion("westeurope");
        properties.setDatabaseConfiguration(new DatabaseConfiguration(DatabaseType.SQL_SERVER, Tier.SERVERLESS));

        Map<String, String> configuration = this.codeGeneratorService.generateAzureConfiguration(properties);

        testGeneratedFiles(properties, "sql-server", configuration, this.templateListService.listMainTemplates(),
                this.templateListService.listAppServiceTemplates(), this.templateListService.listSqlServerTemplates());

    }

    @Test
    void generateStorageBlobConfiguration() throws IOException {
        NubesgenConfiguration properties = new NubesgenConfiguration();
        properties.setApplicationName("nubesgen-testapp-storage-blob");
        properties.setRegion("westeurope");
        properties.setDatabaseConfiguration(new DatabaseConfiguration(DatabaseType.NONE, Tier.BASIC));
        List<AddonConfiguration> addons = new ArrayList<>();
        addons.add(new AddonConfiguration(AddonType.STORAGE_BLOB, Tier.BASIC));
        properties.setAddons(addons);

        Map<String, String> configuration = this.codeGeneratorService.generateAzureConfiguration(properties);

        testGeneratedFiles(properties, "storage-blob", configuration, this.templateListService.listMainTemplates(),
                this.templateListService.listAppServiceTemplates(), this.templateListService.listStorageBlobTemplates());

    }

    private void testGeneratedFiles(NubesgenConfiguration properties, String testDirectory,
                                    Map<String, String> configuration, List<String>... templateLists)
            throws IOException {

        int numberOfGeneratedFiles = 0;
        for (List<String> templateList : templateLists) {
            numberOfGeneratedFiles += templateList.size();
            for (String filename : templateList) {
                this.generateAndTestOneFile(properties, testDirectory, filename);
            }
        }
        assertEquals(numberOfGeneratedFiles, configuration.size());
    }

    private void generateAndTestOneFile(NubesgenConfiguration properties, String testDirectory, String filename) throws IOException {
        log.info("Validating {}", filename);
        String result = this.codeGeneratorService.generateFile(filename, properties);
        File testFile = new ClassPathResource("nubesgen/" + testDirectory + "/" + filename).getFile();
        String test = new String(
                Files.readAllBytes(testFile.toPath()));

        assertEquals(test, result);
    }
}