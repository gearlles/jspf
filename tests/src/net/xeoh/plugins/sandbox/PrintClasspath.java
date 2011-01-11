package net.xeoh.plugins.sandbox;

import java.net.URL;
import java.net.URLClassLoader;

public class PrintClasspath {
    public static void main(String[] args) {

        // Get the System Classloader
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();

        // Get the URLs
        URL[] urls = ((URLClassLoader) sysClassLoader.getParent()).getURLs();

        for (int i = 0; i < urls.length; i++) {
            System.out.println(urls[i].getFile());
        }

    }
}