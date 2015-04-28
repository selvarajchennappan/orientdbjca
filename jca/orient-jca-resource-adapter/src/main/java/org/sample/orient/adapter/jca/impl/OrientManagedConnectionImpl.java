package org.sample.orient.adapter.jca.impl;

import static javax.resource.spi.ConnectionEvent.CONNECTION_CLOSED;
import static javax.resource.spi.ConnectionEvent.CONNECTION_ERROR_OCCURRED;
import static javax.resource.spi.ConnectionEvent.LOCAL_TRANSACTION_COMMITTED;
import static javax.resource.spi.ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK;
import static javax.resource.spi.ConnectionEvent.LOCAL_TRANSACTION_STARTED;

import java.io.Closeable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.sample.orient.adapter.jca.api.OrientDatabaseConnection;
import org.sample.orient.adapter.jca.api.OrientManagedConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.ODatabaseInternal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public class OrientManagedConnectionImpl implements OrientManagedConnection,
		Closeable {

	private static Logger log = LoggerFactory
			.getLogger(OrientManagedConnectionImpl.class);

	private OrientManagedConnectionFactoryImpl mcf;
	private ODatabaseInternal<?> db;
	private PrintWriter logWriter;
	private List<ConnectionEventListener> listeners = new ArrayList<ConnectionEventListener>();
	private ConnectionRequestInfo cri;
	private OrientDatabaseConnectionImpl connection;
	private XAResource xaResource;
	private String engine;

	public OrientManagedConnectionImpl(OrientManagedConnectionFactoryImpl mcf,
			ConnectionRequestInfo cri) throws ResourceException {
		this.mcf = mcf;
		this.cri = cri;
		determineEngine();
		createDatabaseHandle();
		createDatabaseIfNeeded();
		xaResource = new OrientDatabaseXAResourceImpl(this, db);
	}

	@Override
	public Object getConnection(Subject subject,
			ConnectionRequestInfo cxRequestInfo) throws ResourceException {
		log.debug("getConnection()");
		connection = new OrientDatabaseConnectionImpl(this.db, this);
		if (this.db.isClosed()) {
			openDatabase();
		}
		return connection;
	}

	private void createDatabaseHandle() {
		String type = mcf.getType();
		String url = mcf.getConnectionUrl();
		log.debug("instantiating Orient Database of type [{}] with URL [{}]",
				type, url);
		if (type.equals("document")) {
			this.db = new ODatabaseDocumentTx(url);
		} else if (type.equals("object")) {
			this.db = new OObjectDatabaseTx(url);
		} else if (type.equals("graph")) {
			this.db = new ODatabaseDocumentTx(url);
			openDatabase();
		}
	}

	private synchronized void createDatabaseIfNeeded() {
		if (!engine.equals("remote")) {
			if (!this.db.exists()) {
				this.db.create();
			}
		}
	}

	private void determineEngine() throws ResourceException {
		int colon = mcf.getConnectionUrl().indexOf(':');
		if (colon == -1) {
			throw new ResourceException();
		}
		this.engine = mcf.getConnectionUrl().substring(0, colon);
	}

	private void openDatabase() {
		log.debug("opening database for user [{}]", mcf.getUsername());
		if (this.db.isClosed()) {
			this.db.open(mcf.getUsername(), mcf.getPassword());
		}
	}

	private void closeDatabase() {
		this.db.close();
	}

	@Override
	public void destroy() throws ResourceException {
		log.debug("destroy()");
		cleanup();
		closeDatabase();
	}

	@Override
	public void cleanup() throws ResourceException {
		log.debug("cleanup()");
		if (this.xaResource != null) {
			this.xaResource = null;
		}
		this.connection = null;
	}

	@Override
	public void associateConnection(Object connection) throws ResourceException {
		log.debug("associateConnection()");
		this.connection = (OrientDatabaseConnectionImpl) connection;
	}

	@Override
	public void addConnectionEventListener(ConnectionEventListener listener) {
		log.debug("addConnectionEventListener()");
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeConnectionEventListener(ConnectionEventListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	@Override
	public XAResource getXAResource() throws ResourceException {
		return xaResource;
	}

	@Override
	public LocalTransaction getLocalTransaction() throws ResourceException {
		log.debug("getLocalTransaction()");
		return new OrientLocalTransaction();
	}

	@Override
	public ManagedConnectionMetaData getMetaData() throws ResourceException {
		return new OrientManagedConnectionMetaData();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws ResourceException {
		this.logWriter = out;
	}

	@Override
	public PrintWriter getLogWriter() throws ResourceException {
		return logWriter;
	}

	private void fireConnectionEvent(int event) {
		ConnectionEvent connectionEvent = new ConnectionEvent(this, event);
		connectionEvent.setConnectionHandle(connection);
		synchronized (listeners) {
			for (ConnectionEventListener listener : this.listeners) {
				switch (event) {
				case LOCAL_TRANSACTION_STARTED:
					listener.localTransactionStarted(connectionEvent);
					break;
				case LOCAL_TRANSACTION_COMMITTED:
					listener.localTransactionCommitted(connectionEvent);
					break;
				case LOCAL_TRANSACTION_ROLLEDBACK:
					listener.localTransactionRolledback(connectionEvent);
					break;
				case CONNECTION_CLOSED:
					listener.connectionClosed(connectionEvent);
					break;
				case CONNECTION_ERROR_OCCURRED:
					listener.connectionErrorOccurred(connectionEvent);
					break;
				default:
					throw new IllegalArgumentException("Unknown event: "
							+ event);
				}
			}
		}
	}

	/**
	 * Do not close the underlying connection now, as it may be used in a
	 * container-managed transaction. The connection will be closed in
	 * {@link #cleanup()}.
	 */
	@Override
	public void close() {
		log.debug("close()");
		fireConnectionEvent(CONNECTION_CLOSED);
	}

	public ConnectionRequestInfo getConnectionRequestInfo() {
		return cri;
	}

	void sendClosedEvent(OrientDatabaseConnection handle) {
		log.info("send-event-close");
		sendEvent(ConnectionEvent.CONNECTION_CLOSED, handle, null);
	}

	private void sendEvent(int type, OrientDatabaseConnection handle,
			Exception cause) {
		ConnectionEvent event = new ConnectionEvent(this, type, cause);
		if (handle != null) {
			event.setConnectionHandle(handle);
		}
		fireConnectionEvent(type);
	}

	class OrientLocalTransaction implements LocalTransaction {

		@Override
		public void begin() throws ResourceException {
			log.debug("begin()");
			db.begin();
			fireConnectionEvent(LOCAL_TRANSACTION_STARTED);
		}

		@Override
		public void commit() throws ResourceException {
			log.debug("commit()");
			db.commit();
			fireConnectionEvent(LOCAL_TRANSACTION_COMMITTED);
		}

		@Override
		public void rollback() throws ResourceException {
			log.debug("rollback()");
			db.rollback();
			fireConnectionEvent(LOCAL_TRANSACTION_ROLLEDBACK);
		}
	}
}
