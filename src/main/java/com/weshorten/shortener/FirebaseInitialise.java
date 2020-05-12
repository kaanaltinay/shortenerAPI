package com.weshorten.shortener;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.FileInputStream;

//@ApplicationScoped
@Service
public class FirebaseInitialise {

    //@PostConstruct
    @PostConstruct
    public void initialise() {
        try {
            FileInputStream serviceAccount =

                    new FileInputStream("C:/Users/kaanl/Desktop/shortener/src/serviceAccount.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://url-short-dabac.firebaseio.com")
                    .build();

            FirebaseApp.initializeApp(options);
            

        } catch (Exception e) {
            System.out.println("Error here **************************");
            e.printStackTrace();

        }
    }
}