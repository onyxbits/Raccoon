package de.onyxbits.raccoon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;

import com.akdeniz.googleplaycrawler.Utils;

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
	 * Relative path to where we keep APK files. This directory contains one
	 * subdirectory per app, which in turn holds the apks.
	 */
	public static final String APKSTORAGE = "apk_storage";

	/**
	 * Name of the file containing the credentials for connecting to GPlay.
	 */
	public static final String CREDCFG = "credentials.cfg";

	/**
	 * Name of the file where downloads should be logged.
	 */
	private static final String DOWNLOADLOG = "downloadlog.txt";

	/**
	 * Name of the file containing the network config
	 */
	public static final String NETCFG = "network.cfg";

	public static final String PASSWORD = "password";
	public static final String USERID = "userid";
	public static final String ANDROIDID = "androidid";
	public static final String PROXYHOST = "proxyhost";
	public static final String PROXYPORT = "proxyport";

	private File root;
	private DownloadLogger downloadLogger;

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
			credentials.load(new FileInputStream(new File(root, CREDCFG)));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		downloadLogger = new DownloadLogger(new File(root,DOWNLOADLOG));
	}

	/**
	 * Get a proxy client, if it is configured.
	 * 
	 * @return either a client or null
	 * @throws IOException
	 *           if reading the config file fails
	 * @throws KeyManagementException
	 * @throws NumberFormatException
	 *           if that port could not be parsed.
	 * @throws NoSuchAlgorithmException
	 */
	public HttpClient getProxyClient() throws IOException, KeyManagementException,
			NoSuchAlgorithmException, NumberFormatException {
		File cfgfile = new File(root, NETCFG);
		if (cfgfile.exists()) {
			Properties cfg = new Properties();
			cfg.load(new FileInputStream(cfgfile));
			String ph = cfg.getProperty(PROXYHOST, null);
			String pp = cfg.getProperty(PROXYPORT, null);
			PoolingClientConnectionManager connManager = new PoolingClientConnectionManager(
					SchemeRegistryFactory.createDefault());
			connManager.setMaxTotal(100);
			connManager.setDefaultMaxPerRoute(30);

			HttpClient client = new DefaultHttpClient(connManager);
			client.getConnectionManager().getSchemeRegistry().register(Utils.getMockedScheme());
			HttpHost proxy = new HttpHost(ph, Integer.parseInt(pp));
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			return client;
		}
		return null;
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
		credentials.store(new FileOutputStream(new File(root, CREDCFG)), "");
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
		File appRoot = new File(new File(root, APKSTORAGE), packName.replace('.', '-'));
		return new File(appRoot, packName.replace('.', '_') + "-" + vc + ".apk");
	}

	/**
	 * List all apps in the archive
	 * 
	 * @return a list of packagenames.
	 */
	public List<String> list() {
		File storage = new File(root, APKSTORAGE);
		storage.mkdirs();
		File[] lst = storage.listFiles();
		Vector<String> tmp = new Vector<String>();
		for (File f : lst) {
			if (f.isDirectory()) {
				tmp.add(f.getName().replace('-', '.'));
			}
		}
		return tmp;
	}

	/**
	 * Get the download logger.
	 * 
	 * @return the downloadlogger.
	 */
	public DownloadLogger getDownloadLogger() {
		return downloadLogger;
	}
}