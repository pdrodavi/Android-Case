package br.com.truckpad.trucker.data.model;

import java.util.ArrayList;
import java.util.Map;

public class Post {

    public ArrayList<Object> places = new ArrayList<Object>();
    public String fuel_consumption;
    public String fuel_price;


    // Getter Methods


    public ArrayList<Object> getPlaces() {
        return places;
    }

    public void setPlaces(ArrayList<Object> places) {
        this.places = places;
    }

    public String getFuel_consumption() {
        return fuel_consumption;
    }

    public String getFuel_price() {
        return fuel_price;
    }

    // Setter Methods

    public void setFuel_consumption( String fuel_consumption ) {
        this.fuel_consumption = fuel_consumption;
    }

    public void setFuel_price( String fuel_price ) {
        this.fuel_price = fuel_price;
    }
}
