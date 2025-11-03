package net.nicolamurtas.android.emulator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Secure wrapper for executing system processes.
 * Prevents command injection and provides proper error handling.
 */
public class ProcessExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ProcessExecutor.class);
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    /**
     * Result of process execution.
     */
    public record ExecutionResult(
        int exitCode,
        List<String> output,
        List<String> errors
    ) {
        public boolean isSuccess() {
            return exitCode == 0;
        }
    }

    /**
     * Executes a command and waits for completion.
     */
    public static ExecutionResult execute(String... command) throws IOException, InterruptedException {
        return execute(null, null, DEFAULT_TIMEOUT_MINUTES, null, command);
    }

    /**
     * Executes a command with environment variables.
     */
    public static ExecutionResult execute(Path workingDirectory, String... command)
            throws IOException, InterruptedException {
        return execute(workingDirectory, null, DEFAULT_TIMEOUT_MINUTES, null, command);
    }

    /**
     * Executes a command with full control over parameters.
     *
     * @param workingDirectory Working directory for the process
     * @param environmentVars Additional environment variables
     * @param timeoutMinutes Timeout in minutes
     * @param inputProvider Function to provide input to the process
     * @param command Command and arguments to execute
     */
    public static ExecutionResult execute(
            Path workingDirectory,
            java.util.Map<String, String> environmentVars,
            int timeoutMinutes,
            Consumer<PrintWriter> inputProvider,
            String... command) throws IOException, InterruptedException {

        logger.debug("Executing command: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);

        if (workingDirectory != null) {
            pb.directory(workingDirectory.toFile());
        }

        if (environmentVars != null) {
            pb.environment().putAll(environmentVars);
        }

        Process process = pb.start();

        // Provide input if needed
        if (inputProvider != null) {
            try {
                // Don't use try-with-resources here to avoid closing the OutputStream
                // Closing stdin prematurely causes issues on Windows with avdmanager.bat
                PrintWriter writer = new PrintWriter(process.getOutputStream());
                inputProvider.accept(writer);
                writer.flush();
                // Intentionally not closing the writer - the process needs stdin to remain open
            } catch (Exception e) {
                logger.warn("Failed to provide input to process", e);
            }
        }

        // Read output and errors asynchronously
        CompletableFuture<List<String>> outputFuture = readStream(process.getInputStream());
        CompletableFuture<List<String>> errorFuture = readStream(process.getErrorStream());

        // Wait for completion with timeout
        boolean completed = process.waitFor(timeoutMinutes, TimeUnit.MINUTES);

        if (!completed) {
            process.destroyForcibly();
            throw new IOException("Process timed out after " + timeoutMinutes + " minutes");
        }

        int exitCode = process.exitValue();
        List<String> output = outputFuture.join();
        List<String> errors = errorFuture.join();

        if (exitCode != 0) {
            logger.warn("Command failed with exit code {}: {}", exitCode, String.join(" ", command));
        } else {
            logger.debug("Command completed successfully");
        }

        return new ExecutionResult(exitCode, output, errors);
    }

    /**
     * Executes a command asynchronously and returns the Process.
     */
    public static Process executeAsync(Path workingDirectory,
                                      java.util.Map<String, String> environmentVars,
                                      String... command) throws IOException {
        logger.debug("Executing command asynchronously: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);

        if (workingDirectory != null) {
            pb.directory(workingDirectory.toFile());
        }

        if (environmentVars != null) {
            pb.environment().putAll(environmentVars);
        }

        return pb.start();
    }

    /**
     * Executes a command with real-time output streaming.
     */
    public static ExecutionResult executeWithStreaming(
            Path workingDirectory,
            java.util.Map<String, String> environmentVars,
            Consumer<String> outputConsumer,
            Consumer<String> errorConsumer,
            Consumer<PrintWriter> inputProvider,
            String... command) throws IOException, InterruptedException {

        logger.debug("Executing command with streaming: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);

        if (workingDirectory != null) {
            pb.directory(workingDirectory.toFile());
        }

        if (environmentVars != null) {
            pb.environment().putAll(environmentVars);
        }

        Process process = pb.start();

        // Provide input if needed
        if (inputProvider != null) {
            try {
                // Don't use try-with-resources here to avoid closing the OutputStream
                // Closing stdin prematurely causes issues on Windows with avdmanager.bat
                PrintWriter writer = new PrintWriter(process.getOutputStream());
                inputProvider.accept(writer);
                writer.flush();
                // Intentionally not closing the writer - the process needs stdin to remain open
            } catch (Exception e) {
                logger.warn("Failed to provide input to process", e);
            }
        }

        // Stream output and errors in real-time
        CompletableFuture<List<String>> outputFuture = readStreamWithCallback(
            process.getInputStream(), outputConsumer);
        CompletableFuture<List<String>> errorFuture = readStreamWithCallback(
            process.getErrorStream(), errorConsumer);

        int exitCode = process.waitFor();
        List<String> output = outputFuture.join();
        List<String> errors = errorFuture.join();

        return new ExecutionResult(exitCode, output, errors);
    }

    /**
     * Reads an input stream asynchronously.
     */
    private static CompletableFuture<List<String>> readStream(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines().toList();
            } catch (IOException e) {
                logger.error("Error reading stream", e);
                return List.of();
            }
        });
    }

    /**
     * Reads an input stream with a callback for each line.
     */
    private static CompletableFuture<List<String>> readStreamWithCallback(
            InputStream inputStream, Consumer<String> lineConsumer) {
        return CompletableFuture.supplyAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines()
                    .peek(line -> {
                        if (lineConsumer != null) {
                            lineConsumer.accept(line);
                        }
                    })
                    .toList();
            } catch (IOException e) {
                logger.error("Error reading stream", e);
                return List.of();
            }
        });
    }

    /**
     * Kills a process gracefully, then forcefully if needed.
     */
    public static void killProcess(Process process) {
        if (process == null || !process.isAlive()) {
            return;
        }

        logger.debug("Terminating process (PID: {})", process.pid());
        process.destroy();

        try {
            boolean terminated = process.waitFor(5, TimeUnit.SECONDS);
            if (!terminated) {
                logger.warn("Process did not terminate gracefully, forcing termination");
                process.destroyForcibly();
                process.waitFor(2, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }
}
