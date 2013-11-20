package at.ac.tuwien.iter.utils;

public class TableNameParserTest {

	public static void main(String[] args) {
		String candidateTable = "CONTROLLER_ACTIVATION_DSG_CUSTOMERS_ALE_SERVICES_S128";

		String pattern = "(.+)_S(\\d+)";
		String updated = candidateTable.replaceAll(pattern, "$2");

		System.out.println("TableNameParserTest.main() result " + updated);
	}
}
