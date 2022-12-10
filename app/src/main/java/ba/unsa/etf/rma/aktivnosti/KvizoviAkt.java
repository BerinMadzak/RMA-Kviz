package ba.unsa.etf.rma.aktivnosti;

import android.Manifest;
import android.app.AlertDialog;
import android.app.usage.UsageEvents;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.util.DateTime;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.async.DodajKvizTask;
import ba.unsa.etf.rma.async.NabaviKategorijeTask;
import ba.unsa.etf.rma.async.NabaviKvizovePoKategorijiTask;
import ba.unsa.etf.rma.async.NabaviKvizoveTask;
import ba.unsa.etf.rma.async.NabaviPitanjeTask;
import ba.unsa.etf.rma.async.ObrisiKvizTask;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.klase.DBOpenHelper;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.adapteri.KvizAdapter;
import ba.unsa.etf.rma.klase.Pitanje;

public class KvizoviAkt extends AppCompatActivity implements NabaviKategorijeTask.OnKategorijeSearchDone, NabaviKvizoveTask.OnKvizoviSearchDone, NabaviKvizovePoKategorijiTask.OnKvizoviSearchDone {

    public static ArrayList<Kviz> kvizovi;
    public static ArrayList<Kviz> trenutniKvizovi;
    public static ArrayList<Kategorija> kategorije;

    private KvizAdapter adapter;
    private ArrayAdapter<Kategorija> adapter2;
    private Spinner spinner;

    public static ArrayList<Pitanje> tempListaPitanja;

    private FrameLayout listLayout;
    private FrameLayout detailLayout;

    public static DetailFrag dfg;
    public static ListaFrag lfg;

    public static String token;
    public static String id = "kvizovi-e6ee5";

