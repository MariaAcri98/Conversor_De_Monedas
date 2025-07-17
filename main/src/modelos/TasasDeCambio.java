package modelos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class TasasDeCambio {
    private String base;
    private String date;

    @SerializedName("conversion_rates")
    private Map<String, Double> rates;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    public Map<String, Double>getRates(){
        return rates;
    }
    public void setRastes(Map<String, Double> rates){
        this.rates = rates;
    }

}
