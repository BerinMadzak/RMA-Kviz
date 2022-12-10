package ba.unsa.etf.rma.async;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class NabaviKvizovePoKategorijiTask extends AsyncTask<String, Void, Void> implements NabaviPitanjeTask.OnPitanjeSearchDone {

    private NabaviKvizovePoKategorijiTask.OnKvizoviSearchDone pozivatelj;
    public NabaviKvizovePoKategorijiTask(NabaviKvizovePoKategorijiTask.OnKvizoviSearchDone p) { pozivatelj = p;}

    private ArrayList<Kviz> kvizovi = new ArrayList<>();

    @Override
    protected Void doInBackground(String... strings) {
        String url1 = "https://firestore.googleapis.com/v1/projects/"+ KvizoviAkt.id+"/databases/(default)/documents/Kvizovi?access_token="+KvizoviAkt.token;
        try{
            int reduction = 0;
            URL url = new URL(url1);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + KvizoviAkt.token);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String rez = convertStreamToString(in);
            if(rez.length() < 5)
                return null;
            JSONObject jo = new JSONObject(rez);
            JSONArray docs = jo.getJSONArray("documents");
            for(int i = 0; i < docs.length(); i++) {
                JSONObject kviz = docs.getJSONObject(i);
                JSONObject fields = kviz.getJSONObject("fields");
                String name = fields.getJSONObject("naziv").getString("stringValue");
                String kat = fields.getJSONObject("idKategorije").getString("stringValue");
                JSONObject pitanjaO = fields.getJSONObject("pitanja");
                JSONObject values = pitanjaO.getJSONObject("arrayValue");
                JSONArray pitanja = values.getJSONArray("values");
                Kategorija k = new Kategorija("GRESKA", "12");
                for (Kategorija k1 : KvizoviAkt.kategorije){
                    if (k1.getNaziv().equals(kat)) {
                        k = k1;
                        break;
                    }
                }
                if(k.getNaziv().equals(strings[0])) {

                    for (int j = 0; j < pitanja.length(); j++) {
                        JSONObject pjo = pitanja.getJSONObject(j);
                        new NabaviPitanjeTask((NabaviPitanjeTask.OnPitanjeSearchDone) this, i-reduction).execute(pjo.getString("stringValue"));
                    }
                    kvizovi.add(new Kviz(name, new ArrayList<Pitanje>(), k));
                }else{
                    reduction++;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String convertStreamToString(InputStream in){
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try{
            while((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(Void aVoid){
        super.onPostExecute(aVoid);
        pozivatelj.onDone4(kvizovi);
    }

    @Override
    public void onDone3(Pitanje pitanje, int index) {
        kvizovi.get(index).getPitanja().add(pitanje);
    }

    public interface OnKvizoviSearchDone {
        public void onDone4(ArrayList<Kviz> kvizList);
    }
}
