package com.emc.caspian.ccs.workflow;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

/**
 * Created by gulavb on 3/30/2015.
 */
public class TaskExecutor {

  public TaskExecutor(String jarLocation) {
    this.jarLocation = jarLocation;
  }

  public Object execute(PrintWriter out, PrintWriter err, Map<String, String> environment, String taskName,
                        Object... parameters) throws MalformedURLException, IllegalAccessException,
                                                     InstantiationException, InvocationTargetException {
    return this.searchAndExecuteMethod(out, err, environment, taskName, parameters);
  }

  private Object searchAndExecuteMethod(PrintWriter out, PrintWriter err, Map<String, String> environment,
                                        String taskName, Object... parameters) throws MalformedURLException,
                                                                                      IllegalAccessException,
                                                                                      InstantiationException,
                                                                                      InvocationTargetException {

    logger.info("Searching {}, jar location={}", taskName, jarLocation);
    Reflections reflections = null;
    if (jarLocation == null || jarLocation.isEmpty()) {
      reflections = new Reflections(ClasspathHelper.forPackage(defaultPackage), new MethodAnnotationsScanner());
    } else {
      URL jarURL = new URL("file:\\" + jarLocation);
      logger.info("Searching URL {}", jarURL);
      URLClassLoader urlcl = new URLClassLoader(new URL[]{jarURL});
      reflections = new Reflections(
          new ConfigurationBuilder()
              .setUrls(jarURL)
              .addClassLoader(urlcl)
              .setScanners(new MethodAnnotationsScanner())
      );
    }
    for (Method method : reflections.getMethodsAnnotatedWith(Task.class)) {
      if (method.getAnnotation(Task.class).name().equals(taskName)) {
        Class aClass = method.getDeclaringClass();
        logger.info("Declaring class name={}", aClass.getName());
        Object o = method.getDeclaringClass().newInstance();
        if (o instanceof TaskBase) {
          logger.info("Initialising TaskBase");
          ((TaskBase) o).out = out;
          ((TaskBase) o).err = err;
          ((TaskBase) o).environment = environment;
        }
        logger.info("Invoking method={}", method.getName());
        Object returnValue = method.invoke(o, parameters);
        return returnValue;
      }
    }
    throw new RuntimeException(String.format("Task %s not found", taskName));
  }

  private String jarLocation;
  private final String defaultPackage = "com.emc.caspian";

  private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);
}
