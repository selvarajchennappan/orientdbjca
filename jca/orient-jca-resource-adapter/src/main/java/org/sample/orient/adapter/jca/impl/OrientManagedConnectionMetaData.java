package org.sample.orient.adapter.jca.impl;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionMetaData;

import com.orientechnologies.orient.core.OConstants;

public class OrientManagedConnectionMetaData implements
		ManagedConnectionMetaData {

	@Override
	public String getEISProductName() throws ResourceException {
		return "OrientDB";
	}

	@Override
	public String getEISProductVersion() throws ResourceException {
		return OConstants.getVersion();
	}

	
	/**
	 * @return Maximum limit for number of active concurrent connections
	 */
	@Override
	public int getMaxConnections() throws ResourceException {
		return 0;
	}

	@Override
	public String getUserName() throws ResourceException {
		return "admin";
	}

}
