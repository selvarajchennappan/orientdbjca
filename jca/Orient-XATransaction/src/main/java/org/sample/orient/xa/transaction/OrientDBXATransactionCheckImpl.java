package org.sample.orient.xa.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.sql.DataSource;

import org.sample.orient.adapter.jca.api.OrientDatabaseConnection;
import org.sample.orient.adapter.jca.api.OrientDatabaseConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;

@Stateless
public class OrientDBXATransactionCheckImpl implements
		OrientDBXATransactionCheck {
	private static Logger log = LoggerFactory
			.getLogger(OrientDBXATransactionCheckImpl.class);

	// @Resource(name = "RelationalDataSource")
	private DataSource ds;

	// @Resource(name = "eis/orientDB")
	private OrientDatabaseConnectionFactory cf;

	public OrientDBXATransactionCheckImpl() {
		try {
			InitialContext context = new InitialContext();
			ds = (DataSource) context.lookup("RelationalDataSource");
			cf = (OrientDatabaseConnectionFactory) context
					.lookup("eis/orientDB");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void testXA(String city, int id, String className) {
		log.info("test data Received City[{}],Id[{}],graphClassName[{}] ",
				city, id, className);
		try {
			saveIntoGraphDB(city);
			saveIntoRelationalDB(id);
			readFromGraphDB(className);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("testXA Exception", e);
			throw new RuntimeException(e);
		}
		log.info("testXA processed successfully.." + city + "," + id);
	}

	private void readFromGraphDB(String className) throws ResourceException {
		log.info("Graph DB Read started.");
		OrientDatabaseConnection con = cf.createConnection();
		System.out.println(con.document().query(
				new OSQLSynchQuery<ODocument>("select from " + className)));
		log.info("Graph DB successfully  Read.");
		con.close();
	}

	private void saveIntoGraphDB(String city) throws ResourceException {
		log.info("Graph DB insertion started");
		OrientDatabaseConnection con = cf.createConnection();
		TransactionalGraph graph = con.ograph();
		Vertex vAddress = graph.addVertex("class:Address");
		vAddress.setProperty("street", "Van Ness Ave.");
		vAddress.setProperty("city", city);
		vAddress.setProperty("state", "California");
		log.info("Graph DB successfully ");
		con.close();
	}

	private void saveIntoRelationalDB(int id) throws Exception {
		log.info("Relational DB insertion started");
		Connection dbConnection = ds.getConnection();
		String insertSQL = "insert into expert_owner.xa_msg(id,msg)Values("
				+ id + ",'Relational-GraphDB')";
		PreparedStatement preparedStatement = dbConnection
				.prepareStatement(insertSQL);
		log.info("Relational DB insertion success, no of rows : "
				+ preparedStatement.executeUpdate());
		dbConnection.close();

	}

}
