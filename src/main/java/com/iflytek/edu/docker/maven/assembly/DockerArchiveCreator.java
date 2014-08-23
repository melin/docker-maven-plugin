package com.iflytek.edu.docker.maven.assembly;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.assembly.archive.AssemblyArchiver;
import org.apache.maven.plugin.assembly.io.AssemblyReader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

import com.iflytek.edu.docker.maven.util.MojoParameters;

/**
 * Tool for creating a docker image tar ball including a Dockerfile for building
 * a data-only docker image which can be linked into other images, too.
 *
 * @author roland
 * @since 08.05.14
 */
@Component(role = DockerArchiveCreator.class)
public class DockerArchiveCreator {

    @Requirement
    private AssemblyArchiver assemblyArchiver;

    @Requirement
    private AssemblyReader assemblyReader;

    @Requirement
    private ArchiverManager archiverManager;

    public File create(MojoParameters params, String baseImage, String exportDir, List<String> ports,
    		Map<String,String> env, List<String> runs, String command, String assemblyDescriptor, String assemblyDescriptorRef)
            throws MojoFailureException, MojoExecutionException {
    	MavenProject project = params.getProject();
        File target = new File(project.getBasedir(),"target/");
        String docker = project.getArtifactId() + "-" + project.getVersion() + "-bin";
        File dockerDir = new File(target, docker);
        File destFile = new File(target,"docker-tmp/docker-build.tar");

        writeDockerFile(project, baseImage, exportDir, dockerDir, ports, env, runs, command);
        return createDockerBuildArchive(destFile,dockerDir);
    }

    private File createDockerBuildArchive(File archive, File dockerDir) throws MojoExecutionException {
        try {
            Archiver archiver = archiverManager.getArchiver("tar");
            archiver.addDirectory(dockerDir);
            archiver.setDestFile(archive);
            archiver.createArchive();
            return archive;
        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException("No archiver for type 'tar' found: " + e,e);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create archive " + archive + ": " + e,e);
        }

    }

    private File writeDockerFile(MavenProject project, String baseImage, String exportDir, File destDir, List<String> ports,
    		Map<String,String> env, List<String> runs, String command) throws MojoExecutionException {
        try {
            DockerFileBuilder builder = new DockerFileBuilder().exportDir(exportDir).add(project.getArtifactId() + "-" + project.getVersion(), "");
            builder.baseImage(baseImage);
            builder.setPorts(ports);
            builder.setEnv(env);
            builder.setRuns(runs);
            builder.setCommand(command);
            return builder.create(destDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create DockerFile in " + destDir + ": " + e,e);
        }
    }
}
