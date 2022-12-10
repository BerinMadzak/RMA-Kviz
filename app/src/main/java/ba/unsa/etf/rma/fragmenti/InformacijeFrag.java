package ba.unsa.etf.rma.fragmenti;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;

public class InformacijeFrag extends Fragment {

    private TextView naziv;
    private TextView brojPitanja;
    private TextView brojTacnih;
    private TextView procenat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.informacije_fragment, container, false);

        Button zavrsiBtn = (Button) view.findViewById(R.id.btnKraj);

        zavrsiBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(getActivity(), KvizoviAkt.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            myIntent.setAction("EndQuiz");
            startActivity(myIntent);
        }
    });

        if(getArguments() != null){
            naziv = (TextView) view.findViewById(R.id.infNazivKviza);
            brojPitanja = (TextView) view.findViewById(R.id.infBrojPreostalihPitanja);
            brojTacnih = (TextView) view.findViewById(R.id.infBrojTacnihPitanja);
            procenat = (TextView) view.findViewById(R.id.infProcenatTacni);
            naziv.setText(getArguments().getString("nazivKviza"));
            brojPitanja.setText(Integer.toString(getArguments().getInt("brojPitanja")));
            brojTacnih.setText(Integer.toString(getArguments().getInt("brojTacnih")));
            procenat.setText(Float.toString(getArguments().getFloat("procenat")) + "%");
        }



        return view;
    }

    public void setBrojPitanja(int value){
        brojPitanja.setText(Integer.toString(value));
    }

    public void setTacni(int value){
        brojTacnih.setText(Integer.toString(value));
    }

    public void setProcenat(float value){
        procenat.setText(String.format("%.2f", value)+"%");
    }

    public int getBrojPitanja(){
        return Integer.parseInt(brojPitanja.getText().toString());
    }

    public int getTacni(){
        return Integer.parseInt(brojTacnih.getText().toString());
    }
}
