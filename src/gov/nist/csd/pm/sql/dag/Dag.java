/* This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS".  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof including, but
 * not limited to, the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement.
 */
package gov.nist.csd.pm.sql.dag;

//import gov.nist.csd.pm.sql.util.ConfigurationManager;
//import gov.nist.csd.pm.sql.util.Log;

import gov.nist.csd.pm.common.config.ServerConfig;
import gov.nist.csd.pm.sql.PmDatabase;
import gov.nist.csd.pm.sql.dag.DagNodeType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

/**
<pre>
CREATE TABLE dag (
nodepath VARCHAR(100),
PRIMARY KEY (nodepath),
relation VARCHAR(80));
+----------+---------------+------+-----+---------+-------+
| Field    | Type          | Null | Key | Default | Extra |
+----------+---------------+------+-----+---------+-------+
| nodepath | varchar(100)  | NO   | PRI |         |       |
| relation | char(1)       | YES  |     | NULL    |       |
+----------+---------------+------+-----+---------+-------+
The relation column stores the following relationships:

ROOT: Root node
SELF: Self
PARENTCHILD: Comma-delimitted parent-child relation
</pre>
$$Id: Dag.java 35987 2013-01-10 18:42:39Z steveq $$
 */
public class Dag {

	/** The table name of the DAG. */
	private final static String DB_TABLE_NAME = "dag";
	/** A list of relatives.*/
	private static ArrayList<String> relatives = null;
	/** Stores paths always in descendant order from left to right. */
	private static ArrayList<String> paths = null;

	static {
		relatives = new ArrayList<String>();
		paths = new ArrayList<String>();
	}
	
	/**
	 * Create a DAG table.
	 * @return true if table was created, false otherwise.
	 */
	public static boolean createTable() {
		String sqlCommand = "CREATE TABLE dag (" + "nodepath VARCHAR(100), "
				+ "PRIMARY KEY (nodepath), " + "relation VARCHAR(80))";
		if (update(sqlCommand)) {
			System.out.println("Created table " + DB_TABLE_NAME);
			return true;
		} else {
			System.err.println("Could not execute: " + sqlCommand);
			return false;
		}
	}

	/**
	 * Clear a DAG table.
	 * @return true if table was cleared, false otherwise.
	 */
	public static boolean clearTable() {
		String sqlCommand = "DELETE FROM " + DB_TABLE_NAME;
		if (update(sqlCommand)) {
			System.out.println("Cleared table " + DB_TABLE_NAME);
			return true;
		} else {
			System.err.println("Could not execute: " + sqlCommand);
			return false;
		}
	}

	/** Destroy a DAG table.
	 * @return true if table was destroyed, false otherwise.
	 */
	public static boolean destroyTable() {
		String sqlCommand = "DROP TABLE " + DB_TABLE_NAME;
		if (update(sqlCommand)) {
			System.out.println("Destroyed table " + DB_TABLE_NAME);
			return true;
		} else {
			System.err.println("Could not execute: " + sqlCommand);
			return false;
		}
	}

	/**
	 * Add a node to the DAG.
	 * @param nodeId The ID of the node.
	 * @param dagType The DAG type of the node.
	 * @return true if added, false otherwise.
	 */
	public static boolean addNode(String nodeId, DagNodeType dagType) {
		return insertPath(nodeId, dagType);
	}

	/**
	 * Insert a path into the DAG.  Note that paths can either contain a single
	 * node, or a comma-delimited parent-child path (e.g., "parent,child").
	 * @param path The path to be inserted.
	 * @param relation The relation of this path.
	 * @return
	 */
	private static boolean insertPath(String path, DagNodeType relation) {
		String sqlCommand = "INSERT INTO dag (nodepath, relation) values (?, ?)";
		try {
			PreparedStatement statement;
			statement = ServerConfig.pmDB.getConnection().prepareStatement(sqlCommand);
			statement.setString(1, path);
			statement.setString(2, relation.name());
			statement.executeUpdate();
			// Log.debug("Added " + path + " to dag.");
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return false;
		}
	}

