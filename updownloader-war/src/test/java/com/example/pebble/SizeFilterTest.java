package com.example.pebble;

import com.mitchellbosecke.pebble.extension.Filter;
import org.junit.Assert;
import org.junit.Test;

public class SizeFilterTest {

    @Test
    public void formatFileSizeTest() {
        Filter filter = new SizeFilter();

        String b = (String) filter.apply(1L, null);
        Assert.assertEquals("1.00 Bytes", b);

        String kb = (String) filter.apply(1024L, null);
        Assert.assertEquals("1.00 KB", kb);

        String mb = (String) filter.apply(1048576L, null);
        Assert.assertEquals("1.00 MB", mb);

        String gb = (String) filter.apply(1073741824L, null);
        Assert.assertEquals("1.00 GB", gb);

        String tb = (String) filter.apply(1099511627776L, null);
        Assert.assertEquals("1.00 TB", tb);
    }
}
