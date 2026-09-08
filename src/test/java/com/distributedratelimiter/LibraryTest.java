package com.distributedratelimiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class LibraryTest {

    @Test
    void libraryIdentityIsStable() {
        assertEquals("com.distributedratelimiter", Library.GROUP_ID);
        assertEquals("distributed-rate-limiter", Library.ARTIFACT_ID);
        assertNotNull(Library.class.getPackage());
    }
}
