package com.example.android.quakereport.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.quakereport.R;
import com.example.android.quakereport.model.Earthquake;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CustomAdapter extends ArrayAdapter<Earthquake> {

    public CustomAdapter(Context context, ArrayList<Earthquake> arrayList) {
        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listView = convertView;

        if(listView == null)
            listView = LayoutInflater.from(getContext()).inflate(R.layout.list_item,parent,false);

        Earthquake earthquake = getItem(position);

        TextView textView1,textView2,textView3,textView4,textView5;

        textView1 = (TextView)listView.findViewById(R.id.magnitude);
        textView2 = (TextView)listView.findViewById(R.id.place);
        textView5 = (TextView)listView.findViewById(R.id.place2);
        textView3 = (TextView)listView.findViewById(R.id.date);
        textView4 = (TextView)listView.findViewById(R.id.time);

        String [] place = placeUtil(earthquake.getLocation());

        if(place.length == 1) {

            textView5.setText("NEAR THE");
            textView1.setText(String.valueOf(earthquake.getMagnitude()));
            textView2.setText(place[0]);
            textView3.setText(dateUtil(earthquake.getDate()));
            textView4.setText(timeUtil(earthquake.getDate()));
        }

        else
        {
            textView5.setText(" " + place[0]+"OF");
            textView1.setText(String.valueOf(earthquake.getMagnitude()));
            textView2.setText(place[1]);
            textView3.setText(dateUtil(earthquake.getDate()));
            textView4.setText(timeUtil(earthquake.getDate()));
        }
        return listView;
    }

    private String[] placeUtil(String s) {
        String [] parts = s.split("of");

        for(int i=0;i<parts.length;i++)
        {
            parts[i] = parts[i].toUpperCase();
        }
        return parts;
    }

    private String dateUtil(String s) {
        Date date = new Date(s);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(" MMM dd, yyyy");

        return simpleDateFormat.format(date);
    }

    private String timeUtil(String s) {
        Date date = new Date(s);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");

        return simpleDateFormat.format(date);
    }



}
