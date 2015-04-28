package org.sample.rs.test.service;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.sample.orient.xa.transaction.OrientDBXATransactionCheck;

@Stateless
@Path("orientXACheck")
public class XAServiceCheck {

	@EJB
	OrientDBXATransactionCheck orientDBXATransactionCheck;

	@GET
	@Consumes(MediaType.TEXT_PLAIN)
	public String getTestXA(@QueryParam("city") String city,
			@QueryParam("id") int id, @QueryParam("className") String className) {
		orientDBXATransactionCheck.testXA(city, id, className);
		return "Success";
	}

}
