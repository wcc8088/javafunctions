package com.woocc.azure.function;

<<<<<<< HEAD
import java.util.Optional;

=======
import java.io.File;
import java.io.FileReader;
import java.util.Optional;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
>>>>>>> fc93517d18402f739f3d8b746a72afbcd1b57b1a
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
<<<<<<< HEAD
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
=======

import org.apache.log4j.BasicConfigurator;
>>>>>>> fc93517d18402f739f3d8b746a72afbcd1b57b1a

public class AzureMailAddFunction {
    /**
     * This function listens at endpoint "/api/hello". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/hello
     * 2. curl {your host}/api/hello?name=HTTP%20Query
     */
    @FunctionName("MailAdd")
    public HttpResponseMessage run(
<<<<<<< HEAD
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request. -- MailAdd -- ");

        // Parse query parameter
        String query = request.getQueryParameters().get("inputEmail");
        String mailAddr = request.getBody().orElse("inputEmail");
        mailAddr = mailAddr.replace("%40", "@");
        mailAddr = mailAddr.substring(mailAddr.indexOf('=')+1);
        context.getLogger().info("Add Mail : " + mailAddr);
        final String storageConnectionString ="DefaultEndpointsProtocol=http;" + "AccountName=wooccstorage;" + "AccountKey=qRM2Cpcx8AuQkJiVHFaXIAeix5TVgBaAQ/yD9OfZyloVzZKjX6gH154zB4jS6900OPBeZ6mP3tw7yjWJgB4NKw==";
        try {
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            // Container name must be lower case.
            CloudBlobContainer container = serviceClient.getContainerReference("$web");
            container.createIfNotExists();

            // Upload an image file.
            CloudBlockBlob blob = container.getBlockBlobReference("maillist.html");
            String mailTxt = blob.downloadText();
            mailTxt = mailTxt + "\n<br>" + mailAddr;
            blob.uploadText(mailTxt);
        }
        catch (StorageException storageException) {
            context.getLogger().info(storageException.getMessage());
            System.exit(-1);
        }
        catch (Exception e) {
            context.getLogger().info(e.getMessage());
            System.exit(-1);
=======
        @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request. -- MailAdd -- ");

        // Parse query parameter
        String mailAddr = request.getQueryParameters().get("inputEmail");
        mailAddr = mailAddr.replace("%40", "@");
        mailAddr = mailAddr.substring(mailAddr.indexOf('=')+1);
        BasicConfigurator.configure();
        context.getLogger().info("Add Mail : " + mailAddr);
        
        try {
            String accountName = "wooccstorage";
            String accountKey = "qRM2Cpcx8AuQkJiVHFaXIAeix5TVgBaAQ/yD9OfZyloVzZKjX6gH154zB4jS6900OPBeZ6mP3tw7yjWJgB4NKw==";
            StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
            BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint("https://wooccstorage.blob.core.windows.net/").credential(credential).buildClient();
            BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient("www");
            BlobClient blobClient = blobContainerClient.getBlobClient("maillist.html");
            BlockBlobClient blockBlobClient = blobClient.getBlockBlobClient();
            File localFile = new File("/tmp/tmpblob");
            blockBlobClient.downloadToFile("/tmp/tmpblob");
            context.getLogger().info("File downloaded.");
            FileReader reader = new FileReader(localFile);
            context.getLogger().info("File contents : " + reader.toString());
            localFile.delete();
            reader.close();
/*            
            FileWriter writer = new FileWriter(localFile, true);
            writer.write(mailAddr);
            writer.close();
            blobClient.uploadFromFile("/tmp/tmpblob",true);
            context.getLogger().info("File uploaded.");
            localFile.delete();
*/
        }
        catch (Exception e) {
            context.getLogger().info(e.getMessage());
>>>>>>> fc93517d18402f739f3d8b746a72afbcd1b57b1a
        }

        if (mailAddr == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body(mailAddr + " - Mail Added.").build();
        }
    }
}
