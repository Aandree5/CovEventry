package g3.coveventry.events;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Date;

public class Event {
    // Event properties
    public long id;
    public long hostID;
    public String hostName;
    public String title;
    public String description;
    public Bitmap image;
    public String venue;
    public String postCode;
    public Date dateTime;
    public Date created;
    public Boolean isTwitter;


    /**
     * Constructor for not twitter events
     *
     * @param id          Event id
     * @param hostID      User id from who created the event
     * @param hostName    User name form who created the event
     * @param title       Title of the event
     * @param description Description of the event
     * @param image       Image of the event
     * @param venue       Place where event will happen
     * @param postCode    Post code where the event will take place
     * @param dateTime    Date and time where the event will take place
     * @param created     Date and time that the event was created
     */
    public Event(long id, long hostID, String hostName, String title, String description, Bitmap image, String venue, String postCode,
                 Date dateTime, Date created) {
        this.id = id;
        this.hostID = hostID;
        this.hostName = hostName;
        this.title = title;
        this.description = description;
        this.image = image;
        this.venue = venue;
        this.postCode = postCode;
        this.dateTime = dateTime;
        this.created = created;
        this.isTwitter = false;
    }


    /**
     * Constructor allows for event from twitter, where hostId will be the twitterID
     *
     * @param id          Event id
     * @param hostID      User id from who created the event
     * @param hostName    User name form who created the event
     * @param title       Title of the event
     * @param description Description of the event
     * @param image       Image of the event
     * @param venue       Place where event will happen
     * @param postCode    Post code where the event will take place
     * @param dateTime    Date and time where the event will take place
     * @param created     Date and time that the event was created
     * @param isTwitter   True for events from twitter, user id will be twitter id
     */
    public Event(long id, long hostID, String hostName, String title, String description, Bitmap image, String venue, String postCode,
                 Date dateTime, Date created, Boolean isTwitter) {
        this.id = id;
        this.hostID = hostID;
        this.hostName = hostName;
        this.title = title;
        this.description = description;
        this.image = image;
        this.venue = venue;
        this.postCode = postCode;
        this.dateTime = dateTime;
        this.created = created;
        this.isTwitter = isTwitter;
    }
}
