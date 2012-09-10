package eu.trentorise.smartcampus.common;

/**
 * Exception that should be risen when an error occurs
 * 
 * @author Simone Casagranda
 *
 */
public final class LauncherException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private Status mStatus;

	public LauncherException(Status status) {
		mStatus = status;
	}
	
	/**
	 * Retrieves the error cause that has generated exception.
	 */
	public Status getStatus() {
		return mStatus;
	}

}
