package org.adsync4j.demo;

import com.unboundid.ldap.sdk.Attribute;
import org.adsync4j.demo.entity.Employee;
import org.adsync4j.demo.jpa.EmployeeRepository;
import org.adsync4j.spi.EntryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static org.adsync4j.impl.UUIDUtils.bytesToUUID;

/**
 * This class implements the {@link EntryProcessor} callback interface the processNew/Changed/Deleted methods of which are
 * invoked during the synchronization process. This implementation simply persists the new, updates the changed,
 * and removes the deleted entities in the target database.
 */
@Component
public class EmployeeProcessor implements EntryProcessor<Attribute> {

    private final static Logger LOG = LoggerFactory.getLogger(EmployeeProcessor.class);

    private final EmployeeRepository _employeeRepository;

    @Autowired
    public EmployeeProcessor(EmployeeRepository employeeRepository) {
        _employeeRepository = employeeRepository;
    }

    @Override
    public void processNew(List<Attribute> entry) {
        Employee persistentEmployee = _employeeRepository.save(attributesToEmployee(entry));
        LOG.debug("New employee: {}", persistentEmployee);
    }

    @Override
    public void processChanged(List<Attribute> entry) {
        Employee transientEmployee = attributesToEmployee(entry);
        Employee persistentEmployee = _employeeRepository.findByLdapId(transientEmployee.getLdapId().toString());

        persistentEmployee
                .setName(transientEmployee.getName())
                .setEmail(transientEmployee.getEmail());

        _employeeRepository.save(persistentEmployee);

        LOG.debug("Changed employee: {}", persistentEmployee);
    }

    @Override
    public void processDeleted(UUID entryId) {
        Employee employee = _employeeRepository.findByLdapId(entryId.toString());
        _employeeRepository.delete(employee);
        LOG.debug("Deleted employee with ID: " + entryId.toString());
    }

    private Employee attributesToEmployee(List<Attribute> entry) {
        UUID uuid = bytesToUUID(entry.get(0).getValueByteArray());
        String name = entry.get(1).getValue();
        String email = entry.get(3) == null ? null : entry.get(3).getValue();

        return new Employee()
                .setLdapId(uuid)
                .setName(name)
                .setEmail(email);
    }
}
