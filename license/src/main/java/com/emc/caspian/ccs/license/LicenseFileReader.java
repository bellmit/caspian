package com.emc.caspian.ccs.license;

import java.io.File;
import java.util.Set;

/**
 * Interface that contains methods to parse a license file and return a set
 * of java License objects.
 */
public interface LicenseFileReader {

   /**
    * Reads a license file and returns a set of Java License objects.
    * @param file license file containing licensing information.
    * @return Set of License objects containing data from file.
    * @throws Exception 
 
    */
   LicenseFileResults readFile(final File file) throws Exception;

   /**
    * Class that contains information about license file.
    */
   public class LicenseFileResults {

      private boolean containsExpired;
      private String expiredNames;
      private Set<License> licenses;
      private boolean isEmpty;
      private boolean allCorrupted;
      private int numberCorrupted;
      private String featuresCorrupted;

      public int getNumberCorrupted() {
		return numberCorrupted;
	}

	public void setNumberCorrupted(int numberCorrupted) {
		this.numberCorrupted = numberCorrupted;
	}

	public String getFeaturesCorrupted() {
		return featuresCorrupted;
	}

	public void setFeaturesCorrupted(String featuresCorrupted) {
		this.featuresCorrupted = featuresCorrupted;
	}

	/**
	 * @return the isEmpty
	 */
	public boolean isEmpty() {
		return isEmpty;
	}

	/**
	 * @param isEmpty the isEmpty to set
	 */
	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	/**
	 * @return the allCorrupted
	 */
	public boolean isAllCorrupted() {
		return allCorrupted;
	}

	/**
	 * @param allCorrupted the allCorrupted to set
	 */
	public void setAllCorrupted(boolean allCorrupted) {
		this.allCorrupted = allCorrupted;
	}

	/**
       * Returns true if file contains expired licenses.
       * @return boolean true if file contains expired licenses.
       */
      public boolean getContainsExpired() {
         return containsExpired;
      }

      /**
       * Sets if file contains expired licenses.
       * @param c true if file contains expired licenses.
       */
      public void setContainsExpired(final boolean c) {
         containsExpired = c;
      }

      /**
       * Returns names of expired licenses.
       * @return names of expired licenses.
       */
      public String getExpiredNames() {
         return expiredNames;
      }

      /**
       * Sets names of expired licenses.
       * @param s names of expired licenses.
       */
      public void setExpiredNames(final String s) {
         expiredNames = s;
      }

      /**
       * Gets licenses read from file.
       * @return set of licenses read from file.
       */
      public Set<License> getLicenses() {
         return licenses;
      }

      /**
       * Sets the licenses read from file.
       * @param l Set of licenses read from file.
       */
      public void setLicenses(final Set<License> l) {
         licenses = l;
      }
   }
}