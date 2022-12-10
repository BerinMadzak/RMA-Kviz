package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajPitanjeAkt extends AppCompatActivity {

    private ArrayList<String> odgovori;

    int tacanPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje_akt);

        final EditText naziv = (EditText)findViewById(R.id.etNaziv);
        final EditText odgovor = (EditText)findViewById(R.id.etOdgovor);
        final ListView odgovoriList = (ListView)findViewById(R.id.lvOdgovori);
        Button dodajBtn = (Button)findViewById(R.id.btnDodajOdgovor);
        Button dodajTacanBtn = (Button)findViewById(R.id.btnDodajTacan);
        Button spasiBtn = (Button)findViewById(R.id.btnDodajPitanje);

        odgovori = new ArrayList<>();

        final ArrayList<String> trenutnaPitanja = getIntent().getStringArrayListExtra("trenutnaPitanja");

        final ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, odgovori){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);

                if (position == tacanPos) {
                    text.setBackgroundColor(Color.GREEN);
                } else {
                    text.setBackgroundColor(Color.WHITE);
                }

                return view;
            }
        };
        odgovoriList.setAdapter(adapter);

        odgovoriList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(tacanPos == position){
                    tacanPos = -1;
                }
                odgovori.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        dodajBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(odgovor.getText().toString().length() > 0 && !odgovori.contains(odgovor.getText().toString())){
                    odgovori.add(odgovor.getText().toString());
                    odgovor.setText("");
                    adapter.notifyDataSetChanged();
                }
            }
        });

        dodajTacanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(odgovor.getText().toString().length() > 0 && !odgovori.contains(odgovor.getText().toString()) && tacanPos == -1){
                    tacanPos = odgovori.size();
                    odgovori.add(odgovor.getText().toString());
                    odgovor.setText("");
                    adapter.notifyDataSetChanged();
                }
            }
        });

        spasiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (KvizoviAkt.kvizoviAkt.isNetworkAvailable()) {
                    boolean valid = true;
                    if (naziv.getText().toString().length() == 0 || trenutnaPitanja.contains(naziv.getText().toString())) {
                        naziv.getBackground().mutate().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                        valid = false;
                    } else {
                        naziv.getBackground().mutate().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    }

                    if (tacanPos == -1) {
                        odgovor.getBackground().mutate().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                        valid = false;
                    } else {
                        odgovor.getBackground().mutate().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    }

                    if (valid) {
                        Intent newIntent = new Intent(DodajPitanjeAkt.this, DodajKvizAkt.class);
                        newIntent.putExtra("nazivPitanja", naziv.getText().toString());
                        newIntent.putExtra("odgovoriList", odgovori);
                        newIntent.putExtra("tacanOdgovor", odgovori.get(tacanPos));
                        newIntent.putExtra("IconReturn", false);
                        newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        DodajPitanjeAkt.this.startActivity(newIntent);
                    }
                }
            }
        });
    }
}
