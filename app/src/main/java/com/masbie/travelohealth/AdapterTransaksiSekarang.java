package com.masbie.travelohealth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.model.DirectionsResult;
import com.masbie.travelohealth.object.Antrian;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Daneswara Jauhari on 06/08/2017.
 */

public class AdapterTransaksiSekarang extends ArrayAdapter
{
    Context          context;
    DirectionsResult result;
    List<Antrian>    daftar_antrian;
    long             proses;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    public AdapterTransaksiSekarang(Activity context, List<Antrian> daftar_antrian, DirectionsResult result, long proses)
    {
        super(context, R.layout.layout_transaksi_listview, daftar_antrian);
        this.context = context;
        this.daftar_antrian = daftar_antrian;
        this.result = result;
        this.proses = proses;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRow = layoutInflater.inflate(R.layout.layout_transaksi_listview, null,
                true);
        TextView  mtextView      = viewRow.findViewById(R.id.urutan);
        ImageView mimageView     = viewRow.findViewById(R.id.image_view);
        TextView  antriansaatini = viewRow.findViewById(R.id.antrianke);
        TextView  pelayanan      = viewRow.findViewById(R.id.estimasipelayanan);
        TextView  perjalanan     = viewRow.findViewById(R.id.estimasiperjalanan);
        Button    detail         = viewRow.findViewById(R.id.detail);
        mtextView.setText("No. " + daftar_antrian.get(i).no_antrian);
        antriansaatini.setText("Saat ini antrian ke-" + proses);
        CharSequence estimasi = DateUtils.getRelativeTimeSpanString(daftar_antrian.get(i).pelayanan, Calendar.getInstance().getTimeInMillis(), 0);
        pelayanan.setText(estimasi);
        StorageReference storageRef = storage.getReference().child("images/" + daftar_antrian.get(i).gambar);
        Glide.with(context)
             .using(new FirebaseImageLoader())
             .load(storageRef)
             .into(mimageView);
        detail.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, DetailTransaksi.class);
                context.startActivity(intent);
            }
        });
        if(result == null || result.routes.length == 0)
        {
            perjalanan.setText("estimasi waktu tidak tersedia");
        }
        else
        {
            perjalanan.setText(result.routes[0].legs[0].duration.humanReadable);
        }
        return viewRow;
    }


}