	/** 
	 * Link a child node to a parent node.
	 * @param childId The ID of the child node.
	 * @param parentId The ID of the parent node.
	 * @return true if linked, false otherwise.
	 */
	public static boolean linkChild(String childId, String parentId) {
		if (!nodeExists(parentId)) {
			//System.out.println("Parent node " + parentId + " does not exist");
			return false;
		} else if (!nodeExists(childId)) {
			//System.out.println("Child node " + childId + " does not exist");
			return false;
		}
		// Check if link will result in a graph cycle.
		if (isAscendant(childId, parentId)) {
			//System.out.println("Linking parent '" + parentId + "' with" + " child '"
			//		+ childId + "' results in cycle");
			return false;
		} else {
			String parentChildPath = parentId + "," + childId;
			if (insertPath(parentChildPath, DagNodeType.PARENTCHILD)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Get the children of a node.
	 * @param parentId The ID of the node.
	 * @return A list of children IDs.
	 */
	public static ArrayList<String> getChildren(String parentId) {
		ArrayList<String> children = new ArrayList<String>();
		String sqlCommand = "SELECT nodepath FROM dag " + "where relation='"
				+ DagNodeType.PARENTCHILD + "' AND nodepath LIKE '" + parentId
				+ ",%'";
		ResultSet childrenPaths = execute(sqlCommand);
		try {
			while (childrenPaths.next()) {
				String childPath = childrenPaths.getString(1);
				String childNode = getChildNode(childPath);
				children.add(childNode);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return children;
	}

	/**
	 * Get the children of a node.
	 * @param childId The ID of the node.
	 * @return A list of parents IDs.
	 */
	public static ArrayList<String> getParents(String childId) {
		ArrayList<String> parents = new ArrayList<String>();
		String sqlCommand = "SELECT nodepath FROM dag " + "where relation='"
				+ DagNodeType.PARENTCHILD + "' AND nodepath LIKE '%," + childId
				+ "'";
		ResultSet parentPaths = execute(sqlCommand);
		try {
			while (parentPaths.next()) {
				String parentPath = parentPaths.getString(1);
				String parentNode = getParentNode(parentPath);
				parents.add(parentNode);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return parents;
	}

	/**
	 * Parse paths into their node IDs.
	 * @param paths A list of paths
	 * @return A list of node IDs.
	 */
	private static ArrayList<String> parsePaths(ArrayList<String> paths) {
		ArrayList<String> nodes = new ArrayList<String>();
		for (int i = 0; i < paths.size(); i++) {
			String path = paths.get(i);
			String[] items = path.split(",");
			for (int j = 0; j < items.length; j++) {
				if (!nodes.contains(items[j]))
					nodes.add(items[j]);
			}
		}
		return nodes;
	}

	/**
	 * Get all ascendants of a node.
	 * @param nodeId The ID of the node.
	 * @return A list of node IDs.
	 */
	public static ArrayList<String> getAscendants(String nodeId) {
		relatives.clear();
		paths.clear();
		traverseAscendants(nodeId);
		ArrayList<String> nodes = parsePaths(paths);
		nodes.remove(nodeId);
		return nodes;
	}

	/**
	 * Get all descendants of a node.
	 * @param nodeId The ID of the node.
	 * @return A list of node IDs.
	 */
	public static ArrayList<String> getDescendants(String nodeId) {
		relatives.clear();
		paths.clear();
		traverseDescendants(nodeId);
		ArrayList<String> nodes = parsePaths(paths);
		nodes.remove(nodeId);
		return nodes;
	}

	/**
	 * Traverse ascendants of a given node.
	 * @param childId The given node.
	 */
	public static void traverseAscendants(String childId) {
		String getParents = "SELECT nodepath FROM dag " + "where relation='"
				+ DagNodeType.PARENTCHILD + "' AND nodepath LIKE '%," + childId
				+ "'";
		ResultSet parentPaths = execute(getParents);
		try {
			boolean noParents = true;
			while (parentPaths.next()) {
				String parentPath = parentPaths.getString(1);
				String parentNode = getParentNode(parentPath);
				relatives.add(childId);
				traverseAscendants(parentNode);
				relatives.remove(relatives.size() - 1);
				noParents = false;
			}
			if (noParents) {
				relatives.add(childId);
				String ascendantPaths = "";
				for (int i = 0; i < relatives.size(); i++) {
					String ascendant = (String) relatives.get(i);
					if (i == 0)
						ascendantPaths = ascendant;
					else
						// Create path in descending order
						ascendantPaths = ascendant + "," + ascendantPaths;
					if (!paths.contains(ascendantPaths)) {
						paths.add(ascendantPaths);
						// Log.debug("INSERTED PATH: " + ascendantPaths);
					} else {
						// Log.debug("DUPLICATE PATH: " + ascendantPaths);
					}
				}
				relatives.remove(relatives.size() - 1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Traverse descendants of a given node.
	 * @param parentId The given node.
	 */
	public static void traverseDescendants(String parentId) {
		String getChildren = "SELECT * FROM dag " + "where relation='"
				+ DagNodeType.PARENTCHILD + "' AND nodepath LIKE '" + parentId
				+ ",%'";
		ResultSet childPaths = execute(getChildren);
		try {
			boolean noChildren = true;
			while (childPaths.next()) {
				String childPath = childPaths.getString(1);
				String childNode = getChildNode(childPath);
				relatives.add(parentId);
				traverseDescendants(childNode);
				relatives.remove(relatives.size() - 1);
				noChildren = false;
			}
			if (noChildren) {
				relatives.add(parentId);
				String descendantPath = "";
				Collections.reverse(relatives);
				for (int i = 0; i < relatives.size(); i++) {
					String descendant = (String) relatives.get(i);
					if (i == 0)
						descendantPath = descendant;
					else
						// Create path in descending order
						descendantPath = descendant + "," + descendantPath;
					if (!paths.contains(descendantPath)) {
						paths.add(descendantPath);
						// Log.debug("INSERTED PATH: " + descendantPath);
					} else {
						// Log.debug("DUPLICATE PATH: " + descendantPath);
					}
				}
				Collections.reverse(relatives);
				relatives.remove(relatives.size() - 1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Remove a given node.
	 * @param nodeId The given node.
	 * @return true if removed, false otherwise.
	 */
	public static boolean removeNode(String nodeId) {
		boolean nodeExists = nodeExists(nodeId);
		boolean isRoot = isRoot(nodeId);
		if (nodeExists && !isRoot) {
			String deleteCommand = "DELETE FROM dag " + "where relation='"
					+ DagNodeType.SELF + "' AND nodepath='" + nodeId + "'";
			return update(deleteCommand);
		} else {
			return false;
		}
	}

	/**
	 * Unlink a child node from a parent node.
	 * @param childId The child node.
	 * @param parentId The parent node.
	 * @return true if unlined, false otherwise.
	 */
	public static boolean unLinkChild(String childId, String parentId) {
		if (!nodeExists(parentId)) {
			System.out.println("Parent node " + parentId + " does not exist");
			return false;
		} else if (!nodeExists(childId)) {
			System.out.println("Child node " + childId + " does not exist");
			return false;
		}
		String sqlCommand = "DELETE FROM dag " + "where relation='"
				+ DagNodeType.PARENTCHILD + "' AND nodepath='" + parentId + ","
				+ childId + "'";
		return update(sqlCommand);
	}

	/**
	 * Determines if a node exists.
	 * @param nodeId The ID of the node.
	 * @return true if exists, false otherwise.
	 */
	private static boolean nodeExists(String nodeId) {
		String sqlCommand = "SELECT * FROM dag " + "WHERE (relation='"
				+ DagNodeType.SELF + "' OR relation='" + DagNodeType.ROOT
				+ "') AND nodepath='" + nodeId + "'";
		return exists(sqlCommand);
	}

	/**
	 * Determines if the node is a root node.
	 * @param nodeId The ID of the node.
	 * @return true if root node, false otherwise.
	 */
	private static boolean isRoot(String nodeId) {
		String sqlCommand = "SELECT * FROM dag " + "WHERE relation='"
				+ DagNodeType.ROOT + "' AND nodepath='" + nodeId + "'";
		return exists(sqlCommand);
	}

	/**
	 * Get the parent node in a parent-child path.
	 * @param nodePath The path.
	 * @return The ID of the parent node.
	 */
	private static String getParentNode(String nodePath) {
		String[] result = nodePath.split(",");
		if (result.length == 2) // Should contain only parent and child
			return result[0];
		else
			return null;
	}

	/**
	 * Get the child node in a parent-child path.
	 * @param nodePath The path.
	 * @return The ID of the child node.
	 */
	private static String getChildNode(String nodePath) {
		String[] result = nodePath.split(",");
		if (result.length == 2) // Should contain only parent and child
			return result[1];
		else
			return null;
	}

	/**
	 * Execute an SQL command.
	 * @param sqlCommand The command to be executed.
	 * @return The result of the execution.
	 */
	private static ResultSet execute(String sqlCommand) {
		try {
			Statement statement = ServerConfig.pmDB.getConnection().createStatement();
			return statement.executeQuery(sqlCommand);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Update the SQL database.
	 * @param updateCommand The command to be executed.
	 * @return true if executed, false otherwise.
	 */
	private static boolean update(String updateCommand) {
		try {
			Statement statement = ServerConfig.pmDB.getConnection().createStatement();
			statement.executeUpdate(updateCommand);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Determines if a node exists.
	 * @param sqlCommand The SQL command.
	 * @return true if node exists, false otherwise.
	 */
	private static boolean exists(String sqlCommand) {
		try {
			Statement statement = ServerConfig.pmDB.getConnection().createStatement();
			ResultSet result = statement.executeQuery(sqlCommand);
			return result.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Determines if a node1 is an ascendant of node2.
	 * @param node1
	 * @param node2
	 * @return
	 */
	private static boolean isAscendant(String node1, String node2) {
		// TODO:  This method needs to be implemented.
		return false;
	}

	/**
	 * Print the DAG.
	 */
	public static void printTable() {
		try {
			System.out.println("============================================");
			System.out.println("TABLE 'dag' VALUES: nodepath | relation");
			System.out.println("--------------------------------------------");
			String sqlCommand = "SELECT * FROM dag";
			ResultSet result = execute(sqlCommand);
			while (result.next()) {
				String nodePath = result.getString(1);
				String relation = result.getString(2);
				System.out.println(nodePath + " | " + relation);
			}
			System.out.println("");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
