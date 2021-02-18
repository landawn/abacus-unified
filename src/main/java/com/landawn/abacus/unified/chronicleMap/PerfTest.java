package com.landawn.abacus.unified.chronicleMap;

import java.math.BigInteger;

import com.landawn.abacus.cache.OffHeapCache;
import com.landawn.abacus.util.N;
import com.landawn.abacus.util.StringUtil;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

public class PerfTest {

    public static void main(String[] args) {
        ChronicleMap<CharSequence, Object> objCache = ChronicleMapBuilder.of(CharSequence.class, Object.class)
                .averageValueSize(1024)
                .name("city-postal-codes-map")
                .averageKey("Amsterdam")
                .entries(1000_000)
                .create();

        objCache.put("aaa1", BigInteger.ZERO);

        N.println(objCache.get("aaa1"));

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000_000; i++) {
            objCache.put(N.uuid(), StringUtil.repeat(N.uuid(), 10));
        }

        N.println("Took: " + (System.currentTimeMillis() - startTime));

        try (OffHeapCache<String, Object> abacusOHCache = new OffHeapCache<>(2048, 3000, 6000_000, 6000_000)) {

            abacusOHCache.put("aaa1", BigInteger.ZERO);

            N.println(abacusOHCache.gett("aaa1"));

            startTime = System.currentTimeMillis();

            for (int i = 0; i < 1000_000; i++) {
                objCache.put(N.uuid(), StringUtil.repeat(N.uuid(), 10));
            }

            N.println("Took: " + (System.currentTimeMillis() - startTime));
        }
    }
}
