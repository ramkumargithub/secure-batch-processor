Threat Model for Batch Processor

This document identifies potential threats and outlines how they are mitigated in the application design.

1. **XML External Entity (XXE) Attacks**
   
	•	Threat: Malicious XML could exploit XXE to access internal files.
	•	Mitigation: All external entity resolution is disabled in DocumentBuilderFactory.

2. **Directory Traversal**
   
	•	Threat: A crafted file name like ../../evil could break out of intended folders.
	•	Mitigation: All file operations use Path.resolve and sanitize inputs.

3. **File Integrity Attacks**
   
	•	Threat: Tampered PDF files may bypass business rules.
	•	Mitigation: PDF integrity is checked using strong MD5 hashing before processing.

4. **Denial-of-Service (DoS)**
   
	•	Threat: Large or malformed XML may exhaust memory or crash the parser.
	•	Mitigation: Proper XML parsing configuration and error catching prevent crashes.

5. **Symlink Attacks**
    
	•	Threat: Symlinked PDF or XML files could redirect file operations maliciously.
	•	Mitigation: Files are moved and copied using Files API with controlled paths.

6. **Code Execution in Container**
    
	•	Threat: Malicious file contents could cause unexpected execution.
	•	Mitigation: The app does not evaluate or execute file contents.

13. Container-Level Risks
	•	Threat: Exploitable image or exposed data mounts.
	•	Mitigation: Uses minimal Alpine-based OpenJDK, exposes no ports, and mounts only data volume.
