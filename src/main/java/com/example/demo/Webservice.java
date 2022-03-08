/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

/**
 *
 * @author colin
 */
@Path("generic")

public class Webservice {

    Services services;

    public Webservice() {
        services = new Services();
    }

    @GET
    @Path("world")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getWorld(@Context HttpServletRequest request) throws JAXBException {
        String username = request.getHeader("X-user");
        return Response.ok(services.getWorld(username)).build();
    }
    
    @PUT
    @Path("world")
    public void putProduit(@Context HttpServletRequest request, ProductType produit) throws JAXBException {
        String username = request.getHeader("X-user");
        services.updateProduct(username, produit);
    }
    
    @PUT
    @Path("world")
    public void putManager(@Context HttpServletRequest request, PallierType manager) throws JAXBException {
        String username = request.getHeader("X-user");
        services.updateManager(username, manager);
    }
}
