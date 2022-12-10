package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Kategorija;

public class ListaFrag extends Fragment {

    private int lastPos = -1;

    private ArrayAdapter<Kategorija> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lista_fragment, container, false);

        final ListView kategorijeList = (ListView)view.findViewById(R.id.listaKategorija);

        adapter = new ArrayAdapter<Kategorija>(getActivity(), android.R.layout.simple_list_item_1, KvizoviAkt.kategorije);
        kategorijeList.setAdapter(adapter);

        kategorijeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == lastPos)
                    return;

                lastPos = position;
                DetailFrag dfg;
                dfg = new DetailFrag();
                Bundle arguments = new Bundle();
                arguments.putInt("pozicija", position);
                dfg.setArguments(arguments);
                KvizoviAkt.dfg = dfg;
                getFragmentManager().beginTransaction().replace(R.id.detailPlace, dfg).commit();
            }
        });

        return view;
    }

    public void update(){
        adapter.notifyDataSetChanged();
    }
}
