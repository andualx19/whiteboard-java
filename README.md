# Multi-Threaded Java Whiteboard System

A Java-based whiteboard application designed for real-time drawing over a network, using a multi-threaded architecture to keep UI rendering responsive and smooth.

---

## Overview

This project implements a real-time whiteboard system where drawing input is received from a remote client (e.g. tablet or browser) and rendered on a desktop application.  
The architecture separates UI rendering, network communication, and data processing to avoid lag and blocking, even on lower-end or older devices.

---

## Tech Stack

- **Language:** Java 17+
- **GUI:** Java Swing (2D Graphics)
- **Networking:** Java-WebSocket
- **Concurrency:** ExecutorService (Fixed Thread Pool)
- **IDE:** IntelliJ IDEA

---

## Key Features

- **Real-Time Drawing:** Drawing coordinates are transmitted and rendered live.
- **Multi-Threaded Architecture:**  
  Network communication and packet processing run on background threads, keeping the Swing EDT responsive.
- **Binary Data Transfer:**  
  Uses `ByteBuffer` for efficient transmission of drawing coordinates.
- **Smooth Rendering:**  
  Anti-aliasing, rounded strokes, and optimized 2D rendering for natural-looking lines.
- **Basic Synchronization:**  
  Drawing state is kept consistent between client and host.
- **Error Handling & Logging:**  
  Graceful handling of malformed packets and connection issues using custom exceptions and logging.

---

## Architecture & Design

The project follows a **decoupled, event-driven design** inspired by the Observer pattern:

- **WhiteboardServer**  
  Handles WebSocket connections and parses incoming binary or string data.
- **WhiteboardListener**  
  Interface defining how UI components receive drawing updates.
- **Whiteboard**  
  Concrete implementation that manages the drawing canvas using a `BufferedImage` and queues render updates.

This separation allows networking logic and UI rendering to evolve independently.

---

## How It Works

1. The client captures drawing input (coordinates, strokes).
2. Data is sent over a WebSocket connection to the server.
3. Incoming packets are processed by a worker thread pool.
4. Parsed drawing commands are forwarded to the UI layer.
5. The whiteboard renders updates smoothly on the desktop canvas.

---

## How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/andualx19/whiteboard-java.git
2. Open the project in IntelliJ IDEA
3. Locate the Main class
4. Run the application to start the WebSocket server (default port: 8000)
5. Connect a client from the same Wi-Fi network

## Limitations

- Single client connection
- No authentication or encryption
- Basic drawing tools only
- Experimental zoom/scroll synchronization

## Future Improvements

- Multi-client support
- Undo / redo functionality
- Canvas export (PNG/PDF)
- Improved UI and toolset
- Secure WebSocket communication (WSS)

## Author: Andu - Lead Developer
