package fds.hai811i.pathio;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fds.hai811i.pathio.model.Itinerary;

public class SmallItineraryAdapter extends RecyclerView.Adapter<SmallItineraryAdapter.SmallItineraryViewHolder> {

    private List<Itinerary> itineraries = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Itinerary itinerary);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItineraries(List<Itinerary> newItineraries) {
        this.itineraries = newItineraries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SmallItineraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_small_itinerary, parent, false);
        return new SmallItineraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmallItineraryViewHolder holder, int position) {
        Itinerary itinerary = itineraries.get(position);

        holder.title.setText(itinerary.title);

        String details = itinerary.time + " • " + itinerary.distance;
        holder.details.setText(details);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(itinerary);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itineraries.size();
    }

    public static class SmallItineraryViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView details;

        public SmallItineraryViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txt_itinerary_title);
            details = itemView.findViewById(R.id.txt_itinerary_details);
        }
    }
}