package ro.prc.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ro.prc.android.R;
import ro.prc.android.models.CNP;

public class CnpAdapter extends RecyclerView.Adapter<CnpAdapter.CnpHolder> {
    private LayoutInflater inflater;
    private final ArrayList<CNP> cnpList;

    public CnpAdapter(Context context, ArrayList<CNP> cnps) {
        this.inflater = LayoutInflater.from(context);
        this.cnpList = new ArrayList<>(cnps);
    }

    @Override
    public CnpHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View view = inflater.inflate(R.layout.cnp_item, viewGroup, false);
        return new CnpHolder(view);
    }

    @Override
    public void onBindViewHolder(CnpHolder cnpHolder, final int i) {
        final CNP cnp = cnpList.get(i);

        cnpHolder.cnp.setText(cnp.getCnp());
        cnpHolder.name.setText(cnp.getName() + " " + cnp.getSurname());
    }

    @Override
    public int getItemCount() {
        return cnpList.size();
    }

    public class CnpHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView cnp;

        public CnpHolder(final View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.idItemName);
            cnp = (TextView) itemView.findViewById(R.id.idItemCnp);
        }
    }


}
