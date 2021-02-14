package com.landawn.abacus.unified.chronicleMap;

import java.math.BigInteger;

import com.landawn.abacus.util.N;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

public class Helllo {

    public static void main(String[] args) {
        ChronicleMap<CharSequence, Object> objCache = ChronicleMapBuilder.of(CharSequence.class, Object.class)
                .averageValueSize(1024)
                .name("city-postal-codes-map")
                .averageKey("Amsterdam")
                .entries(50_000)
                .create();

        objCache.put("aaa1", BigInteger.ZERO);

        N.println(objCache.get("aaa1"));
    }
}
