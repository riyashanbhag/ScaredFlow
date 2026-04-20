package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;

    public BookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use item_booking which has the QR code ImageView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        
        holder.tvTemple.setText(booking.templeName);
        holder.tvDateTime.setText(booking.date + " | " + booking.timeSlot);
        holder.tvCategory.setText("Category: " + booking.userCategory);
        holder.tvStatus.setText("Status: " + booking.status);

        // Update status color
        if ("Confirmed".equals(booking.status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
        } else if ("Checked In".equals(booking.status)) {
            holder.tvStatus.setTextColor(Color.parseColor("#1976D2"));
        } else {
            holder.tvStatus.setTextColor(Color.GRAY);
        }

        // Generate QR Code
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(booking.bookingId, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            holder.ivQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvTemple, tvDateTime, tvCategory, tvStatus;
        ImageView ivQRCode;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTemple = itemView.findViewById(R.id.tvItemTemple);
            tvDateTime = itemView.findViewById(R.id.tvItemDateTime);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvStatus = itemView.findViewById(R.id.tvItemStatus);
            ivQRCode = itemView.findViewById(R.id.ivQRCode);
        }
    }
}
