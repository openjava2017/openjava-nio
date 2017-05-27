package com.openjava.nio.logback;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.util.Assert;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;

import com.openjava.nio.util.ClassUtils;
import com.openjava.nio.util.StringUtils;

public class LogbackContextLoader
{
    private static final String LOGBACK_CONF_FILE_PATH = "logback.conf.file.path";

    public static void initLogback(String defaultLocation)
    {
        Assert.notNull(defaultLocation, "Default logback conf file path must not be null");
        URL confUrl = null;
        
        // Find the configuration file path in system environment
        String pathToUse = System.getProperty(LOGBACK_CONF_FILE_PATH);
        if (StringUtils.isNotBlank(pathToUse)) {
            File file = new File(pathToUse);
            if (file.exists() && file.isFile()) {
                try {
                    confUrl = file.toURI().toURL();
                } catch (MalformedURLException mue) {
                }
            } else {
                confUrl = getClassLoaderResource(pathToUse);
            }
        } else {
            confUrl = getClassLoaderResource(defaultLocation);
        }
        
        LoggerContext loggerContext = (LoggerContext)StaticLoggerBinder.getSingleton().getLoggerFactory();
        if (confUrl != null) {
            // in the current version logback automatically configures at startup the context, so we have to reset it
            loggerContext.reset();
            // reinitialize the logger context.  calling this method allows configuration through groovy or xml
            try {
                new ContextInitializer(loggerContext).configureByResource(confUrl);
            } catch (Exception je) {
                je.printStackTrace();
            }
        } else {
            // No need basic configure logback perhaps
            BasicConfigurator.configure(loggerContext);
        }
    }
    
    private static URL getClassLoaderResource(String pathToUse)
    {
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        
        return ClassUtils.getDefaultClassLoader().getResource(pathToUse);
    }
}