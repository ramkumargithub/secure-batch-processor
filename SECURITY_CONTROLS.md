### ✅ `SECURITY_CONTROLS.md

# Security Controls for Batch Processor

This document outlines the cybersecurity best practices implemented in the Java batch processor.

## 1. Input Validation
- **XML Validation**: Disables DTDs and external entities to prevent XXE attacks.
- **File Names**: All file operations use strict path resolution to avoid directory traversal.

## 2. Cryptographic Checks
- **MD5 Hashing**: Verifies integrity of PDF files before processing. Rejects mismatches.

## 3. Secure File Handling
- Uses `Files` APIs with `StandardCopyOption.REPLACE_EXISTING` to prevent symlink attacks.
- Ensures target directories are created safely before writing.

## 4. Logging and Auditing
- Logs all key actions using `java.util.logging`.
- Warnings for any invalid or unexpected behavior.

## 5. Error Handling
- Graceful error handling with fallback to `error/` directory.
- Prevents app crash from malformed inputs.

## 6. Container Security
- Based on `openjdk:14-jdk-alpine` (minimal attack surface).
- Runs as default non-root user.
- Data directory is mounted at runtime.

## 7. Dependency Management
- Uses Maven Central only.
- Locked to specific versions in `pom.xml`.

## 8. Runtime Isolation
- Processed files are deleted from `in/` after success or failure.
- XML files archived for traceability.
