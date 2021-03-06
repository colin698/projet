/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author colin
 */
public class Services {
    
    World readWorldFromXml(String username) throws JAXBException {
        World world = null;
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            File file = new File(username + "-world.xml");
            world = (World) u.unmarshal(file);
            
        } catch (Exception e) {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Unmarshaller u = cont.createUnmarshaller();
            world = (World) u.unmarshal(input);
            
        }
        return world;
    }
    
    void saveWorldToXML(String username, World world) {
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller u = cont.createMarshaller();
            OutputStream output = new FileOutputStream(username + "-world.xml");
            u.marshal(world, output);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    World getWorld(String username) throws JAXBException {
        World leMonde = readWorldFromXml(username);
        updateWorld(leMonde, username);
        saveWorldToXML(username, leMonde);//eadWorldFromXml(String username);
        return leMonde;
    }
    
    public void deleteWorld(String username) throws JAXBException {
        World world = readWorldFromXml(username);
        double ange = world.getActiveangels();
        double totalAnge = world.getTotalangels();
        double score = world.getScore();
        
        double angeSup = Math.round(150 * Math.sqrt((world.getScore()) / Math.pow(10, 15))) - totalAnge;
        
        ange += ange + angeSup;
        totalAnge += ange + angeSup;
        
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
        world = (World) u.unmarshal(input);
        
        world.setActiveangels(ange);
        world.setTotalangels(totalAnge);
        world.setScore(0);

        // sauvegarder les changements du monde
        saveWorldToXML(username, world);
    }

    // prend en param??tre le pseudo du joueur et le produit
// sur lequel une action a eu lieu (lancement manuel de production ou
// achat d???une certaine quantit?? de produit)
// renvoie false si l???action n???a pas pu ??tre trait??e
    public Boolean updateProduct(String username, ProductType newproduct) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le produit ??quivalent ?? celui pass??
        // en param??tre
        ProductType product = findProductById(world, newproduct.getId());
        if (product == null) {
            return false;
        }

        // calculer la variation de quantit??. Si elle est positive c'est
        // que le joueur a achet?? une certaine quantit?? de ce produit
        // sinon c???est qu???il s???agit d???un lancement de production.
        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire de l'argent du joueur le cout de la quantit??
            // achet??e et mettre ?? jour la quantit?? de product
            int newQuantite = newproduct.getQuantite();
            double argent = world.getMoney();
            double coutProd = product.getCout();
            double prix = coutProd * (1 - Math.pow(product.getCroissance(), qtchange)) / (1 - product.getCroissance());
            double newCout = coutProd * Math.pow(product.getCroissance(), qtchange);
            double revenu = product.getRevenu();
            double newRevenu = revenu * Math.pow(product.getCroissance(),qtchange);
            double newArgent = argent - prix; 
            product.setRevenu(newRevenu);
            product.setCout(newCout);
            product.setQuantite(newQuantite);
            world.setMoney(newArgent);
        } else {
            // initialiser product.timeleft ?? product.vitesse
            // pour lancer la production
            product.setTimeleft(product.getVitesse());
            world.setMoney(world.getMoney() + (product.getRevenu() * product.getQuantite()));
        }
        //Prise en compte des upgrades
        List<PallierType> unlocks = product.getPalliers().getPallier();
        for (PallierType u : unlocks) {
            if (product.getQuantite() >= u.getSeuil() && u.isUnlocked() == false) {
                calculUpgrade(u, product);
            }
        }
        // sauvegarder les changements du monde
        saveWorldToXML(username, world);
        return true;
    }
    
    public ProductType findProductById(World world, int id) {
        ProductType idProduit = null;
        for (ProductType produit : world.getProducts().product) {
            if (id == produit.id) {
                idProduit = produit;
            }
        }
        return idProduit;
    }//Jusqu'ici tout marche
    // prend en param??tre le pseudo du joueur et le manager achet??.
