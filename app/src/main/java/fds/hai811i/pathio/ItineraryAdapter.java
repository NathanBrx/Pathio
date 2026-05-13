package fds.hai811i.pathio;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import fds.hai811i.pathio.databinding.ItemItineraryBinding;
import fds.hai811i.pathio.model.Itinerary;

public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ViewHolder> {

    public interface OnItinerarySelectedListener {
        void onItinerarySelected(Itinerary itinerary);
    }

    private List<Itinerary> itineraries;
    private int selectedPosition = -1;
    private OnItinerarySelectedListener listener;

    public ItineraryAdapter(List<Itinerary> itineraries, OnItinerarySelectedListener listener) {
        this.itineraries = itineraries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemItineraryBinding binding = ItemItineraryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Itinerary item = itineraries.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return itineraries.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemItineraryBinding binding;

        public ViewHolder(ItemItineraryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Itinerary item, int position) {
            binding.itineraryType.setText(item.title);
            binding.itineraryTime.setText(item.time);
            binding.itineraryDistance.setText(item.distance);
            binding.itineraryPrice.setText(item.price);

            boolean isSelected = (position == selectedPosition);

            if (isSelected) {
                binding.rootCard.setStrokeWidth(8);
                binding.btnChoisir.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D45D3B")));
                binding.btnChoisir.setTextColor(Color.WHITE);
            } else {
                binding.rootCard.setStrokeWidth(0);
                binding.btnChoisir.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EAE4DD")));
                binding.btnChoisir.setTextColor(Color.parseColor("#2D3142"));
            }

            setBadgeState(binding.badgeTime, binding.iconTime, binding.itineraryTime, isSelected);
            setBadgeState(binding.badgeDistance, binding.iconDistance, binding.itineraryDistance, isSelected);
            setBadgeState(binding.badgePrice, binding.iconPrice, binding.itineraryPrice, isSelected);

            binding.rootCard.setOnClickListener(v -> {
                if (selectedPosition != position) {
                    int previousSelected = selectedPosition;
                    selectedPosition = position;

                    notifyItemChanged(previousSelected);
                    notifyItemChanged(selectedPosition);
                }
            });

            binding.btnChoisir.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItinerarySelected(item);
                }
            });
        }

        private void setBadgeState(androidx.cardview.widget.CardView badge, android.widget.ImageView icon, android.widget.TextView text, boolean isActive) {
            if (isActive) {
                badge.setCardBackgroundColor(Color.parseColor("#D45D3B"));
                icon.setColorFilter(Color.WHITE);
                text.setTextColor(Color.WHITE);
            } else {
                badge.setCardBackgroundColor(Color.parseColor("#F4F1ED"));
                icon.setColorFilter(Color.parseColor("#D45D3B"));
                text.setTextColor(Color.parseColor("#2D3142"));
            }
        }
    }
}