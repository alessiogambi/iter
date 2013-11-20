package at.ac.tuwien.iter.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.dsg.cloud.data.StaticServiceDescription;
import at.ac.tuwien.dsg.cloud.data.VeeDescription;
import at.ac.tuwien.dsg.cloud.manifest.StaticServiceDescriptionFactory;

public class ServiceManifestUtils {

	public static List<String> getColumnsFromManifest(String manifestURL) {

		List<String> result = new ArrayList<String>();
		StaticServiceDescription serviceSpec = null;
		try {
			// Download and parse the manifest file (either xml or js)
			serviceSpec = StaticServiceDescriptionFactory.fromURL(manifestURL);
			// Get the name of only the variable components
			for (VeeDescription vee : serviceSpec.getOrderedVees()) {
				if (vee.getMinInstances() != vee.getMaxInstances()) {
					result.add(vee.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return result;

	}

	public static List<Integer> getVariabilitySpaceFromManifest(URL manifestURL) {

		List<Integer> result = new ArrayList<Integer>();
		StaticServiceDescription serviceSpec = null;
		try {
			// Download and parse the manifest file (either xml or js)
			serviceSpec = StaticServiceDescriptionFactory.fromURL(manifestURL
					.toString());
			// Get the name of only the variable components
			for (VeeDescription vee : serviceSpec.getOrderedVees()) {
				if (vee.getMinInstances() != vee.getMaxInstances()) {
					// TODO Not sure about this... we start from 1 to 10, 10 - 1
					// =9 but the space must be 10
					result.add(vee.getMaxInstances() - vee.getMinInstances()
							+ 1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		return result;

	}
}
