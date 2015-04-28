package org.sample.orient.adapter.jca.api;

import java.io.Serializable;

import javax.resource.Referenceable;
import javax.resource.ResourceException;

public interface OrientDatabaseConnectionFactory extends Serializable,
		Referenceable {
	OrientDatabaseConnection createConnection() throws ResourceException;
}
