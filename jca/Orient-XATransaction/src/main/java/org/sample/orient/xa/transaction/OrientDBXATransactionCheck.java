package org.sample.orient.xa.transaction;

import javax.ejb.Remote;

@Remote
public interface OrientDBXATransactionCheck {
	public void testXA(String city, int id, String className);
}
