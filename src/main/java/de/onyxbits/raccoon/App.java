package de.onyxbits.raccoon;

import java.io.File;

import javax.swing.SwingUtilities;
import org.apache.commons.cli.ParseException;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;
import de.onyxbits.raccoon.gui.MainActivity;
import de.onyxbits.raccoon.io.Archive;

/**
 * Just the application launcher.
 * 
 * @author patrick
 * 
 */
public class App {

	/**
	 * Version identifier.
	 */
	public static final String VERSIONSTRING = "3.0";

	/**
	 * Relative path for keeping extension jars in
	 */
	public static final String EXTDIR = "ext";

	/**
	 * Relative path for keeping archives in (the user is not required to put
	 * archives here, its just the suggested folder).
	 */
	public static final String ARCHIVEDIR = "archives";

	/**
	 * Relative path, root directory for the app.
	 */
	public static final String HOMEDIR = "Raccoon";

	/**
	 * Application Entry
	 * 
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		getDir(HOMEDIR).mkdirs();
		getDir(EXTDIR).mkdirs();
		getDir(ARCHIVEDIR).mkdirs();
		
		if (args == null || args.length == 0) {
			SwingUtilities.invokeLater(new MainActivity(null));
		}
		else {
			new CliService(args).run();
		}
	}

	/**
	 * Query the location of a directory.
	 * 
	 * @param which
	 *          EXTDIR, ARCHIVEDIR, or HOMEDIR.
	 * @return the file (may or may not exist).
	 */
	public static File getDir(String which) {
		File root = new File(System.getProperty("user.home"), HOMEDIR);
		if (which.equals(HOMEDIR)) {
			return root;
		}
		return new File(root, which);
	}

	/**
	 * Utility method for hooking up with Google Play.
	 * 
	 * @param archive
	 *          The archive from which to take configuration data.
	 * @return a ready to use connection
	 * @throws Exception
	 *           if something goes seriously wrong.
	 */
	public static synchronized GooglePlayAPI createConnection(Archive archive) throws Exception {
		String pwd = archive.getPassword();
		String uid = archive.getUserId();
		String aid = archive.getAndroidId();
		GooglePlayAPI ret = new GooglePlayAPI(uid, pwd, aid);

		if (archive.getProxyClient() != null) {
			ret.setClient(archive.getProxyClient());
		}
		// I am not quite sure if this method needs to be synchronized, but if so,
		// this is why:
		ret.setToken(archive.getAuthToken());
		if (ret.getToken() == null) {
			ret.login();
			// Caching the token considerably speeds up talking to the server, but
			// since multiple downloaders may be active at the same time and the
			// network may produce all kinds of timing effects, there is a good chance
			// that two threads would try to connect at the same time. Both see that
			// the token is not yet available and perform a login. Thread A logs in
			// first, but B's token returns faster. This results in B's token being
			// overwritten by A. This is a problem if the tokens are different and
			// only the latest one is valid. I'm not sure if this is the case, but
			// serializing connection requests prevents potential trouble.
			archive.setAuthToken(ret.getToken());
		}
		return ret;
	}

}
