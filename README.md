# 🗃️ Distributed Object Storage with Versioning (DOSV)

Distributed Object Storage with Versioning (DOSV) is a scalable, fault-tolerant system that stores and manages files across multiple storage nodes. It ensures redundancy and performance through data distribution and maintains complete version histories using metadata for each file update. DOSV supports all file types and provides client-side utilities for version-aware file storage, retrieval, and management.

---

## 🚀 Features

- 🗂️ File versioning
- ⚖️ Load-balanced architecture
- 💾 Chunk-based storage with replication
- 🛠  Fault tolerance
- 🔄 Consistent hashing for balanced data distribution. Dynamic node addition and removal without system disruption.

---

## 📦 Project Modules

- **Client API** – Upload/download/delete files.
- **Load Balancer** – Distributes client requests to metadata servers based on current load to ensure balanced handling.
- **Metadata Service** – Manages file metadata and versions.
- **Storage Nodes** – Stores chunked file data.

---

## 🧰 Tech Stack

| Component            | Technology / Tool                          |
|----------------------|--------------------------------------------|
| **Language**         | Java                                       |
| **Client Interface** | Java-based Command-Line Application        |
| **Metadata Storage** | PostgreSQL                                 |
| **Chunk Storage**    | SQLite                                     |

---

## ▶️ Run Instructions

> ✅ **Prerequisite**: Ensure **Java JDK** is installed and added to your system’s `PATH`.

---

### 1. Start the Load Balancer

```bash
cd path/to/DOSV/LoadBalanceProgram

# Compile
javac -d ./out ./src/*.java

# Run
java -cp "./out" LoadBalancer
```

### 2. Start the Metadata Server

```bash
cd path/to/DOSV/MetaServerProgram

# Compile
javac -cp "./src/lib/postgresql-42.7.5.jar" -d ./out ./src/app/*.java  ./src/config/*.java  ./src/dao/*.java  ./src/distribution/*.java  ./src/models/*.java  ./src/requests/*.java  ./src/service/*.java  ./src/taskhandle/*.java

# Run
java -cp "./src/lib/postgresql-42.7.5.jar;./out" app.Server
```

### 3. Start the Storage Node(s)

```bash
cd path/to/DOSV/StorageServerProgram

# Compile
javac -d ./out -cp "./src/lib/sqlite-jdbc-3.49.1.0.jar" ./src/app/*.java  ./src/config/*.java  ./src/dao/*.java  ./src/model/*.java  ./src/service/*.java  ./src/taskhandle/*.java

# Run
java -cp "./src/lib/sqlite-jdbc-3.49.1.0.jar;./out/" app.StorageServer
```

### 4. Start the Client

```bash
cd path/to/DOSV/ClientProgram

# Compile
javac -d ./out ./src/app/*.java ./src/taskhandle/*.java

# Run
java -cp "./out" app.Client
```
