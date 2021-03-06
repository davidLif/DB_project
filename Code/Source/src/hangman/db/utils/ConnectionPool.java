package hangman.db.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Singleton connection pool
 * Usage:
 * 		1. call configure() to configure connection credentials and pool size
 * 		2. call getPool() to fetch the singleton connection pool
 * 		3. at the end of the program, remember to call closePool()
 *
 * To fetch a connection from the pool, use getConnection()
 * Each such connection must be returned to the pool, by calling returnConnection()
 *
 * NOTE: pool size must be at-least 3 (see documentation)
 */



public class ConnectionPool {

	/**
	 * DB connection details
	 */
	private static String host;
	private static String port;
	private static String schema;
	private static String username;
	private static String password;
	private static int poolSize;

	
	/**
	 * whether or not configure was called
	 */
	private static boolean configured = false;
	
	/**
	 * Set connection parameters
	 * @param host
	 * @param port
	 * @param schema
	 * @param username
	 * @param password
	 * @param poolSize  -  number of connections to construct
	 */
	
	public static void configure(String host,
			String port,
			String schema,
			String username,
			String password,
			int poolSize)
	{
		ConnectionPool.host = host;
		ConnectionPool.port = port;
		ConnectionPool.schema = schema;
		ConnectionPool.username = username;
		ConnectionPool.password = password;
		ConnectionPool.poolSize = Math.max(3, poolSize);
		configured = true;
		
	}
	/**
	 * 
	 * @return max pool size (capacity specified in config file)
	 */
	
	public static int getMaxPoolSize()
	{
		return poolSize;
	}
	
	
	/**
	 * 
	 * @return actual number of connections currently in pool
	 */
	
	public synchronized int getCurrentPoolSize()
	{
		return connections.size();
	}
	
	/**
	 * load the JDBC driver
	 * @return true on success
	 */
	private static boolean registerJDBC()
	{
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Unable to load the MySQL JDBC driver.");
			return false;
		}
		//System.out.println("Driver loaded successfully");
		
		driverLoaded = true;
		return true;
	}
	
	private static boolean driverLoaded = false;
	
	
	private static ConnectionPool singleton;
	
	/**
	 * fetch the ConnectionPool singleton
	 * pool will be constructed on first method call
	 * 
	 * NOTE: must call configure() method before attempting to call getPool() for the first time
	 *       in case of an error, all connections that were opened during the initialization process will be closed
	 * 
	 * method is synchronized, thus accessed by only one thread at a time
	 * @return
	 * @throws Exception
	 */
	public static synchronized ConnectionPool getPool() throws Exception
	{
		
		if(singleton == null)
		{
			if(!configured)
			{
				throw new Exception("Error: must configure() connection pool before creating one");
			}
			// construct the singleton
			singleton = new ConnectionPool();
			singleton.init();
		}
		return singleton;
	}
	
	/**
	 * Dispose of existing connection pool
	 * Call only at the end of the program execution
	 * @throws Exception
	 */
	
	public static synchronized void closePool() throws Exception
	{
		if(singleton == null)
		{
			return;
		}
		
		// close all connections
		for(Connection conn : singleton.connections)
		{
			
			closeConnection(conn);
		}
		singleton.openedConnections = 0;
		
		//TODO: remove at release mode
		//
		if(singleton.openedConnections > singleton.connections.size())
		{
			System.out.println("Error: not all connections were returned");
			throw new Exception("Error: not all connections were returned");
			
		}
		// -----------
		singleton = null;
		
	}
	
	/**
	 * the connection pool itself
	 * when a connection is fetched it is removed from the list
	 * when a connection is returned it is added back to the list
	 */
	protected List<Connection> connections = new ArrayList<Connection>();
	
	
	/**
	 * The actual amount of connections that were constructed
	 * 
	 * openedConnections <= getMaxPoolSize() at all times
	 * 
	 */
	protected int openedConnections = 0;
	
	/**
	 * loads driver if required
	 *  
	 */
	
	protected synchronized void init() throws Exception
	{
		// load driver
		if(!driverLoaded)
		{
			if(!registerJDBC())
			{
				throw new Exception("Error: could not load SQL driver");
			}
		}
	}
	
	
	
	/**
	 * close given connection
	 */
	private static void closeConnection(Connection conn) {
		// closing the connection
		try {
			conn.close();
		} catch (SQLException e) {
			System.out.println("Unable to close the connection - " + e.getMessage());
		}

	}

	/**
	 * Fetch a free connection
	 * May be accessed by multiple threads
	 * 
	 * @return a free connection
	 * @throws InterruptedException 
	 * @throws SQLException 
	 */
	
	public synchronized Connection getConnection() throws InterruptedException, SQLException
	{
		
		
		if(this.connections.isEmpty() && openedConnections >= ConnectionPool.getMaxPoolSize())
		{
			// wait for a connection to be returned
			// cannot allocate a new connection
			wait();
		}
		else if(this.connections.isEmpty())
		{
			// may open a new connection
			++openedConnections;
			return DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s?tcpKeepAlive=true", host, port, schema),
					username,
					password);
		}
		
		// may fetch existing connection
		Connection conn = connections.get(0);
		connections.remove(0);
		return conn;
	

	}
	/**
	 * get an array of free connections
	 * @param   amount - number of require connections
	 * @return  null if the request is impossible, not enough connections will ever exist
	 * @throws SQLException 
	 * @throws InterruptedException 
	 */
	
	public synchronized Connection[] getConnections(int amount) throws InterruptedException, SQLException
	{
		if(amount > ConnectionPool.getMaxPoolSize())
		{
			return null;
		}
		
		Connection[] result = new Connection[amount];
		for( int i = 0; i < amount; ++i)
		{
			result[i]  = getConnection();
			
		}
		
		return result;
	}
	
	
	
	/**
	 * return an array of connections to the pool
	 * @param connections
	 */
	public synchronized void returnConnections(Connection[] connections)
	{
		for(Connection connection : connections)
		{
			returnConnection(connection);
		}
	}
	
	/**
	 * return connection to the pool
	 * @param conn
	 */
	public synchronized void returnConnection(Connection conn)
	{
		this.connections.add(conn);
		
		// notify waiting a waiting thread that a connection has been returned
		notify();
	}

	
	protected ConnectionPool()
	{
		
	}
	
}
