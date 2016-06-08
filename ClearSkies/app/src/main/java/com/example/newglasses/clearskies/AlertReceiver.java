package com.example.newglasses.clearskies;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by newglasses on 06/06/2016.
 */
public class AlertReceiver extends BroadcastReceiver {

    private String weatherFinal = "";
    private String auroraForecast = "";

    // Called when a broadcast is made targeting this class
    @Override
    public void onReceive(Context context, Intent intent) {


        // start file service
        startFileService(context);

        auroraForecast = auroraForecast(context);

        startWeatherService(context);

        try {
            weatherFinal = weatherForecast(context);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createNotification(context, weatherFinal, auroraForecast, "Alert");

    }

    public void startFileService(Context context) {

        // Create an intent to run the IntentService in the background
        Intent auroraIntent = new Intent(context, XMLFileService.class);

        // Pass the URL that the IntentService will download from
        auroraIntent.putExtra("url", "http://aurorawatch.lancs.ac.uk/api/0.1/status.xml");

        // Start the intent service
        context.startService(auroraIntent);

    }

    public void startWeatherService(Context context) {

        // Create an intent to run the IntentService in the background
        Intent weatherIntent = new Intent(context, JSONFileService.class);
        // add two hours in millisecs = 7,200,000
        long unixTime = (System.currentTimeMillis() + 7200000) / 1000L;

        // String weatherQuery = "https://api.forecast.io/forecast/57e606614d55dbee13c97a1736097f91/54.640891,-5.941169100000025,";
        // String weatherQueryFinal = weatherQuery + unixTime;

        // Pass the URL that the IntentService will download from
        weatherIntent.putExtra("url", "https://api.forecast.io/forecast/57e606614d55dbee13c97a1736097f91/54.640891,-5.941169100000025,1464570000");

        // Start the intent service
        context.startService(weatherIntent);

    }

    public void createNotification(Context context, String msg, String msgText, String msgAlert){

        // Define an Intent and an action to perform with it by another application
        PendingIntent notificIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, Main.class), 0);

        // Builds a notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(msg)
                        .setTicker(msgAlert)
                        .setContentText(msgText);

        // Defines the Intent to fire when the notification is clicked
        mBuilder.setContentIntent(notificIntent);

        // Set the default notification option
        // DEFAULT_SOUND : Make sound
        // DEFAULT_VIBRATE : Vibrate
        // DEFAULT_LIGHTS : Use the default light notification
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);

        // Auto cancels the notification when clicked on in the task bar
        mBuilder.setAutoCancel(true);

        // Gets a NotificationManager which is used to notify the user of the background event
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Post the notification
        mNotificationManager.notify(1, mBuilder.build());

    }
    // See more at: http://www.newthinktank.com/2014/12/make-android-apps-19/#sthash.qQhbHKzz.dpuf

    public String weatherForecast(Context context) throws JSONException, IOException {

        // Will build the String from the local file
        StringBuilder sb = new StringBuilder();

        try {
            // Opens a stream so we can read from our local file
            FileInputStream fis = context.openFileInput("myJSONFile");

            // Gets an input stream for reading data
            // InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            // Used to read the data in small bytes to minimize system load
            BufferedReader bufferedReader = new BufferedReader(isr);

            if (bufferedReader != null) {
                int cp;
                while ((cp = bufferedReader.read()) != -1) {
                    sb.append((char) cp);
                }
                bufferedReader.close();
            }

            /*

            // Read the data in bytes until nothing is left to read
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            */


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject object = new JSONObject(sb.toString());
        JSONObject currently = object.getJSONObject("currently");
        String summary = currently.getString("summary");
        String visibility = currently.getString("visibility");
        String weatherCombined = summary + " " + visibility;
        return weatherCombined;
    }

    // Will read our local file and put the text in the EditText
    public String auroraForecast(Context context){

        // Will build the String from the local file
        String forecast = "";

        // Array to hold parsed XML document
        String [] xmlPullParserArray = new String[6];

        int parserArrayIncrement = 0;

        try {
            // Opens a stream so we can read from our local file
            FileInputStream fis = context.openFileInput("myXMLFile");

            // Gets an input stream for reading data
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

            // Used to read the data in small bytes to minimize system load
            BufferedReader bufferedReader = new BufferedReader(isr);

            // Create pull parser to parse XML documents
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

            // parser supports XML namespaces
            factory.setNamespaceAware(true);

            // provides the methods needed to parse XML documents
            XmlPullParser parser = factory.newPullParser();

            // InputStreamReader converts bytes of data into a stream of characters
            parser.setInput(bufferedReader);

            // Passes the parser and the first tag into the XML document for processing
            beginDocument(parser, "aurorawatch");

            // Get the currently targeted event type, which starts as START_DOCUMENT
            int eventType;

            do {
                // Cycles through elements in the XML document while neither a start nor
                // end tag are found
                nextElement(parser);

                // Switch to the next element
                parser.next();

                // Get the current event type
                eventType = parser.getEventType();

                // Check if a value was found between 2 tags

                if (eventType == XmlPullParser.TEXT){

                    // Get the text from between the tags

                    String valueFromXML = parser.getText();

                    // Store it in an array with the corresponding tag value
                    xmlPullParserArray[parserArrayIncrement] = valueFromXML;
                    parserArrayIncrement++;

                }

            } while (eventType != XmlPullParser.END_DOCUMENT);

            forecast = xmlPullParserArray[1];

            /*
            // Read the data in bytes until nothing is left to read
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }


            // Put downloaded text into the EditText
            //textView.setText(sb.toString());

            //textView.setText(xmlPullParserArray[1]);

            sb = new StringBuilder();
            for (int i=0; i <xmlPullParserArray.length; i++) {
                sb.append(xmlPullParserArray[i]).append(" " + i).append("\n");
            }

            textView.setText(sb.toString());
            */


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return forecast;
    }
    // See more at: http://www.newthinktank.com/2014/12/make-android-apps-18/#sthash.BJ58qBzp.dpuf

    public final void beginDocument (XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {

        int type;

        // next() advances to the next element in the XML document being a starting or ending tag, or
        // a value or the END_DOCUMENT

        while ((type=parser.next()) != parser.START_TAG && type != parser.END_DOCUMENT) {;}

        // Throw an error if a start tag isn't found

        if (type != parser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        // Verify that the tag passed in is the first tag in the XML document

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() + ", expected " + firstElementName);
        }
    }

    public final void nextElement (XmlPullParser parser) throws XmlPullParserException, IOException {

        int type;

        // Cycles through elements in the XML document while neither a start nor end tag are found

        while ((type = parser.next()) != parser.START_TAG && type != parser.END_DOCUMENT) {
            ;
        }
    }



}
