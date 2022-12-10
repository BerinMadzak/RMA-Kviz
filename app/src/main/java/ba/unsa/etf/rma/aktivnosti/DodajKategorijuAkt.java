package ba.unsa.etf.rma.aktivnosti;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;

public class DodajKategorijuAkt extends AppCompatActivity implements  IconDialog.Callback {

    Icon[] selectedIcons;

    EditText ikonaID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        final EditText nazivText = (EditText)findViewById(R.id.etNaziv);
        ikonaID = (EditText)findViewById(R.id.etIkona);
        final Button ikonaBtn = (Button)findViewById(R.id.btnDodajIkonu);
        Button kategorijaBtn = (Button)findViewById(R.id.btnDodajKategoriju);

        final TextView tw = (TextView)findViewById(R.id.twKategorijaPostoji);

        final IconDialog iconDialog = new IconDialog();

        ikonaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconDialog.setSelectedIcons(selectedIcons);
                iconDialog.show(getSupportFragmentManager(), "icon_dialog");
            }
        });

        kategorijaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (KvizoviAkt.kvizoviAkt.isNetworkAvailable()) {
                    boolean valid = true;
                    if (nazivText.getText().toString().length() == 0 || categoryExists(nazivText.getText().toString())) {
                        valid = false;
                        nazivText.getBackground().mutate().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                        tw.setText("Unesena kategorija već postoji!");
                    } else {
                        nazivText.getBackground().mutate().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                        tw.setText("");
                    }

                    if (ikonaID.getText().toString().length() == 0 || categoryIconExists(ikonaID.getText().toString())) {
                        valid = false;
                        ikonaID.getBackground().mutate().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                    } else {
                        ikonaID.getBackground().mutate().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                    }

                    if (valid) {
                        Kategorija kat = new Kategorija(nazivText.getText().toString(), ikonaID.getText().toString());
                        Intent myIntent = new Intent(DodajKategorijuAkt.this, DodajKvizAkt.class);
                        myIntent.putExtra("Kategorija", kat);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        myIntent.setAction("Icon");
                        DodajKategorijuAkt.this.startActivity(myIntent);
                    }
                }
            }
        });
    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        ikonaID.setText(Integer.toString(icons[0].getId()));
    }

    private boolean categoryExists(String name){
        for(Kategorija kat : KvizoviAkt.kategorije)
            if(kat.getNaziv().equals(name))
                return true;
        return false;
    }

    private boolean categoryIconExists(String id){
        for(Kategorija kat : KvizoviAkt.kategorije)
            if(kat.getId().equals(id))
                return true;
        return false;
    }

}
