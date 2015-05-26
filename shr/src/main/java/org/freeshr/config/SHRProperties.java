package org.freeshr.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URISyntaxException;

@Component
public class SHRProperties {

    public static final int ONE_DAY = 86400;
    public static final int ONE_MINUTE = 6000;
    public static final String DIAGNOSTICS_SERVLET_PATH = "/diagnostics/health";
    private static final String REF_TERM_PATTERN = "/openmrs/ws/rest/v1/tr/referenceterms/";
    private static final String CONCEPT_PATTERN = "/openmrs/ws/rest/v1/tr/concepts/";
    private static final String MEDICATION_URL_PATTERN = "/openmrs/ws/rest/v1/tr/drugs/";
    private static final String VALUE_SET_PATTERN = "/openmrs/ws/rest/v1/tr/vs/";

    @Value("${MCI_PATIENT_PATH}")
    private String mciPatientPath;
    @Value("${TR_USER}")
    private String trUser;
    @Value("${TR_PASSWORD}")
    private String trPassword;

    @Value("${CASSANDRA_USER}")
    private String cassandraUser;
    @Value("${CASSANDRA_PASSWORD}")
    private String cassandraPassword;

    @Value("${CASSANDRA_KEYSPACE}")
    private String cassandraKeySpace;
    @Value("${CASSANDRA_HOST}")
    private String cassandraHost;
    @Value("${CASSANDRA_PORT}")
    private int cassandraPort;
    @Value("${CASSANDRA_TIMEOUT}")
    private int cassandraTimeout;
    @Value("${REST_POOL_SIZE}")
    private int restPoolSize;
    @Value("${VALIDATION_ZIP_PATH}")
    private String validationZipPath;

    @Value("${FACILITY_REGISTRY_URL}")
    private String facilityRegistryUrl;

    @Value("${PROVIDER_REGISTRY_URL}")
    private String providerRegistryUrl;

    @Value("${ENCOUNTER_FETCH_LIMIT}")
    private int encounterFetchLimit;
    @Value("${SERVER_CONNECTION_TIMEOUT}")
    private int serverConnectionTimeout;

    @Value("${TR_SERVER_BASE_URL}")
    private String trServerBaseUrl;

    @Value("${IDENTITY_SERVER_BASE_URL}")
    private String identityServerBaseUrl;

    @Value("${FACILITY_CACHE_TTL_SECONDS}")
    private String facilityCacheTTL;

    @Value("${IDP_CLIENT_ID}")
    private String idPClientId;

    @Value("${IDP_AUTH_TOKEN}")
    private String idPAuthToken;

    @Value("${LOCAL_CACHE_TTL}")
    private int localCacheTTL;

    @Value("${MCI_SERVER_URL}")
    private String mciServerUrl;

    @Value("${FHIR_DOCUMENT_SCHEMA_VERSION}")
    private String fhirDocumentSchemaVersion;


    @Value("${IDENTITY_CACHE_TTL}")
    private int identityCacheTTL;


    private String[] mciServerLocationUrls = null;
    private String[] facilityServerLocationUrls = null;
    private String[] providerServerLocationUrls = null;
    private String[] terminologyServerLocationUrls = null;

    public String getInterfaceTermContextPath() {
        return CONCEPT_PATTERN;
    }

    public String getTerminologiesContextPathForValueSet() {
        return VALUE_SET_PATTERN;
    }

    public String getTerminologiesContextPathForMedication() {
        return MEDICATION_URL_PATTERN;
    }

    public String getReferenceTermContextPath() {
        return REF_TERM_PATTERN;
    }

    public String getIdentityServerBaseUrl() {
        return identityServerBaseUrl;
    }


    public String getCassandraUser() {
        return cassandraUser;
    }

    public String getCassandraPassword() {
        return cassandraPassword;
    }

    public String getCassandraKeySpace() {
        return cassandraKeySpace;
    }

    public String getContactPoints() {
        return cassandraHost;
    }

    public int getCassandraPort() {
        return cassandraPort;
    }

    public int getCassandraTimeout() {
        return cassandraTimeout;
    }

    public int getRestPoolSize() {
        return restPoolSize;
    }

    public String getTrUser() {
        return trUser;
    }

    public String getTrPassword() {
        return trPassword;
    }

