package com.vladimirionescu.codejam.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

@Mojo(name = "initialize", requiresProject = false)
public class InitializeMojo extends AbstractMojo
{

    @Parameter(property = "contestId", defaultValue = "")
    private String contestId;
    
    @Component
    private Prompter prompter;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if(contestId ==  null || "".equals(contestId))
        {
            getLog().info("No Contest Id specified, will attempt to find ongoig contest");
        }
        else
        {
            getLog().info("Setting up contest " + contestId);
        }
        getLog().info("You typed " + getUserInput("Type something"));
    }
    
    public String getUserInput(String message) throws MojoExecutionException
    {
        try
        {
            return prompter.prompt(message);
        }
        catch (PrompterException e)
        {
            throw new MojoExecutionException("Failed to get user input", e);
        }
    }

}
