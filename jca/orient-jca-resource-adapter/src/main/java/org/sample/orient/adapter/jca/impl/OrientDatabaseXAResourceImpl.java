package org.sample.orient.adapter.jca.impl;

import java.io.Serializable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.sample.orient.adapter.jca.api.OrientManagedConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.ODatabaseInternal;

public class OrientDatabaseXAResourceImpl implements XAResource, Serializable {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory
			.getLogger(OrientDatabaseXAResourceImpl.class);
	private int timeout;
	private ODatabaseInternal<?> database;
	private OrientManagedConnectionImpl connection;

	public OrientDatabaseXAResourceImpl(
			final OrientManagedConnectionImpl orientDBOrientManagedConnection,
			final ODatabaseInternal<?> oGraphDatabase) {
		this.database = oGraphDatabase;
		this.setConnection(orientDBOrientManagedConnection);
		logger.debug("Graph DBXA resource instantiated");
	}

	@Override
	public void start(final Xid xid, final int flags) throws XAException {
		boolean tmJoin = (flags & XAResource.TMJOIN) != 0;
		boolean tmResume = (flags & XAResource.TMRESUME) != 0;
		if (xid == null || (tmJoin && tmResume)
				|| (!tmJoin && !tmResume && flags != XAResource.TMNOFLAGS)) {
			throw new XAException(XAException.XAER_INVAL);
		}
		try {
			// default the Transaction type is OPTIMISTIC
			this.database.begin();
		} catch (Throwable t) {
			t.printStackTrace();
			logger.error("Unable to open the transaction", t);
			throw new RuntimeException(t);
		}
		logger.debug("xa begin");
	}

	@Override
	public void commit(Xid xid, boolean ignore) throws XAException {
		if (xid == null) {
			logger.debug("XA commit: No active transaction");
			return;
		}
		this.database.commit();
		logger.debug("xa commit");
	}

	@Override
	public void rollback(Xid arg0) throws XAException {
		this.database.rollback();
		logger.debug("xa rollback");
	}

	@Override
	public void end(Xid arg0, int flags) throws XAException {
		boolean tmFail = (flags & XAResource.TMFAIL) != 0;
		boolean tmSuccess = (flags & XAResource.TMSUCCESS) != 0;
		boolean tmSuspend = (flags & XAResource.TMSUSPEND) != 0;
		if ((tmFail && tmSuccess) || ((tmFail || tmSuccess) && tmSuspend)) {
			throw new XAException(XAException.XAER_INVAL);
		}
		if (tmSuspend && this.database.getTransaction() != null) {
			try {
				this.database.getTransaction().close();
			} finally {
				logger.debug("xa close");
			}
		}
	}

	@Override
	public void forget(Xid arg0) throws XAException {
		logger.debug("xa forget");
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return this.timeout;
	}

	@Override
	public boolean isSameRM(XAResource arg0) throws XAException {
		return (this == arg0);
	}

	@Override
	public int prepare(Xid xid) throws XAException {
		logger.debug("xa prepare [{}]", xid);
		return XA_OK;
	}

	@Override
	public Xid[] recover(int flag) throws XAException {
		logger.debug("xa recover");
		return new Xid[0];
	}

	@Override
	public boolean setTransactionTimeout(int timeout) throws XAException {
		this.timeout = timeout;
		return true;
	}

	public OrientManagedConnection getConnection() {
		return connection;
	}

	public void setConnection(OrientManagedConnectionImpl connection) {
		this.connection = connection;
	}

}
