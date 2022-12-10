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

public class NabaviPitanjeTask extends AsyncTask<String, Void, Void> {

    private OnPitanjeSearchDone pozivatelj;
    public NabaviPitanjeTask(OnPitanjeSearchDone p, int i) {pozivatelj = p; index = i;}

    private Pitanje p;
    private int index;

    @Override
    protected Void doInBackground(String... strings) {
        String url1 = "https://firestore.googleapis.com/v1/projects/"+ KvizoviAkt.id+"/databases/(default)/documents/Pitanja/"+strings[0]+"?access_token="+KvizoviAkt.token;
        try{
            URL url = new URL(url1);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer " + KvizoviAkt.token);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String rez = convertStreamToString(in);
            JSONObject jo = new JSONObject(rez);
            JSONObject fields = jo.getJSONObject("fields");
            String name = fields.getJSONObject("naziv").getString("stringValue");
            int tacni = fields.getJSONObject("indexTacnog").getInt("integerValue");
            JSONObject odgovoriO = fields.getJSONObject("odgovori");
            JSONObject values = odgovoriO.getJSONObject("arrayValue");
            JSONArray odgovori = values.getJSONArray("values");
            ArrayList<String> odgovoriString = new ArrayList<>();
            for (int j = 0; j < odgovori.length(); j++){
                JSONObject pjo = odgovori.getJSONObject(j);
                odgovoriString.add(pjo.getString("stringValue"));
            }
            p = new Pitanje(name, name, odgovoriString, odgovoriString.get(tacni));
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
        pozivatelj.onDone3(p, index);
    }

    public interface OnPitanjeSearchDone {
        public void onDone3(Pitanje pitanje, int index);
    }

}
