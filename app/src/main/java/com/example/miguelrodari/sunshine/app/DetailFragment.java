package com.example.miguelrodari.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.miguelrodari.sunshine.app.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    private Uri mUri;
    private static final int DETAIL_LOADER = 0;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_TAG = "#SunshineApp";
    private String mForecastStr;
    private ShareActionProvider mShareActionProvider;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_WIND = 6;
    static final int COL_WEATHER_PRESSURE = 7;
    static final int COL_WEATHER_DEGREES = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;

    private TextView dayNameView;
    private TextView dateView;
    private ImageView iconView;
    private TextView descriptionView;
    private TextView highView;
    private TextView minView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null){
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        iconView = (ImageView) rootView.findViewById(R.id.list_item_detail_icon);
        dayNameView = (TextView) rootView.findViewById(R.id.list_detail_dayname_textview);
        dateView = (TextView) rootView.findViewById(R.id.list_detail_date_textview);
        descriptionView = (TextView) rootView.findViewById(R.id.list_detail_forecast_textview);
        highView = (TextView) rootView.findViewById(R.id.list_item_detail_high_textview);
        minView = (TextView) rootView.findViewById(R.id.list_item_detail_low_textview);
        humidityView = (TextView) rootView.findViewById(R.id.list_detail_humidity_textview);
        windView = (TextView) rootView.findViewById(R.id.list_detail_wind_textview);
        pressureView = (TextView) rootView.findViewById(R.id.list_detail_pressure_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.detailfragment, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(mForecastStr != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }else{
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }

    }

    void onLocationChanged(String newLocation){
        Uri uri = mUri;
        if(null != uri){
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updateUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updateUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        if(null != mUri){
            return new CursorLoader(getActivity(),mUri,FORECAST_COLUMNS, null,null,null);
        }
        return null;
    }

    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor){
        if(!cursor.moveToFirst()){
            return;
        }

        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(cursor.getInt(COL_WEATHER_CONDITION_ID)));
        dayNameView.setText(Utility.getDayName(getActivity(), cursor.getLong(COL_WEATHER_DATE)));
        dateView.setText(Utility.getFormattedMonthDay(getActivity(), cursor.getLong(COL_WEATHER_DATE)));
        descriptionView.setText(cursor.getString(COL_WEATHER_DESC));

        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MAX_TEMP));
        highView.setText(high);
        String min = Utility.formatTemperature(getActivity(), cursor.getDouble(COL_WEATHER_MIN_TEMP));
        minView.setText(min);

        humidityView.setText(getActivity().getString(R.string.format_humidity, cursor.getFloat(COL_WEATHER_HUMIDITY)));
        windView.setText(Utility.getFormattedWind(getActivity(), cursor.getFloat(COL_WEATHER_WIND), cursor.getFloat(COL_WEATHER_DEGREES)));
        pressureView.setText(getActivity().getString(R.string.format_pressure, cursor.getFloat(COL_WEATHER_PRESSURE)));

        mForecastStr = String.format("%s - %s - %s/%s", dateView.getText(), descriptionView.getText(), high, min);
        //TextView tx = (TextView) getView().findViewById(R.id.detail_text);
        //tx.setText(mForecastStr);

        if (mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    public void onLoaderReset(Loader<Cursor> cursorLoader){

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private Intent createShareForecastIntent(){
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_TAG);
        return sendIntent;
    }
}