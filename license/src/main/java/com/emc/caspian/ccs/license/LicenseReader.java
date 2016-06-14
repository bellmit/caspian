package com.emc.caspian.ccs.license;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import com.emc.cams.elm.ELMFeatureDetail;
import com.emc.cams.elm.ELMLicenseProps;
import com.emc.cams.elm.ELMLicenseSource;
import com.emc.cams.elm.exception.ELMFilterException;
import com.emc.cams.elm.exception.ELMLicenseException;
import com.emc.caspian.ccs.license.util.AppLogger;
import com.emc.caspian.ccs.license.util.ConvertDate;
import com.emc.caspian.ccs.license.util.HashGenerator;

/**
 * Class that has capability to read a license file and create a set of License
 * Objects.
 */
public class LicenseReader implements LicenseFileReader {

	/**
	 * Reads in a license file and saves information as a set of License
	 * objects.
	 * 
	 * @param file
	 *            license file containing licensing information.
	 * @return Set of Licenses containing license file information.
	 * @throws Exception
	 * @throws ELMLicenseException
	 * @throws ELMFilterException
	 */
	public LicenseFileResults readFile(final File file) throws Exception {

		if (file == null) {
			AppLogger.error("File is null.");
			throw new Exception("Cannot read licenses from empty file");
		}

		final Set<License> licenses = new HashSet<License>();
		final ELMLicenseProps licProps = new ELMLicenseProps();
		AppLogger.info("Created new license properties");

		licProps.setLicPath(file.getAbsolutePath());
		AppLogger.debug("Set license path to " + file.getAbsolutePath());

		final LicenseFileResults results = new LicenseFileResults();
		try {
			final ELMLicenseSource source = new ELMLicenseSource(licProps);

			AppLogger.info("Created ELM License source from %s",licProps.getLicPath().toString());

			ArrayList<String> featureListFromELM = new ArrayList<String>();

			final ELMFeatureDetail[] details = source.getFeatureDetailList();
			for(int i=0;i<details.length;i++){
				featureListFromELM.add(details[i].getFeatureName());
			}

			AppLogger.info("Retrieved %s licenses from file", details.length);
			boolean allValid = true;
			String messageString = null;
			final List<String> messages = new ArrayList<String>();
			for (ELMFeatureDetail detail : details) {
				if (!detail.isValid()) {
					allValid = false;
					messages.add(detail.getFeatureName());
				} else {
					License license = null ;
					try{
						license = createLicense(detail);
						AppLogger.debug("Created new license object with name %s",
								license.getName());
						licenses.add(license);
					}catch (DateParseException e){
						AppLogger.error("Exception", e);
					} 
				}
			}
			if (!allValid) {
				messageString = LicenseReader
						.createSeparatedList(messages, ',');
			}

			results.setContainsExpired(!allValid);
			results.setExpiredNames(messageString);
			results.setLicenses(licenses);

			FileReader reader = null;

			reader = new FileReader(file);

			BufferedReader bufferedReader = new BufferedReader(reader);
			String fullFile = "";
			String s;
			while ((s = bufferedReader.readLine()) != null) {
				fullFile = fullFile + s;
			}
			bufferedReader.close();

			String sepLicenses[] = fullFile.split("INCREMENT");

			int noLicenses = (sepLicenses.length) - 1;
			String readfeatureList[] = new String[noLicenses];

			for (int i = 1; i <= noLicenses; i++) {
				sepLicenses[i] = sepLicenses[i].trim();
				String[] sepName = sepLicenses[i].split(" ", 2);
				readfeatureList[i - 1] = sepName[0];
				readfeatureList[i - 1] = readfeatureList[i - 1].trim();
			}

			ArrayList<String> featureListFromRaw = new ArrayList<String>(Arrays.asList(readfeatureList));

			for (String temp : featureListFromELM) {
				featureListFromRaw.remove(temp);
			}

			results.setNumberCorrupted(featureListFromRaw.size());
			results.setFeaturesCorrupted(featureListFromRaw.toString());

			return results;

		} catch (ELMLicenseException e) {
			AppLogger.error("Error reading license file.", e);
			String errorMessage = e.toString();
			if (errorMessage
					.equals("ELM License Exception: Retrieving list of licenses failed.\n\nError List Returned (-513,4050) ; No license files were found. (0,0)")) {
				results.setEmpty(true);
				return results;
			} else if (errorMessage.contains("Error List Returned (-513,4050")
					&& errorMessage.contains("Authentication Failed (-8,4048")
					&& errorMessage
					.contains("No license files were found. (0,0)")) {
				results.setAllCorrupted(true);
				return results;
			} else if(errorMessage.contains("No license files were found.")){
				results.setAllCorrupted(true);
				return results;
			}

			throw new ELMException("Error while parsing ELM License file");
		} catch (ELMFilterException e) {
			AppLogger.error("Error filtering license file.", e);
			throw new ELMException("Error filtering ELM License file");
		} catch (Exception e){
			AppLogger.error("Error while reading or parsing ELM License file",e);
			throw e;
		}
	}

