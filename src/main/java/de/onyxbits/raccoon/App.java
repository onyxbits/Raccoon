package de.onyxbits.raccoon;

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
	 * Application Entry
	 * 
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		if (args == null || args.length == 0) {
			SwingUtilities.invokeLater(new MainActivity(null));
		}
		else {
			new CliService(args).run();
		}
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
