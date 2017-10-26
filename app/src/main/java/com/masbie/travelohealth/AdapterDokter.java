package com.masbie.travelohealth;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.pedant.SweetAlert.SweetAlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.masbie.travelohealth.dao.external.Dao;
import com.masbie.travelohealth.dao.external.request.RegisterDao;
import com.masbie.travelohealth.pojo.response.ResponsePojo;
import com.masbie.travelohealth.pojo.service.DoctorsServicesPojo;
import com.masbie.travelohealth.pojo.service.ServiceOperatedPojo;
import com.masbie.travelohealth.pojo.service.ServiceQueueProcessedPojo;
import com.masbie.travelohealth.pojo.service.ServiceRequestPojo;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Daneswara Jauhari on 06/08/2017.
 */

@SuppressWarnings("ConstantConditions") public class AdapterDokter extends ArrayAdapter<DoctorsServicesPojo>
{
    private List<DoctorsServicesPojo> daftar_dokter;
    private Context                   context;
    private BottomNavigationView      navigation;
    private LinearLayout              fl, f2;
    private FirebaseAuth      mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage   storage = FirebaseStorage.getInstance();
    private DateTimeFormatter hms     = DateTimeFormat.forPattern("HH:mm:ss");
    private DateTimeFormatter ymd     = DateTimeFormat.forPattern("YYYY-MM-dd");
    private Random            random  = new Random();
    private DateTimeZone      zone    = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));

    public AdapterDokter(Activity context, List<DoctorsServicesPojo> daftar_dokter)
    {
        super(context, R.layout.layout_dokter_listview, daftar_dokter);
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.daftar_dokter = daftar_dokter;
        fl = context.findViewById(R.id.transaksi);
        f2 = context.findViewById(R.id.dokter);
        navigation = context.findViewById(R.id.navigation);
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup)
    {
        /*
        * Layout Initialization====================================================================
        * */
        final LayoutInflater      layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View                viewRow        = layoutInflater.inflate(R.layout.layout_dokter_listview, null, true);
        final TextView            mtextView      = viewRow.findViewById(R.id.namadokter);
        final TextView            jam            = viewRow.findViewById(R.id.jampraktek);
        final TextView            pesan          = viewRow.findViewById(R.id.pesanDokter);
        final ImageView           mimageView     = viewRow.findViewById(R.id.image_view);
        final DoctorsServicesPojo dokter         = daftar_dokter.get(i);

        /*
        * Content Initialization====================================================================
        * */
        mtextView.setText(dokter.getName());
        if(dokter.getServices().size() > 0)
        {
            final ServiceOperatedPojo service = dokter.getServices().get(0);
            jam.setText(String.format(Locale.getDefault(), "%s [%s]\n%s - %s", "Setiap Hari",
                    service.getName(),
                    service.getOperationStart().toString(hms),
                    service.getOperationEnd().toString(hms)));
        }
        else
        {
            jam.setText("-");
        }
        mimageView.setImageResource(R.drawable.dokterl);
        /*StorageReference storageRef = storage.getReference().child("images/" + dokter.gambar);
        Glide.with(context)
             .using(new FirebaseImageLoader())
             .load(storageRef)
             .into(mimageView);*/

        /*
        * Dialog Initialization ===================================================================
        * */
        //if(!cekKetersediaan(dokter.hari, dokter.jampraktek))
        if(false)
        {
            pesan.setText("TUTUP");
            pesan.setBackgroundColor(Color.GRAY);
            pesan.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Layanan TUTUP!")
                            .setContentText("Anda dapat mencoba lagi ketika layanan telah dibuka.")
                            .show();
                }
            });
        }
        else
        {
            pesan.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Apa anda yakin?")
                            .setContentText("Anda akan memesan " + dokter.getName() + "!")
                            .setConfirmText("Ya, saya yakin!")
                            .setCancelText("Batal")
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener()
                            {
                                @Override
                                public void onClick(SweetAlertDialog sDialog)
                                {
                                    sDialog.cancel();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
                            {
                                @Override
                                public void onClick(final SweetAlertDialog sDialog)
                                {
                                    if(dokter.getServices().size() > 0)
                                    {
                                        final ServiceOperatedPojo service  = dokter.getServices().get(0);
                                        Integer                   doctor   = service.getId();
                                        LocalDate                 tanggal  = new LocalDate(zone);
                                        ServiceRequestPojo        selected = new ServiceRequestPojo(doctor, tanggal);
                                        RegisterDao.registerServiceRequest(selected, context, new Callback<ResponsePojo<ServiceQueueProcessedPojo>>()
                                        {
                                            @Override public void onResponse(@NonNull Call<ResponsePojo<ServiceQueueProcessedPojo>> call, @NonNull Response<ResponsePojo<ServiceQueueProcessedPojo>> response)
                                            {
                                                ServiceQueueProcessedPojo queue = response.body().getData().getResult();
                                                //Simpan ke DB atau firebase terserah enaknya gimana buat trigger notif
                                                //Implementasi ini sama persis dengan AdapterLayanan jadi 1 db
                                                //FirebaseDao.subscribe(String.format(Locale.getDefault(), "service-%s-%d", queue.getOrder().toString(ymd), queue.getService().getId()));
                                                sDialog
                                                        .setTitleText("Berhasil!")
                                                        .setContentText("Anda telah masuk dalam antrian!")
                                                        .setConfirmText("OK")
                                                        .showCancelButton(false)
                                                        .setConfirmClickListener(null)
                                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                            }

                                            @Override public void onFailure(@NonNull Call<ResponsePojo<ServiceQueueProcessedPojo>> call, @NonNull Throwable throwable)
                                            {
                                                Dao.defaultFailureTask(context, call, throwable);
                                            }
                                        });
                                        /*Calendar calendar = Calendar.getInstance();
                                        String   tanggal  = new SimpleDateFormat("ddMMyyyy").format(calendar.getTime());
                                        mDatabase.child("antrian").child(tanggal).child(dokter.poli).addListenerForSingleValueEvent(new ValueEventListener()
                                        {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot)
                                            {
                                                Calendar calendar = Calendar.getInstance();
                                                String   tanggal  = new SimpleDateFormat("ddMMyyyy").format(calendar.getTime());
                                                calendar.add(Calendar.HOUR, 2);
                                                if(!dataSnapshot.hasChild("antrian"))
                                                {
                                                    mDatabase.child("antrian").child(tanggal).child(dokter.poli).child("antrian").setValue(1);
                                                    mDatabase.child("antrian").child(tanggal).child(dokter.poli).child("proses").setValue(1);
                                                    Pesan pesanan = new Pesan(dokter.id, 1, calendar.getTimeInMillis(), "proses", mAuth.getCurrentUser().getUid(), dokter.gambar);
                                                    mDatabase.child("antrian").child(tanggal).child(dokter.poli).child(String.valueOf(Calendar.getInstance().getTimeInMillis())).setValue(pesanan);
                                                    sDialog
                                                            .setTitleText("Berhasil!")
                                                            .setContentText("Anda telah masuk dalam antrian!")
                                                            .setConfirmText("OK")
                                                            .showCancelButton(false)
                                                            .setConfirmClickListener(null)
                                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                                }
                                                else
                                                {
                                                    mDatabase.child("antrian").child(tanggal).child(dokter.poli).child("antrian").addListenerForSingleValueEvent(new ValueEventListener()
                                                    {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot)
                                                        {
                                                            Calendar calendar = Calendar.getInstance();
                                                            String   tanggal  = new SimpleDateFormat("ddMMyyyy").format(calendar.getTime());
                                                            calendar.add(Calendar.HOUR, 2);
                                                            mDatabase.child("antrian").child(tanggal).child(dokter.poli).child("antrian").setValue((long) dataSnapshot.getValue() + 1);
                                                            Pesan pesanan = new Pesan(dokter.id, (long) dataSnapshot.getValue() + 1, calendar.getTimeInMillis(), "antri", mAuth.getCurrentUser().getUid(), dokter.gambar);
                                                            mDatabase.child("antrian").child(tanggal).child(dokter.poli).child(String.valueOf(Calendar.getInstance().getTimeInMillis())).setValue(pesanan);
                                                            fl.setVisibility(View.VISIBLE);
                                                            f2.setVisibility(View.GONE);
                                                            navigation.setSelectedItemId(R.id.navigation_home);
                                                            sDialog
                                                                    .setTitleText("Berhasil!")
                                                                    .setContentText("Anda telah masuk dalam antrian!")
                                                                    .setConfirmText("OK")
                                                                    .showCancelButton(false)
                                                                    .setConfirmClickListener(null)
                                                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError)
                                                        {

                                                        }
                                                    });

                                                }
                                                //subscripe
                                                FirebaseMessaging.getInstance().subscribeToTopic(dokter.poli);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError)
                                            {

                                            }
                                        });*/

                                    }
                                    else
                                    {
                                        Toast.makeText(context, "Tidak Ada Layanan", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .show();
                }
            });
        }

        return viewRow;
    }

    public boolean cekKetersediaan(String hari, String waktu)
    {
        waktu = waktu.replace(".", "titik");
        hari = hari.replace(" ", "");
        Calendar calendar    = Calendar.getInstance();
        int      day         = calendar.get(Calendar.DAY_OF_WEEK);
        String   semuahari[] = hari.split(",");
        String   hari_now    = "";
        switch(day)
        {
            case Calendar.SUNDAY:
                hari_now = "minggu";
                break;
            case Calendar.MONDAY:
                hari_now = "senin";
                break;
            case Calendar.TUESDAY:
                hari_now = "selasa";
                break;
            case Calendar.WEDNESDAY:
                hari_now = "rabu";
                break;
            case Calendar.THURSDAY:
                hari_now = "kamis";
                break;
            case Calendar.FRIDAY:
                hari_now = "jum'at";
                break;
            case Calendar.SATURDAY:
                hari_now = "sabtu";
                break;
            default:
                hari_now = "tidak ada";
        }
        boolean cek_hari = false;
        for(int i = 0; i < semuahari.length; i++)
        {
            if(semuahari[i].equalsIgnoreCase(hari_now))
            {
                cek_hari = true;
            }
        }
        String[] waktu_tersedia = waktu.split("-");
        System.out.println(waktu_tersedia[0]);
        System.out.println(waktu_tersedia[1]);
        String[] waktuawal = waktu_tersedia[0].split("titik");
        System.out.println(waktuawal[0]);
        System.out.println(waktuawal[1]);
        String[] waktuakhir      = waktu_tersedia[1].split("titik");
        int      jam             = calendar.get(Calendar.HOUR_OF_DAY);
        int      menit           = calendar.get(Calendar.MINUTE);
        int      konv_waktu      = jam * 60 + menit;
        int      konv_waktuawal  = Integer.parseInt(waktuawal[0]) * 60 + Integer.parseInt(waktuawal[1]);
        int      konv_waktuakhir = Integer.parseInt(waktuakhir[0]) * 60 + Integer.parseInt(waktuakhir[1]);
        boolean  cek_waktu       = konv_waktu >= konv_waktuawal && konv_waktu <= konv_waktuakhir;
        System.out.println("cek hari" + cek_hari);
        System.out.println("cek waktu" + cek_waktu);
        return cek_hari && cek_waktu;
    }
}
