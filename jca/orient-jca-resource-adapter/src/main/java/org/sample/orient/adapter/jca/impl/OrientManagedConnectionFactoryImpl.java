package org.sample.orient.adapter.jca.impl;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ResourceAdapter;
import javax.security.auth.Subject;

import org.sample.orient.adapter.jca.api.OrientDatabaseConnection;
import org.sample.orient.adapter.jca.api.OrientDatabaseConnectionFactory;
import org.sample.orient.adapter.jca.api.OrientManagedConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ConnectionDefinition(connectionFactory = OrientDatabaseConnectionFactory.class, connectionFactoryImpl = OrientDatabaseConnectionFactoryImpl.class, connection = OrientDatabaseConnection.class, connectionImpl = OrientDatabaseConnectionImpl.class)
public class OrientManagedConnectionFactoryImpl implements
		OrientManagedConnectionFactory {

	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerFactory
			.getLogger(OrientManagedConnectionFactoryImpl.class);

	private PrintWriter logWriter;
	private OrientResourceAdapter ra;

	@ConfigProperty(defaultValue = "graph")
	private String type;

	@ConfigProperty
	private String connectionUrl;

	@ConfigProperty(defaultValue = "admin")
	private String username;

	@ConfigProperty(defaultValue = "admin")
	private String password;

	@ConfigProperty(defaultValue = "true")
	private boolean xa = true;

	public OrientManagedConnectionFactoryImpl() {
		this.logWriter = new PrintWriter(System.out);
	}

	@Override
	public Object createConnectionFactory(ConnectionManager cxManager)
			throws ResourceException {
		log.debug("creating managed connection factory");
		validate();
		return new OrientDatabaseConnectionFactoryImpl(this, cxManager);
	}

	private void validate() throws ResourceException {
		if (connectionUrl == null || connectionUrl.trim().isEmpty()) {
			throw new ResourceException(
					"configuration property [connectionUrl] must not be empty");
		}

		if (!Arrays.asList("document", "graph", "object").contains(type)) {
			throw new ResourceException(
					"configuration property [type] must be one of 'document', 'graph', 'object'");
		}
	}

	@Override
	public Object createConnectionFactory() throws ResourceException {
		throw new ResourceException("unmanaged environments are not supported");
	}

	@Override
	public ManagedConnection createManagedConnection(Subject subject,
			ConnectionRequestInfo cxRequestInfo) throws ResourceException {
		log.debug("creating managed connection");
		return new OrientManagedConnectionImpl(this, cxRequestInfo);
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "resource" })
	@Override
	public ManagedConnection matchManagedConnections(Set connectionSet,
			Subject subject, ConnectionRequestInfo cxRequestInfo)
			throws ResourceException {

		Set<ManagedConnection> connections = connectionSet;

		for (ManagedConnection connection : connections) {
			if (connection instanceof OrientManagedConnectionImpl) {
				OrientManagedConnectionImpl orientConnection = (OrientManagedConnectionImpl) connection;
				ConnectionRequestInfo cri = orientConnection
						.getConnectionRequestInfo();
				if (cri == null || cri.equals(cxRequestInfo)) {
					return connection;
				}
			}
		}
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws ResourceException {
		this.logWriter = out;
	}

	@Override
	public PrintWriter getLogWriter() throws ResourceException {
		return logWriter;
	}

	@Override
	public ResourceAdapter getResourceAdapter() {
		return ra;
	}

	@Override
	public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
		this.ra = (OrientResourceAdapter) ra;
	}

	@Override
	public TransactionSupportLevel getTransactionSupport() {
		if (isXa()) {
			return TransactionSupportLevel.XATransaction;
		} else {
			return TransactionSupportLevel.LocalTransaction;
		}
	}

	public String getConnectionUrl() {
		return connectionUrl;
	}

	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ra == null) ? 0 : ra.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		OrientManagedConnectionFactoryImpl other = (OrientManagedConnectionFactoryImpl) obj;
		if (ra == null) {
			if (other.ra != null) {
				return false;
			}
		} else if (!ra.equals(other.ra)) {
			return false;
		}
		return true;
	}

	public final OrientResourceAdapter getRa() {
		return ra;
	}

	public final void setRa(OrientResourceAdapter ra) {
		this.ra = ra;
	}

	public final boolean isXa() {
		return xa;
	}

	public final void setXa(boolean xa) {
		this.xa = xa;
	}
}
