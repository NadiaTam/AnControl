package nadia.epfl.com.ancontrol;

import android.os.Parcelable;
import android.os.Parcel;

/**
 * Created by tamburra on 04.10.17.
 */

public class Patients implements Parcelable {

    String usernameEvent;
    String tokenEvent;
    String name;
    String surname;
    String number;
    String placeOfBirth;
    String dateOfBirth;
    String gender;
    String serverIP;
    int serverPort;
    Boolean first_timeSocket;
    Boolean pryvChecked;
    Boolean statusConnection;
    int counterPropofol;
    int counterMidazolam;
    int counterParacetamol;


    public Patients() {}

    public Patients(String username, String token) {
        this.usernameEvent = username;
        this.tokenEvent = token;
    }

    public Patients(String username, String token, String name, String surname, String number, String placeOfBirth, String dateOfBirth,
                    String gender, Boolean pryvChecked, String serverIP, int serverPort, Boolean first_timeSocket, Boolean statusConnection,
                    int counterPropofol, int counterMidazolam, int counterParacetamol) {
        this.usernameEvent = username;
        this.tokenEvent = token;
        this.name = name;
        this.surname = surname;
        this.number = number;
        this.dateOfBirth = dateOfBirth;
        this.placeOfBirth = placeOfBirth;
        this.gender = gender;
        this.pryvChecked = pryvChecked;
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.first_timeSocket = first_timeSocket;
        this.statusConnection = statusConnection;
        this.counterPropofol = counterPropofol;
        this.counterMidazolam = counterMidazolam;
        this.counterParacetamol = counterParacetamol;
    }

    private Patients(Parcel in) {
        usernameEvent = in.readString();
        tokenEvent = in.readString();
        name = in.readString();
        surname = in.readString();
        number = in.readString();
        gender = in.readString();
        placeOfBirth = in.readString();
        dateOfBirth = in.readString();
        pryvChecked =in.readInt()==1;
        serverIP = in.readString();
        serverPort = in.readInt();
        first_timeSocket = in.readInt()==1;
        statusConnection = in.readInt()==1;
        counterPropofol = in.readInt();
        counterMidazolam = in.readInt();
        counterParacetamol = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(usernameEvent);
        dest.writeString(tokenEvent);
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeString(number);
        dest.writeString(placeOfBirth);
        dest.writeString(gender);
        dest.writeString(dateOfBirth);
        dest.writeInt(pryvChecked ? 1 : 0);
        dest.writeString(serverIP);
        dest.writeInt(serverPort);
        dest.writeInt(first_timeSocket ? 1 : 0);
        dest.writeInt(statusConnection ? 1 : 0);
        dest.writeInt(counterPropofol);
        dest.writeInt(counterMidazolam);
        dest.writeInt(counterParacetamol);
    }

    public static final Parcelable.Creator<Patients> CREATOR = new Parcelable.Creator<Patients>() {

        @Override
        public Patients createFromParcel(Parcel in) {
            return new Patients(in);
        }

        @Override
        public Patients[] newArray(int size) {
            return new Patients[size];
        }
    };

    public Boolean getFirst_timeSocket() {
        return first_timeSocket;
    }
    public void setFirst_timeSocket(Boolean first_timeSocket) {
        this.first_timeSocket = first_timeSocket;
    }

    public void setIPserver(String serverIP){
        this.serverIP = serverIP;
    }
    public String getIPserver(){
        return this.serverIP;
    }

    public void setPORTserver(int serverPORT){
        this.serverPort = serverPORT;
    }
    public int getPORTserver(){
        return this.serverPort;
    }

    public String getUsername() {
        return this.usernameEvent;
    }
    public void setUsername(String username) {
        this.usernameEvent = username;
    }

    public String getToken() {
        return this.tokenEvent;
    }
    public void setToken(String token) {
        this.tokenEvent = token;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return this.surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNumber() {
        return this.number;
    }
    public void setNumber(String number) {
        this.number= number;
    }

    public String getPlaceOfBirth() {
        return this.placeOfBirth;
    }
    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return this.gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public Boolean getPryvChecked() {
        return this.pryvChecked;
    }
    public void setPryvChecked(Boolean pryvChecked){
        this.pryvChecked = pryvChecked;
    }

    public Boolean getStatusConnection() {
        return statusConnection;
    }
    public void setStatusConnection(Boolean statusConnection) {
        this.statusConnection = statusConnection;
    }
    public int getCounterPropofol() {
        return this.counterPropofol;
    }
    public void setCounterPropofol(int counterPropofol){
        this.counterPropofol = counterPropofol;
    }

    public int getCounterMidazolam() {
        return this.counterMidazolam;
    }
    public void setCounterMidazolam(int counterMidazolam){
        this.counterMidazolam = counterMidazolam;
    }

    public int getCounterParacetamol() {
        return this.counterParacetamol;
    }
    public void setCounterParacetamol(int counterParacetamol){
        this.counterParacetamol = counterParacetamol;
    }
}

