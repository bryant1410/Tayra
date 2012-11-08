package com.ee.beaver.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import java.net.UnknownHostException;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ee.beaver.domain.MongoCollection;
import com.ee.beaver.domain.NotALocalDB;
import com.ee.beaver.domain.Oplog;
import com.ee.beaver.io.Copier;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class OplogSpecs {

	private static Mongo replicaSet;
	private static final String HOST = "localhost";
	private static final int PORT = 27017;
	private MongoCollection oplog;

	@BeforeClass
	public static void connectToMongo() throws UnknownHostException, MongoException {
		replicaSet = new Mongo(HOST, PORT);
	}

	@AfterClass
	public static void closeConnectionToMongo() {
		replicaSet.close();
	}
	
	@Before
	public void useLocalDB() {
		DB local = replicaSet.getDB("local");
	    oplog = new Oplog(local);
	}

	@Test
	public void doesNotConnectToStandaloneMongoInstance() throws Exception {
		Mongo standalone = new Mongo(HOST, 27020);
		DB local = standalone.getDB("local");
		try {
			// When
			new Oplog(local);
			fail("Should Not Connect to a standalone node");
		} catch (NotAReplicaSetNode e) {
			// Then
			assertThat(e.getMessage(),
					is("localhost is not a part of ReplicaSet"));
		}
	}
	
	@Test
	public void itReadsFirstDocument() throws UnknownHostException, MongoException {
		//Given
		Iterator<DBObject> iterator = oplog.find();

		//When
		DBObject actualDBObject = iterator.next();

		//Then
		assertThat(actualDBObject, instanceOf(DBObject.class));
	}

	@Test
	public void doesNotAllowDocumentRemoval() {
		//Given
		Iterator<DBObject> iterator = oplog.find();

		//When
		try {
			iterator.remove();
			fail("Removing Document is not supported, Should have raised Exception");
		} catch (UnsupportedOperationException uoe) {
			//Then
			assertThat(uoe.getMessage(), is("remove document on oplog is not supported"));
		}
	}
	
	@Test
	public void permitsReadingFromLocalDBOnly() {
		//Given
		DB nonLocalDB = replicaSet.getDB("nonLocal");
		
		//When
		try{
			oplog = new Oplog(nonLocalDB);
			fail("DB used is not local, should raise an Exception");
		} catch (NotALocalDB naldb) {
			//Then
			assertThat(naldb.getMessage(), is("Not a local DB"));
		}
	}
}
