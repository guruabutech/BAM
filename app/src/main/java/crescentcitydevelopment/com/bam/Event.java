package crescentcitydevelopment.com.bam;


import com.google.firebase.database.Exclude;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class Event  {
    @Exclude
    private String key;
    private boolean privateEvent;
    private String eventName;
    private String eventDescription;
    private long eventHours;
    private long timeStamp;
    private User admin;
    private double longitude;
    private double latitude;
    private float radius;
    private List<User> attendees;
    private List<User> privateInvites;
    public Event(){

    }
    public Event(String eName, String eDescription, double elatitude, double longitude, long duration, float radius, List<User> attendees, User admin, boolean privateEvent, List<User> privateInvites, long timeStamp){
        this.eventName = eName;
        this.eventDescription = eDescription;
        this.latitude = elatitude;
        this.longitude = longitude;
        this.eventHours = duration;
        this.attendees = attendees;
        this.radius = radius;
        this.admin = admin;
        this.privateEvent = privateEvent;
        this.privateInvites = privateInvites;
        this.timeStamp = timeStamp;
    }
   public Event(String eName, String eDiscription, double elatitude, double longitude, long hours){
        this.eventName = eName;
        this.eventDescription = eDiscription;
        this.latitude = elatitude;
        this.longitude = longitude;
        this.eventHours = hours;
    }
    public Event(double elatitude, double elongitude, float radius, long hours, long timeStamp){
        this.latitude = elatitude;
        this.longitude = elongitude;
        this.radius = radius;
        this.eventHours = hours;
        this.timeStamp = timeStamp;
    }
    public Event(String eventName, String eventDescription){
        this.eventName = eventName;
        this.eventDescription = eventDescription;
    }




    public Event(String eventName, String eventDescription, int hours){
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventHours = hours;
    }
    public Event(String eventName, String eventDescription, User administrator) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.admin = administrator;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }
    public void setKey(String key){
        this.key = key;
    }

    public String getKey(){
        return key;
    }

    public long getEventHours(){
        return eventHours;
    }

    public User getAdmin() {
        return admin;
    }

    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }
    public float getRadius(){
        return radius;
    }
    public boolean getPrivateEvent(){ return privateEvent; }


    public List<User> getAttendees(){return attendees;}

    public List<User> getPrivateInvites(){ return privateInvites;}

    public long getTimeStamp(){ return timeStamp;}
}
