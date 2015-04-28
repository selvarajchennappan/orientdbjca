package org.sample.orient.xa.it

import groovy.sql.Sql
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.OCommandSQL
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery

class IntegrationTest extends Specification {

	static final String baseURL =  "http://localhost:7001/Orient-XATransaction/rest/orientXACheck?"

	static Sql sql

	static final String SUCCESS = "200"

	static final String ERROR = "500"

	static ODatabaseDocumentTx db

	List<ODocument> graphResult

	def setupSpec() {

		sql = Sql.newInstance(
				"jdbc:oracle:thin:@apdaas05-scan-oravip.nat.bt.com:61901/BERWA_ANY",
				"expert_owner",
				"expert_owner",
				"oracle.jdbc.pool.OracleDataSource")

		db = new ODatabaseDocumentTx("remote:/localhost:study");

		db.open("admin", "admin");
		db.command(new OCommandSQL("create class Address extends V")).execute();
		sql.executeUpdate("create table xa_msg (id number primary key,msg varchar(30))")
	}


	def "should insert into graph db and relational db successfully." (){

		given:

		def params =  [city: 'Rome',id:8,className:"Address"]


		when:

		def responseCode = getResponse(params)

		and:

		def rdbms_rec_count = sql.firstRow("select count(*) as NO_OF_RECORD from xa_msg where id = 8")

		graphResult = db.query(
				new OSQLSynchQuery<ODocument>("select * from Address where city = 'Rome'"));


		then: "The request should be successful"

		responseCode == SUCCESS

		and:

		rdbms_rec_count.NO_OF_RECORD == 1
		graphResult.size() == 1
	}

	@Unroll
	def "insert into graph db successfully and unique key constraint during second time execution at relational db with same :#id" (){

		given:

		def params =  [city: ipcity,id:id,className:"Address"]

		when:

		def responseCode = getResponse(params)

		and:

		graphResult = db.query(
				new OSQLSynchQuery<ODocument>("select * from Address where city = '"+ipcity+"'"));
		

		then:

		responseCode == expectedStatus

		graphResult.size() == graphRecCount
		
		where:

		id        | ipcity              | expectedStatus          | graphRecCount
		"1001"    | "Madras"            | SUCCESS       		  |  1
		"1001"    | "Newyork"           | ERROR         		  |  0 
	}


	def " graph db and relational db successfully inserted . However graph read failed,So rollback both" (){

		given:

		def params =  [city: "London",id:107,className:"Address1233"]

		when:

	    def responseCode = getResponse(params)
		
		and:

		def rdbms_rec_count = sql.firstRow("select count(*) NO_OF_RECORD from xa_msg where id =107")
		graphResult = db.query(
				new OSQLSynchQuery<ODocument>("select * from Address where city ='London'"));

		then:

		responseCode == ERROR

		and: "Rollback both RDBMS and graphDB"

		rdbms_rec_count.NO_OF_RECORD   == 0
		graphResult.size() 			   == 0
	}

	static String getResponse(def params){
		def getURL = baseURL + params.collect { k,v -> "$k=$v" }.join('&')
	    def url = new URL(getURL)
		def connection = url.openConnection()
		connection.setRequestMethod("GET")
		connection.connect()
		return connection.responseCode
	}



	def cleanupSpec() {
		sql.execute("delete from xa_msg")
		sql.execute("drop table xa_msg")
		db.command(new OCommandSQL("DELETE VERTEX Address")).execute();
		db.command(new OCommandSQL("drop class Address")).execute();
		sql.close()
		db.close()
	}
}