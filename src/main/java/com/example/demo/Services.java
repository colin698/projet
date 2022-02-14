/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import com.example.demo.generated.World;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author colin
 */
public class Services {

    World readWorldFromXml() {
        World world = null;
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            world = (World) u.unmarshal(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return world;
    }

    void saveWorldToXML(World world) {
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller u = cont.createMarshaller();
            OutputStream output = new FileOutputStream("world.xml");
            u.marshal(world, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    World getWorld() {
        return readWorldFromXml();
    }
}
