package org.sample.orient.adapter.jca.api;

import java.io.Closeable;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;

public interface OrientDatabaseConnection extends Closeable {
	ODatabaseDocumentTx document();

	OObjectDatabaseTx object();

	OrientGraph ograph();

	void close();
}
