package com.weather.khks.weather.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.weather.khks.weather.R;
import com.weather.khks.weather.models.City;

import java.util.List;

/**
 * Created by KhKS on 10/11/2017.
 */

public class CityListAdapter extends ArrayAdapter<City> {
    private Activity context;
    private List<City> cityList;

    public CityListAdapter(Activity context, List<City> cityList){
        super(context, R.layout.list_layout_weather, cityList);
        this.context = context;
        this.cityList = cityList;
    }

    /*
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.list_layout_weather, null,true);

        TextView textViewCity = (TextView) listViewItem.findViewById(R.id.textViewCity);
        TextView textViewCityID = (TextView) listViewItem.findViewById((R.id.textViewCityID));

        City city= cityList.get(position);

        textViewCity.setText(city.getCityName());
        textViewCityID.setText(city.getCityID());
        return listViewItem;
    }
    */

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        ViewHolder holder = null;

        City city= cityList.get(position);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_layout_weather, null);
            holder = new ViewHolder();
            holder.textViewCity = (TextView) convertView.findViewById(R.id.textViewCity);
            holder.textViewCityID = (TextView) convertView.findViewById(R.id.textViewCityID);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textViewCity.setText(city.getCityName());
        holder.textViewCityID.setText(city.getCityID());
        return convertView;
    }

    static class ViewHolder {
        TextView textViewCity;
        TextView textViewCityID;
    }
}
