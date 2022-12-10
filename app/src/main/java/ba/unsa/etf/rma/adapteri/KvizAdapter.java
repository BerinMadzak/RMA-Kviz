package ba.unsa.etf.rma.adapteri;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconHelper;
import com.maltaisn.icondialog.IconView;

import java.io.Console;
import java.util.List;

import javax.xml.transform.Source;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Kviz;

public class KvizAdapter extends ArrayAdapter<Kviz> {

    int resource;
    Context context;

    public KvizAdapter(Context context, int _resource, List<Kviz> items){
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

        Kviz classInstance = getItem(position);
        TextView text = (TextView)newView.findViewById(R.id.Itemname);
        text.setText(classInstance.getNaziv());
        ImageView image = (ImageView) newView.findViewById(R.id.icon);
        while(!IconHelper.getInstance(context).isDataLoaded()){

        }
        if(!classInstance.getKategorija().getNaziv().equals("DodajKviz"))
        {
            if(classInstance.getKategorija() != null)
                image.setImageDrawable(IconHelper.getInstance(context).getIcon(Integer.parseInt(classInstance.getKategorija().getId())).getDrawable(context));
            else
                image.setImageDrawable(IconHelper.getInstance(context).getIcon(0).getDrawable(context));
        }else{
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.slika1));
        }
        return newView;
    }
}
