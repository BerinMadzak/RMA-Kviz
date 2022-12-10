package ba.unsa.etf.rma.klase;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;

public class Pitanje implements Parcelable {

    private String naziv;
    private String tekstPitanja;
    ArrayList<String> odgovori;
    private String tacan;

    public Pitanje(String naziv, String tekstPitanja, ArrayList<String> odgovori, String tacan) {
        this.naziv = naziv;
        this.tekstPitanja = tekstPitanja;
        this.odgovori = odgovori;
        this.tacan = tacan;
    }

    public ArrayList<String> dajRandomOdgovore(){
        ArrayList<String> randomOdgovori = new ArrayList<>();
        randomOdgovori.addAll(odgovori);
        Collections.shuffle(randomOdgovori);
        return randomOdgovori;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getTekstPitanja() {
        return tekstPitanja;
    }

    public void setTekstPitanja(String tekstPitanja) {
        this.tekstPitanja = tekstPitanja;
    }

    public ArrayList<String> getOdgovori() {
        return odgovori;
    }

    public void setOdgovori(ArrayList<String> odgovori) {
        this.odgovori = odgovori;
    }

    public String getTacan() {
        return tacan;
    }

    public void setTacan(String tacan) {
        this.tacan = tacan;
    }

    @Override
    public String toString() {
        return naziv;
    }

    protected Pitanje(Parcel in) {
        naziv = in.readString();
        tekstPitanja = in.readString();
        in.readStringList(odgovori);
        tacan = in.readString();
    }

    public static final Creator<Pitanje> CREATOR = new Creator<Pitanje>() {
        @Override
        public Pitanje createFromParcel(Parcel in) {
            return new Pitanje(in);
        }
        @Override
        public Pitanje[] newArray(int size) {
            return new Pitanje[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(naziv);
        dest.writeString(tekstPitanja);
        dest.writeStringList(odgovori);
        dest.writeString(tacan);
    }
}
