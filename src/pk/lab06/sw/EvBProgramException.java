package pk.lab06.sw;

/**
 * Signals that an {@link EvBProgram} exception has occurred.
 * @see EvBProgram
 */
public class EvBProgramException extends EvBException {
    public EvBProgramException() {
    }

    public EvBProgramException(String message) {
        super(message);
    }

    public EvBProgramException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvBProgramException(Throwable cause) {
        super(cause);
    }

    protected EvBProgramException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
