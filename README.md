# secure-batch-processor

# Batch Processor for Accountor Finago
This project is a secure and scalable Java-based batch processor designed to handle PDF and XML documents. It reads files from an input directory, validates and processes them according to metadata in the XML, and routes output to appropriate folders.

## Features
- Secure XML parsing
- PDF integrity verification using MD5 checksum
- Organized file storage into out/error/archive folders
- Resilient and robust processing logic
- Dockerized for platform independence

## Prerequisites
- Java 14+
- Apache Maven
- Docker

## Build & Run

### Maven Build
mvn clean package

### Docker Build
docker build -t batch-processor .

### Run the Container
docker run -v $(pwd)/data:/opt/batch-processor/data batch-processor

### Directory Structure
data/
├── archive/
├── error/
├── in/          # Place incoming XML and PDF files here
├── out/


### Security Highlights
	•	Disables external entity resolution in XML parsers
	•	Validates MD5 checksums for file integrity
	•	Prevents path traversal via strict path handling
	•	Logs all operations securely
