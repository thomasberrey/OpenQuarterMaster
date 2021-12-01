package com.ebp.openQuarterMaster.baseStation.demo;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/demo")
@RegisterRestClient(configKey = "demoService")
public interface DemoServiceCaller {

    @GET
    @Path("1")
    @Produces(MediaType.TEXT_PLAIN)
    String get1(@HeaderParam("authorization") String id);

    @GET
    @Path("2")
    @Produces(MediaType.TEXT_PLAIN)
    String get2(@HeaderParam("authorization") String id);

}