package com.example.cft_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivityViewModel extends ViewModel {

    final Realm realm = Realm.getDefaultInstance();
    final long MINIMAL_UPDATE_TIME = 60000; // milliseconds

    final private String url = "https://www.cbr-xml-daily.ru/daily_json.js";

    final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    private String chosenValuteID;
    private String charCode;
    private int nominal;
    private String name;
    private double value;
    private String rublesAmount = "1";

    SharedPreferences sharedPreferences;

    Date lastUpdateDateTime = null;

    public String getChosenValuteID() {
        return chosenValuteID;
    }

    public void setChosenValuteID(String chosenValuteID) {
        this.chosenValuteID = chosenValuteID;
    }

    public String getCharCode() {
        return charCode;
    }

    public void setCharCode(String charCode) {
        this.charCode = charCode;
    }

    public int getNominal() {
        return nominal;
    }

    public void setNominal(int nominal) {
        this.nominal = nominal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getRublesAmount() {
        return rublesAmount;
    }

    public void setRublesAmount(String rublesAmount) {
        this.rublesAmount = rublesAmount;
    }

    public void updateValutesWithDateCheck(Context context, RequestQueue queue) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastUpdate = sharedPreferences.getString("last_update_date", "1970-01-01T00:00:00+00:00");

        try {
            lastUpdateDateTime = FORMAT.parse(lastUpdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date currentDate = new Date();

        long currentDiff = currentDate.getTime() - lastUpdateDateTime.getTime();

        if (currentDiff > MINIMAL_UPDATE_TIME) {
            doDataRequest(context, queue);
        }
    }

    public void doDataRequest(Context context, RequestQueue queue) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, response -> {

                    try {
                        Date dataDate = FORMAT.parse(response.getString("Timestamp"));

                        if (dataDate.getTime() > lastUpdateDateTime.getTime()) {

                            workWithJson(response);

                            ((MainActivity) context).refillValutes();

                            Toast.makeText(context, context.getString(R.string.data_updated), Toast.LENGTH_SHORT).show();
                        }

                        setNewLastUpdateDate();

                        ((MainActivity) context).stopRefresh();

                    } catch (ParseException | JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);

        queue.add(jsonObjectRequest);
    }

    private void setNewLastUpdateDate() {

        Date currentDate = new Date();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("last_update_date", FORMAT.format(currentDate));
        editor.apply();
    }

    private void workWithJson(JSONObject response) throws JSONException {

        JSONObject valute = response.getJSONObject("Valute");

        if (valute != null) writeNewValuteDataToRealm(valute);
    }

    private void writeNewValuteDataToRealm(JSONObject valute) throws JSONException {
        Iterator<String> keys = valute.keys();

        while (keys.hasNext()) {
            String key = keys.next();

            JSONObject chosenValute = valute.getJSONObject(key);

            realm.beginTransaction();

            ValuteModel valuteModel = realm.where(ValuteModel.class).equalTo("id", chosenValute.getString("ID")).findFirst();

            if (valuteModel == null) {
                valuteModel = realm.createObject(ValuteModel.class, chosenValute.getString("ID"));
                valuteModel.setCharCode(chosenValute.getString("CharCode"));
                valuteModel.setName(chosenValute.getString("Name"));
            }

            valuteModel.setNominal(chosenValute.getInt("Nominal"));
            valuteModel.setValue(chosenValute.getDouble("Value"));

            realm.commitTransaction();
        }
    }

    public void updateValutesWithoutDateCheck(Context context, RequestQueue queue) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastUpdate = sharedPreferences.getString("last_update_date", "1970-01-01T00:00:00+00:00");

        try {
            lastUpdateDateTime = FORMAT.parse(lastUpdate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        doDataRequest(context, queue);
    }

    public List<String> getValutes() {

        List<String> valutes = new ArrayList<>();

        RealmResults<ValuteModel> valuteModels = realm.where(ValuteModel.class).findAll();

        for (ValuteModel valuteModel : valuteModels) {
            valutes.add(valuteModel.getName());
        }

        return valutes;
    }

    public void setChosenValutebyName(String chosenValuteName) {
        this.chosenValuteID = realm.where(ValuteModel.class).equalTo("name", chosenValuteName).findFirst().getId();
        setChosenValuteData();
    }

    private void setChosenValuteData() {
        ValuteModel valuteModel = realm.where(ValuteModel.class).equalTo("id", chosenValuteID).findFirst();
        this.charCode = valuteModel.getCharCode();
        this.nominal = valuteModel.getNominal();
        this.name = valuteModel.getName();
        this.value = valuteModel.getValue();
    }

}