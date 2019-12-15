package com.ecommerce.microcommerce.web.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Api( description="API pour es opÃ©rations CRUD sur les produits.")

@RestController
public class ProductController {

    @Autowired
    private ProductDao productDao;


    //RÃ©cupÃ©rer la liste des produits

    @RequestMapping(value = "/Produits", method = RequestMethod.GET)

    public MappingJacksonValue listeProduits() {

        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }


    //RÃ©cupÃ©rer un produit par son Id
    @ApiOperation(value = "RÃ©cupÃ¨re un produit grÃ¢ce Ã  son ID Ã  condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")

    public MappingJacksonValue afficherUnProduit(@PathVariable int id) {

        Product produit = productDao.findById(id);

        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Ã‰cran Bleu si je pouvais.");
        
        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
        MappingJacksonValue produitFiltres = new MappingJacksonValue(produit);
        produitFiltres.setFilters(listDeNosFiltres);

        
        return produitFiltres;
    }

    //ajouter un produit
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {
    	
    	// si le prix de vente du produit ajouté est de 0, on lance une exception
        if(product.getPrix() == 0)  throw new ProduitGratuitException("Attention: Le prix du produit doit étre supérieur à 0 ");

        Product productAdded =  productDao.save(product);

        if (productAdded == null)
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {

        productDao.delete(id);
    }

    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {

        productDao.save(product);
    }


    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {

        return productDao.chercherUnProduitCher(prix);
    }

    // La liste des produits affichée avec la marge entre le prix d'achat et le prix de vente
    @GetMapping(value="/AdminProduits")
    public MappingJacksonValue calculerMargeProduit() {
    	
        // Liste des produits avec la marge
    	List<String> produits_marge = new ArrayList<String>();
    	// recuperer la liste des produits
    	List<Product> produits = productDao.findAll();

    	int marge;
    	
    	for(Product produit : produits) {
    		
    		// calcule de la marge
    		marge = produit.getPrix() - produit.getPrixAchat();
    		
    		String mProduit = produit.toString() + ": "+marge;
    		
    		produits_marge.add(mProduit);
    	}
    	
    	 // Ajout d'un filtre dynamique pour ne pas afficher le prixAchat
    	 SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

         FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

         MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits_marge);

         produitsFiltres.setFilters(listDeNosFiltres);
    	 
    	 
    	return produitsFiltres;
    }
    
    // Afficher la liste des produits triés par ordre alphabetique
    @GetMapping(value="/ProduitsTrie")
    public MappingJacksonValue trierProduitsParOrdreAlphabetique() {

    	 List<Product> produits = productDao.findAllByOrderByNomAsc();
    	 
    	 SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("");
    	 FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
    	 
    	 MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);
    	 produitsFiltres.setFilters(listDeNosFiltres);
    	 return produitsFiltres;
    }
    

}
