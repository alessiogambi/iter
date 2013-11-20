package at.ac.tuwien.iter.executors;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.LoggerFactory;

public class DBCreateAndDestroy {

	public static void main(String[] args) {

		String dbname = "testdb";

		// Is this needed ... I must check on the doc :) ?!
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:"
					+ dbname, "sa", "");

			String command = "CREATE TABLE theTable (ID int);";

			// Executing command
			Statement statement = conn.createStatement();
			statement.execute(command);

			BasicRunner r = new BasicRunner(
					LoggerFactory.getLogger(DBCreateAndDestroy.class), null,
					null, null, 0);

			// r.dropTempDB(dbname);

			DatabaseMetaData dbm = conn.getMetaData();
			ResultSet rs = dbm.getTables(null, null, null, null);

			while (rs.next()) {
				System.out.println("DBCreateAndDestroy.main(): "
						+ rs.getString(3));
			}

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
