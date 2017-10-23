package data.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;

import data.properties.PropertiesBundle;
import exception.system.DBException;


/*
 * use connection pool 
 */
class DBConnector 
{
	final private Logger log = Logger.getLogger( DBConnector.class );
	private static DBConnector instance = new DBConnector();
	
	private final static String DBCP_DRIVERNAME = "jdbc:apache:commons:dbcp:cp";
	private final static int minConnectionCount = 5;
	private final static int maxConnectionCount = 20;
	
	/*
	 * JDBC가 DB와 연결 기다리는 timeout.
	 * 
	 * DBCP는 object pool로서 관심은 이미 맺어진 connection입니다. 
	 * connection들을 pool에 미리 넣어두었다가 사용하고, 반환하고. 관심은 재활용입니다. 
	 * 그리고 DBMS와의 실제 connection은 DBCP가 하지 않고 JDBC가 합니다.
	 */
	private final static int jdbcConnectionCount = 3000;
	
	private DBConnector() 
	{
		try 
		{
			loadJDBCDriver();
			initConnectionPool();
		} 
		catch (Exception e) 
		{
			log.error("Failed INIT DB.");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static DBConnector getInstance() {
		return instance;
	}
	
	private void loadJDBCDriver() throws DBException
	{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			//java.sql.DriverManager.setLoginTimeout(jdbcConnectionCount); 
		} catch (ClassNotFoundException e) {
			throw new DBException("JDBC Driver load failed. ", e.getCause());
		}
	}
	
	private void initConnectionPool() throws DBException 
	{
		final PropertiesBundle prop = PropertiesBundle.getInstance();
		if( prop.dbUrl().isEmpty() || prop.dbId().isEmpty() || prop.dbPassword().isEmpty() ){
			throw new IllegalArgumentException("invalid db properties. "
												+ "url("+prop.dbUrl()+")"
												+ ",id("+prop.dbId()+"), "
												+ ",pwd("+prop.dbPassword()+")");
		}
		
		
		log.debug("db properties. "
					+ "url("+prop.dbUrl()+")"
					+ ",id("+prop.dbId()+"), "
					+ ",pwd("+prop.dbPassword()+")");
		// new connection maker
		ConnectionFactory connFactory = new DriverManagerConnectionFactory( prop.dbUrl(), prop.dbId(), prop.dbPassword() ); 
//		ConnectionFactory connFactory = new DriverManagerConnectionFactory(prop.dbUrl(), prop.prop());
		
		// connection pool 저장소
		PoolableConnectionFactory poolableConnFactory = new PoolableConnectionFactory(connFactory, null);
		
		// set query for connection available checking.
		poolableConnFactory.setValidationQuery("select 1");
		
		// connection pool setting 
		GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
		// 사용하지 않는 커넥션 체크 및 삭제 주기.
		poolConfig.setTimeBetweenEvictionRunsMillis(1000L * 60L * 1L);  // check duration for able connections
		poolConfig.setTestWhileIdle(true);  // turn on check
		poolConfig.setMinIdle(minConnectionCount); // min connection count
		poolConfig.setMaxTotal(maxConnectionCount); // max connection count
		
		
		// create connection pool
		GenericObjectPool<PoolableConnection> connectionPool 
				= new GenericObjectPool<>( poolableConnFactory, poolConfig );
		
		poolableConnFactory.setPool(connectionPool);
		
		try {
			// register connection pool driver
			Class.forName("org.apache.commons.dbcp2.PoolingDriver");
		} catch (ClassNotFoundException e) {
			throw new DBException("org.apache.commons.dbcp2.PoolingDriver load failed. ", e.getCause());
		}
		
		PoolingDriver driver = null;
		try {
			driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
		} catch (SQLException e) {
			throw new DBException("dbcp2.PoolingDriver: database access error occurs.", e.getCause());
		}
		// should use like this: 
		// DriverManager.getConnection("jdbc:apache:commons:dbcp:cp")
		// 커넷션 풀 이름을 cp로 지정함.
		driver.registerPool("cp", connectionPool);  
	}
	
	
	public Connection getConnection() throws SQLException {
		return (Connection) DriverManager.getConnection(DBCP_DRIVERNAME);
	}
}



/*
 * 	only available in db package
	db package 안의 class에서만 사용가능
 */
class DBConnectionHelper {
	final private Logger log = Logger.getLogger( DBConnectionHelper.class );
	private Connection connection = null;
	private PreparedStatement psmt = null;
	private ResultSet rs = null;
	private String mQuery = null;
	
	public DBConnectionHelper( String query ) throws SQLException {
		this.connection = DBConnector.getInstance().getConnection();
		this.psmt = (PreparedStatement) connection.prepareStatement(query);
		this.mQuery = query;
	}
	
	public void close() {
		try 
		{
			if (this.rs != null)
				this.rs.close();
		} catch (SQLException e) {
			log.error("Failed close ResultSet");
			e.printStackTrace();
		}

		try 
		{
			if (this.psmt != null)
				this.psmt.close();
		} catch (SQLException e) {
			log.error("Failed close PreparedStatement");
			e.printStackTrace();
		}

		try 
		{
			// return connection to the pool. (No close connection)
			// Connection의 close() 메소드를 호출하면, 커넥션이 닫히는 것이 아니라 커넥션 풀로 반환
			if (this.connection != null) {
				this.connection.close();
			}
		} catch (SQLException e) {
			log.error("Failed close Connection");
			e.printStackTrace();
		}
	}

	public void setInt( Integer idx, Integer i ) throws SQLException {
		psmt.setInt(idx, i);
	}
	
	public void setString( Integer idx, String s ) throws SQLException {
		psmt.setString(idx, s);
	}
	
	public ResultSet executeQuery() throws SQLException {
		rs = psmt.executeQuery();
		return rs;
	}
	
	public Integer executeUpdate() throws SQLException {
		return psmt.executeUpdate();
	}
	
	public Integer executeUpdate(int key) throws SQLException {
		return psmt.executeUpdate(mQuery, key);
	}
}