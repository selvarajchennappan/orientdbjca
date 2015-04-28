package org.sample.orient.adapter.jca.api;

import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.TransactionSupport;

public interface OrientManagedConnectionFactory extends
		ManagedConnectionFactory, ResourceAdapterAssociation,
		TransactionSupport {

}
