package nadia.epfl.com.ancontrol;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;


/**
 * Created by tamburra on 18.08.17.
 */

public class MyListAdapter extends ArrayAdapter<Device> {

    //We first need to specify how many views the adapter will hold
    private ArrayList<Device> list;
    private Context myContext;
    private int layout;

    static final String TAG = "MyListAdapter";

    //Constructor
    public MyListAdapter(Context context, int resource, ArrayList<Device> objects) {
        super(context, resource, objects);
        this.layout = resource;
        this.myContext = context;
        this.list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //list = new ArrayList<Device>();
        ViewHolder viewHolder = null;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) myContext).getLayoutInflater();
            view = inflater.inflate(layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.button_Name = (Button) view.findViewById(R.id.buttonName);
            viewHolder.textView_IpAddress = (TextView) view.findViewById(R.id.textview_ipAddress);
            viewHolder.textView_IpPort = (TextView) view.findViewById(R.id.textview_ipPort);
            viewHolder.buttonRemove = (ImageButton) view.findViewById(R.id.imageButton_Remove);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final Device device = list.get(position);

        viewHolder.button_Name.setText(device.getName());
        viewHolder.textView_IpAddress.setText(device.getIpAddress());
        viewHolder.textView_IpPort.setText(Integer.toString(device.getPort()));
        viewHolder.buttonRemove.setTag(position);

        viewHolder.buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = (int)v.getTag();
                Log.d(TAG, "Device " + v.getTag());
                list.remove(index);
                notifyDataSetChanged();
            }
        });

        return view;
    }

    public class ViewHolder {
        // The holder should contain a member variable for any view that will be set in the row
        public Button button_Name;
        public TextView textView_IpAddress;
        public ImageButton buttonRemove;
        public TextView textView_IpPort;

    }
}
