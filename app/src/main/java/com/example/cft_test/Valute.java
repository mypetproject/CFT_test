package com.example.cft_test;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;

public class Valute extends BaseObservable {

    private final Locale LOCALE;

    private String ID;
    private String charCode;
    private int nominal;
    private String name;
    private double value;
    private String rublesAmount = "";
    private String valuteAmount = "";


    public Valute(String id, String charCode, int nominal, String name, double value, String rublesAmount, Locale locale) {
        this.ID = id;
        this.charCode = charCode;
        this.nominal = nominal;
        this.name = name;
        this.value = value;
        this.LOCALE = locale;

        setRublesAmount(rublesAmount);
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
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
        NumberFormat format = NumberFormat.getInstance(LOCALE);

        try {
            this.rublesAmount = String.format(LOCALE, "%,.2f", Objects.requireNonNull(format.parse(rublesAmount)).doubleValue());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        notifyPropertyChanged(BR.rublesAmount);
    }

    @Bindable
    public String getValuteAmount() {
        return valuteAmount;
    }

    public void setValuteAmount(String valuteAmount) {
        this.valuteAmount = valuteAmount;
        notifyPropertyChanged(BR.valuteAmount);
    }

}
