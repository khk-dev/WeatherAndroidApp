package com.weather.khks.weather.models;

import org.json.JSONObject;

/**
 * Created by msi on 12/11/2017.
 */

public class Channel implements JSONPopulator {
    private Units units;
    private Atmosphere atmosphere;
    private Item item;

    public Units getUnits() {
        return units;
    }

    public Atmosphere getAtmosphere() {
        return atmosphere;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public void populate(JSONObject data) {
        // "units" node
        units = new Units();
        units.populate(data.optJSONObject("units"));

        // "atmosphere" node
        atmosphere = new Atmosphere();
        atmosphere.populate(data.optJSONObject("atmosphere"));

        // "item" node
        item = new Item();
        item.populate(data.optJSONObject("item"));
    }
}
