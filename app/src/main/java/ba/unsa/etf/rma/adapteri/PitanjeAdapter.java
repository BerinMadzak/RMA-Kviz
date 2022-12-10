package ba.unsa.etf.rma.adapteri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maltaisn.icondialog.IconHelper;

import java.util.List;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

public class PitanjeAdapter extends ArrayAdapter<Pitanje> {
    int resource;
    Context context;

    public PitanjeAdapter(Context context, int _resource, List<Pitanje> items){
        super(context, _resource, items);
        this.context = context;
        resource = _resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LinearLayout newView;
        if(convertView == null){
            newView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li;
            li = (LayoutInflater)getContext().getSystemService(inflater);
            li.inflate(resource, newView, true);
        }else{
            newView = (LinearLayout)convertView;
        }

        Pitanje classInstance = getItem(position);
        TextView text = (TextView)newView.findViewById(R.id.Itemname);
        text.setText(classInstance.getNaziv());
        ImageView image = (ImageView) newView.findViewById(R.id.icon);
        if(!classInstance.getNaziv().equals("Dodaj Pitanje"))
        {
            image.setImageDrawable(null);
        }else{
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.slika1));
        }
        return newView;
    }
}
