package com.iflytek.edu.docker.maven;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.filtering.MavenFileFilter;

import com.iflytek.edu.docker.maven.access.DockerAccess;
import com.iflytek.edu.docker.maven.assembly.DockerArchiveCreator;
import com.iflytek.edu.docker.maven.util.MojoParameters;

/**
 * Base class which supports on-the-fly creation of data container
 *
 * @author roland
 * @since 12.06.14
 */
@Mojo(name = "build")
public class BuildDataImageMojo extends AbstractDockerMojo {
	
	@Parameter
    private List<String> ports;

    @Parameter
    private Map<String,String> env;

    @Parameter
    private List<String> runs;
    
    @Parameter
    private String command;
    
    @Parameter
    protected String assemblyDescriptor;

    @Parameter
    protected String assemblyDescriptorRef;

    @Parameter(property = "docker.image", required = false)
    private String image;

    @Parameter(property = "docker.baseImage", required = false)
    private String baseImage;

    // Directory as it is exported
    @Parameter(property = "docker.dataExportDir", required = false, defaultValue = "/maven")
    private String dataExportDir;

    @Component
    private DockerArchiveCreator dockerArchiveCreator;

    @Parameter
    private MavenArchiveConfiguration archive;

    @Component
    private MavenSession session;

    @Component
    private MavenFileFilter mavenFileFilter;

    protected String buildImage(DockerAccess dockerAccess) throws MojoFailureException, MojoExecutionException {
        String imageName = getImageName();
        MojoParameters params =  new MojoParameters(session, project, archive, mavenFileFilter);
        File dockerArchive = dockerArchiveCreator.create(params, baseImage, dataExportDir, ports, env, runs, command,
        		assemblyDescriptor, assemblyDescriptorRef);
        info("Created data image " + imageName);
        dockerAccess.buildImage(imageName, dockerArchive);
        return imageName;
    }

    protected String createImage(DockerAccess dockerAccess) throws MojoExecutionException, MojoFailureException {
        if (assemblyDescriptor != null && assemblyDescriptorRef != null) {
            throw new MojoExecutionException("No assemblyDescriptor or assemblyDescriptorRef has been given");
        }

        return buildImage(dockerAccess);
    }
    protected String getImageName() {
        String name = image != null ?
        		image :
                sanitizeDockerRepo(project.getGroupId()) + "/" + project.getArtifactId() + ":" + project.getVersion();
        return name;
    }

    private String sanitizeDockerRepo(String groupId) {
        return groupId.replace('.','-');
    }

    @Override
	protected void executeInternal(DockerAccess dockerAccess)
			throws MojoExecutionException, MojoFailureException {
    	createImage(dockerAccess);
	}
}
