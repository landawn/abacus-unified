package com.landawn.abacus.unified.jwt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.VerificationJwkSelector;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import com.landawn.abacus.http.HttpRequest;
import com.landawn.abacus.util.N;
import com.landawn.abacus.util.Tuple;
import com.landawn.abacus.util.Tuple.Tuple2;

public final class Jose4jUtil {
    private Jose4jUtil() {
        // singleton.
    }

    private static final Map<String, String> iss2jwks = new ConcurrentHashMap<>();

    private static String getJWKS(String iss) {
        String jwks = iss2jwks.get(iss);

        if (jwks == null) {
            jwks = HttpRequest.url(iss + "/.well-known/jwks.json").get();

            if (N.notNullOrEmpty(jwks)) {
                iss2jwks.put(iss, jwks);
            }
        }

        return jwks;
    }

    /**
     * 
     * @return 0 -> success, 1 -> expired, 2 -> invalid signature. 3 -> invalid JWT.
     * @throws JoseException 
     * @throws Exception
     */
    public static Tuple2<Integer, Map<String, Object>> verifyJWT(final String jwt) throws JoseException {
        final String[] parts = jwt.split("\\.");

        if (parts.length != 3) {
            return Tuple.of(3, null);
        }

        final Map<String, String> header = N.fromJSON(Map.class, N.base64DecodeToString(parts[0]));
        final String alg = header.get("alg");

        if (N.isNullOrEmpty(alg)) {
            return Tuple.of(3, null);
        }

        final Map<String, Object> payload = N.fromJSON(Map.class, N.base64DecodeToString(parts[1]));
        final String iss = (String) payload.get("iss");

        if (N.isNullOrEmpty(iss)) {
            return Tuple.of(3, payload);
        }

        final String jwks = getJWKS(iss);

        if (N.isNullOrEmpty(iss)) {
            return Tuple.of(3, payload);
        }

        // Create a new JsonWebSignature object
        JsonWebSignature jws = new JsonWebSignature();

        // Set the algorithm constraints based on what is agreed upon or expected from the sender
        jws.setAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.PERMIT, alg));

        // Set the compact serialization on the JWS
        jws.setCompactSerialization(jwt);

        // Create a new JsonWebKeySet object with the JWK Set JSON
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jwks);

        // The JWS header contains information indicating which key was used to secure the JWS.
        // In this case (as will hopefully often be the case) the JWS Key ID
        // corresponds directly to the Key ID in the JWK Set.
        // The VerificationJwkSelector looks at Key ID, Key Type, designated use (signatures vs. encryption),
        // and the designated algorithm in order to select the appropriate key for verification from
        // a set of JWKs.
        VerificationJwkSelector jwkSelector = new VerificationJwkSelector();
        JsonWebKey jwk = jwkSelector.select(jws, jsonWebKeySet.getJsonWebKeys());

        // The verification key on the JWS is the public key from the JWK we pulled from the JWK Set.
        jws.setKey(jwk.getKey());

        // Check the signature
        boolean signatureVerified = jws.verifySignature();

        if (signatureVerified == false) {
            return Tuple.of(2, payload);
        }

        if (Long.valueOf(((Number) payload.get("exp")).longValue() * 1000L) < System.currentTimeMillis()) {
            return Tuple.of(1, payload);
        }

        return Tuple.of(0, payload);
    }
}
