package at.ac.tuwien.iter.utils;

import java.util.List;

public class ColumnExtractorTest {

	public static void main(String[] args) {
		String manifestURL = "http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml";

		List<String> columns = ServiceManifestUtils
				.getColumnsFromManifest(manifestURL);

		System.out.println("ColumnExtractorTest.main() Columns = " + columns);
	}
}
