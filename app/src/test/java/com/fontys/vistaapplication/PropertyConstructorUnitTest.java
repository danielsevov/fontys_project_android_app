package com.fontys.vistaapplication;

import static org.junit.Assert.assertEquals;

import com.fontys.vistaapplication.API.PropertyConstructor;

import org.junit.Test;

/**
 * Unit test for PropertyConstructor.
 */
public class PropertyConstructorUnitTest {
    private PropertyConstructor propertyConstructor;

    @Test
    public void add_get_property_test() {
        propertyConstructor = new PropertyConstructor();
        propertyConstructor.addProperty("prop1", "This is property 1");
        assertEquals("This is property 1", propertyConstructor.getProperty("prop1"));
    }

    @Test
    public void construct_test() {
        propertyConstructor = new PropertyConstructor();
        propertyConstructor.addProperty("prop1", "This is property 1");
        assertEquals("{\"prop1\":\"This is property 1\"}", propertyConstructor.construct());
    }
}