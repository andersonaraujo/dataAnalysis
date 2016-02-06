package org.andersonaraujo.dataAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main class to start the application.
 *
 * @author Anderson Araujo.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String INPUT_FILE_EXTENSION = ".dat";

    public static final String OUTPUT_FILE_EXTENSION = ".done.dat";

    static final String ENV_VAR_NAME = "HOMEPATH";

    static final String ENV_VAR_HOMEPATH_NOT_CREATED_ERROR_MSG = "Environment variable 'HOMEPATH' was not created.";

    /**
     * Thread executor.
     */
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private String fullInputPath;

    private String fullOutputPath;

    public static void main(String[] args) {

        logger.info("Starting Data Analysis application.... ");

        new Main().startWatching();

    }

    private void startWatching() {

        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {

            setDirectories();

            Path watchingDir = Paths.get(fullInputPath);
            watchingDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            logger.info("Starting to watch for new files in the directory '{}'.", fullInputPath);

            while (true) {
                WatchKey key;
                try {
                    // wait for a key to be available
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    // get file name
                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    logger.debug("New file '{}' created in the directory '{}'.", fileName, fullInputPath);

                    // process create event
                    executor.submit(new FlatFileProcessor(fileName.toString(), fullInputPath, fullOutputPath));
                }

                // IMPORTANT: The key must be reset after processed
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }

        } catch (Exception e) {
            logger.error("Error while executing Data Analysis application.", e);
        }
    }

    /**
     * Gets the value of the environment variable {Main#ENV_VAR_NAME} defines the input and output directories.
     * <p>
     * Additionally verifies whether the directories exist, if not, then create them.
     */
    void setDirectories() {
        String homePath = System.getenv(ENV_VAR_NAME);
        if (homePath == null || homePath.isEmpty()) {
            throw new IllegalStateException(ENV_VAR_HOMEPATH_NOT_CREATED_ERROR_MSG);
        }

        logger.info("HOMEPATH is '{}'.", homePath);

        fullInputPath = homePath + File.separator + "data" + File.separator + "in" + File.separator;
        fullOutputPath = homePath + File.separator + "data" + File.separator + "out" + File.separator;

        logger.debug("Full input path is '{}'.", fullInputPath);
        logger.debug("Full input path is '{}'.", fullOutputPath);

        File inputDir = new File(fullInputPath);
        if (!inputDir.exists()) {
            logger.debug("Directory '{}' does not exist. It will be created.", fullInputPath);
            inputDir.mkdirs();
        }

        File outputDir = new File(fullOutputPath);
        if (!outputDir.exists()) {
            logger.debug("Directory '{}' does not exist. It will be created.", fullOutputPath);
            outputDir.mkdirs();
        }
    }

}
