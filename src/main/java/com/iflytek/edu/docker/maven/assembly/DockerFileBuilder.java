package com.iflytek.edu.docker.maven.assembly;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.util.FileUtils;

/**
 * Create a dockerfile
 *
 * @author roland
 * @since 17.04.14
 */
public class DockerFileBuilder {

    private String baseImage = "centos:centos6";

    private String maintainer = "libinsong1204@gmail.com";

    private String exportDir = "/maven";

    private List<String> ports;

    private Map<String,String> env;

    private List<String> runs;
    
    private String command;
    
    private List<AddEntry> addEntries;

    public File create(File destDir) throws IOException {
        File target = new File(destDir,"Dockerfile");
        FileUtils.fileWrite(target, content());
        return target;
    }

    public String content() {
        if (addEntries.size() == 0) {
            throw new IllegalArgumentException("No entries added");
        }
        StringBuilder b = new StringBuilder();
        b.append("FROM ").append(baseImage).append("\n");
        b.append("MAINTAINER ").append(maintainer).append("\n");
        b.append("\n");

        // Entries
        for (AddEntry entry : addEntries) {
            b.append("ADD ").append(entry.source).append(" ")
             .append(exportDir).append("/").append(entry.destination).append("\n");
        }
        b.append("\n");
        
        if(env != null) {
	        for(Entry<String, String> entry : env.entrySet()) {
	        	 b.append("ENV ").append(entry.getKey()).append(" ").append(entry.getValue()).append("\n");
	        }
	        b.append("\n");
        }
        
        if(runs != null) {
        	b.append("RUN \\").append("\n");
	        for(int i=0, len=runs.size(); i<len; i++) {
	        	if(i == (len-1))
	        		b.append("  ").append(runs.get(i)).append("\n");
	        	else
	        		b.append("  ").append(runs.get(i)).append(" && \\").append("\n");
	        }
	        b.append("\n");
        }

        // Volume export
        b.append("VOLUME [\"").append(exportDir).append("\"]\n").append("\n");
        
        b.append("CMD ").append(command).append("\n").append("\n");
        
        if(ports != null) {
	        for(String port : ports) {
	        	b.append("EXPOSE ").append(port).append("\n");
	        }
	        b.append("\n");
        }
        
        if(ports != null) {
	        for(String port : ports) {
	        	b.append("EXPOSE ").append(port).append("\n");
	        }
        }

        return b.toString();
    }

    // ==========================================================================
    // Builder stuff ....
    public DockerFileBuilder() {
        addEntries = new ArrayList<AddEntry>();
    }

    public DockerFileBuilder baseImage(String baseImage) {
        this.baseImage = baseImage;
        return this;
    }

    public DockerFileBuilder maintainer(String maintainer) {
        this.maintainer = maintainer;
        return this;
    }

    public DockerFileBuilder exportDir(String exportDir) {
        this.exportDir = exportDir;
        return this;
    }

    public void setPorts(List<String> ports) {
		this.ports = ports;
	}

	public void setEnv(Map<String, String> env) {
		this.env = env;
	}

	public void setRuns(List<String> runs) {
		this.runs = runs;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public DockerFileBuilder add(String source,String destination) {
        this.addEntries.add(new AddEntry(source, destination));
        return this;
    }

    // All entries required, destination is relative to exportDir
    private static final class AddEntry {
        private String source,destination;

        private AddEntry(String src, String dest) {
            source = src;

            // Strip leading slashes
            destination = dest;

            // squeeze slashes
            while (destination.startsWith("/")) {
                destination = destination.substring(1);
            }
        }

    }
}
