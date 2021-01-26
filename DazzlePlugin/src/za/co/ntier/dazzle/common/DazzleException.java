package za.co.ntier.dazzle.common;

/**
 * @author nTier Software Services Neil Gordon (NCG)
 * Date 2015/08/19
 */
public class DazzleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DazzleException() {
		super();
	}

	public DazzleException(String message, Throwable cause) {
		super(message, cause);
	}

	public DazzleException(String message) {
		super(message);
	}

	public DazzleException(Throwable cause) {
		super(cause);
	}

}
