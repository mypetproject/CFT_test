package com.example.cft_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.databinding.Bindable;
import androidx.databinding.Observable;
import androidx.databinding.PropertyChangeRegistry;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivityViewModel extends ViewModel implements Observable {

    private PropertyChangeRegistry callbacks = new PropertyChangeRegistry();

    final Realm realm = Realm.getDefaultInstance();
    final long MINIMAL_UPDATE_TIME = 60000; // milliseconds

    final private String url = "https://www.cbr-xml-daily.ru/daily_json.js";

    final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    private String chosenValuteID;
    private String charCode;
    private int nominal;
    private String name;
    private double value;
    private String rublesAmount;
    private String valuteAmount = "";

    private String rublesTILHint;
    private String valuteTILHint;

    private Locale locale;

    SharedPreferences sharedPreferences;

    Date lastUpdateDateTime = null;

    @Bindable
    public String getChosenValuteID() {
        return chosenValuteID;
    }

    public void setChosenValuteID(String chosenValuteID) {
        this.chosenValuteID = chosenValuteID;
        notifyPropertyChanged(BR.chosenValuteID);
    }

    @Bindable
    public String getCharCode() {
        return charCode;
    }

    public void setCharCode(String charCode) {
        this.charCode = charCode;
        notifyPropertyChanged(BR.charCode);
    }

    @Bindable
    public int getNominal() {
        return nominal;
    }

    public void setNominal(int nominal) {
        this.nominal = nominal;
        notifyPropertyChanged(BR.nominal);
    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    @Bindable
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
        notifyPropertyChanged(BR.value);
    }

    @Bindable
    public String getRublesAmount() {
        return rublesAmount;
    }

    public void setRublesAmount(String rublesAmount) {
        NumberFormat format = NumberFormat.getInstance(locale);

        try {
            this.rublesAmount = String.format(locale, "%,.2f", Objects.requireNonNull(format.parse(rublesAmount)).doubleValue());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        notifyPropertyChanged(BR.rublesAmount);
    }


    @Bindable
    public String getValuteAmount() {
        return valuteAmount;
    }

    public void setValuteAmount() {

        NumberFormat format = NumberFormat.getInstance(locale);

        try {
            this.valuteAmount = (String.format(locale, "%,.2f", (Objects.requireNonNull(format.parse(rublesAmount)).doubleValue() * nominal / value)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (this.valuteAmount.equals("NaN")) this.valuteAmount = "";

        notifyPropertyChanged(BR.valuteAmount);
    }

    @Bindable
    public String getRublesTILHint() {

        return rublesTILHint;
    }

    public void setRublesTILHint() {
        this.rublesTILHint = this.charCode == null ?
                ""
                : "1 RUB = "
                + String.format(locale, " %, .4f", (((double) this.nominal) / this.value))
                + " "
                + this.charCode;

        notifyPropertyChanged(BR.rublesTILHint);
    }

    @Bindable
    public String getValuteTILHint() {
        return valuteTILHint;
    }

    public void setValuteTILHint() {
        this.valuteTILHint = this.charCode == null ?
                ""
                : this.nominal
                + " "
                + this.charCode
                + " = "
                + String.format(locale, "%,.4f", (this.value))
                + " RUB";

        notifyPropertyChanged(BR.valuteTILHint);
    }


    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
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

    public void setChosenValuteByName(String chosenValuteName) {
        setChosenValuteID(realm.where(ValuteModel.class).equalTo("name", chosenValuteName).findFirst().getId());
        setChosenValuteData();
    }

    private void setChosenValuteData() {
        ValuteModel valuteModel = realm.where(ValuteModel.class).equalTo("id", chosenValuteID).findFirst();
        setCharCode(valuteModel.getCharCode());
        setNominal(valuteModel.getNominal());
        setName(valuteModel.getName());
        setValue(valuteModel.getValue());
        setRublesTILHint();
        setValuteTILHint();
        setValuteAmount();
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {
        callbacks.remove(callback);
    }

    void notifyPropertyChanged(int fieldId) {
        callbacks.notifyCallbacks(this, fieldId, null);
    }

}