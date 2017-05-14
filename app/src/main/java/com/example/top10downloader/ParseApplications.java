package com.example.top10downloader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by BRIAN on 5/10/2017.
 */

public class ParseApplications {
    private static final String TAG = "ParseApplications";

    private ArrayList<FeedEntry> applications; //Used to store each FeedEntry object into an ArrayList (Info for each download in the Top 10 list)

    public ParseApplications() {
        this.applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    public boolean parse(String xmlData) {
        boolean status = true; //Simply determines if parsing is successful
        FeedEntry currentRecord = null;
        boolean inEntry = false; //Determine if we are inside the entry or not
        String textValue = ""; //Store value of current tag

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); //Create factory to later create XML Parser object
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser(); //Creates Pull Parser Object using API's factory
            xpp.setInput(new StringReader(xmlData)); //Pull Parser uses String Reader which requires a String
            int eventType = xpp.getEventType(); //Determines current area it has parsed to (START TAG, END TAG, TEXT, END DOC)
            while(eventType != XmlPullParser.END_DOCUMENT) { //Keep looping until end of XML Document
                String tagName = xpp.getName(); // Retrieve the name of the tag
                switch(eventType) { //Check if parser is at a START tag, END tag, or TEXT in-between
                    case XmlPullParser.START_TAG:
//                        Log.d(TAG, "parse: Starting tag for: " + tagName);
                        if("entry".equalsIgnoreCase(tagName)) { //Check to see if START tag is an entry in the XML
                            inEntry = true; //Boolean to determine if parser is inside an entry
                            currentRecord = new FeedEntry(); //Create a new FeedEntry object once we determined we have found the start of an entry tag
                        }
                        //******We can add an else if here to check for Image start tag inside entry and to check tag attributes such as specific height******
                        break;

                    case XmlPullParser.TEXT: //Check to see if it has reached a portion inside the tag with text
                        textValue = xpp.getText(); //Retrieve the text and store into textValue field
                        break;

                    case XmlPullParser.END_TAG: //Check to see if parser has reached an END tag
//                        Log.d(TAG, "parse: Ending tag for: " + tagName);
                        if(inEntry) { //If we are inside an entry, we will obtain name, artist, releaseDate, summary, and imageURL text of that app entry
                            if("entry".equalsIgnoreCase(tagName)) { //If ending tag is entry, we will add the current FeedEntry object to the ArrayList
                                applications.add(currentRecord);
                                inEntry = false; //Set inEntry value to false since we are assumed to have left the entry after the closing entry tag
                            } else if("name".equalsIgnoreCase(tagName)) {
                                currentRecord.setName(textValue);
                            } else if("artist".equalsIgnoreCase(tagName)) {
                                currentRecord.setArtist(textValue);
                            } else if("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textValue);
                            } else if("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textValue);
                            } else if("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textValue); // By default, this will select the url belonging to the last image tag inside each entry
                            }
                        }
                        break;

                    default:
                        //Nothing needed to be done if none of the cases match up
                }
                eventType = xpp.next(); //go to the next event in the parser.
            }
//            for (FeedEntry app: applications) { // Print out each FeedEntry stored in Applications ArrayList
//                Log.d(TAG, "************************");
//                Log.d(TAG, app.toString());
//            }

        } catch(Exception e) { //Catches all exceptions
            status = false;
            e.printStackTrace();
        }
        return status;
    }
}
