package ba.unsa.etf.rma.async;

import android.icu.util.LocaleData;
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

public class NabaviKategorijeTask extends AsyncTask<String, Void, Void> {

    private OnKategorijeSearchDone pozivatelj;
    public NabaviKategorijeTask(OnKategorijeSearchDone p) {pozivatelj = p;}

    private ArrayList<Kategorija> kategorije = new ArrayList<>();

    @Override
    protected Void doInBackground(String... strings) {
        String url1 = "https://firestore.googleapis.com/v1/projects/"+ KvizoviAkt.id+"/databases/(default)/documents/Kategorije?access_token="+KvizoviAkt.token;
        try{
            URL url = new URL(url1);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + KvizoviAkt.token);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String rez = convertStreamToString(in);
            JSONObject jo = new JSONObject(rez);
            JSONArray docs = jo.getJSONArray("documents");
            for(int i = 0; i < docs.length(); i++) {
                JSONObject cat = docs.getJSONObject(i);
                JSONObject fields = cat.getJSONObject("fields");
                Kategorija catObj = new Kategorija(fields.getJSONObject("naziv").getString("stringValue"), fields.getJSONObject("idIkonice").getString("stringValue"));
                kategorije.add(catObj);
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
        pozivatelj.onDone(kategorije);
    }

    public interface OnKategorijeSearchDone {
        public void onDone(ArrayList<Kategorija> kategorije2);
    }
}
