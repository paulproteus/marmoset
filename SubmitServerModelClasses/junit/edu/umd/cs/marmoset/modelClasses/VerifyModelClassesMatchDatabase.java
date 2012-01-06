package edu.umd.cs.marmoset.modelClasses;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.umd.cs.marmoset.utilities.DatabaseUtilities;

public class VerifyModelClassesMatchDatabase  {


	public static File toFile(URL u) {
		try {
		 return  new File(u.toURI());
		} catch(URISyntaxException e) {
			return   new File(u.getPath());
		}
	}
	public static void getAllModelClasses() throws ClassNotFoundException {
		Class<Submission> c = Submission.class;
		URL u = c.getResource(c.getSimpleName()+".class");
		File dir = toFile(u).getParentFile();
		for(File f : dir.listFiles())
			if (f.canRead() && !f.isDirectory() && f.getName().endsWith(".class") && !f.getName().contains("$")) {
				String name = f.getName();
				name = name.substring(0, name.length()-6);
				Class<?> cc = c.getClassLoader().loadClass(c.getPackage().getName() + "." + name);
				checkClass(cc);
		}
	}

	static Map<String,Collection<String>> fromClassData = new TreeMap<String, Collection<String>>();
	static Map<String,Collection<String>> fromJDBC = new TreeMap<String, Collection<String>>();

	/**
	 * @param cc
	 */
	private static void checkClass(Class<?> cc) {
		Field check = null;
		for(Field f : cc.getDeclaredFields()) {
			if ((f.getModifiers() & Modifier.STATIC) != 0 && f.getType().equals(String[].class) && f.getName().charAt(0) == 'F') {
				check = f;
				break;
			}
		}
		try {
			Field attributesField = cc.getDeclaredField("ATTRIBUTE_NAME_LIST");
			Field tableNameField = cc.getDeclaredField("TABLE_NAME");

			String  tableName = (String) tableNameField.get(null);
			String[] attributes = (String[]) attributesField.get(null);
			Collection<String> existing = fromClassData.get(tableName);
			if (existing == null) {
				existing = new TreeSet<String>();
				fromClassData.put(tableName, existing);
			}
			existing.addAll(Arrays.asList(attributes));

			// System.out.printf("%20s %22s   %s%n", cc.getSimpleName(), tableName, Arrays.toString(attributes));
			return;
		} catch (SecurityException e) {
			assert true;
		} catch (NoSuchFieldException e) {
			assert true;
		} catch (IllegalArgumentException e) {
			assert true;
		} catch (IllegalAccessException e) {
			assert true;
		}
		if (check != null)
			System.out.println(cc.getName() + "." + check.getName());
	}

	public static void main(String args[] ) throws Exception {
		getAllModelClasses();
		getAllMetaData();
		System.out.println("Missing from jdbc data: " + difference(fromClassData.keySet(), fromJDBC.keySet()));
		System.out.println("Missing from model classes data: " + difference(fromJDBC.keySet(), fromClassData.keySet()));
		for(String tableName : union(fromClassData.keySet(), fromJDBC.keySet())) {
			Collection<String> c1 = fromClassData.get(tableName);
			Collection<String> c2 = fromJDBC.get(tableName);
			if (c1.equals(c2)) continue;
			System.out.println(tableName);
			System.out.println(" model only: " + difference(c1,c2));
			System.out.println(" jdbc only: " + difference(c2,c1));

		}
	}

	private static  <T extends Comparable<T>> Collection<T> difference(Collection<T> a, Collection<T> b) {
		Collection<T> result = new TreeSet<T>(a);
		result.removeAll(b);
		return result;
	}
	private static  <T extends Comparable<T>> Collection<T> union(Collection<T> a, Collection<T> b) {
		Collection<T> result = new TreeSet<T>(a);
		result.retainAll(b);
		return result;
	}
	private static void getAllMetaData() throws SQLException {
		java.sql.Connection conn = DatabaseUtilities.getConnection();
		DatabaseMetaData md = conn.getMetaData();
		System.out.println("Got metadata " );
		ResultSet rs = md.getTables(null, null, null, null);
		while (rs.next()) {
			String tableCatalog = rs.getString(1);
			String tableName = rs.getString(3);
			TreeSet<String> columnNames = new TreeSet<String>();
			System.out.printf("%s %s%n", tableCatalog, tableName);
			if (true) {
				try {
				ResultSet rs2 = md.getColumns("submitserver", null, tableName, null);
				while (rs2.next()) {
					String columnName = rs2.getString(4);
					// System.out.println(columnName);
					columnNames.add(columnName);
				}
				
				rs2.close();
				} catch (SQLException e) {
					System.out.println("Error getting columsn for " + tableName);
					e.printStackTrace(System.out);
				}
			}
			fromJDBC.put(tableName, columnNames);

		}
		conn.close();
	}
}
