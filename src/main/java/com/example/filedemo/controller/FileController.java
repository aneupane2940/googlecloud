package com.example.filedemo.controller;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.filedemo.payload.UploadFileResponse;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);



    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) throws FileNotFoundException, IOException {
        //String fileName = fileStorageService.storeFile(file);
        
		Credentials credentials = GoogleCredentials
				.fromStream(new FileInputStream("/Users/achyutneupane/Desktop/google.json"));

		Storage storage = StorageOptions.newBuilder().setCredentials(credentials)
				.setProjectId("striking-scout-224000").build().getService();

		//Bucket bucket = storage.create(BucketInfo.of("achyut-123/test/anotherTest/wow"));

		Bucket bucket = storage.get("achyut-123");

		//String targetFileStr = "/Users/achyutneupane/hwyHaul/hwyhaul-backend/output_1543449038984_.pdf";
		// BlobId blobId = BlobId.of("achyut-123", "output.pdf");
		// Blob blob = bucket.create("my_blob_name", "a simple blob".getBytes("UTF-8"),
		// "text/plain");
		// Blob blob = bucket.create("my_blob_name", targetFileStr.getBytes("UTF-8"),
		// "text/pdf");

		Blob blob = bucket.create("appUpload/"+file.getName(), file.getInputStream());
		Acl acl = blob.createAcl(Acl.of(User.ofAllUsers(), Acl.Role.READER));
		
		System.out.println(blob.getMediaLink());
		

        //String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
        //        .path("/downloadFile/")
        //        .path(fileName)
        //        .toUriString();

        return new UploadFileResponse(file.getName(), blob.getMediaLink(),
                file.getContentType(), file.getSize());
    }

    @GetMapping("/downloadFile/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws FileNotFoundException, IOException {
        // Load file as Resource
        //Resource resource = fileStorageService.loadFileAsResource(fileName);
        
    	Storage storage = StorageOptions.newBuilder().setProjectId("striking-scout-224000")
				.setCredentials(
						GoogleCredentials.fromStream(new FileInputStream("/Users/achyutneupane/Desktop/google.json")))
				.build().getService();
		Blob blob = storage.get("achyut-123", "appUpload/"+fileName);
		
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(new InputStreamResource(new ByteArrayInputStream(blob.getContent())));
    }

}
