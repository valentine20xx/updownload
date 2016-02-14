package com.example.pebble;

import com.mitchellbosecke.pebble.extension.Filter;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LastModifiedFilter implements Filter {
    @Override
    public Object apply(Object input, Map<String, Object> args) {
        return formatLastModifiedToDate((Long) input);
    }

    @Override
    public List<String> getArgumentNames() {
        return null;
    }

    private Date formatLastModifiedToDate(long lastModified) {
        return new Date(lastModified);
    }
}
