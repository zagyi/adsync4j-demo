package org.adsync4j.demo.jpa;

import org.adsync4j.SimpleRepository;
import org.adsync4j.demo.entity.DomainControllerAffiliation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.adsync4j.demo.jpa.JpaDCARepositoryAdapter.*;

/**
 * This class wraps a {@link DomainControllerAffiliationRepository} and adapts it to the {@link SimpleRepository} interface
 * which is expected by ADSync4J.
 */
@Component(BEAN_NAME)
public class JpaDCARepositoryAdapter implements SimpleRepository<String, DomainControllerAffiliation> {

    public static final String BEAN_NAME = "affiliationRepository";

    private final DomainControllerAffiliationRepository _jpaRepository;

    @Autowired
    public JpaDCARepositoryAdapter(DomainControllerAffiliationRepository jpaRepository) {_jpaRepository = jpaRepository;}

    @Override
    public DomainControllerAffiliation load(String key) {
        return _jpaRepository.findOne(key);
    }

    @Override
    public DomainControllerAffiliation save(DomainControllerAffiliation domainControllerAffiliation) {
        return _jpaRepository.save(domainControllerAffiliation);
    }
}
