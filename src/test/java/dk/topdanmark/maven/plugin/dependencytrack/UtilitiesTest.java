package dk.topdanmark.maven.plugin.dependencytrack;

import org.junit.Assert;
import org.junit.Test;

public class UtilitiesTest {
    private final String testUrl = "http://going.nowhere/at/all";
    private final String testApiKey = "1234567890abcdefg";
    private final String testArtifactId = "myTestArtifactId";
    private final String testVersion = "1.1.1";


    @Test
    public void testDependencyTrack() {
        String testUuid = "MyRandomTestUUID";
        Utilities utilities = new Utilities();
        utilities.setUrl(testUrl);
        utilities.setApiKey(testApiKey);
        utilities.setArtifactId(testArtifactId);
        utilities.setVersion(testVersion);
        utilities.setUuid(testUuid);

        Assert.assertEquals(utilities.getUrl(), testUrl);
        Assert.assertEquals(utilities.getApiKey(), testApiKey);
        Assert.assertEquals(utilities.getArtifactId(), testArtifactId);
        Assert.assertEquals(utilities.getVersion(), testVersion);
        Assert.assertEquals(utilities.getUuid(), testUuid);
    }

    @Test
    public void testDependencyTrackWithTwoParameters() {
        Utilities utilities = new Utilities(testArtifactId, testVersion);

        Assert.assertEquals(utilities.getArtifactId(), testArtifactId);
        Assert.assertEquals(utilities.getVersion(), testVersion);
    }

    @Test
    public void testDependencyTrackWithThreeParameters() {
        Utilities utilities = new Utilities(testUrl, testArtifactId, testVersion);

        Assert.assertEquals(utilities.getUrl(), testUrl);
        Assert.assertEquals(utilities.getArtifactId(), testArtifactId);
        Assert.assertEquals(utilities.getVersion(), testVersion);
    }

    @Test
    public void testDependencyTrackWithFourParameters() {
        Utilities utilities = new Utilities(testUrl, testApiKey, testArtifactId, testVersion);

        Assert.assertEquals(utilities.getUrl(), testUrl);
        Assert.assertEquals(utilities.getApiKey(), testApiKey);
        Assert.assertEquals(utilities.getArtifactId(), testArtifactId);
        Assert.assertEquals(utilities.getVersion(), testVersion);
    }

}