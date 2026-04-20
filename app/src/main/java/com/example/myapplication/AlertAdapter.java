package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private List<Alert> alertList;

    public AlertAdapter(List<Alert> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        Alert alert = alertList.get(position);
        holder.tvStatus.setText("🚨 " + alert.status);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        holder.tvTime.setText("Time: " + sdf.format(new Date(alert.timestamp)));

        holder.btnViewLocation.setOnClickListener(v -> {
            String uri = "geo:" + alert.latitude + "," + alert.longitude + "?q=" + alert.latitude + "," + alert.longitude;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvTime;
        Button btnViewLocation;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tvAlertStatus);
            tvTime = itemView.findViewById(R.id.tvAlertTime);
            btnViewLocation = itemView.findViewById(R.id.btnViewLocation);
        }
    }
}
