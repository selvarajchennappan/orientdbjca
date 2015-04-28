package org.sample.orient.adapter.jca.impl;

import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.Orient;

@Connector(reauthenticationSupport = false, transactionSupport = TransactionSupport.TransactionSupportLevel.XATransaction, vendorName = "TEST", eisType = "OrientDB")
public class OrientResourceAdapter implements ResourceAdapter, Serializable {
	private static final long serialVersionUID = -362083135805815420L;
	private static Logger log = LoggerFactory
			.getLogger(OrientResourceAdapter.class);

	@Override
	public void start(BootstrapContext ctx)
			throws ResourceAdapterInternalException {
		log.debug("starting OrientResourceAdapter");
		Orient.instance().removeShutdownHook();
	}

	@Override
	public void stop() {
		log.debug("stopping OrientResourceAdapter");
		Orient.instance().shutdown();
	}

	@Override
	public void endpointActivation(MessageEndpointFactory endpointFactory,
			ActivationSpec spec) throws ResourceException {
		log.info("endpointActivation");
	}

	@Override
	public void endpointDeactivation(MessageEndpointFactory endpointFactory,
			ActivationSpec spec) {
		log.info("endpointDeactivation");
	}

	@Override
	public XAResource[] getXAResources(ActivationSpec[] specs)
			throws ResourceException {
		log.info("getXAResources");
		return new XAResource[0];
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
