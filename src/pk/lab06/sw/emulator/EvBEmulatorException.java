package pk.lab06.sw.emulator;
/**
 * Signals that an {@link EvBEmulator} exception has occurred.
 * @see EvBEmulator
 */
public class EvBEmulatorException extends EvBException {
    public EvBEmulatorException() {
    }

    public EvBEmulatorException(String message) {
        super(message);
    }

    public EvBEmulatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvBEmulatorException(Throwable cause) {
        super(cause);
    }

    protected EvBEmulatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
