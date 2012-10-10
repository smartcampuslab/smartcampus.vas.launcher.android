package eu.trentorise.smartcampus.common;

/**
 * Enum that manages status through int values.
 * 
 * @author Simone Casagranda
 * 
 */
public enum Status {

	OK(0), NOT_FOUND(1), NOT_VALID_UID(2), NOT_VALID_SIGNATURE(3), NOT_UPDATED(3);

	private int mStatus;

	private Status(int status) {
		mStatus = status;
	}

	public int value() {
		return mStatus;
	}
}
