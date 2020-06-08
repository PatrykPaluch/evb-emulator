package pk.lab06.sw.emulator;

/**
 * Signals that an EvB exception has occurred.
 * @see EvBProgramException
 * @see EvBEmulatorException
 */
public class EvBException extends RuntimeException  {
    public EvBException() {
    }

    public EvBException(String message) {
        super(message);
    }

    public EvBException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvBException(Throwable cause) {
        super(cause);
    }

    protected EvBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
