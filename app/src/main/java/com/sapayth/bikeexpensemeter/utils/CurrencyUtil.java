package com.sapayth.bikeexpensemeter.utils;

import java.util.Comparator;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by S6H on 2/13/2018.
 */

public class CurrencyUtil {
    public static SortedMap<java.util.Currency, Locale> currencyLocaleMap;

    static {
        currencyLocaleMap = new TreeMap<java.util.Currency, Locale>(new Comparator<java.util.Currency>() {
            public int compare(java.util.Currency c1, java.util.Currency c2) {
                return c1.getCurrencyCode().compareTo(c2.getCurrencyCode());
            }
        });
        for (Locale locale : Locale.getAvailableLocales()) {
            try {
                java.util.Currency currency = java.util.Currency.getInstance(locale);
                currencyLocaleMap.put(currency, locale);
            } catch (Exception e) {
            }
        }
    }


    public static String getCurrencySymbol(String currencyCode) {
        java.util.Currency currency = java.util.Currency.getInstance(currencyCode);
        System.out.println(currencyCode + ":-" + currency.getSymbol(currencyLocaleMap.get(currency)));
        return currency.getSymbol(currencyLocaleMap.get(currency));
    }
}
