package ba.unsa.etf.rma.aktivnosti;

import android.content.Intent;
import android.provider.AlarmClock;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.klase.Pitanje;

public class IgrajKvizAkt extends AppCompatActivity {

    public static ArrayList<Pitanje> pitanja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz_akt);

        FragmentManager fm = getSupportFragmentManager();
        FrameLayout linformacije = (FrameLayout)findViewById(R.id.informacijePlace);
        FrameLayout lpitanja = (FrameLayout)findViewById(R.id.pitanjePlace);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        minute += Math.ceil((float)pitanja.size()/2);
        int extraHours = minute/60;
        minute %= 60;
        hour += extraHours;
        hour %= 24;

        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
        startActivity(intent);

        if(linformacije != null){
            InformacijeFrag ifg;
            ifg = (InformacijeFrag)fm.findFragmentById(R.id.informacijePlace);
            if(ifg == null){
                ifg = new InformacijeFrag();
                Bundle arguments = new Bundle();
                arguments.putString("nazivKviza", getIntent().getStringExtra("nazivKviza"));
                arguments.putInt("brojPitanja", pitanja.size());
                arguments.putInt("brojTacnih", 0);
                arguments.putFloat("procenat", 0);
                ifg.setArguments(arguments);
                fm.beginTransaction().replace(R.id.informacijePlace, ifg).commit();
            }
        }

        if(lpitanja != null){
            PitanjeFrag pfg;
            pfg = (PitanjeFrag)fm.findFragmentById(R.id.pitanjePlace);
            if(pfg == null){
                pfg = new PitanjeFrag();
                fm.beginTransaction().replace(R.id.pitanjePlace, pfg).commit();
            }
        }

    }
}
