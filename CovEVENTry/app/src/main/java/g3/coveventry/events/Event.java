package g3.coveventry.events;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Date;

public class Event {
    public long id;
    public long hostID;
    public String title;
    public String description;
    public Bitmap image;
    public String venue;
    public String postCode;
    public Date dateTime;
    public Date created;

    public Event(long id, long hostID, String title, String description, Bitmap image, String venue, String postCode, Date dateTime, Date created) {
        this.id = id;
        this.hostID = hostID;
        this.title = title;
        this.description = description;
        this.image = image;
        this.venue = venue;
        this.postCode = postCode;
        this.dateTime = dateTime;
        this.created = created;
    }

    public Event(long hostID, String title, String description, Bitmap image, String venue, String postCode, Date dateTime) {
        this.hostID = hostID;
        this.title = title;
        this.description = description;
        this.image = image;
        this.venue = venue;
        this.postCode = postCode;
        this.dateTime = dateTime;
    }
}
