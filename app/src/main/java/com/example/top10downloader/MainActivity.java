package com.example.top10downloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.example.top10downloader.R.id.xmlListView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listApps; //Used to refer to the listView in the layout

    //Two strings used as keys to store existing feedURL and feedLimit into bundle
    private static final String savedURL = "savedURL";
    private static final String savedLimit = "savedLimit";

    //Used to store menu id to determine if id has changed
    private int id;
    //used to store boolean for menu selection change
    private boolean menuItemChanged = false;

    //Set up base url for RSS feed using %d to represent entries limit
    //Changing this part of the URL determines how many entries are retrieved
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10; //Default limit of entries to retrieve is 10

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Check to see if bundle contains stored information. If so, restore the feedURL and feedLimit.
        if (savedInstanceState != null) {
            feedUrl = savedInstanceState.getString(savedURL);
            feedLimit = savedInstanceState.getInt(savedLimit);
        }
        listApps = (ListView) findViewById(xmlListView);

        //Download Top 10 Free apps by default when activity first created
        downloadUrl(String.format(feedUrl, feedLimit));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //Called when activity's menu is inflated
        getMenuInflater().inflate(R.menu.feeds_menu, menu); //No need for context because main activity is the context
        if (feedLimit == 10) {
            menu.findItem(R.id.mnu10).setChecked(true);
        } else {
            menu.findItem(R.id.mnu25).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //Called when an menu item is selected

        //Determine if current id is equal to the new item id to see if option selection has changed
        menuItemChanged = (id != item.getItemId());

        //Retrieve selected menu item id
        id = item.getItemId();

        switch(id) {
            case R.id.mnuFree: //Top 10 Free apps
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;

            case R.id.mnuPaid: // Top 10 Paid apps
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;

            case R.id.mnuSongs: // Top 10 Songs
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;

            case R.id.mnu10: //Execute same code as mnu25
            case R.id.mnu25:
                if(!item.isChecked()) { //Check to see if radio option selected is checked
                    item.setChecked(true); //Set checked to true if selected radio option was not checked
                    feedLimit = 35 - feedLimit; //Switch between feedLimits 10 and 25 depending on the previous limit set
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " setting feedLimit to " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " feedLimit unchanged");
                }
                break;

            case R.id.mnuRefresh: //When the refresh button is selected
                //Refresh button does not count as a menu item change
                menuItemChanged = false;
                downloadUrl(String.format(feedUrl, feedLimit));
                break;

            default:
                return super.onOptionsItemSelected(item); //Prevents downloadUrl from calling with empty feedUrl field

        }

        //Execute downloadUrl if selected menu item has changed
        if (menuItemChanged) {
            downloadUrl(String.format(feedUrl, feedLimit)); //Method to download data from the input URL
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(savedURL, feedUrl); //Save existing feedURL
        outState.putInt(savedLimit, feedLimit); //Save existing feed limit selected
        super.onSaveInstanceState(outState);
    }

    private void downloadUrl (String feedUrl) {
        Log.d(TAG, "downloadUrl: Starting Async Task");
        DownloadData downloadData = new DownloadData();
        downloadData.execute(feedUrl); // Execute method is extended from AsyncTask class
        Log.d(TAG, "downloadUrl: Done AsyncTask");
    }

    private class DownloadData extends AsyncTask<String, Void, String> { //Extend AsyncTask to separate download task on another thread
        private static final String TAG = "downloadData";

        @Override
        protected void onPostExecute(String s) { //parameter "s" returned from doInBackground method
            super.onPostExecute(s);
//            Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            // First parameter (Context)
            // Second Parameter (textView created in layout) used by adapter to put text into
            // Third parameter passes list of FeedEntry objects to be displayed
//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this, R.layout.list_item, parseApplications.getApplications());
//            listApps.setAdapter(arrayAdapter); // Link the arrayAdapter with the listView created in the layout

            //Use custom adapter for list_record layout
            FeedAdapter<FeedEntry> feedAdapter = new FeedAdapter<FeedEntry>(MainActivity.this, R.layout.list_record, parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);

        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground: starts with " + params[0]);
            String rssFeed = downloadXML(params[0]); // Only one url is ever used in this app. No need for loop
            if (rssFeed == null) {
//                **Use log e to retain log messages in production. Log d debug msgs get removed automatically**
                Log.e(TAG, "doInBackground: Error downloading xml");
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder(); //More efficient than using concatenation

            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode(); //Retrieve HTTP codes
                Log.d(TAG, "downloadXML: The response code was " + response);

//              **Separated method calls to move data into a Buffered Reader**
//                InputStream inputStream = connection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); //Chained method calls to move data into Buffered Reader

                int charsRead;
                char[] inputBuffer = new char[500];
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0) {
                        break;
                    }
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close(); //Closes InputStream + InputStreamReader objects
                return xmlResult.toString(); //Convert StringBuilder to String and return result
            } catch (MalformedURLException e) { //Check if URL is correct
                Log.e(TAG, "downloadXML: Invalid URL " + e.getMessage());
            } catch (IOException e) { // Check if able to read data (i.e. no 404 error msg)
                Log.e(TAG, "downloadXML: IO Exception reading data " + e.getMessage());
            } catch (SecurityException e) { //Replaces display of stack trace with Security Exception log. Issue caused by internet access permissions
                Log.e(TAG, "downloadXML: Security Exception. Needs permission?" + e.getMessage());
//                e.printStackTrace();
            }
            return null; //Return null if there are errors catched

        }
    }
}