    public String getFhirDocumentSchemaVersion() {
        return fhirDocumentSchemaVersion;
    }

    public String getFacilityReferencePath() {
        String[] locations = getFacilityRegistryLocationUrls();
        return locations[0];
    }

    public String getFRLocationPath() {
        String[] locations = getFacilityRegistryLocationUrls();
        return locations[1];
    }

    public String getProviderReferencePath() {
        String[] locations = getProviderRegistryLocationUrls();
        return locations[0];
    }

    public String getValidationFilePath() throws URISyntaxException {
        if (StringUtils.isNotBlank(validationZipPath)) {
            return validationZipPath;
        } else {
            return new File(this.getClass().getClassLoader().getResource("validation.zip").toURI()).getAbsolutePath();
        }
    }

    public int getServerConnectionTimeout() {
        return serverConnectionTimeout;
    }

    public int getEncounterFetchLimit() {
        return encounterFetchLimit;
    }

    public String getTerminologyServerReferencePath() {
        String[] locations = getTerminologyRegistryLocationUrls();
        return locations[0];
    }

    public String getTRLocationPath() {
        String[] locations = getTerminologyRegistryLocationUrls();
        return locations[1];
    }

    public int getFacilityCacheTTL() {
        return facilityCacheTTL != null ? Integer.parseInt(facilityCacheTTL) : ONE_DAY;
    }

    public String getIdPClientId() {
        return idPClientId;
    }

    public String getIdPAuthToken() {
        return idPAuthToken;
    }

    public int getLocalCacheTTL() {
        return localCacheTTL;
    }

    public String getMciPatientPath() {
        return mciPatientPath;
    }

    public String getPatientReferencePath() {
        String[] mciUrls = getMciServerLocationUrls();
        String serverUrl = mciUrls[0];
        return getPatientUrl(serverUrl);
    }

    /**
     * Gets the internal server URL for MCI.
     * If no internal is provided then its the same as the public URL
     *
     * @return
     */
    public String getMCIPatientLocationPath() {
        String[] mciUrls = getMciServerLocationUrls();
        String serverUrl = mciUrls[1];
        return getPatientUrl(serverUrl);
    }

    private String getPatientUrl(String serverUrl) {
        if (serverUrl.endsWith("/")) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }
        String mciPatientCtxPath = getMciPatientPath();
        if (mciPatientCtxPath.startsWith("/")) {
            return serverUrl + mciPatientCtxPath;
        } else {
            return serverUrl + "/" + mciPatientCtxPath;
        }
    }

    private String[] getMciServerLocationUrls() {
        if (this.mciServerLocationUrls == null) {
            this.mciServerLocationUrls = parsePublicAndPrivateUrls(mciServerUrl);
        }
        return this.mciServerLocationUrls;
    }

    /**
     * The server URL are provided in 2 parts comma separated.
     * the first being the public URL and second being the internal network URL if any
     *
     * @param value
     * @return
     */
    private String[] parsePublicAndPrivateUrls(String value) {
        String[] parts = value.split(",");
        String[] results = new String[2];
        results[0] = parts[0].trim();
        if (parts.length > 1) {
            results[1] = parts[1].trim();
            if (results[1].length() == 0) {
                results[1] = results[0];
            }
        } else {
            results[1] = results[0];
        }
        return results;
    }

    private String[] getFacilityRegistryLocationUrls() {
        if (this.facilityServerLocationUrls == null) {
            this.facilityServerLocationUrls = parsePublicAndPrivateUrls(this.facilityRegistryUrl);
        }
        return this.facilityServerLocationUrls;
    }


    private String[] getProviderRegistryLocationUrls() {
        if (this.providerServerLocationUrls == null) {
            this.providerServerLocationUrls = parsePublicAndPrivateUrls(this.providerRegistryUrl);
        }
        return this.providerServerLocationUrls;
    }

    private String[] getTerminologyRegistryLocationUrls() {
        if (this.terminologyServerLocationUrls == null) {
            this.terminologyServerLocationUrls = parsePublicAndPrivateUrls(this.trServerBaseUrl);
        }
        return this.terminologyServerLocationUrls;
    }

    public int getIdentityCacheTTL() {
        return identityCacheTTL;
    }

}
