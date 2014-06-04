package de.onyxbits.raccoon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * An archive is a container for managing downloaded apk files and credentials.
 * The idea here is that the user should be able to keep around different apk
 * versions of an app and should also be able to maintain different download
 * folders (e.g. one for anonymously downloading apps and one for downloading
 * bought apps or one downloadfolder per device/account). The later means that
 * credentials have to be kept within the archive.
 * 
 * @author patrick
 * 
 */
public class Archive {

	/**
	 * Name of the file containing the credentials for connecting to GPlay.
	 */
	public static final String CREDS = "credentials.cfg";

	public static final String PASSWORD = "password";
	public static final String USERID = "userid";
	public static final String ANDROIDID = "androidid";

	private File root;

	private Properties credentials;

	// TODO: Figure out if this can produce a race condition. It is possible that
	// two workers run at the same time, find this to be null and go through a
	// full login. As long as both login attempts produce the same token or both
	// tokens are valid, there is no problem here.
	/**
	 * Cache of the auth token. This is not persisted. This may be null.
	 */
	private String authToken;

	/**
	 * @return the authToken
	 */
	public String getAuthToken() {
		return authToken;
	}

	/**
	 * @param authToken
	 *          the authToken to set
	 */
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	/**
	 * @return the androidId
	 */
	public String getAndroidId() {
		return credentials.getProperty(ANDROIDID, "");
	}

	/**
	 * @param androidId
	 *          the androidId to set
	 */
	public void setAndroidId(String androidId) {
		credentials.setProperty(ANDROIDID, androidId);
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return credentials.getProperty(PASSWORD, "");
	}

	/**
	 * @param password
	 *          the password to set
	 */
	public void setPassword(String password) {
		credentials.setProperty(PASSWORD, password);
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return credentials.getProperty(USERID, "");
	}

	/**
	 * @param userId
	 *          the userId to set
	 */
	public void setUserId(String userId) {
		credentials.setProperty(USERID, userId);
	}

	/**
	 * Create a new archive in the designated place
	 * 
	 * @param root
	 *          directory in which to generate the archive
	 */
	public Archive(File root) {
		this.root = root;
		try {
			credentials = new Properties();
			credentials.load(new FileInputStream(new File(root, CREDS)));
		}
		catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * Query the location of the archive
	 * 
	 * @return the directory in which the archive is kept.
	 */
	public File getRoot() {
		return root;
	}

	/**
	 * Persist credentials to the CREDS file.
	 * 
	 * @throws IOException
	 *           if saving goes wrong.
	 */
	public void saveCredentials() throws IOException {
		root.mkdirs();
		credentials.store(new FileOutputStream(new File(root, CREDS)), "");
	}

	/**
	 * Figure out where to save an app
	 * 
	 * @param packName
	 *          packagename of the app
	 * @param vc
	 *          versioncode of the app
	 * @return the file where to save this app.
	 */
	public File fileUnder(String packName, int vc) {
		File appRoot = new File(root, packName.replace('.', '_'));
		return new File(appRoot, packName.replace('.', '_') + "-" + vc + ".apk");
	}

	/**
	 * Figure out where to save meta information for an app
	 * 
	 * @param packName
	 *          packagename of the app
	 * @param vc
	 *          versioncode
	 * @return the properties file for saving meta information
	 */
	public File fileMeta(String packName, int vc) {
		File appRoot = new File(root, packName.replace('.', '_'));
		return new File(appRoot, packName + "-" + vc + ".txt");
	}

}