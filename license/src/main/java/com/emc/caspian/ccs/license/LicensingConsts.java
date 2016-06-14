package com.emc.caspian.ccs.license;

/**
 * Class containing constants for licensing.
 */
public final class LicensingConsts {

	private LicensingConsts() {
	}

	/** Permanent License expiration date. */
	public static final Long PERMANENT_LICENSE = 316705642862663L;

	/** Date formats. */
	public static final String LICENSE_FILE_DATE_FORMAT = "dd-MMM-yyyy";

	public static final String SMIS_DATE_FORMAT = "yyyyMMddHHmmss.SSSSSS";

	/** Measurement conversions. */
	public static final float TERABYTES_TO_BYTES = 1099511627776F;

	public static final float GIGABYTES_TO_BYTES = 1073741824F;

	public static final float PETABYTES_TO_BYTES = 1125899906842624F;

}
