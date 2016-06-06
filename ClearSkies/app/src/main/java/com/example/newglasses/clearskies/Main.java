package com.example.newglasses.clearskies;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import java.util.GregorianCalendar;

public class Main extends AppCompatActivity {

    Button showNotificationBut, stopNotificationBut, alertButton;

    TextView textViewAurora, textViewWeather;

    // Allows us to notify the user that something happened in the background
    NotificationManager notificationManager;

    // Used to track notifications
    int notifID = 33;

    // Used to track if notification is active in the task bar
    boolean isNotificActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        showNotificationBut = (Button) findViewById(R.id.show);
        stopNotificationBut = (Button) findViewById(R.id.stop);
        alertButton = (Button) findViewById(R.id.alert);
        textViewAurora = (TextView) findViewById(R.id.textAurora);
        textViewWeather = (TextView) findViewById(R.id.textWeather);

        // Allows use to track when an intent with the id TRANSACTION_DONE is executed
        // We can call for an intent to execute something and then tell use when it finishes
        //IntentFilter intentFilterAurora = new IntentFilter();
        //intentFilterAurora.addAction(FileService.TRANSACTION_DONE);

        IntentFilter intentFilterWeather = new IntentFilter();
        intentFilterWeather.addAction(JSONFileService.JSON_TRANSACTION_DONE);

        // Prepare the main thread to receive a broadcast and act on it
        //registerReceiver(downloadReceiverAurora, intentFilterAurora);

        registerReceiver(downloadReceiverWeather, intentFilterWeather);

        setAlarm(textViewAurora, textViewWeather);

    }

    // Is alerted when the IntentService broadcasts TRANSACTION_DONE
    private BroadcastReceiver downloadReceiverAurora = new BroadcastReceiver() {

        // Called when the broadcast is received
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e("XMLFileService", "Service Received");

            showFileContents();

        }
    };
    // Is alerted when the IntentService broadcasts TRANSACTION_DONE
    private BroadcastReceiver downloadReceiverWeather = new BroadcastReceiver() {

        // Called when the broadcast is received
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e("JSONFileService", "Service Received");

            try {
                showWeatherContents();
            } catch (JSONException e) {
                e.getMessage();
            }

        }
    };

    public void showWeatherContents() throws JSONException {

        // Will build the String from the local file
        StringBuilder sb = new StringBuilder();

        try {
            // Opens a stream so we can read from our local file
            FileInputStream fis = this.openFileInput("myJSONFile");

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

            // Put downloaded text into the EditText
            // downloadedEditText.setText(sb.toString());

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

        textViewWeather.setText(weatherCombined);

    }

    // Will read our local file and put the text in the EditText
    public void showFileContents(){

        // Will build the String from the local file
        StringBuilder sb;

        // Array to hold parsed XML document
        String [] xmlPullParserArray = new String[6];

        int parserArrayIncrement = 0;

        try {
            // Opens a stream so we can read from our local file
            FileInputStream fis = this.openFileInput("myXMLFile");

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

            /*
            // Read the data in bytes until nothing is left to read
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            */

            // Put downloaded text into the EditText
            //textView.setText(sb.toString());

            //textView.setText(xmlPullParserArray[1]);

            sb = new StringBuilder();
            for (int i=0; i <xmlPullParserArray.length; i++) {
                sb.append(xmlPullParserArray[i]).append(" " + i).append("\n");
            }

            textViewAurora.setText(sb.toString());


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
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

    public void showNotification(View view) {

        // Builds a notification
        NotificationCompat.Builder notificBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Message")
                .setContentText("New Message")
                .setTicker("Alert New Message")
                .setSmallIcon(R.mipmap.ic_launcher);

        // Define that we have the intention of opening MoreInfoNotification
        Intent moreInfoIntent = new Intent(this, MoreInfoNotification.class);

        // Used to stack tasks across activities so we go to the proper place when back is clicked
        TaskStackBuilder tStackBuilder = TaskStackBuilder.create(this);

        // Add all parents of this activity to the stack
        tStackBuilder.addParentStack(MoreInfoNotification.class);

        // Add our new Intent to the stack
        tStackBuilder.addNextIntent(moreInfoIntent);

        // Define an Intent and an action to perform with it by another application
        // FLAG_UPDATE_CURRENT : If the intent exists keep it but update it if needed
        PendingIntent pendingIntent = tStackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Defines the Intent to fire when the notification is clicked
        notificBuilder.setContentIntent(pendingIntent);

        // Gets a NotificationManager which is used to notify the user of the background event
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Post the notification
        notificationManager.notify(notifID, notificBuilder.build());

        // Used so that we can't stop a notification that has already been stopped
        isNotificActive = true;


    }

    public void stopNotification(View view) {

        // If the notification is still active close it
        if(isNotificActive) {
            notificationManager.cancel(notifID);
        }

    }

    public void setAlarm(View aurora, View weather) {

        // Define a time value of 5 seconds
        Long alertTime = new GregorianCalendar().getTimeInMillis()+5*1000;
        // Long intervalTime = Long.valueOf(5000);

        Intent fileServiceIntent = new Intent (this, XMLFileService.class);

        // Pass the URL that the IntentService will download from
        fileServiceIntent.putExtra("url", "http://aurorawatch.lancs.ac.uk/api/0.1/status.xml");

        // Start the intent service
        //this.startService(fileServiceIntent);

        // Define our intention of executing AlertReceiver
        Intent alertIntent = new Intent(this, AlertReceiver.class);

        // Allows you to schedule for your application to do something at a later date
        // even if it is in the background or isn't active
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // set() schedules an alarm to trigger
        // Trigger for alertIntent to fire in 5 seconds
        // FLAG_UPDATE_CURRENT : Update the Intent if active
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));



    }
    // See more at: http://www.newthinktank.com/2014/12/make-android-apps-19/#sthash.qQhbHKzz.dpuf
}
