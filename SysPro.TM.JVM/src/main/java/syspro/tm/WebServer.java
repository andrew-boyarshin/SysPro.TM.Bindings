package syspro.tm;

import java.time.Duration;

/**
 * Dev Tools web server enables visual inspection of syntax trees to simplify debugging
 * of your SysPro language implementation.
 * <ol>
 *     <li>Call {@link WebServer#start()} at the beginning of your solution entry point (in your main method).</li>
 *     <li>Call {@link WebServer#waitForWebServerExit()} at the end of your solution.</li>
 *     <li>Run your solution.</li>
 *     <li>Navigate to <a href="http://localhost:15412/">localhost:15412</a> with your browser.</li>
 *     <li>Choose the test and parser run you are interested in debugging.</li>
 *     <li>Expand the syntax tree as needed, examine diagnostics in more detail.</li>
 * </ol>
 */
public final class WebServer {

    public static final int DEFAULT_PORT = 15412;

    private WebServer() {
    }

    /**
     * Start a Dev Tools web server thread.
     * Waits for the server to start, but doesn't block the current thread otherwise.
     * You should use this overload only when there is a conflict for {@link WebServer#DEFAULT_PORT} ownership.
     * @param port Network port to bind the web server to.
     */
    public static void start(int port) {
        Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().name("Web Server Shutdown Hook").unstarted(WebServer::stopHook));
        Library.startWebServer(port);
    }

    /**
     * Start a web server listening on the {@link WebServer#DEFAULT_PORT default port}.
     * Waits for the server to start, but doesn't block the current thread otherwise.
     * @see WebServer#start(int)
     */
    public static void start() {
        start(DEFAULT_PORT);
    }

    /**
     * Block the current thread indefinitely until web server has stopped.
     * That happens when platform-specific "stop" signal is sent to the JVM.
     * In IntelliJ IDEA that can be done by pressing the red rectangle in the "Run" pane.
     */
    public static void waitForWebServerExit() {
        Library.waitForWebServerExit();
    }

    /**
     * Block the current thread until web server has stopped or the time limit is exceeded.
     * @see WebServer#waitForWebServerExit()
     */
    public static void waitForWebServerExit(long timeoutMillis) {
        Library.waitForWebServerExitWithTimeout(timeoutMillis);
    }

    /**
     * Block the current thread until web server has stopped or the time limit is exceeded.
     * @see WebServer#waitForWebServerExit()
     */
    public static void waitForWebServerExit(Duration timeout) {
        Library.waitForWebServerExitWithTimeout(timeout.toMillis());
    }

    volatile static boolean shouldWaitForWebServerExitOnFatalErrors = false;

    /**
     * In the event of a fatal error, block the current thread indefinitely until web server has stopped.
     * The web server is stopped when platform-specific "stop" signal is sent to the JVM.
     * In IntelliJ IDEA that can be done by pressing the red rectangle in the "Run" pane.
     * <p>
     * Warning: after fatal errors the internal state is undefined and likely to be inconsistent.
     * Dev Tools might not work properly or display data that doesn't make sense. Do not trust the output.
     */
    public static void waitForWebServerExitOnFatalErrors() {
        shouldWaitForWebServerExitOnFatalErrors = true;
    }

    private static void stopHook() {
        Library.stopWebServer();
    }
}
