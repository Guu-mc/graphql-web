package com.github.mc.graphql.web.scalar;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class DateScalarType extends GraphQLScalarType {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 
    public DateScalarType() {
        super("Date", "Date value", new Coercing<Date, String>() {
            @Override
            public String serialize(Object o) {
                return SDF.format((Date) o);
            }
 
            @Override
            public Date parseValue(Object o) {
                String value = String.valueOf(o);
                if ("null".equalsIgnoreCase(value) || "".equalsIgnoreCase(value)) {
                    return null;
                }
                try {
                    return SDF.parse(value);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }
            }
 
            @Override
            public Date parseLiteral(Object o) {
                String value = String.valueOf(o);
                if ("null".equalsIgnoreCase(value) || "".equalsIgnoreCase(value)) {
                    return null;
                }
                try {
                    return SDF.parse(value);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }
}