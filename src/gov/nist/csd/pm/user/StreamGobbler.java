package gov.nist.csd.pm.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StreamGobbler extends Thread {
	/**
	 * @uml.property  name="is"
	 */
	InputStream is;
	/**
	 * @uml.property  name="sPrefix"
	 */
	String sPrefix;

	StreamGobbler(InputStream is, String sPrefix) {
		this.sPrefix = sPrefix;
		this.is = is;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(sPrefix + line);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.out.println(sPrefix + ioe.getMessage());
		}
	}
}