// renvoie false si l???action n???a pas pu ??tre trait??e

    public Boolean updateManager(String username, PallierType newmanager) throws JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager ??quivalent ?? celui pass??
        // en param??tre
        PallierType manager = findManagerByName(world, newmanager.getName());
        if (manager == null) {
            System.out.println("manager non trouv??");
            return null;
        }

        // d??bloquer ce manager
        manager.setUnlocked(true);
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // d??bloquer le manager de ce produit
        product.setManagerUnlocked(true);
        // soustraire de l'argent du joueur le cout du manager
        double argent = world.getMoney();
        double seuil = manager.getSeuil();
        double newArgent = argent - seuil;
        world.setMoney(newArgent);
        // sauvegarder les changements au monde
        saveWorldToXML(username, world);
        return true;
    }
    
    public PallierType findManagerByName(World world, String nom) {
        PallierType nomManager = null;
        for (PallierType nomMana : world.getManagers().pallier) {
            if (nom.equals(nomMana.getName())) {
                nomManager = nomMana;
            }
        }
        return nomManager;
    }
    
    void updateWorld(World world, String username) {
        long temps = System.currentTimeMillis() - world.getLastupdate();
        List<ProductType> produits = world.getProducts().getProduct();
        for (ProductType p : produits) {
            //produits.forEach(p -> {
            //Cas manager non debloqu??
            if (p.isManagerUnlocked() == false) {
                if (p.getTimeleft() < temps && p.getTimeleft() != 0) {
                    int angeBonus = world.getAngelbonus();
                    double newScore = world.getScore() + p.getRevenu() * (1 + world.getActiveangels() * angeBonus / 100);
                    double newArgent = world.getMoney() + p.getRevenu() * (1 + world.getActiveangels() * angeBonus / 100);
                    world.setScore(newScore);
                    world.setMoney(newArgent);
                } else {
                    long newTempsRest = p.getTimeleft() - temps;
                    p.setTimeleft(newTempsRest);
                }
            } else {
                int vitesse = p.getVitesse();
                long prod = temps / vitesse;
                int angeBonus = world.getAngelbonus();
                double newScore = world.getScore() + (p.getRevenu() * prod) * (1 + world.getActiveangels() * angeBonus / 100);
                double newArgent = world.getMoney() + (p.getRevenu() * prod) * (1 + world.getActiveangels() * angeBonus / 100);
                world.setScore(newScore);
                world.setMoney(newArgent);
                long tempsRestant = vitesse - temps % vitesse;
                p.setTimeleft(tempsRestant);
            }
        }
        world.setLastupdate(System.currentTimeMillis());
    }
    
    public Boolean updateUpgrade(String username, PallierType newupgrade) throws JAXBException {
        World world = getWorld(username);
        // trouver dans ce monde, le manager ??quivalent ?? celui pass??
        // en param??tre
        PallierType upgrade = findUpgradeByName(world, newupgrade.getName());
        if (upgrade == null) {
            return false;
        }

        // d??bloquer ce manager
        upgrade.setUnlocked(true);
        // trouver le produit correspondant au manager
        ProductType product = findProductById(world, upgrade.getIdcible());
        if (product == null) {
            return false;
        }
        // soustraire de l'argent du joueur le cout du manager
        double argent = world.getMoney();
        double seuil = upgrade.getSeuil();
        double newArgent = argent - seuil;
        world.setMoney(newArgent);
        calculUpgrade(upgrade, product);
        // sauvegarder les changements au monde
        saveWorldToXML(username, world);
        return true;
    }
    
    public PallierType findUpgradeByName(World world, String nom) {
        PallierType nomUpgrade = null;
        for (PallierType nomUp : world.getUpgrades().pallier) {
            if (nom.equals(nomUp.getName())) {
                nomUpgrade = nomUp;
            }
        }
        return nomUpgrade;
    }
    
    public void calculUpgrade(PallierType u, ProductType product) {
        u.setUnlocked(true);
        if (u.getTyperatio() == TyperatioType.VITESSE) {
            int vitesse = product.getVitesse();
            double newVitesse = vitesse * u.getRatio();
            product.setVitesse((int) newVitesse);
        }
        if (u.getTyperatio() == TyperatioType.GAIN) {
            double revenu = product.getRevenu();
            double newRevenu = revenu * u.getRatio();
            product.setRevenu(newRevenu);
        }
        
    }
    
    public Boolean updateAngel(String username, PallierType angel) throws JAXBException {
        World world = getWorld(username);
        PallierType ange = findAngelByName(world, angel.getName());
        if (ange == null) {
            return false;
        }

        // d??bloquer cet ange
        ange.setUnlocked(true);
        double totalAnge = world.getTotalangels();
        int anges = ange.getSeuil();
        double newAnge = totalAnge - anges;
        if (ange.getTyperatio() == TyperatioType.ANGE) {
            int angeBonus = world.getAngelbonus();
            angeBonus += ange.getRatio();
            world.setAngelbonus(angeBonus);
            
        } else {
            updateUpgrade(username, ange);
            
        }
        world.setMoney(0);
        world.setScore(0);
        world.setActiveangels(newAnge);
        // sauvegarder les changements au monde
        saveWorldToXML(username, world);
        return true;
    }
    
    public PallierType findAngelByName(World world, String nom) {
        PallierType ange = null;
        for (PallierType nomAnge : world.getAngelupgrades().pallier) {
            if (nom.equals(nomAnge.getName())) {
                ange = nomAnge;
            }
        }
        return ange;
    }
     
}
