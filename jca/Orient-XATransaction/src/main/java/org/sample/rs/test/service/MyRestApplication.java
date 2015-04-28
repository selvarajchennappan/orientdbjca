package org.sample.rs.test.service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/rest/*")
public class MyRestApplication extends Application {}
