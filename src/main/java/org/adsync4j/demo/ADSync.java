package org.adsync4j.demo;

import com.unboundid.ldap.sdk.Attribute;
import org.adsync4j.api.InitialFullSyncRequiredException;
import org.adsync4j.api.InvocationIdMismatchException;
import org.adsync4j.demo.entity.DomainControllerAffiliation;
import org.adsync4j.demo.jpa.DomainControllerAffiliationRepository;
import org.adsync4j.demo.jpa.EmployeeRepository;
import org.adsync4j.impl.ActiveDirectorySyncServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.adsync4j.demo.Utils.loadPropertiesFile;
import static org.adsync4j.demo.Utils.printClassPathResource;

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

    private final ActiveDirectorySyncServiceImpl<String, DomainControllerAffiliation, Attribute> _syncService;
    private final DomainControllerAffiliationRepository _dcaRepository;
    private final EmployeeRepository _employeeRepository;
    private final EmployeeProcessor _employeeProcessor;

    @Autowired
    public ADSync(
            ActiveDirectorySyncServiceImpl<String, DomainControllerAffiliation, Attribute> syncService,
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

    private static ADSync createInstance() throws IOException {
        return new ClassPathXmlApplicationContext(MAIN_APP_CTX_LOCATION).getBean(ADSync.class);
    }

    private void sync(String dcaPropertiesFileName) throws IOException {
        ensureDCA(dcaPropertiesFileName);

        try {
            System.out.println("Attempting an incremental sync...");
            _syncService.incrementalSync(_employeeProcessor);
            System.out.println("Incremental sync successfully done.");
            return;
        } catch (InvocationIdMismatchException e) {
            System.out.println("The ID of the domain controller has been changed (Has it been restored after a failure?), " +
                               "therefore a full re-sync is required now.");
            _employeeRepository.deleteAllInBatch();
        } catch (InitialFullSyncRequiredException e) {
            System.out.println("It seems to be the first sync operation with this domain controller, " +
                               "an initial full sync is required.");
        }

        System.out.println("Performing a full sync.");
        _syncService.fullSync(_employeeProcessor);
        System.out.println("Full sync successfully done.");
    }

    /**
     * Checks if the "default" Domain Controller Affiliation is already persisted. In case it's not (which means it's the
     * first execution of the program), the specified properties file is parsed and the DCA is saved into the repository.
     */
    private void ensureDCA(String dcaPropertiesFileName) throws IOException {
        if (_dcaRepository.findOne("default") == null) {
            assertPropertiesFileIsSpecified(dcaPropertiesFileName);
            Map<String, String> dcaProperties = loadPropertiesFile(dcaPropertiesFileName);
            DomainControllerAffiliation dca = DomainControllerAffiliation.fromMap(dcaProperties);
            dca.setKey("default");
            _dcaRepository.save(dca);
        } else {
            if (dcaPropertiesFileName != null) {
                System.out.println("WARNING! The database already contains the Domain Controller Affiliation. " +
                                   "The DCA properties file you specified will be ignored!");
            }
        }
    }

    private static void assertPropertiesFileIsSpecified(String dcaPropertiesFileName) {
        if (isNullOrEmpty(dcaPropertiesFileName)) {
            System.out.println("Before synchronizing from Active Directory, you will need to specify a\n" +
                               "properties file that describes a Domain Controller Affiliation record.\n" +
                               "This is only necessary before the first run, after which the affiliation record is persisted.");
            System.out.println("\nUse the following template to create the properties file:\n");
            printClassPathResource(DCA_PROPERTIES_TEMPLATE);
            System.exit(1);
        }
    }
}
