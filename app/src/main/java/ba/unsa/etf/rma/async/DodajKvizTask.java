package ba.unsa.etf.rma.async;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import ba.unsa.etf.rma.aktivnosti.DodajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Kategorija;

public class DodajKvizTask extends AsyncTask<String,Void,Void> {
    @Override
    protected Void doInBackground(String... strings) {
        String url = "https://firestore.googleapis.com/v1/projects/"+KvizoviAkt.id+"/databases/(default)/documents/Kvizovi?documentId="+strings[0]+"&access_token="+KvizoviAkt.token;
        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            String document = "{\"fields\": {\"naziv\": {\"stringValue\": \""+strings[0]+"\"}, \"idKategorije\": {\"stringValue\": \""+strings[1]+"\"}, \"pitanja\": {\"arrayValue\": {\"values\": [";
            for(int i = 0; i < Integer.parseInt(strings[2]); i++)
            {
                document += "{\"stringValue\": \""+strings[3+i]+"\"}";
                if(i != Integer.parseInt(strings[2])-1)
                    document += ", ";
            }
            document += "]}}}}";
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = document.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int code = connection.getResponseCode();
            InputStream odgovor = connection.getInputStream();
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(odgovor,"utf-8"))){
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null){
                    response.append(responseLine.trim());
                };
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