    public static KvizoviAkt kvizoviAkt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kvizovi_akt);

        kvizoviAkt = this;

        new GetToken().execute("test");

        FragmentManager fm = getSupportFragmentManager();
        listLayout = (FrameLayout)findViewById(R.id.listPlace);
        detailLayout = (FrameLayout)findViewById(R.id.detailPlace);

        kvizovi = new ArrayList<>();
        kategorije = new ArrayList<>();
        kategorije.add(new Kategorija("Svi", "0"));
        kategorije.add(new Kategorija("Bez kategorije", "1"));

        new NabaviKategorijeTask((NabaviKategorijeTask.OnKategorijeSearchDone)KvizoviAkt.this).execute();

        tempListaPitanja = new ArrayList<>();

        trenutniKvizovi = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(KvizoviAkt.this,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(KvizoviAkt.this, new String[]{Manifest.permission.READ_CALENDAR}, 0);
        }
        if(listLayout == null) {

            spinner = (Spinner) findViewById(R.id.spPostojeceKategorije);
            ListView listView = (ListView) findViewById(R.id.lvKvizovi);

            adapter = new KvizAdapter(this, R.layout.element_liste, trenutniKvizovi);
            listView.setAdapter(adapter);

            trenutniKvizovi.add(new Kviz("Dodaj Kviz", null, new Kategorija("DodajKviz", "temp")));
            adapter.notifyDataSetChanged();

            adapter2 = new ArrayAdapter<Kategorija>(this, android.R.layout.simple_list_item_1, kategorije);
            spinner.setAdapter(adapter2);

            listView.setLongClickable(true);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if(isNetworkAvailable()) {
                        Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                        if (position != trenutniKvizovi.size() - 1) {
                            myIntent.putExtra("nazivKviza", trenutniKvizovi.get(position).getNaziv());
                            myIntent.putExtra("kategorijaIndex", getCatPosition(trenutniKvizovi.get(position).getKategorija().getNaziv()) - 1);
                            //myIntent.putParcelableArrayListExtra("pitanja", tempListaPitanja);
                            tempListaPitanja = trenutniKvizovi.get(position).getPitanja();
                            myIntent.putExtra("pozicija", position);
                            myIntent.putExtra("oldNaziv", trenutniKvizovi.get(position).getNaziv());
                            myIntent.putExtra("edit", true);
                        } else {
                            myIntent.putExtra("edit", false);
                        }
                        ArrayList<String> kvizoviImena = new ArrayList<>();
                        for (Kviz k : kvizovi)
                            kvizoviImena.add(k.getNaziv());
                        myIntent.putExtra("kvizoviUzetaImena", kvizoviImena);
                        KvizoviAkt.this.startActivity(myIntent);
                    }
                    return true;
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (trenutniKvizovi.get(position).getNaziv() != "Dodaj Kviz") {
                        long ms = 0, ms2 = 0;
                        long msDiff = (long)Math.ceil(trenutniKvizovi.get(position).getPitanja().size()/2);
                        msDiff*=60000;
                        ContentResolver cr = getContentResolver();
                        String[] kolone = new String[]{
                                CalendarContract.Events.TITLE,
                                CalendarContract.Events.DTSTART,
                                CalendarContract.Events.DTEND
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
                            Intent myIntent = new Intent(KvizoviAkt.this, IgrajKvizAkt.class);
                            myIntent.putExtra("nazivKviza", trenutniKvizovi.get(position).getNaziv());
                            myIntent.putExtra("kategorijaIndex", getCatPosition(trenutniKvizovi.get(position).getKategorija().getNaziv()) - 1);
                            IgrajKvizAkt.pitanja = trenutniKvizovi.get(position).getPitanja();
                            if (IgrajKvizAkt.pitanja.size() != 0)
                                if (IgrajKvizAkt.pitanja.get(IgrajKvizAkt.pitanja.size() - 1).getNaziv().equals("Dodaj Pitanje"))
                                    IgrajKvizAkt.pitanja.remove(IgrajKvizAkt.pitanja.size() - 1);
                            KvizoviAkt.this.startActivity(myIntent);
                        }else{
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(KvizoviAkt.this);
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
                        if(isNetworkAvailable()) {
                            Intent myIntent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                            if (position != trenutniKvizovi.size() - 1) {
                                myIntent.putExtra("nazivKviza", trenutniKvizovi.get(position).getNaziv());
                                myIntent.putExtra("kategorijaIndex", getCatPosition(trenutniKvizovi.get(position).getKategorija().getNaziv()) - 1);
                                //myIntent.putParcelableArrayListExtra("pitanja", tempListaPitanja);
                                tempListaPitanja = trenutniKvizovi.get(position).getPitanja();
                                myIntent.putExtra("edit", true);
                            } else {
                                myIntent.putExtra("edit", false);
                            }
                            ArrayList<String> kvizoviImena = new ArrayList<>();
                            for (Kviz k : kvizovi)
                                kvizoviImena.add(k.getNaziv());
                            myIntent.putExtra("kvizoviUzetaImena", kvizoviImena);
                            KvizoviAkt.this.startActivity(myIntent);
                        }
                    }
                }
            });

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if(position == 0){
                        updateQuizList(0, null);
                    }else {
                        new NabaviKvizovePoKategorijiTask((NabaviKvizovePoKategorijiTask.OnKvizoviSearchDone)KvizoviAkt.this).execute(kategorije.get(position).getNaziv());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {

                }

            });
        }else{
            dfg = new DetailFrag();
            Bundle arguments = new Bundle();
            arguments.putInt("pozicija", 0);
            dfg.setArguments(arguments);
            fm.beginTransaction().replace(R.id.detailPlace, dfg).commit();
            lfg = (ListaFrag) fm.findFragmentById(R.id.listPlace);
            if(lfg == null){
                lfg = new ListaFrag();
                fm.beginTransaction().replace(R.id.listPlace, lfg).commit();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent.getAction() != "EndQuiz") {
            boolean editMode = intent.getBooleanExtra("edit", false);
            String naziv = intent.getStringExtra("naziv");
            Kategorija kat = intent.getParcelableExtra("kategorija");
            String oldNaziv = intent.getStringExtra("oldNaziv");
            //ArrayList<Pitanje> pitanja = intent.getParcelableArrayListExtra("pitanja");
            if (!editMode) {
                kvizovi.add(new Kviz(naziv, tempListaPitanja, kat));
            } else {
                int pos = getQuizPosition(oldNaziv);
                if (pos != -1) {
                    Kviz k = kvizovi.get(pos);
                    k.setNaziv(naziv);
                    k.setPitanja(tempListaPitanja);
                    k.setKategorija(kat);
                    new ObrisiKvizTask().execute(oldNaziv);
                }
            }
            String[] array = new String[tempListaPitanja.size()+3];
            array[0] = naziv;
            array[1] = kat.getNaziv();
            array[2] = Integer.toString(tempListaPitanja.size());
            for(int i = 0; i < tempListaPitanja.size(); i++)
                array[i+3] = tempListaPitanja.get(i).getNaziv();
            new DodajKvizTask().execute(array);
            if(listLayout == null) {
                if(spinner.getSelectedItemPosition() == 0)
                    updateQuizList(spinner.getSelectedItemPosition(), null);
                else
                    new NabaviKvizovePoKategorijiTask((NabaviKvizovePoKategorijiTask.OnKvizoviSearchDone)KvizoviAkt.this).execute(kategorije.get(spinner.getSelectedItemPosition()).getNaziv());
            }else{
                dfg.update();
            }
        }
    }

    private void updateQuizList(int position, ArrayList<Kviz> kvizoviArg){
        trenutniKvizovi.clear();
        if(position == 0) {
            trenutniKvizovi.addAll(kvizovi);
        }
        else{
            trenutniKvizovi.addAll(kvizoviArg);
        }
        trenutniKvizovi.add(new Kviz("Dodaj Kviz",null, new Kategorija("DodajKviz", "temp")));
        if(adapter != null)
            adapter.notifyDataSetChanged();
    }

    private int getQuizPosition(String name){
        for(int i = 0; i < kvizovi.size(); i++) {
            if (kvizovi.get(i).getNaziv().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private int getCatPosition(String name){
        for(int i = 0; i < kategorije.size(); i++) {
            if (kategorije.get(i).getNaziv().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean doesCategoryExist(String name) {
        for(Kategorija kat : kategorije){
            if(kat.getNaziv().equals(name))
                return true;
        }
        return false;
    }

    public static boolean doesQuizExist(String name){
        for(Kviz k : kvizovi){
            if(k.getNaziv().equals(name))
                return true;
        }
        return false;
    }

    @Override
    public void onDone(ArrayList<Kategorija> kategorije2) {
        kategorije.clear();
        kategorije.add(new Kategorija("Svi", "0"));
        kategorije.add(new Kategorija("Bez kategorije", "1"));
        kategorije.addAll(kategorije2);
        if(adapter2 != null)
            adapter2.notifyDataSetChanged();
        else
            lfg.update();
        new NabaviKvizoveTask((NabaviKvizoveTask.OnKvizoviSearchDone)KvizoviAkt.this).execute();
    }

    @Override
    public void onDone2(ArrayList<Kviz> kvizList) {
        kvizovi.clear();
        kvizovi.addAll(kvizList);
        if(dfg == null)
            updateQuizList(0, null);
        else
            dfg.update();
    }

    @Override
    public void onDone4(ArrayList<Kviz> kvizList) {
        updateQuizList(1, kvizList);
    }

    public class GetToken extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            GoogleCredential gc;
            try{
                InputStream is = getResources().openRawResource(R.raw.secret);
                gc = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                gc.refreshToken();
                KvizoviAkt.token = gc.getAccessToken();
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
