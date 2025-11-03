package net.nicolamurtas.android.emulator.model;

/**
 * Represents the current status of an Android Virtual Device.
 *
 * @author Nicola Murtas
 * @version 3.0.0
 */
public enum EmulatorStatus {
    /**
     * Emulator is not running
     */
    STOPPED,

    /**
     * Emulator is in the process of starting up
     */
    STARTING,

    /**
     * Emulator is running and operational
     */
    RUNNING,

    /**
     * Emulator is in the process of shutting down
     */
    STOPPING,

    /**
     * Emulator encountered an error
     */
    ERROR
}
