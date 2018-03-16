package nadia.epfl.com.ancontrol;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by tamburra on 03.10.17.
 */

public class MyPryvAdapter extends RecyclerView.Adapter<MyPryvAdapter.ViewHolder> {

    private ArrayList<Patients> mCustomEvent;
    private ArrayList<Patients> patients;
    private Context mContext;

    static final String TAG = "MyPryvAdapter";

    public MyPryvAdapter(Context context, ArrayList<Patients> events, ArrayList<Patients> patients) {
        mCustomEvent = events;
        mContext = context;
        this.patients = patients;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView usernameView;
        public TextView tokenView;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameView = (TextView) itemView.findViewById(R.id.textView_username);
            tokenView = (TextView) itemView.findViewById(R.id.textView_token);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();

                    if (pos != RecyclerView.NO_POSITION) {
                        Patients clickedDataItem = mCustomEvent.get(pos);
                        v.setBackgroundColor(Color.MAGENTA);
                        patients.get(patients.size()-1).setUsername(clickedDataItem.getUsername());
                        patients.get(patients.size()-1).setToken(clickedDataItem.getToken());

                        Log.d(TAG, "patients: " + patients.get(patients.size()-1) + " " + "name: " + patients.get(patients.size()-1).getName() + " " + "surname: " + patients.get(patients.size()-1).getSurname() +
                        " " + "number" + patients.get(patients.size()-1).getNumber() + " " + "token" + patients.get(patients.size()-1).getToken() + " " + "username" + patients.get(patients.size()-1).getUsername());

                        Intent intent = new Intent(mContext, GraphActivity.class);
                        intent.putExtra("NUMBER_OF_PATIENTS", patients.size());
                        intent.putParcelableArrayListExtra("PATIENTINFO", patients);
                        intent.putExtra("FROM", "PryvTokenActivity");
                        mContext.startActivity(intent);
                    }
                }
            });
        }
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public MyPryvAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.tokenlayout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyPryvAdapter.ViewHolder holder, int position) {

        Patients object = mCustomEvent.get(position);
        String objectUsername = object.getUsername();
        String objectToken = object.getToken();
        holder.usernameView.setText(objectUsername);
        holder.tokenView.setText(objectToken);
    }

    @Override
    public int getItemCount() {
        return mCustomEvent.size();
    }
}
