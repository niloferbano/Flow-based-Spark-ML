package de.tum.spark.ml.codegenerator;

import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Collections;


public class MavenBuild {

    public static final String MAVEN_PATH = "/usr/local/Cellar/maven/3.8.1";

    /**
     * Java package generation using Maven
     * @param mavenCommand
     * @param projectPath
     * @return
     * @throws MavenInvocationException
     */
    public static String runMavenCommand(String mavenCommand, String projectPath) throws MavenInvocationException {
        InvocationRequest invocationRequest = new DefaultInvocationRequest();
        invocationRequest.setPomFile(new File(projectPath));
        invocationRequest.setGoals(Collections.singletonList(mavenCommand));

        // create invoker
        Invoker invoker = new DefaultInvoker();

        // prepare output parser
        final StringBuilder mavenOutput = new StringBuilder();
        invoker.setOutputHandler(new InvocationOutputHandler() {
            public void consumeLine(String line) {
                mavenOutput.append(line).append(System.lineSeparator());
            }
        });

        // specify maven home path
        invoker.setMavenHome(new File(MAVEN_PATH));

        // run command
        InvocationResult invocationResult = invoker.execute(invocationRequest);
        if (invocationResult.getExitCode() != 0) {
            throw new IllegalStateException("Build failed.");
        }
        System.out.println("Build successful");
        return mavenOutput.toString();

    }
}
