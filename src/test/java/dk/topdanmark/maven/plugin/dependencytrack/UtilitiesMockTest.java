package dk.topdanmark.maven.plugin.dependencytrack;

import de.onyxbits.raccoon.semanticversion.SemanticVersion;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static dk.topdanmark.maven.plugin.TestUtilities.readFromInputStream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class UtilitiesMockTest {

    @Mock
    private CloseableHttpClient client;
    @Mock
    private CloseableHttpResponse response;
    @Mock
    private HttpEntity entity;
    @Mock
    private StatusLine statusLine;

    private Utilities utilities;
    private String jsonData;
    private String testArtifactId;
    private String testVersion;
    private String testUuid;

    @Before
    public void setUp() {
        utilities = new Utilities(client);

        jsonData = "{\"TestData\":\"True and empty test data\"}";
        String testUrl = "http://going.nowhere/at/all";
        String testApiKey = "1234567890abcdefg";
        testArtifactId = "JavaEESampleApp";
        testVersion = "1.9.3-SNAPSHOT";
        testUuid = "521cad69-f21e-4c57-9cf1-0d2f3d1fd90b";

        utilities.setArtifactId(testArtifactId);
        utilities.setVersion(testVersion);
        utilities.setUrl(testUrl);
        utilities.setApiKey(testApiKey);
    }

    @Test
    public void getProjectTest() throws IOException {
        JSONArray testJsonData;
        Class clazz = UtilitiesMockTest.class;

        InputStream expectedInputStream = clazz.getResourceAsStream("/dk/topdanmark/maven/plugin/dependencytrack/dependencytracktest.json");
        InputStream actualInputStream = clazz.getResourceAsStream("/dk/topdanmark/maven/plugin/dependencytrack/dependencytracktest.json");

        try {
            jsonData = readFromInputStream(expectedInputStream);
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
        testJsonData = new JSONArray(jsonData);
        when(client.execute(any(HttpGet.class))).thenReturn(response);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(actualInputStream);
        JSONAssert.assertEquals(testJsonData, utilities.getDependencyTrackProject(testArtifactId), true);
    }

    @Test
    public void createProjectTest() throws IOException {
        Class clazz = UtilitiesMockTest.class;

        InputStream actualInputStream = clazz.getResourceAsStream("/dk/topdanmark/maven/plugin/dependencytrack/dependencytracktest-create-project.json");

        when(client.execute(any(HttpPut.class))).thenReturn(response);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(actualInputStream);
        Assert.assertEquals(testUuid, utilities.createDependencyTrackProject());
    }

    @Ignore("Does not mock getDependencyTrackUUID() properly")
    @Test
    public void removeVersionTest() throws IOException {
        //Cannot figure out how to make this work
        //getDependencyTrackUUID uses getDependencyTrackProject that I cannot circumvent
        when(utilities.getDependencyTrackUUID(testArtifactId, testVersion)).thenReturn(testUuid);
        when(client.execute(any(HttpDelete.class))).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(204);
        Assert.assertEquals("success", utilities.removeVersion(testArtifactId, testVersion, true));
    }

    @Test
    public void getVersionListTest() throws IOException, ParseException {
        Class clazz = UtilitiesMockTest.class;
        Utilities utilities = new Utilities(client);
        List<SemanticVersion> expected = Arrays.asList(
                new SemanticVersion("1.9.99-SNAPSHOT"),
                new SemanticVersion("1.9.90-SNAPSHOT"),
                new SemanticVersion("1.9.3-SNAPSHOT"),
                new SemanticVersion("1.9.2-SNAPSHOT"),
                new SemanticVersion("1.3.62"),
                new SemanticVersion("1.10.0-SNAPSHOT"));

        InputStream actualInputStream = clazz.getResourceAsStream("/dk/topdanmark/maven/plugin/dependencytrack/dependencytracktest.json");

        when(client.execute(any(HttpGet.class))).thenReturn(response);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(actualInputStream);
        Assert.assertEquals(expected, utilities.getVersionList());
    }

    @Test
    public void getDependencyTrackUUIDReturnsEmptyStringTest() throws IOException {
        InputStream emptyInputStream = new ByteArrayInputStream("[]".getBytes());

        JSONArray emptyArray = new JSONArray("[]");

        Utilities utilities = new Utilities(client);
        when(client.execute(any(HttpGet.class))).thenReturn(response);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContent()).thenReturn(emptyInputStream);

        JSONAssert.assertEquals(emptyArray, utilities.getDependencyTrackProject(utilities.getArtifactId()), false);
    }

}
