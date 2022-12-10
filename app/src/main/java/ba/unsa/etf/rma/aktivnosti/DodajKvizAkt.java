package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.async.DodajKategorijuTask;
import ba.unsa.etf.rma.async.DodajPitanjeTask;
import ba.unsa.etf.rma.async.NabaviPitanjaTask;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.adapteri.PitanjeAdapter;

public class DodajKvizAkt extends AppCompatActivity implements NabaviPitanjaTask.OnPitanjeSearchDone {

    private ArrayList<Pitanje> pitanjaUKvizu;
    private ArrayList<Pitanje> mogucaPitanja;

    private ArrayList<Kategorija> kategorijeList;

    private PitanjeAdapter adapter;
    private ArrayAdapter<Kategorija> adapter3;
    private Spinner spinner;

    private EditText nameText;

    private Pitanje addPitanje;

    private ArrayAdapter<Pitanje> adapter2;

    private String oldNaziv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        addPitanje = new Pitanje("Dodaj Pitanje", "", null, "");

        final ListView pitanjaUKvizuList = (ListView)findViewById(R.id.lvDodanaPitanja);
        final ListView mogucaPitanjaList = (ListView)findViewById(R.id.lvMogucaPitanja);
        spinner = (Spinner)findViewById(R.id.spKategorije);
        nameText = (EditText)findViewById(R.id.etNaziv);
        Button dodajBtn = (Button)findViewById(R.id.btnDodajKviz);
        Button importBtn = (Button)findViewById(R.id.btnImportKviz);

        pitanjaUKvizu = new ArrayList<>();
        mogucaPitanja = new ArrayList<>();
        kategorijeList = new ArrayList<>();
        kategorijeList.addAll(KvizoviAkt.kategorije);
        kategorijeList.add(new Kategorija("Dodaj kategoriju", "0"));
        kategorijeList.remove(0);

        int pozicija = getIntent().getIntExtra("pozicija", -1);
        if(pozicija != -1) {
            if(KvizoviAkt.dfg == null)
                pitanjaUKvizu = KvizoviAkt.trenutniKvizovi.get(pozicija).getPitanja();
            else
                pitanjaUKvizu = DetailFrag.trenutniKvizovi.get(pozicija).getPitanja();
        }

        final boolean editMode = getIntent().getBooleanExtra("edit", false);
        final ArrayList<String> kvizoviUzetaImena = getIntent().getStringArrayListExtra("kvizoviUzetaImena");

        adapter = new PitanjeAdapter(this, R.layout.element_liste, pitanjaUKvizu);
        pitanjaUKvizuList.setAdapter(adapter);

        adapter2 = new ArrayAdapter<Pitanje>(this, R.layout.element_liste, R.id.Itemname, mogucaPitanja);
        mogucaPitanjaList.setAdapter(adapter2);

        adapter3 = new ArrayAdapter<Kategorija>(this, android.R.layout.simple_list_item_1, kategorijeList);
        spinner.setAdapter(adapter3);

        final String currentName;

        if(editMode) {
            currentName = getIntent().getStringExtra("nazivKviza");
            nameText.setText(currentName);
            spinner.setSelection(getIntent().getIntExtra("kategorijaIndex", 0));
            oldNaziv = getIntent().getStringExtra("oldNaziv");
            new NabaviPitanjaTask((NabaviPitanjaTask.OnPitanjeSearchDone)DodajKvizAkt.this).execute();
        }else{
            pitanjaUKvizu.clear();
            currentName = "";
        }

        if(!containsAddQuestion())
            pitanjaUKvizu.add(new Pitanje("Dodaj Pitanje", "", null, ""));

        pitanjaUKvizuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position != pitanjaUKvizu.size() - 1){
                    mogucaPitanja.add(pitanjaUKvizu.get(position));
                    adapter2.notifyDataSetChanged();
                    pitanjaUKvizu.remove(position);
                    adapter.notifyDataSetChanged();
                }else {
                    if (KvizoviAkt.kvizoviAkt.isNetworkAvailable()) {
                        Intent myIntent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                        ArrayList<String> trenutnaPitanja = new ArrayList<>();
                        for (Pitanje p : pitanjaUKvizu)
                            trenutnaPitanja.add(p.getNaziv());
                        for (Pitanje p : mogucaPitanja)
                            trenutnaPitanja.add(p.getNaziv());
                        myIntent.putExtra("trenutnaPitanja", trenutnaPitanja);
                        DodajKvizAkt.this.startActivity(myIntent);
                    }
                }
            }
        });

        mogucaPitanjaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pitanjaUKvizu.add(pitanjaUKvizu.size() - 1, mogucaPitanja.get(position));
                adapter.notifyDataSetChanged();
                mogucaPitanja.remove(position);
                adapter2.notifyDataSetChanged();
            }
        });

        dodajBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (KvizoviAkt.kvizoviAkt.isNetworkAvailable()) {
                    boolean valid = true;
                    if (nameText.getText().toString().length() == 0 || kvizoviUzetaImena.contains(nameText.getText().toString())) {
                        if (!editMode || (editMode && !currentName.equals(nameText.getText().toString())))
                            valid = false;
                        nameText.getBackground().mutate().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                    } else {
                        nameText.getBackground().mutate().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    }

                    if (pitanjaUKvizu.size() == 1) {
                        valid = false;
                    } else {
                        valid = true;
                    }

                    if (valid) {
                        Intent newIntent = new Intent(DodajKvizAkt.this, KvizoviAkt.class);
                        KvizoviAkt.tempListaPitanja = pitanjaUKvizu;
                        KvizoviAkt.tempListaPitanja.remove(KvizoviAkt.tempListaPitanja.size() - 1);
                        newIntent.putExtra("naziv", nameText.getText().toString());
                        //newIntent.putParcelableArrayListExtra("pitanja", pitanjaUKvizu);
                        newIntent.putExtra("kategorija", kategorijeList.get(spinner.getSelectedItemPosition()));
                        newIntent.putExtra("edit", editMode);
                        newIntent.putExtra("oldNaziv", oldNaziv);
                        newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        DodajKvizAkt.this.startActivity(newIntent);
                    }
                }
            }
        });

        importBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                startActivityForResult(intent, 42);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position == kategorijeList.size() - 1)
                {
                    if(KvizoviAkt.kvizoviAkt.isNetworkAvailable()) {
                        Intent myIntent = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
                        DodajKvizAkt.this.startActivity(myIntent);
                    }else{
                        spinner.setSelection(0);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent.getAction() == "Icon"){
            Kategorija k = intent.getParcelableExtra("Kategorija");
            kategorijeList.add(kategorijeList.size()-1, k);
            KvizoviAkt.kategorije.add(k);
            adapter3.notifyDataSetChanged();
            spinner.setSelection(kategorijeList.size()-2);
            new DodajKategorijuTask().execute(k.getNaziv(), k.getId());
        }else {
            String nazivPitanja = intent.getStringExtra("nazivPitanja");
            ArrayList<String> odgovori = intent.getStringArrayListExtra("odgovoriList");
            String tacanOdgovor = intent.getStringExtra("tacanOdgovor");
            pitanjaUKvizu.add(pitanjaUKvizu.size() - 1, new Pitanje(nazivPitanja, "", odgovori, tacanOdgovor));
            adapter.notifyDataSetChanged();
            String[] array = new String[odgovori.size()+3];
            array[0] = nazivPitanja;
            array[1] = Integer.toString(odgovori.indexOf(tacanOdgovor));
            array[2] = Integer.toString(odgovori.size());
            for(int i = 0; i < odgovori.size(); i++)
                array[i+3] = odgovori.get(i);
            new DodajPitanjeTask().execute(array);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if(requestCode == 42 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if(resultData != null){
                uri = resultData.getData();
                try {
                    setQuizFromCSV(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void setQuizFromCSV(Uri uri) throws IOException {
        Kviz kviz = null;
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        //
        String naziv, kategorija, nazivPitanja;
        int brojPitanja, brojOdgovora, indeksTacnog;
        //
        if((line = reader.readLine()) != null){
            naziv = new String();
            int i = 0;
            while(line.charAt(i) != ','){
                naziv += line.charAt(i);
                i++;
            }

            if(KvizoviAkt.doesQuizExist(naziv)){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Kviz kojeg importujete već postoji!");
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
                return;
            }

            i++;
            kategorija = new String();
            while (line.charAt(i) != ','){
                kategorija += line.charAt(i);
                i++;
            }

            brojPitanja = Integer.parseInt(Character.toString(line.charAt(++i)));

            ArrayList<Pitanje> pitanjaList = new ArrayList<>();

            while((line = reader.readLine()) != null){
                ArrayList<String> odgovoriList = new ArrayList<>();
                nazivPitanja = new String();
                i = 0;

                while (line.charAt(i) != ','){
                    nazivPitanja += line.charAt(i);
                    i++;
                }

                brojOdgovora = Integer.parseInt(Character.toString(line.charAt(++i)));
                i++;
                indeksTacnog = Integer.parseInt(Character.toString(line.charAt(++i)));
                if(indeksTacnog >= brojOdgovora){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setMessage("Kviz kojeg importujete ima neispravan index tačnog odgovora!");
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
                    return;
                }

                i += 2;
                boolean end = false;
                int j;
                for(j = 0; j < brojOdgovora; j++){
                    String odgovor = new String();
                    while (line.charAt(i) != ','){
                        odgovor += line.charAt(i);
                        i++;
                        if(i == line.length()) {
                            end = true;
                            j = brojOdgovora;
                            break;
                        }
                    }
                    odgovoriList.add(odgovor);
                    i++;
                }

                if(odgovoriList.size() != brojOdgovora || !end){
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setMessage("Kviz kojeg importujete ima neispravan broj odgovora!");
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
                    return;
                }

                pitanjaList.add(new Pitanje(nazivPitanja, "", odgovoriList, odgovoriList.get(indeksTacnog)));
                String[] array = new String[odgovoriList.size()+3];
                array[0] = nazivPitanja;
                array[1] = Integer.toString(indeksTacnog);
                array[2] = Integer.toString(odgovoriList.size());
                for(int k = 0; k < odgovoriList.size(); k++)
                    array[k+3] = odgovoriList.get(k);
                new DodajPitanjeTask().execute(array);
            }

            if(pitanjaList.size() != brojPitanja){
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Kviz kojeg imporujete ima neispravan broj pitanja!");
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
                return;
            }

            if(!KvizoviAkt.doesCategoryExist(kategorija)){
                Kategorija kategorijaTemp = new Kategorija(kategorija, "162");
                kategorijeList.add(kategorijeList.size()-1, kategorijaTemp);
                KvizoviAkt.kategorije.add(kategorijaTemp);
                spinner.setSelection(kategorijeList.size()-2);
                new DodajKategorijuTask().execute(kategorijaTemp.getNaziv(), kategorijaTemp.getId());
            }else{
                int index = getCatPosition(kategorija);
                if(index != -1){
                    spinner.setSelection(index);
                }
            }

            nameText.setText(naziv);
            pitanjaUKvizu.clear();
            pitanjaUKvizu.addAll(pitanjaList);
            if(!containsAddQuestion())
                pitanjaUKvizu.add(addPitanje);
            adapter.notifyDataSetChanged();
            adapter3.notifyDataSetChanged();
        }
        inputStream.close();
        reader.close();
    }

    private int getCatPosition(String name){
        for(int i = 0; i < kategorijeList.size(); i++)
            if(kategorijeList.get(i).getNaziv().equals(name)){
                return i;
            }
        return -1;
    }

    private boolean containsAddQuestion(){
        for(Pitanje pitanje : pitanjaUKvizu){
            if(pitanje.getNaziv().equals("Dodaj Pitanje"))
                return true;
        }
        return false;
    }

    @Override
    public void onDone(ArrayList<Pitanje> pitanjaList) {
        ArrayList<Pitanje> pitanjaList2 = new ArrayList<>();
        for(Pitanje p : pitanjaList)
            for(Pitanje p1 : pitanjaUKvizu)
                if(p1.getNaziv().equals(p.getNaziv()))
                    pitanjaList2.add(p);
        for(Pitanje p : pitanjaList2)
            pitanjaList.remove(p);
        mogucaPitanja.addAll(pitanjaList);
        adapter2.notifyDataSetChanged();
    }

}
