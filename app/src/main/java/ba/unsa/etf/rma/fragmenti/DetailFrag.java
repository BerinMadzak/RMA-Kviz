package ba.unsa.etf.rma.fragmenti;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.KvizAdapter;
import ba.unsa.etf.rma.aktivnosti.DodajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.async.NabaviKvizovePoKategorijiTask;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;

public class DetailFrag extends Fragment implements NabaviKvizovePoKategorijiTask.OnKvizoviSearchDone {

    public static ArrayList<Kviz> trenutniKvizovi;
    private KvizAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_fragment, container, false);

        GridView grid = (GridView)view.findViewById(R.id.gridKvizovi);

        trenutniKvizovi = new ArrayList<>();

        adapter = new KvizAdapter(getActivity(), R.layout.element_grida, trenutniKvizovi);
        grid.setAdapter(adapter);

        sort(getArguments().getInt("pozicija"));
        adapter.notifyDataSetChanged();

        grid.setLongClickable(true);
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(KvizoviAkt.kvizoviAkt.isNetworkAvailable()) {
                    Intent myIntent = new Intent(getActivity(), DodajKvizAkt.class);
                    if (position != trenutniKvizovi.size() - 1) {
                        myIntent.putExtra("nazivKviza", trenutniKvizovi.get(position).getNaziv());
                        myIntent.putExtra("kategorijaIndex", getCatPosition(trenutniKvizovi.get(position).getKategorija().getNaziv()) - 1);
                        myIntent.putExtra("pozicija", position);
                        myIntent.putExtra("oldNaziv", trenutniKvizovi.get(position).getNaziv());
                        //myIntent.putParcelableArrayListExtra("pitanja", tempListaPitanja);
                        KvizoviAkt.tempListaPitanja = trenutniKvizovi.get(position).getPitanja();
                        myIntent.putExtra("edit", true);
                    } else {
                        myIntent.putExtra("edit", false);
                    }
                    ArrayList<String> kvizoviImena = new ArrayList<>();
                    for (Kviz k : KvizoviAkt.kvizovi)
                        kvizoviImena.add(k.getNaziv());
                    myIntent.putExtra("kvizoviUzetaImena", kvizoviImena);
                    startActivity(myIntent);
                }
                return true;
            }
        });

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (trenutniKvizovi.get(position).getNaziv() != "Dodaj Kviz") {
                    long ms = 0, ms2 = 0;
                    long msDiff = (long)Math.ceil(trenutniKvizovi.get(position).getPitanja().size()/2);
                    msDiff*=60000;
                    ContentResolver cr = getContext().getContentResolver();
                    String[] kolone = new String[]{
                            CalendarContract.Events.TITLE,
                            CalendarContract.Events.DTSTART
                    };
                    Uri adresa = CalendarContract.Events.CONTENT_URI;
                    Cursor cur = cr.query(adresa, kolone, null, null, null);
                    DateFormat formatter = new SimpleDateFormat("dd:MM:YYYY:HH:mm", Locale.GERMANY);
                    formatter.setTimeZone(TimeZone.getTimeZone("CET"));
                    boolean event = false;
                    while (cur.moveToNext()) {
                        ms = Long.parseLong(cur.getString(1));
                        ms2 = Calendar.getInstance().getTimeInMillis();
                        if(ms > ms2 && ms < (ms2+msDiff)){
                            event = true;
                            break;
                        }
                    }
                    cur.close();
                    if(!event) {
                        Intent myIntent = new Intent(getActivity(), IgrajKvizAkt.class);
                        myIntent.putExtra("nazivKviza", trenutniKvizovi.get(position).getNaziv());
                        myIntent.putExtra("kategorijaIndex", getCatPosition(trenutniKvizovi.get(position).getKategorija().getNaziv()) - 1);
                        IgrajKvizAkt.pitanja = trenutniKvizovi.get(position).getPitanja();
                        if (IgrajKvizAkt.pitanja.size() != 0)
                            if (IgrajKvizAkt.pitanja.get(IgrajKvizAkt.pitanja.size() - 1).getNaziv().equals("Dodaj Pitanje"))
                                IgrajKvizAkt.pitanja.remove(IgrajKvizAkt.pitanja.size() - 1);
                        startActivity(myIntent);
                    }else{
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                        builder1.setMessage("Imate događaj u kalendaru za " + (int)Math.ceil((float)(ms-ms2)/60000) + " minuta!");
                        builder1.setCancelable(true);
                        builder1.setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                } else {
                    if(KvizoviAkt.kvizoviAkt.isNetworkAvailable()) {
                        Intent myIntent = new Intent(getActivity(), DodajKvizAkt.class);
                        if (position != trenutniKvizovi.size() - 1) {
                            myIntent.putExtra("nazivKviza", trenutniKvizovi.get(position).getNaziv());
                            myIntent.putExtra("kategorijaIndex", getCatPosition(trenutniKvizovi.get(position).getKategorija().getNaziv()) - 1);
                            //myIntent.putParcelableArrayListExtra("pitanja", tempListaPitanja);
                            KvizoviAkt.tempListaPitanja = trenutniKvizovi.get(position).getPitanja();
                            myIntent.putExtra("pozicija", KvizoviAkt.kvizovi.indexOf(trenutniKvizovi.get(position)));
                            myIntent.putExtra("edit", true);
                        } else {
                            myIntent.putExtra("edit", false);
                        }
                        ArrayList<String> kvizoviImena = new ArrayList<>();
                        for (Kviz k : KvizoviAkt.kvizovi)
                            kvizoviImena.add(k.getNaziv());
                        myIntent.putExtra("kvizoviUzetaImena", kvizoviImena);
                        startActivity(myIntent);
                    }
                }
            }
        });

        return view;
    }

    private void updateQuizList(int position, ArrayList<Kviz> kvizoviArg){
        trenutniKvizovi.clear();
        if(position == 0) {
            trenutniKvizovi.addAll(KvizoviAkt.kvizovi);
        }
        else{
            trenutniKvizovi.addAll(kvizoviArg);
        }
        trenutniKvizovi.add(new Kviz("Dodaj Kviz",null, new Kategorija("DodajKviz", "temp")));
        adapter.notifyDataSetChanged();
    }

    private int getCatPosition(String name){
        for(int i = 0; i < KvizoviAkt.kategorije.size(); i++)
            if(KvizoviAkt.kategorije.get(i).getNaziv().equals(name)){
                return i;
            }
        return -1;
    }

    public static boolean doesCategoryExist(String name) {
        for(Kategorija kat : KvizoviAkt.kategorije){
            if(kat.getNaziv().equals(name))
                return true;
        }
        return false;
    }

    public static boolean doesQuizExist(String name){
        for(Kviz k : KvizoviAkt.kvizovi){
            if(k.getNaziv().equals(name))
                return true;
        }
        return false;
    }

    public void update(){
        adapter.notifyDataSetChanged();
        updateQuizList(0, null);
    }

    public void sort(int i){
        if(i == 0)
            update();
        else
            new NabaviKvizovePoKategorijiTask((NabaviKvizovePoKategorijiTask.OnKvizoviSearchDone)DetailFrag.this).execute(KvizoviAkt.kategorije.get(i).getNaziv());
    }

    @Override
    public void onDone4(ArrayList<Kviz> kvizList) {
        updateQuizList(1, kvizList);
    }
}
