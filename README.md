# Multi-Threaded Whiteboard System
A high-performance Java drawing suite with real-time WebSocket synchronization.

## Overview
This application provides a robust real-time whiteboard experience. 
Built for performance, it uses a multithreaded architecture to ensure that drawing, 
network processing, and UI rendering never interfere with each other, even when used 
from older tablet browsers.

## Tech Stack
- Core: Java 17+
- GUI: Java Swing (with 2D Graphics optimization)
- Networking: Java-WebSocket
- Concurrency: Java ExecutorService (Fixed Thread Pool)
- Build Tool: IntelliJ IDEA / Maven

## Key Features
- Real-Time Binary Protocol: Uses ByteBuffer for ultra-low latency coordinate transmission.
- Multi-Threaded Processing: A dedicated WorkerPool handles incoming network packets, keeping 
the UI thread (EDT) lag-free.
- Advanced Sync: Synchronized Zoom and Scroll capabilities between the remote client (tablet) 
and the host.
- Smooth Rendering: Features antialiasing, bilinear interpolation, and custom BasicStroke for 
fluid, rounded lines.
- Robust Error Handling: Custom ProtocolException and logging via java.util.logging to manage 
legacy browser connectivity.

## Architecture & Design Patterns
The project follows a Decoupled Observer Pattern:
1. WhiteboardServer: The "Subject" that parses incoming binary/string data.
2. WhiteboardListener: The interface that defines the contract for any UI component.
3. Whiteboard: The concrete implementation that manages the BufferedImage canvas and 
the ConcurrentLinkedQueue for batch rendering.

## How to Run
1. Clone the repository:
   ```bash
   git clone https://github.com/USERNAME/REPO_NAME.git
2. Open in IntelliJ IDEA
   - File > Open -> Select the project folder.
3. Run the Application
   - Locate the Main class.
   - Run it to start the WebSocket server on the default port (e.g., 5000).

## Authors:
- Andu - Lead Developer
