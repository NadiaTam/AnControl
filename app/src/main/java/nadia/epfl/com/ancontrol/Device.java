package nadia.epfl.com.ancontrol;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tamburra on 21.08.17.
 */

public class Device implements Parcelable {
    String name;
    String ip;
    int port;

    public Device (String deviceName, String deviceIpAddress, int devicePort) {
        this.name = deviceName;
        this.ip = deviceIpAddress;
        this.port = devicePort;
    }

    protected Device(Parcel in) {
        name = in.readString();
        ip = in.readString();
        port = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(ip);
        dest.writeInt(port);
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name)  {
        this.name = name;
    }

    public String getIpAddress() {
        return ip;
    }

    public void setIpAddress(String ipAddress) {
        this.ip = ipAddress;
    }

    public void setPort(int port)  {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

}
