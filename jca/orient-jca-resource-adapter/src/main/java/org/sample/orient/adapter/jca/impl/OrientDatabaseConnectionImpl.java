package org.sample.orient.adapter.jca.impl;

import org.sample.orient.adapter.jca.api.OrientDatabaseConnection;

import com.orientechnologies.orient.core.db.ODatabaseInternal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

public class OrientDatabaseConnectionImpl implements OrientDatabaseConnection {

	private OrientManagedConnectionImpl mc;
	private ODatabaseInternal<?> db;
	private boolean valid = true;

	public OrientDatabaseConnectionImpl(ODatabaseInternal<?> db,
			OrientManagedConnectionImpl mc) {
		this.db = db;
		this.mc = mc;
	}

	@Override
	public ODatabaseDocumentTx document() {
		checkValidity();
		return (ODatabaseDocumentTx) db;
	}

	@Override
	public OObjectDatabaseTx object() {
		checkValidity();
		return (OObjectDatabaseTx) db;
	}

	@Override
	public OrientGraph ograph() {
		checkValidity();
		return new OrientGraph((ODatabaseDocumentTx) db);
	}

	@Override
	public void close() {
		mc.close();
	}

	protected synchronized void setValid(boolean valid) {
		this.valid = valid;
	}

	protected synchronized boolean isValid() {
		return valid;
	}

	private void checkValidity() {
		if (!isValid()) {
			throw new RuntimeException("Invalid connection");
		}
	}
}
