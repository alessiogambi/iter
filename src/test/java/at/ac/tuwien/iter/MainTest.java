package at.ac.tuwien.iter;

import java.io.File;

public class MainTest {

	public static void main(String[] args) {

		System.getProperties().put("log4j.configuration",
				new File("conf/log4j.properties").toURI());

		System.getProperties().put("at.ac.tuwien.dsg.cloud.configuration",
				"conf/cloud.properties");

		String manifestURL = "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml";
		String jmxURL = "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx";
		String[] _args = { "-b", "-c", "ite", "-s", "ite", "-m", manifestURL, "-j",
				jmxURL };
		try {
			Main.main(_args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
