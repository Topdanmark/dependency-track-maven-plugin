package dk.topdanmark.maven.plugin.dependencytrack;

import de.onyxbits.raccoon.semanticversion.SemanticVersion;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains the logic required to communicate with an instance of Dependency-Track
 */

public class Utilities {
    private final CloseableHttpClient client;

    private String url;
    private String apiKey;
    private String artifactId;
    private String version;
    private String uuid;
    private String bomFilePath;
    private boolean debugStatus = false;

    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String ACCEPT_KEY = "Accept";
    private static final String X_API_KEY = "X-API-Key";
    private static final String VERSION_KEY = "version";
    private static final String APPLICATION_JSON = "application/json";

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());


    /**
     * Create a default instance
     */
    Utilities() {
        this.client = HttpClients.createDefault();
    }

    /**
     * Creates an instance
     * Used for Mockito tests
     */
    Utilities(CloseableHttpClient client) {
        this.client = client;
    }

    /**
     * Creates an instance
     *
     * @param artifactId - the name of the project in Dependency-Track
     * @param version    - the version of the project in Dependency-Track
     */
    Utilities(String artifactId, String version) {
        this.client = HttpClients.createDefault();
        this.setApiKey("");
        this.setUrl("");
        this.setArtifactId(artifactId);
        this.setVersion(version);
    }

    /**
     * Creates an instance
     *
     * @param url        - the URL to the running instance of Dependency-Track
     * @param artifactId - the name of the project in Dependency-Track
     * @param version    - the version of the project in Dependency-Track
     */
    Utilities(String url, String artifactId, String version) {
        this(artifactId, version);
        this.setUrl(url);
    }

    /**
     * Creates an instance
     *
     * @param url        - the URL to the running instance of Dependency-Track
     * @param apiKey     - the apiKey needed to authenticate to Dependency-Track
     * @param artifactId - the name of the project in Dependency-Track
     * @param version    - the version of the project in Dependency-Track
     */
    public Utilities(String url, String apiKey, String artifactId, String version) {
        this(url, artifactId, version);
        this.setApiKey(apiKey);
    }

    /**
     * Retrieves the uuid
     *
     * @return String uuid - the uuid of the project asked
     */
    public String getDependencyTrackUUID(String artifactId, String version) {
        AtomicReference<String> arUuid = new AtomicReference<>("no uuid found");
        JSONArray jsonArray = getDependencyTrackProject(artifactId);

        jsonArray.forEach(object -> {
            JSONObject jsonObject = (JSONObject) object;
            if (this.getDebugStatus()) {
                logger.log(Level.INFO, "Expected Version: {0}", version);
                logger.log(Level.INFO, "Actual Version: {0}", jsonObject.getString(VERSION_KEY));
            }
            if (version.equals(jsonObject.getString(VERSION_KEY))) {
                if (this.getDebugStatus()) {
                    logger.log(Level.INFO, "jsonObject: {0}", jsonObject);
                }
                arUuid.set(jsonObject.getString("uuid"));
            }
        });
        return arUuid.get();
    }

    /**
     * Gets information of a given from from Dependency Track
     *
     * @return A JSON array of project information
     */
    JSONArray getDependencyTrackProject(String artifactId) {
        String dTrackUrl = this.getUrl() + "/api/v1/project?name=" + artifactId;
        JSONArray jsonArray;
        String result;
        HttpGet getRequest = new HttpGet(dTrackUrl);

        getRequest.setHeader(CONTENT_TYPE_KEY, APPLICATION_JSON);
        getRequest.setHeader(ACCEPT_KEY, APPLICATION_JSON);
        getRequest.setHeader(X_API_KEY, this.getApiKey());

        try (CloseableHttpResponse httpResponse = client.execute(getRequest)) {
            result = this.dTrackHttpResponse(httpResponse);

            if (this.getDebugStatus()) {
                this.showDebugInfo(dTrackUrl, getRequest.getURI().toString(), httpResponse, result);
            }

        } catch (IOException exception) {
            logger.severe(exception.getMessage());
            result = "[]";
        }
        jsonArray = new JSONArray(result);
        return jsonArray;
    }

    /**
     * Creates a new project in Dependency Track
     *
     * @return String uuid - the uuid of the project created
     */
    public String createDependencyTrackProject() {
        String dTrackUrl = this.getUrl() + "/api/v1/project";
        String result;
        HttpPut putRequest = new HttpPut(dTrackUrl);
        String body = "{\"name\": \"" + this.getArtifactId() + "\", \"version\": \"" + this.getVersion() + "\"}";
        putRequest.setHeader(CONTENT_TYPE_KEY, APPLICATION_JSON);
        putRequest.setHeader(ACCEPT_KEY, APPLICATION_JSON);
        putRequest.setHeader(X_API_KEY, this.getApiKey());

        try {
            putRequest.setEntity(new StringEntity(body));
        } catch (UnsupportedEncodingException exception) {
            logger.severe(exception.getMessage());
        }

        try (CloseableHttpResponse httpResponse = client.execute(putRequest)) {
            String response = this.dTrackHttpResponse(httpResponse);

            JSONObject jsonObject = new JSONObject(response);
            result = jsonObject.getString("uuid");
            if (this.getDebugStatus()) {
                this.showDebugInfo(dTrackUrl, putRequest.getURI().toString(), httpResponse, result);
            }
        } catch (IOException exception) {
            logger.log(Level.SEVERE, exception.getMessage());
            result = "";
        }
        return result;
    }

    /**
     * Uploads a BOM file to Dependency Track
     *
     * @return String result
     */
    public String uploadBom() {
        String dTrackUrl = this.getUrl() + "/api/v1/bom";
        String result;

        HttpPut putRequest = new HttpPut(dTrackUrl);
        putRequest.setHeader(CONTENT_TYPE_KEY, APPLICATION_JSON);
        putRequest.setHeader(ACCEPT_KEY, APPLICATION_JSON);
        putRequest.setHeader(X_API_KEY, this.getApiKey());
        putRequest.setEntity(new StringEntity(createBomPayload(), "UTF-8"));

        try (CloseableHttpResponse httpResponse = client.execute(putRequest)) {
            result = this.dTrackHttpResponse(httpResponse);

            if (this.getDebugStatus()) {
                this.showDebugInfo(dTrackUrl, putRequest.getURI().toString(), httpResponse, result);
            }

        } catch (IOException exception) {
            logger.log(Level.SEVERE, exception.getMessage());
            result = "";
        }

        return result;
    }

    public void removeOldVersions(boolean snapshotOnly) {
        List<SemanticVersion> versions = getVersionList();
        Collections.sort(versions);
        String currentVersion = getVersion();
        versions.forEach(v ->
                {
                    try {
                        SemanticVersion semanticVersion = new SemanticVersion(currentVersion);
                        int sigNum = Integer.signum(v.compareTo(semanticVersion));
                        if (sigNum < 0) {
                            String result = removeVersion(getArtifactId(), v.toString(), snapshotOnly);
                            logger.log(Level.INFO, "Delete result: {0}", result);
                        }
                    } catch (ParseException exception) {
                        logger.log(Level.SEVERE, exception.getMessage());
                    }
                }
        );
    }

    String removeVersion(String artifactId, String version, boolean snapshotOnly) {
        String result = "";

        if (!snapshotOnly || (version.contains("-SNAPSHOT"))) {
            String dTrackUrl = this.getUrl() + "/api/v1/project/" + getDependencyTrackUUID(artifactId, version);
            HttpDelete deleteRequest = new HttpDelete(dTrackUrl);

            deleteRequest.setHeader(CONTENT_TYPE_KEY, APPLICATION_JSON);
            deleteRequest.setHeader(ACCEPT_KEY, APPLICATION_JSON);
            deleteRequest.setHeader(X_API_KEY, this.getApiKey());

            try (CloseableHttpResponse httpResponse = client.execute(deleteRequest)) {
                StatusLine statusLine = httpResponse.getStatusLine();
                int status = statusLine.getStatusCode();
                if (status == 204) {
                    logger.log(Level.INFO, "Project removed from Dependency Track");
                    result = "success";
                }

            } catch (IOException exception) {
                logger.severe(exception.getMessage());
                result = "";
            }
        }
        return result;
    }

    private String dTrackHttpResponse(CloseableHttpResponse httpResponse) throws IOException {
        String result;
        HttpEntity entity = httpResponse.getEntity();
        result = EntityUtils.toString(entity);
        return result;
    }

    List<SemanticVersion> getVersionList() {
        List<SemanticVersion> semanticVersions = new ArrayList<>();
        JSONArray jsonArray = getDependencyTrackProject(this.getArtifactId());
        if (jsonArray != null) {
            jsonArray.forEach(object -> {
                        JSONObject jsonObject = (JSONObject) object;
                        String artifactVersion = jsonObject.getString(VERSION_KEY);
                        try {
                            semanticVersions.add(new SemanticVersion(artifactVersion));
                        } catch (ParseException exception) {
                            logger.log(Level.SEVERE, exception.getMessage());
                        }
                    }
            );
        }
        return semanticVersions;
    }

    String removeSnapshot(String artifactVersion) {
        if (artifactVersion.contains("-SNAPSHOT")) {
            int index = artifactVersion.indexOf("-SNAPSHOT");
            artifactVersion = artifactVersion.substring(0, index);
        }
        return artifactVersion;
    }

    private void showDebugInfo(String dTrackUrl, String actualURL, HttpResponse response, String result) {
        logger.log(Level.INFO, "Target HTTP URL: {0}", dTrackUrl);
        logger.log(Level.INFO, "Actual HTTP URL: {0}", actualURL);

        for (Header header : response.getAllHeaders()) {
            logger.log(Level.INFO, "Header:" + header.getName() + " value: " + header.getValue());
        }
        logger.log(Level.INFO, "HTTP Response result: {0}", result);
        logger.log(Level.INFO, "HTTP Response Status Code: {0}", response.getStatusLine().getStatusCode());

    }

    private String createBomPayload() {
        String bomAsString = readBomFile(this.getBomFilePath());
        String bomBase64Encoded = Base64.getEncoder().encodeToString(bomAsString.getBytes());
        return "{\"project\": \"" + this.getUuid() + "\", \"autoCreate\": \"true\", \"bom\": \"" + bomBase64Encoded + "\"}";
    }

    private String readBomFile(String filePath) {
        String content = "";

        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException exception) {
            logger.log(Level.SEVERE, exception.getMessage());
        }
        return content;
    }

    String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }

    String getApiKey() {
        return apiKey;
    }

    void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    String getArtifactId() {
        return artifactId;
    }

    void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    String getVersion() {
        return version;
    }

    void setVersion(String version) {
        this.version = version;
    }

    private boolean getDebugStatus() {
        return debugStatus;
    }

    public void setDebugStatus(boolean debugStatus) {
        this.debugStatus = debugStatus;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setBomFilePath(String bomFilePath) {
        this.bomFilePath = bomFilePath;
    }

    public String getBomFilePath() {
        return bomFilePath;
    }

}
