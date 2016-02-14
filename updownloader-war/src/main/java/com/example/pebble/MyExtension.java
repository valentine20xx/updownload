package com.example.pebble;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import java.util.HashMap;
import java.util.Map;

public class MyExtension extends AbstractExtension {
    @Override
    public Map<String, Filter> getFilters() {

        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("sizeFilter", new SizeFilter());
        filterMap.put("lastModifiedFilter",new LastModifiedFilter());

        return filterMap;
    }
}
