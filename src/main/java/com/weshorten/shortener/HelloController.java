package com.weshorten.shortener;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.xml.ws.Response;
import javax.websocket.server.PathParam;
//import javax.ws.rs.core.Response;
import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import com.google.gson.Gson;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.common.hash.Hashing;



@RestController
public class HelloController {

	@RequestMapping(
		value = "/hello", 
		method = RequestMethod.GET, 
		produces = "application/json"
	  )
	  @ResponseBody
	  public String getFoosAsJsonFromREST() {
		  return "Get some Fools";
	  }


	  	//b
	@RequestMapping(
		value = "/create", 
		method = RequestMethod.POST)	 
		@ResponseBody
		public String create(@RequestBody String url) {
        String urlID = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
 
        Map<String, String> docData = new HashMap<>();
        docData.put("lUrl", url);
        Firestore dbFireStore = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> collectionsApiFuture = dbFireStore.collection("common").document(urlID).set(docData);
        return urlID;
	
		}

  //a
	@RequestMapping(
		value = "/createWithAuth", 
		method = RequestMethod.POST)	 
	@ResponseBody
	public String createWithAuth( @RequestHeader(value="userKey") String userKey, @RequestBody String url) {
		
		String uid = ""+userKey;
	
		 String urlID = Hashing.murmur3_32().hashString(url, StandardCharsets.UTF_8).toString();
		final Map<String, Object> docData = new HashMap<>();
		docData.put(urlID, url);
		final Map<String, Object> docDataForCommon = new HashMap<>();
		docDataForCommon.put("lUrl", url);
		final Firestore dbFireStore = FirestoreClient.getFirestore();
		final ApiFuture<WriteResult> collectionsApiFuture = dbFireStore.collection("users").document(uid).set(docData, SetOptions.merge());

		final ApiFuture<WriteResult> collectionsApiFutureForCommon = dbFireStore.collection("common").document(urlID).set(docDataForCommon);
		
		return urlID;
	}
	
	//c
	@RequestMapping(
	value = "/createCustomWithAuth", 
	method = RequestMethod.POST)	 
	@ResponseBody
	public String createCustomWithAuth(@RequestHeader("userKey") String userKey, @RequestHeader("custom") String custom ,  @RequestBody String url)throws ExecutionException, InterruptedException {	
	String uid = ""+userKey;
	String urlID = ""+custom;

	//write data
	Map<String, Object> docData = new HashMap<>();
	docData.put(urlID, url);
	Map<String, Object> docDataForCommon = new HashMap<>();
	docDataForCommon.put("lUrl", url);
	Firestore dbFireStore = FirestoreClient.getFirestore();


	DocumentReference docRef = dbFireStore.collection("common").document(urlID);
	ApiFuture<DocumentSnapshot> future = docRef.get();
	// block on response
	DocumentSnapshot document = future.get();
	if (document.exists()) {
	  // convert document to POJO
	  return "exists";
	} else {
		ApiFuture<WriteResult> collectionsApiFutureForCommon = dbFireStore.collection("common").document(urlID).set(docDataForCommon);
		ApiFuture<WriteResult> collectionsApiFuture = dbFireStore.collection("users").document(uid).set(docData, SetOptions.merge());
		return urlID;
	}


	}
    
	//retrieve/id
	@RequestMapping(
		value = "/r/{id}", 
		method = RequestMethod.GET)	 
		@ResponseBody
		//@PathVariable
		public RedirectView retrieve(@PathVariable("id") String id){
			String url = "";
			Firestore dbFireStore = FirestoreClient.getFirestore();
			DocumentReference documentReference = dbFireStore.collection("common").document(id);
			
			ApiFuture<DocumentSnapshot> future = documentReference.get();
			DocumentSnapshot document = null;
			try {
				document = future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			if (document != null) {
				 url = document.get("lUrl").toString();
				 System.out.println(url);
				
			}
			RedirectView redirectView = new RedirectView();
			redirectView.setUrl(url);
			return redirectView;
		}
	//all
	@RequestMapping(
		value = "/all", 
		method = RequestMethod.GET,
		produces = "application/json")
	

		@ResponseBody
		
		public ResponseEntity<String> allWithAuth(@RequestHeader(value="userKey") String userKey){
        if (userKey==null){
		 	return new ResponseEntity<>(HttpStatus.NOT_FOUND);
           
        }
        String uid = userKey;
        Firestore dbFireStore = FirestoreClient.getFirestore();
        DocumentReference documentReference = dbFireStore.collection("users").document(""+uid);
        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document = null;
        try {
            document = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (document.exists()) {
            Object allData = document.getData();
            Gson gson = new Gson();
			String jsonString = gson.toJson(allData);
			return new ResponseEntity<>(jsonString, HttpStatus.OK);
            
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
		}
}
