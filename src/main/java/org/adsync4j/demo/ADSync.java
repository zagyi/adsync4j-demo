package org.adsync4j.demo;

import com.unboundid.ldap.sdk.Attribute;
import org.adsync4j.InitialFullSyncRequiredException;
import org.adsync4j.InvocationIdMismatchException;
import org.adsync4j.demo.entity.DomainControllerAffiliation;
import org.adsync4j.demo.jpa.DomainControllerAffiliationRepository;
import org.adsync4j.demo.jpa.EmployeeRepository;
import org.adsync4j.impl.ActiveDirectorySyncServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Main entry point of the application. Implements the following workflow:
 * <ul>
 * <li>Checks if the database has a Domain Controller Affiliation record with key "default".</li>
 * <li>If the DCA record is absent, it loads the properties file (passed as command line argument),
 * and tries to create the DCA record with values specified in the file.</li>
 * <li>Tries to perform an incremental synchronization.</li>
 * <li>Falls back to full synchronization if that fails.</li>
 * </ul>
 */
@Component
public class ADSync {

    private static final String MAIN_APP_CTX_LOCATION = "classpath:spring/application-context.xml";
    public static final String DCA_PROPERTIES_TEMPLATE = "/dcaffiliation_template.properties";

    private final ActiveDirectorySyncServiceImpl<String, Attribute> _syncService;
    private final DomainControllerAffiliationRepository _dcaRepository;
    private final EmployeeRepository _employeeRepository;
    private final EmployeeProcessor _employeeProcessor;

    @Autowired
    public ADSync(
            ActiveDirectorySyncServiceImpl<String, Attribute> syncService,
            DomainControllerAffiliationRepository dcaRepository,
            EmployeeRepository employeeRepository,
            EmployeeProcessor employeeProcessor)
    {
        _syncService = syncService;
        _dcaRepository = dcaRepository;
        _employeeRepository = employeeRepository;
        _employeeProcessor = employeeProcessor;
    }

    public static void main(String[] args) {
        try {
            String dcaPropertiesFileName = args.length > 0 ? args[0] : null;
            createInstance().sync(dcaPropertiesFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ADSync createInstance() {
        return new ClassPathXmlApplicationContext(MAIN_APP_CTX_LOCATION).getBean(ADSync.class);
    }

    private void sync(String dcaPropertiesFileName) throws IOException {
        ensureDCA(dcaPropertiesFileName);

        boolean isFullSyncNeeded = true;

        try {
            System.out.println("Trying an incremental sync...");
            _syncService.incrementalSync(_employeeProcessor);
            System.out.println("Incremental sync successfully done.");
            isFullSyncNeeded = false;
        } catch (InvocationIdMismatchException e) {
            System.out.println("ID of the Active Directory database has been changed. Has the server been restored from a " +
                               "backup?");
            _employeeRepository.deleteAllInBatch();
        } catch (InitialFullSyncRequiredException e) {
            System.out.println("It seems to be the first sync from this server, cannot start with an incremental sync.");
        }

        if (isFullSyncNeeded) {
            System.out.println("Performing a full sync.");
            _syncService.fullSync(_employeeProcessor);
        }
    }

    private void ensureDCA(String dcaPropertiesFileName) throws IOException {
        if (_dcaRepository.findOne("default") == null) {
            assertPropertiesFileIsSpecified(dcaPropertiesFileName);
            DomainControllerAffiliation dca = loadDCAFromPropertiesFile(dcaPropertiesFileName);
            dca.setKey("default");
            _dcaRepository.save(dca);
        }
    }

    private static void assertPropertiesFileIsSpecified(String dcaPropertiesFileName) {
        if (isNullOrEmpty(dcaPropertiesFileName)) {
            System.out.println("Before synchronizing from Active Directory, you will need to specify a\n" +
                               "properties file that describes a Domain Controller Affiliation record.\n" +
                               "This is only necessary before the first run, as the affiliation record is persisted.");
            System.out.println("\nUse the following template to create the properties file:\n");

            printClassPathResource(DCA_PROPERTIES_TEMPLATE);
            System.exit(1);
        }
    }

    @SuppressWarnings("unchecked")
    private static DomainControllerAffiliation loadDCAFromPropertiesFile(String fileName) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(fileName));
        return DomainControllerAffiliation.fromMap((Map) properties);
    }

    private static void printClassPathResource(String resourceName) {
        try(InputStream stream = ADSync.class.getResourceAsStream(resourceName)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String line = in.readLine();
            while (line != null) {
                System.out.println(line);
                line = in.readLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
