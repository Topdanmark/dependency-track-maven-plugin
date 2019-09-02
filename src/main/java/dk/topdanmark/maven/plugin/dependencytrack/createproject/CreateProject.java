package dk.topdanmark.maven.plugin.dependencytrack.createproject;

import dk.topdanmark.maven.plugin.dependencytrack.Utilities;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "createProject")
public class CreateProject extends AbstractMojo {
    @Parameter(property = "dTrackHostUrl", required = true)
    private String dTrackHostUrl;

    @Parameter(property = "apiKey", required = true)
    private String apiKey;

    @Parameter(property = "artifactId", defaultValue = "${project.artifactId}", required = true)
    private String artifactId;

    @Parameter(property = "version", defaultValue = "${project.version}", required = true)
    private String version;

    @Parameter(property = "debug", defaultValue = "false")
    private boolean debugStatus;

    @Override
    public void execute() throws MojoExecutionException {
        Utilities utilities = new Utilities(dTrackHostUrl, apiKey, artifactId, version);

        utilities.setDebugStatus(debugStatus);
        getLog().info("Creating project in Dependency Track");
        String uuid = utilities.createDependencyTrackProject();
        getLog().info("This project has UUID: " + uuid);
    }

}
