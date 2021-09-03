package com.example.cft_test;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ValuteModel extends RealmObject {

    @PrimaryKey
    private String id;
    private String charCode;
    private int nominal;
    private String name;
    private Double value;

    public ValuteModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setValue(double value) {
        this.value = value;
    }


}
