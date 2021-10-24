package com.example.weatherapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.adapters.CardViewBindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.databinding.WeatherAdapterBinding;
import com.example.weatherapp.model.WeatherModel;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.viewHolder> {
    ArrayList<WeatherModel> weatherModels;
    LayoutInflater inflater;

    public WeatherAdapter(ArrayList<WeatherModel> weatherModels) {
        this.weatherModels = weatherModels;
    }

    @Override
    public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (inflater==null){
            inflater=LayoutInflater.from(parent.getContext());
        }
        return new viewHolder(DataBindingUtil.inflate(inflater, R.layout.weather_adapter,parent,false));
    }

    @Override
    public void onBindViewHolder(WeatherAdapter.viewHolder holder, int position) {
        WeatherModel weatherModel=weatherModels.get(position);
        holder.binding.temperatureTv.setText(weatherModel.getTemperature()+"°C");
        Picasso.get().load("https:".concat(weatherModel.getIcon())).into(holder.binding.conditionImageView);
        holder.binding.windSpeedTextView.setText(weatherModel.getWindSpeed()+"Km/h");
        SimpleDateFormat input=new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat output=new SimpleDateFormat("hh:mm aa");
        try{
            Date t=input.parse(weatherModel.getTime());
            holder.binding.timeTextView.setText(output.format(t));

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public int getItemCount() {
        return weatherModels.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        WeatherAdapterBinding binding;

        public viewHolder(WeatherAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
