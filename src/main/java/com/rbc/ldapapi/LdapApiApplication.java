package com.rbc.ldapapi;

import com.github.javafaker.Faker;
import com.rbc.ldapapi.model.User;
import com.rbc.ldapapi.service.UserService;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
public class LdapApiApplication implements CommandLineRunner {

	@Autowired
	UserService userService;

	public static void main(String[] args) {

		SpringApplication.run(LdapApiApplication.class, args);
	}

	@Override
	public void run(String... args) {


		long startTime = System.nanoTime();
		//Faker faker = new Faker();
		//this.createFakeUsers(faker,20000);
		//this.exportUsers();

		//this.updateUsers();

		this.restoreUsers();

		long endTime = System.nanoTime();

		long duration = (endTime - startTime)/1000000;  //divide by 1000000 to get milliseconds

		System.out.println(duration);
	}

	public void createFakeUsers(Faker faker, Integer numberOfUsers){
		for(Integer count=0; count <numberOfUsers; count ++){
			createFakeUser(faker);
			if(count % 100==0){
				System.out.println(String.format("[%s] - created %d users", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")), count));
			}
		}
	}

	public void createFakeUser(Faker faker){

		User user= User.Create(UUID.randomUUID(), faker.name().firstName(), faker.name().lastName(), "itIsASecret");

		try {
			userService.create(user);
		} catch (LdapException e) {
			e.printStackTrace();
		}

	}

	public void exportUsers(){
		Charset charset = Charset.forName("UTF-8");
		Path filePath = Paths.get("src/main/resources", "backup.ldif");
		BufferedWriter writer = null;

		try {
			writer = Files.newBufferedWriter(filePath, charset);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			userService.exportTree( writer);
		} catch (CursorException e) {
			e.printStackTrace();
		} catch (LdapException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateUsers(){
		try {
			InputStream is = new FileInputStream("src/main/resources/backup.ldif");
			String[] attributeNames= {"cn","sn","userPassword"};

			Map<String,String> map = new HashMap<String, String>();
			//map.put("cn", "common-name");
			map.put("sn","surname");
			//map.put("userPassword", "userPassword");

			userService.updateTree(is, map);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void restoreUsers(){
		try {
			InputStream is = new FileInputStream("src/main/resources/backup.ldif");
			String[] attributeNames= {"sn"};


			userService.importTree(is, attributeNames);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
