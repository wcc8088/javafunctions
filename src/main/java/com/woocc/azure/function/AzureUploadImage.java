package com.woocc.azure.function;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.BasicConfigurator;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Azure Functions with HTTP Trigger.
 */
public class AzureUploadImage {
    /**
     * This function listens at endpoint "/api/ImageUpload". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/ImageUpload 2. curl {your host}/api/ImageUpload?name=HTTP%20Query
     */
    @FunctionName("UploadImage")
	public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
			HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<byte[]>> request,
			final ExecutionContext context) throws IOException {
        final String storageConnectionString ="DefaultEndpointsProtocol=http;AccountName=wooccstorage;AccountKey=qRM2Cpcx8AuQkJiVHFaXIAeix5TVgBaAQ/yD9OfZyloVzZKjX6gH154zB4jS6900OPBeZ6mP3tw7yjWJgB4NKw==";
        final byte[] body = request.getBody().orElseThrow(() -> new IllegalArgumentException("No content attached"));
        final String contentType = request.getHeaders().get("content-type");

        BasicConfigurator.configure();
        InputStream in = new ByteArrayInputStream(body); // Convert body to an input stream
        String boundary = contentType.split(";")[1].split("=")[1]; // Get boundary from content-type header
        int bufSize = 1024;
        MultipartStream multipartStream = new MultipartStream(in, boundary.getBytes(), bufSize, null); 
        boolean nextPart = multipartStream.skipPreamble();
        String filename = null;
        String blobname = null;
        long now = System.currentTimeMillis();
        CloudBlobContainer container = null;
        StringBuffer html = new StringBuffer();

        try{
            CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);
            CloudBlobClient serviceClient = account.createCloudBlobClient();

            // Container name must be lower case.
            container = serviceClient.getContainerReference("upload");
            container.createIfNotExists();

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
                    filename = header.substring(header.indexOf("filename=") + "filename=".length() + 1, header.indexOf("\r\n") - 1);
                    context.getLogger().info("Filename : " + filename);
                    context.getLogger().info("BAOS Size : " + baos.size());
                    blobname = filename.replaceFirst("[.]", "_"+now+".");

                    CloudBlockBlob blob = container.getBlockBlobReference(blobname);
                    ByteArrayInputStream inputstream = new ByteArrayInputStream(byteArray);
                    blob.upload(inputstream, byteArray.length);
                    baos.flush();
                    context.getLogger().info("Blob name : " + blob.getName());
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

        html.append("<html>");
        html.append("<img width=450 src=\"http://wooccstorage.blob.core.windows.net/upload/"+blobname+"\">");
        html.append("</html>");

        if (filename == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            request.createResponseBuilder(HttpStatus.OK).header("Content-Type", "application/json");
            return request.createResponseBuilder(HttpStatus.OK).body(html.toString()).build();
        }
    }
}
