package org.sample.orient.adapter.jca.impl;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

import org.sample.orient.adapter.jca.api.OrientDatabaseConnection;
import org.sample.orient.adapter.jca.api.OrientDatabaseConnectionFactory;
import org.sample.orient.adapter.jca.api.OrientManagedConnectionFactory;

public class OrientDatabaseConnectionFactoryImpl implements
		OrientDatabaseConnectionFactory {

	private static final long serialVersionUID = 1L;

	private OrientManagedConnectionFactory mcf;
	private ConnectionManager cm;
	private Reference reference;

	public OrientDatabaseConnectionFactoryImpl(
			OrientManagedConnectionFactory mcf, ConnectionManager cm) {
		this.mcf = mcf;
		this.cm = cm;
	}

	@Override
	public void setReference(Reference reference) {
		this.reference = reference;
	}

	@Override
	public Reference getReference() throws NamingException {
		return reference;
	}

	@Override
	public OrientDatabaseConnection createConnection() throws ResourceException {
		return (OrientDatabaseConnection) cm.allocateConnection(mcf, null);
	}
}
