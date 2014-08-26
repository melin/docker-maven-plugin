基于[docker-maven-plugin](https://github.com/rhuss/docker-maven-plugin)项目定制修改，之保留构建image功能。

```xml
<plugin>
	<groupId>com.iflytek.edu.docker.maven</groupId>
	<artifactId>docker-maven-plugin</artifactId>
	<version>0.1.0-SNAPSHOT</version>
  	<configuration>
  		<baseImage>melin/cloud-server:latest</baseImage>
  		<url>http://192.168.68.133:2375</url>
  		<image>melin/${project.artifactId}:${project.version}</image>
		<dataExportDir>/${project.artifactId}</dataExportDir>
		<assemblyDescriptor>src/main/assembly/assembly.xml</assemblyDescriptor>
		<forceRemoveExistImage>true</forceRemoveExistImage>
		<runs>
			<run>chmod +x /${project.artifactId}/bin/server.sh</run>
		</runs>
		<command>/${project.artifactId}/bin/server.sh start</command>
  	</configuration>
</plugin>
```

执行构建image命令: 

	clean install assembly:single docker:build