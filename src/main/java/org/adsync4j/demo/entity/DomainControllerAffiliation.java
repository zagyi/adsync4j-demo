package org.adsync4j.demo.entity;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JPA entity class implementing the similarly named {@link org.adsync4j.spi.DomainControllerAffiliation
 * DomainControllerAffiliation} interface.
 */
@Entity
public class DomainControllerAffiliation implements org.adsync4j.spi.DomainControllerAffiliation {

    private static final Splitter SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();

    @Id
    private String key;

    private String url;
    private String bindUser;
    private String bindPassword;

    private String invocationId;
    private Long highestCommittedUSN;
    private String syncBaseDN;
    private String searchFilter;
    private String searchDeletedObjectsFilter;
    private String attributesToSync;
    private String rootDN;

    public static DomainControllerAffiliation fromMap(Map<String, String> properties) {
        DomainControllerAffiliation result = new DomainControllerAffiliation();

        result.url = properties.get("url");
        result.bindUser = properties.get("bindUser");
        result.bindPassword = properties.get("bindPassword");

        result.rootDN = properties.get("rootDN");

        String invocationId = properties.get("invocationId");
        if (invocationId != null) {
            result.invocationId = UUID.fromString(invocationId).toString(); // make sure it's an UUID
        }
        String highestCommittedUSN = properties.get("highestCommittedUSN");
        if (highestCommittedUSN != null) {
            result.highestCommittedUSN = Long.valueOf(highestCommittedUSN);
        }
        result.syncBaseDN = properties.get("syncBaseDN");
        result.searchFilter = properties.get("searchFilter");
        result.searchDeletedObjectsFilter = properties.get("searchDeletedObjectsFilter");
        result.attributesToSync = properties.get("attributesToSync");

        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                      .add("key", key)
                      .add("invocationId", invocationId)
                      .add("highestCommittedUSN", highestCommittedUSN)
                      .add("syncBaseDN", syncBaseDN)
                      .add("searchFilter", searchFilter)
                      .add("searchDeletedObjectsFilter", searchDeletedObjectsFilter)
                      .add("attributesToSync", attributesToSync)
                      .add("rootDN", rootDN)
                      .add("url", url)
                      .add("bindUser", bindUser)
                      .add("bindPassword", bindPassword)
                      .toString();
    }

    public String getKey() {
        return key;
    }

    @Override
    public UUID getInvocationId() {
        return invocationId == null ? null : UUID.fromString(invocationId);
    }

    @Override
    public Long getHighestCommittedUSN() {
        return highestCommittedUSN;
    }

    @Override
    public String getSyncBaseDN() {
        return syncBaseDN;
    }

    @Override
    public String getSearchFilter() {
        return searchFilter;
    }

    @Override
    public String getSearchDeletedObjectsFilter() {
        return searchDeletedObjectsFilter;
    }

    @Override
    public List<String> getAttributesToSync() {
        return Lists.newArrayList(SPLITTER.split(attributesToSync));
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getBindUser() {
        return bindUser;
    }

    @Override
    public String getBindPassword() {
        return bindPassword;
    }

    @Override
    public String getRootDN() {
        return rootDN;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBindUser(String bindUser) {
        this.bindUser = bindUser;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    public DomainControllerAffiliation setKey(String key) {
        this.key = key;
        return this;
    }

    public DomainControllerAffiliation setInvocationId(UUID invocationId) {
        this.invocationId = invocationId.toString();
        return this;
    }

    public DomainControllerAffiliation setHighestCommittedUSN(Long highestCommittedUSN) {
        this.highestCommittedUSN = highestCommittedUSN;
        return this;
    }

    public DomainControllerAffiliation setSyncBaseDN(String syncBaseDN) {
        this.syncBaseDN = syncBaseDN;
        return this;
    }

    public DomainControllerAffiliation setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
        return this;
    }

    public DomainControllerAffiliation setSearchDeletedObjectsFilter(String searchDeletedObjectsFilter) {
        this.searchDeletedObjectsFilter = searchDeletedObjectsFilter;
        return this;
    }

    public DomainControllerAffiliation setAttributesToSync(String attributesToSync) {
        this.attributesToSync = attributesToSync;
        return this;
    }

    public DomainControllerAffiliation setRootDN(String rootDN) {
        this.rootDN = rootDN;
        return this;
    }
}
