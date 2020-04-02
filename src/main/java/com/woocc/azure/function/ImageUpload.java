package com.woocc.azure.function;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.log4j.BasicConfigurator;

/**
 * Azure Functions with HTTP Trigger.
 */
public class ImageUpload {
    /**
     * This function listens at endpoint "/api/ImageUpload". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/ImageUpload 2. curl {your host}/api/ImageUpload?name=HTTP%20Query
     */
    @FunctionName("ImageUpload")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws IOException {
        String contentType = request.getHeaders().get("content-type"); // Get content-type header
        // here the "content-type" must be lower-case
        String body = request.getBody().get(); // Get request body

        BasicConfigurator.configure();
        InputStream in = new ByteArrayInputStream(body.getBytes()); // Convert body to an input stream
        String boundary = contentType.split(";")[1].split("=")[1]; // Get boundary from content-type header
        int bufSize = 1024;
        MultipartStream multipartStream = new MultipartStream(in, boundary.getBytes(), bufSize, null); 
        boolean nextPart = multipartStream.skipPreamble();

        String accountName = "wooccstorage";
        String accountKey = "qRM2Cpcx8AuQkJiVHFaXIAeix5TVgBaAQ/yD9OfZyloVzZKjX6gH154zB4jS6900OPBeZ6mP3tw7yjWJgB4NKw==";
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
        BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint("https://wooccstorage.blob.core.windows.net/").credential(credential).buildClient();
        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient("www");
        BlockBlobClient blobClient = blobContainerClient.getBlobClient("han.jpeg").getBlockBlobClient();
        
        try{
            while(nextPart) {
                String header = multipartStream.readHeaders();
                context.getLogger().info("Stream Start");
                context.getLogger().info("Headers:");
                context.getLogger().info(header);
                context.getLogger().info("Body:");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                multipartStream.readBodyData(baos);
                byte[] byteArray = null;
                byteArray = baos.toByteArray();

                if (header.indexOf("Content-Disposition: form-data; name=\"upfile\"") >= 0) {
                    context.getLogger().info("Filename : " + header.substring(header.indexOf("filename")+10, header.length()-3));
                    context.getLogger().info("BAOS Size : " + baos.size());
                    ByteArrayInputStream inputstream = new ByteArrayInputStream(byteArray);
                    blobClient.upload(inputstream, byteArray.length);
                    baos.flush();
                    context.getLogger().info("Blob name : " + blobClient.getBlobName());
                    baos.close();
                    inputstream.close();
                    // To upload image byte array to Blob Storage
                    // You can get the upload image filename from the form input `note`, please notes the order of form input elements.
                }
                context.getLogger().info("Stream End");
                nextPart = multipartStream.readBoundary();
            }
        } catch (Exception exception)
        {
            context.getLogger().info("Exception : " + exception);
        }

        return request.createResponseBuilder(HttpStatus.OK).body("Success").build();
    }
}
