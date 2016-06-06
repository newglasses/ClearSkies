package com.example.newglasses.clearskies;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by newglasses on 06/06/2016.
 */
public class JSONFileService extends IntentService {


    String summary;
    String visibility;


    // Used to identify when the IntentService finishes
    public static final String JSON_TRANSACTION_DONE = "com.example.newglasses.JSON_TRANSACTION_DONE";

    // Validates resource references inside Android XML files
    public JSONFileService() {
        super(JSONFileService.class.getName());
    }


    public JSONFileService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.e("JSONFileService", "Service Started");

        // Get the URL for the file to download
        String passedURL = intent.getStringExtra("url");

        try {
            downloadFile(passedURL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("JSONFileService", "Service Stopped");

        // Broadcast an intent back to Main when file is downloaded
        Intent i = new Intent(JSON_TRANSACTION_DONE);
        JSONFileService.this.sendBroadcast(i);

    }

    protected void downloadFile(String theURL) throws IOException {

        // The name for the file we will save data to
        String fileName = "myJSONFile";

        try {

            Log.e("inside fileService", "url: " + theURL);

            // Create an output stream to write data to a file (private to everyone except our app)
            FileOutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            // Get File
            URL fileURL = new URL(theURL);

            // Create a connection we can use to read data from a url
            URLConnection urlConnection = fileURL.openConnection();

            if (urlConnection != null)
                urlConnection.setReadTimeout(60 * 1000);
            if (urlConnection != null && urlConnection.getInputStream() != null) {
                InputStream in = urlConnection.getInputStream();

                // Define the size of the buffer
                byte[] buffer = new byte[1024];
                int bufferLength = 0;

                // read reads a byte of data from the stream until there is nothing more
                while ((bufferLength = in.read(buffer)) > 0) {

                    // Write the data received to our file
                    outputStream.write(buffer, 0, bufferLength);

                    Log.e("inside JSONFileService", "buffer length: " + bufferLength);

                }

                // Close our connection to our file
                outputStream.close();

                // Get File Done
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // downloadFile

}
