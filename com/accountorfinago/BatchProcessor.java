package com.accountorfinago;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.logging.*;
import java.util.concurrent.*;

public class BatchProcessor {
    private static final Logger logger = Logger.getLogger(BatchProcessor.class.getName());
    private static final Path BASE_DIR = Paths.get("data");
    private static final Path IN_DIR = BASE_DIR.resolve("in");
    private static final Path OUT_DIR = BASE_DIR.resolve("out");
    private static final Path ERR_DIR = BASE_DIR.resolve("error");
    private static final Path ARCHIVE_DIR = BASE_DIR.resolve("archive");

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("Batch Processor started. Monitoring 'in' directory...");

        while (true) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(IN_DIR, "*.xml")) {
                for (Path xmlFile : stream) {
                    processXml(xmlFile);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to read 'in' directory", e);
            }
            Thread.sleep(5000);
        }
    }

    private static void processXml(Path xmlFile) {
        logger.info("Processing XML file: " + xmlFile);

        Document doc;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile.toFile());
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            logger.warning("Invalid XML: " + xmlFile);
            moveTo(xmlFile, ERR_DIR.resolve(xmlFile.getFileName()));
            return;
        }

        NodeList receivers = doc.getElementsByTagName("receiver");
        Set<String> usedPdfs = new HashSet<>();

        for (int i = 0; i < receivers.getLength(); i++) {
            Element receiver = (Element) receivers.item(i);
            String receiverId = receiver.getElementsByTagName("receiver_id").item(0).getTextContent();
            String pdfName = receiver.getElementsByTagName("file").item(0).getTextContent();
            String expectedMd5 = receiver.getElementsByTagName("file_md5").item(0).getTextContent();
            Path pdfPath = IN_DIR.resolve(pdfName);

            Path targetDir = computeTargetPath(receiverId);
            boolean fileExists = Files.exists(pdfPath);
            boolean fileValid = fileExists && checkMd5(pdfPath, expectedMd5);

            Path finalDir = fileExists && fileValid ? OUT_DIR : ERR_DIR;
            Path receiverDir = finalDir.resolve(targetDir);
            try {
                Files.createDirectories(receiverDir);
                if (fileExists) Files.copy(pdfPath, receiverDir.resolve(pdfName), StandardCopyOption.REPLACE_EXISTING);
                writeReceiverXml(receiver, receiverDir.resolve(pdfName.replace(".pdf", ".xml")));
                usedPdfs.add(pdfName);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error processing receiver", e);
            }
        }

        moveTo(xmlFile, ARCHIVE_DIR.resolve(xmlFile.getFileName()));
        for (String pdf : usedPdfs) {
            try {
                Files.deleteIfExists(IN_DIR.resolve(pdf));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to delete used PDF: " + pdf, e);
            }
        }
    }

    private static boolean checkMd5(Path file, String expected) {
        try (InputStream fis = Files.newInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            byte[] digest = md.digest();
            String actual = bytesToHex(digest);
            return actual.equalsIgnoreCase(expected);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to compute MD5", e);
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static Path computeTargetPath(String receiverIdStr) {
        int receiverId = Integer.parseInt(receiverIdStr);
        int mod = receiverId % 100;
        return Paths.get(String.valueOf(mod), receiverIdStr);
    }

    private static void moveTo(Path source, Path target) {
        try {
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to move file: " + source, e);
        }
    }

    private static void writeReceiverXml(Element receiver, Path output) throws IOException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document newDoc = dBuilder.newDocument();
            Element root = newDoc.createElement("receivers");
            Node imported = newDoc.importNode(receiver, true);
            root.appendChild(imported);
            newDoc.appendChild(root);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(newDoc), new StreamResult(output.toFile()));
        } catch (Exception e) {
            throw new IOException("Error writing XML", e);
        }
    }
}
