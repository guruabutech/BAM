package crescentcitydevelopment.com.bam;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {

    public EventAdapter(Context context, int resource, List<Event>objects){
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.event_list_item, parent, false);
        }
        TextView eventAdminName = (TextView) convertView.findViewById(R.id.adminName);
        TextView eventStatus = (TextView) convertView.findViewById(R.id.eventStatus);
        TextView eventNameView = (TextView) convertView.findViewById(R.id.event_list_name);
        TextView eventDescriptionView = (TextView) convertView.findViewById(R.id.event_list_description);
        Event event = getItem(position);
        eventAdminName.setText(event.getAdmin().getUserName());
        eventNameView.setText(event.getEventName());
        eventDescriptionView.setText(event.getEventDescription());
        String eventPublic = "Event Status: Public";
        String eventPrivate = "Event Status: Private";
        if(event.getPrivateEvent()){
            eventStatus.setText(eventPublic);
        }else{
            eventStatus.setText(eventPrivate);

        }
        return convertView;
    }
}
