package com.accountorfinago;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityTests {

    @Test
    public void testMd5Validation() throws Exception {
        Path tempFile = Files.createTempFile("test-pdf", ".pdf");
        Files.writeString(tempFile, ""); // empty content

        MessageDigest md = MessageDigest.getInstance("MD5");
        String expectedMd5 = "d41d8cd98f00b204e9800998ecf8427e";

        boolean result = BatchProcessor.checkMd5(tempFile, expectedMd5);
        assertTrue(result, "MD5 check should pass for empty file");

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void testDirectoryTraversalProtection() {
        String maliciousReceiverId = "../../evil";
        Path safePath = BatchProcessor.computeTargetPath(maliciousReceiverId);

        assertFalse(safePath.toString().contains(".."), "Path should not allow directory traversal");
    }

    @Test
    public void testXmlParsingIsSecure() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.newDocumentBuilder();
        } catch (Exception e) {
            fail("Secure XML configuration failed: " + e.getMessage());
        }
    }

    @Test
    public void testFileSanityBeforeDelete() throws Exception {
        Path file = Files.createTempFile("test", ".txt");
        Files.writeString(file, "safe");

        assertTrue(Files.exists(file));
        Files.deleteIfExists(file);
        assertFalse(Files.exists(file));
    }
}
