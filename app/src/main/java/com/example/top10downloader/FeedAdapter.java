package com.example.top10downloader;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by BRIAN on 5/13/2017.
 */

    // Set T to extend FeedEntry such that the FeedAdapter only accepts data of the correct type (FeedEntry)
    // Useful in case we are using more than one adapter. Makes FeedAdapter class more versatile
public class FeedAdapter<T extends FeedEntry> extends ArrayAdapter {
    private static final String TAG = "FeedAdapter";
    private final int layoutResource; //Final Prevents layout resource reference from changing
    private final LayoutInflater layoutInflater; //Final Prevents layout inflater from changing
    private List<T> applications;


    //Context: Holds state of application while running. Global info about application's environment. Used to request layout Inflater. Can also contain information
    //regarding screen dimensions or whether device has GPS or not

    //LayoutInflater: Creates all the view objects that are described in the XML and adds them to the layout
    public FeedAdapter(@NonNull Context context, @LayoutRes int resource, List<T> applications) {
        super(context, resource);
        // Set field values from parameter
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context); //retrieve layout inflater from context. Similar to getInflater from system service.
        this.applications = applications; //list of FeedEntry objects
    }

    //Allow the list view to determine how many items there are
    //*** If not overrided then listview will not display any items
    @Override
    public int getCount() {
        return applications.size(); //Number of entries in applications list

    }

    //Allow the list view to ask for a new view to display while scrolling
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder; //Used to store a viewHolder object to limit the amount of times findViewById is called

        // convertView prevents the creation of a new view if views are already created to be used
        // Ex. if views were created to display 5 records in the listview, then scrolling to records 6 to 10 will re-use the 5 views
        // More efficient than creating a new view object every time you scroll up or down
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView); //Create a new view holder if no previous views available
            convertView.setTag(viewHolder); //Store viewHolder

        } else {
            viewHolder = (ViewHolder) convertView.getTag(); //Retrieve view's viewHolder
        }

        /** Inefficient to use findViewById every time

        TextView tvName = (TextView) convertView.findViewById(R.id.tvName); // Set reference to tvName textView from the newly inflated view
        TextView tvArtist = (TextView) convertView.findViewById(R.id.tvArtist); // Set reference to tvArtist textView from the newly inflated view
        TextView tvSummary = (TextView) convertView.findViewById(R.id.tvSummary); // Set reference to tvSummary textView from the newly inflated view

         **/

        T currentApp = applications.get(position); //Retrieve the FeedEntry in the list depending on the index position being asked

        //Set the textViews with the updated data for the current record from the viewHolder
        viewHolder.tvName.setText(currentApp.getName());
        viewHolder.tvArtist.setText(currentApp.getArtist());
        viewHolder.tvSummary.setText(currentApp.getSummary());

        return convertView;
    }

    //Class used to store the reference to the 3 textViews used in the layout
    //Helps improve scrolling animation especially on older devices.
    private class ViewHolder {
        final TextView tvName;
        final TextView tvArtist;
        final TextView tvSummary;

        ViewHolder (View v) {
            this.tvName = (TextView) v.findViewById(R.id.tvName);
            this.tvArtist = (TextView) v.findViewById(R.id.tvArtist);
            this.tvSummary = (TextView) v.findViewById(R.id.tvSummary);
        }
    }
}
