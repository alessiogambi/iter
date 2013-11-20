package at.ac.tuwien.iter.data;

import java.util.concurrent.atomic.AtomicLong;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * This class contains the definition of a test case
 * 
 * @author alessiogambi, antoniofilieri
 * 
 */
public class Test implements Comparable<Test> {

	private static final AtomicLong nextId = new AtomicLong(0l);

	private Long id;
	private final String testFile;
	private final String manifestFile;
	private final String traceFile;
	private final String loadGeneratorID;
	private final Number[] parameters;
	private Integer hashCode = null;

	private Test(long id, String testFile, String manifestFile,
			String traceFile, String loadGeneratorID, Number... parameters) {
		super();
		this.id = id;
		this.testFile = testFile;
		this.manifestFile = manifestFile;
		this.traceFile = traceFile;
		this.loadGeneratorID = loadGeneratorID;
		this.parameters = parameters.clone();
	}

	private void setId(Long id) {
		this.id = id;
	}

	/**
	 * Factory method to create a "unique" test that contains service manifest,
	 * workload definition (trace file), and references to the load generator
	 * that creates it withing its parameters
	 * 
	 * @param testFile
	 * @param manifestFile
	 * @param traceFile
	 * @param loadGeneratorID
	 * @param parameters
	 * @return
	 */
	public synchronized static Test newInstance(String testFile,
			String manifestFile, String traceFile, String loadGeneratorID,
			Number... parameters) {

		long instanceId = nextId.incrementAndGet();
		Test test = new Test(instanceId, testFile, manifestFile, traceFile,
				loadGeneratorID, parameters);

		Long uniqId = test.getId() + test.hashCode() * 79;
		test.setId(uniqId);

		return test;
	}

	public final long getId() {
		return id;
	}

	public final String getClientsURL() {
		return testFile;
	}

	public final String getManifestURL() {
		return manifestFile;
	}

	public final String getTraceURL() {
		return traceFile;
	}

	public final Number[] getParameters() {
		return parameters;
	}

	public final String getLoadGeneratorID() {
		return loadGeneratorID;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Test) {
			return (((Test) obj).hashCode() == hashCode());
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (this.hashCode == null) {
			HashFunction hashFunction = Hashing.sha512();
			Hasher hasher = hashFunction.newHasher();
			// TODO Relaxed the id constraint and use only the others
			hasher = hasher.putString(testFile).putString(manifestFile)
					.putString(traceFile);
			for (Number number : parameters) {
				hasher = hasher.putString(number.toString());
			}
			this.hashCode = hasher.hash().asInt();
		}
		return this.hashCode;
	}

	public int compareTo(Test arg0) {
		if (this.equals(testFile)) {
			return 0;
		} else {
			if (this.id < arg0.getId()) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
