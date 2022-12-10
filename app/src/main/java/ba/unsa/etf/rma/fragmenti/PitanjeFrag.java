package ba.unsa.etf.rma.fragmenti;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Pitanje;

public class PitanjeFrag extends Fragment {

    private ArrayAdapter<String> adapter;

    boolean odgovoren = false;

    ArrayList<Pitanje> pitanja;

    int trenutnoPitanje = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.pitanje_fragment, container, false);

        pitanja = IgrajKvizAkt.pitanja;

        if(pitanja.size() > 0){
            final TextView tekst = (TextView) view.findViewById(R.id.tekstPitanja);
            final ListView odgovori = (ListView) view.findViewById(R.id.odgovoriPitanja);

            tekst.setText(pitanja.get(trenutnoPitanje).getNaziv());
            final ArrayList<String> odgovoriList = pitanja.get(trenutnoPitanje).dajRandomOdgovore();
            adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, odgovoriList){

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                    View view = super.getView(position, convertView, parent);
                    return view;
                }
            };
            odgovori.setAdapter(adapter);

            odgovori.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    if(odgovoren == true)
                        return;

                    InformacijeFrag ifg = (InformacijeFrag) getFragmentManager().findFragmentById(R.id.informacijePlace);
                    final TextView text = (TextView) view.findViewById(android.R.id.text1);
                    View tacanView = odgovori.getChildAt(odgovoriList.indexOf(pitanja.get(trenutnoPitanje).getTacan()));
                    final TextView text2 = (TextView) tacanView.findViewById(android.R.id.text1);

                    if(position == odgovoriList.indexOf(pitanja.get(trenutnoPitanje).getTacan())){
                        text.setBackgroundColor(getResources().getColor(R.color.zelena));
                        ifg.setTacni(ifg.getTacni()+1);
                    } else {
                        text.setBackgroundColor(getResources().getColor(R.color.crvena));
                        text2.setBackgroundColor(getResources().getColor(R.color.zelena));
                    }
                    ifg.setBrojPitanja(ifg.getBrojPitanja()-1);
                    ifg.setProcenat(((float)ifg.getTacni()/(IgrajKvizAkt.pitanja.size()-ifg.getBrojPitanja()))*100);
                    odgovoren = true;
                    trenutnoPitanje++;

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(trenutnoPitanje < pitanja.size()) {
                                tekst.setText(pitanja.get(trenutnoPitanje).getNaziv());
                                text.setBackgroundColor(Color.WHITE);
                                text2.setBackgroundColor(Color.WHITE);
                                odgovoriList.clear();
                                odgovoriList.addAll(pitanja.get(trenutnoPitanje).dajRandomOdgovore());
                                odgovoren = false;
                            }else{
                                tekst.setText("Kviz je završen!");
                                odgovoriList.clear();
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }, 2000);
                }
            });
        }
        return view;
    }
}
