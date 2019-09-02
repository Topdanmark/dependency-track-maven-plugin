package dk.topdanmark.maven.plugin.dependencytrack.uploadbom;

import dk.topdanmark.maven.plugin.dependencytrack.Utilities;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "uploadBom")
public class UploadBom extends AbstractMojo {
    @Parameter(property = "dTrackHostUrl", required = true)
    private String dTrackHostUrl;

    @Parameter(property = "apiKey", required = true)
    private String apiKey;

    @Parameter(property = "artifactId", defaultValue = "${project.artifactId}")
    private String artifactId;

    @Parameter(property = "version", defaultValue = "${project.version}")
    private String version;

    @Parameter(property = "pathToBom", defaultValue = "target/bom.xml", required = true)
    private String bomFilePath;

    @Parameter(property = "debug", defaultValue = "false")
    private boolean debugStatus;

    @Override
    public void execute() throws MojoExecutionException {
        Utilities utilities = new Utilities(dTrackHostUrl, apiKey, artifactId, version);

        utilities.setDebugStatus(debugStatus);
        String uuid = utilities.getDependencyTrackUUID(artifactId, version);
        if(uuid.equals("no uuid found")) uuid=utilities.createDependencyTrackProject();
        utilities.setUuid(uuid);
        utilities.setBomFilePath(bomFilePath);
        utilities.uploadBom();
        getLog().info("This project has UUID: " + utilities.getUuid());
    }

}
