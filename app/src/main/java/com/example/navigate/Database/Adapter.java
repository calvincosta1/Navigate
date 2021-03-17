package com.example.navigate.Database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.navigate.R;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder>
{
    // Variables
    private ArrayList<String> id;
    private ArrayList<String> transp;
    private ArrayList<String> startAd;
    private ArrayList<String> endAd;

    Context context;
    CardView cv;
    DBHelper dbH;

    // Passing the parameters to ArrayLists
    public Adapter(Context context, ArrayList<String> id, ArrayList<String> transp, ArrayList<String> startAd, ArrayList<String> endAd)
    {
        dbH = new DBHelper(context);

        this.context = context;
        this.id = id;
        this.transp = transp;
        this.startAd = startAd;
        this.endAd = endAd;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.hist_row_layout, parent, false);
        cv = view.findViewById(R.id.cardView);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        // Setting tvs by pulling info from the arrays
        holder.tvTransportMethod.setText("Transport Method: " + transp.get(position).substring(0, 1).toUpperCase()
                + transp.get(position).substring(1));
        holder.tvStartAddress.setText("Start: " + startAd.get(position));
        holder.tvEndAddress.setText("End: " + endAd.get(position));
        holder.tvDateTime.setText("Date/Time: " + id.get(position));
    }

    @Override
    public int getItemCount()
    {
        return id.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        // Variables to hold elements in the xml file
        TextView tvDateTime;
        TextView tvTransportMethod;
        TextView tvStartAddress;
        TextView tvEndAddress;

        // Constructor
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            // Sets tvs for each element
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvTransportMethod = itemView.findViewById(R.id.tvTransportMethod);
            tvStartAddress = itemView.findViewById(R.id.tvStartAddress);
            tvEndAddress = itemView.findViewById(R.id.tvEndAddress);
        }
    }
}