	/**
	 * Creates a License object from an ELMFeatureDetail.
	 * 
	 * @param feature
	 *            ELMFeatureDetail containing license information.
	 * @return License containing information from ELMFeatureDetail.
	 * @throws Exception
	 */
	private License createLicense(final ELMFeatureDetail detail)throws Exception, DateParseException {
		final License license = new License();
		boolean isTypePresent =false;
		try {
			if (detail == null) {
				throw new Exception("Details of license could not be obtained");
			}

			license.setName(detail.getFeatureName());
			AppLogger.debug("Set name of license to " + license.getName());

			final Long exp = detail.getExpDate().getTimeInMillis();
			if (exp != LicensingConsts.PERMANENT_LICENSE) {
				license.setExpirationDate(exp);
				AppLogger.debug("Set expiration date of license to " + exp);
			}

			String toHash = detail.getAllAttributes().toString();
			license.setId(HashGenerator.gen(toHash));
			AppLogger.debug("Set id of license.");

			license.setTimeAdded(System.currentTimeMillis());
			AppLogger.debug("Set time added of license.");

			final String vendorString = detail.getAttribute("VENDOR_STRING");
			if (vendorString == null) {
				throw new Exception("License " + detail.getFeatureName()
						+ " does not contain necessary vendor information");
			}
			AppLogger.debug("Got vendor string %s from feature", vendorString);

			final StringTokenizer vendorTokenizer = new StringTokenizer(
					vendorString, ";");
			while (vendorTokenizer.hasMoreElements()) {
				final String property = vendorTokenizer.nextToken();
				if (property.contains("=")) {
					final StringTokenizer propertyTokenizer = new StringTokenizer(
							property, "=");
					String propertyName = propertyTokenizer.nextToken();
					String propertyValue = propertyTokenizer.nextToken();

					//TODO: Explore if we need multiple language support for locale
					String uppercasePropertyName=propertyName.toUpperCase(Locale.ENGLISH);
					switch(uppercasePropertyName){
					case LicenseStrings.TYPE:
						if(propertyValue.equalsIgnoreCase(LicenseStrings.PERMANENT)||
								propertyValue.equalsIgnoreCase(LicenseStrings.SUBSCRIPTION)||
								propertyValue.equalsIgnoreCase(LicenseStrings.EVAL)){
							isTypePresent=true;
						}
							
						if(propertyValue.equals(LicenseStrings.EVAL)){
							long endDateMillis = detail.getExpDate().getTimeInMillis();
							String endDate = Long.toString(endDateMillis);
							license.addProperty(LicenseStrings.END_DATE,endDate);
							AppLogger.debug("Added property %s with value %s",
									propertyName, propertyValue);
						}
						break;
					case LicenseStrings.START_DATE:
						long tempValue=ConvertDate.convertrawToMillis(propertyValue);
						propertyValue = tempValue +"";
						break;

					case LicenseStrings.END_DATE:
						long tempValue1=ConvertDate.convertrawToMillis(propertyValue);
						propertyValue = tempValue1 +"";
						break;

					case LicenseStrings.CAPACITY:
						license.addProperty(LicenseStrings.LIMIT, propertyValue);
						AppLogger.debug("Added property %s with value %s",
								propertyName, propertyValue);
						propertyValue=LicenseStrings.STORAGE;
						propertyName=LicenseStrings.ENTITLEMENT;
						break;

					case LicenseStrings.STORAGE:
						license.addProperty(LicenseStrings.LIMIT, propertyValue);
						AppLogger.debug("Added property %s with value %s",
								propertyName, propertyValue);
						propertyValue=LicenseStrings.STORAGE;
						propertyName=LicenseStrings.ENTITLEMENT;
						break;

					case LicenseStrings.CAPACITY_UNIT:
						propertyName=LicenseStrings.UNIT;
						break;

					case LicenseStrings.STORAGE_UNIT:
						propertyName=LicenseStrings.UNIT;
						break;

					case LicenseStrings.CORES:
						license.addProperty(LicenseStrings.ENTITLEMENT, propertyName);
						AppLogger.debug("Added property %s with value %s",
								propertyName, propertyValue);
						license.addProperty(LicenseStrings.UNIT, LicenseStrings.COUNT);
						AppLogger.debug("Added property %s with value %s",
								propertyName, propertyValue);
						propertyName=LicenseStrings.LIMIT;
						break;	
					}	

					license.addProperty(propertyName, propertyValue);
					AppLogger.debug("Retrieved property %s with value %s",
							propertyName, propertyValue);
				}
			}
		}catch (DateParseException e){
			AppLogger.error("Exception", e);
			throw new DateParseException("Error parsing license date");
		} 
		catch (Exception e) {
			AppLogger.error("Exception", e);
			throw new Exception("Error filtering ELM License file");
		}
		if(isTypePresent)
			return license;
		else{
			AppLogger.error("Exception", "TYPE is absent in the license: %s",license.getName());
			throw new ELMException("Invalid license file, TYPE is absent in the license file");
		}
	}

	private static String createSeparatedList(final List<String> strings,
			final Character separator) {
		if (separator == null || strings == null) {
			throw new IllegalArgumentException(
					"Cannot create string with empty separator or contents");
		}
		final StringBuilder builder = new StringBuilder();
		for (final String s : strings) {
			builder.append(s).append(separator);
		}
		final String builderString = builder.toString();
		if (builderString.isEmpty()) {
			return builderString;
		}
		return builderString.substring(0, builderString.length() - 1);
	}
}
