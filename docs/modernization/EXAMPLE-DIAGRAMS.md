### Example Mermaid Diagrams

#### Database Schema ERD
```mermaid
erDiagram
    CUSTOMER ||--o{ ORDER : places
    ORDER ||--|{ LINE_ITEM : contains
    PRODUCT ||--o{ LINE_ITEM : "ordered as"
    CUSTOMER {
        int customer_id PK
        string email
        string name
        datetime created_at
    }
    ORDER {
        int order_id PK
        int customer_id FK
        decimal total_amount
        string status
        datetime order_date
    }
    LINE_ITEM {
        int line_item_id PK
        int order_id FK
        int product_id FK
        int quantity
        decimal unit_price
    }
    PRODUCT {
        int product_id PK
        string name
        decimal price
        int inventory_count
    }
```

#### System Architecture Overview
```mermaid
graph LR
    Client[Browser/Mobile] --> LB[Load Balancer]
    LB --> App1[App Server 1]
    LB --> App2[App Server 2]
    App1 --> DB[(PostgreSQL)]
    App2 --> DB
    App1 --> Cache[(Redis Cache)]
    App2 --> Cache
    App1 --> Queue[Message Queue]
    App2 --> Queue
    App1 --> External[External APIs]
    Queue --> Worker[Background Workers]
    Worker --> DB
    
    subgraph "Monitoring"
        App1 --> Metrics[Prometheus]
        App2 --> Metrics
        Metrics --> Dashboard[Grafana]
    end
```

#### User Journey Map
```mermaid
journey
    title Customer Purchase Journey
    section Discovery
        Browse Products: 5: Customer
        View Product Details: 4: Customer
        Read Reviews: 3: Customer
    section Purchase
        Add to Cart: 5: Customer
        Login/Register: 3: Customer
        Enter Payment: 2: Customer
        Confirm Order: 4: Customer
    section Fulfillment
        Receive Confirmation: 5: Customer, System
        Track Shipment: 4: Customer, System
        Receive Product: 5: Customer
    section Support
        Contact Support: 2: Customer, CSR
        Process Return: 3: Customer, CSR
        Leave Review: 4: Customer
```

#### API Endpoint Hierarchy
```mermaid
graph TD
    API[API Root /api/v1/]
    API --> Auth[Authentication /auth/*]
    API --> Users[User Management /users/*]
    API --> Products[Product Catalog /products/*]
    API --> Orders[Order Management /orders/*]
    API --> Admin[Admin /admin/*]
    
    Auth --> Login[POST /auth/login]
    Auth --> Logout[POST /auth/logout]
    Auth --> Refresh[POST /auth/refresh]
    
    Users --> Profile[GET /users/profile]
    Users --> Update[PUT /users/profile]
    
    Products --> List[GET /products]
    Products --> Details[GET /products/:id]
    Products --> Search[GET /products/search]
    
    Orders --> Create[POST /orders]
    Orders --> History[GET /orders]
    Orders --> Status[GET /orders/:id]
    
    Admin --> Analytics[GET /admin/analytics]
    Admin --> UserMgmt[/admin/users/*]
    Admin --> ProductMgmt[/admin/products/*]
```

#### Module Dependency Graph
```mermaid
graph TD
    Controllers[Controllers Layer] --> Services[Services Layer]
    Services --> Repositories[Repository Layer]
    Services --> External[External Services]
    Repositories --> DB[(Database)]
    External --> PaymentAPI[Payment Gateway]
    External --> EmailAPI[Email Service]
    External --> LoggingAPI[Logging Service]
    
    subgraph "Core Modules"
        UserModule[User Management]
        OrderModule[Order Processing]
        ProductModule[Product Catalog]
        PaymentModule[Payment Processing]
    end
    
    Controllers --> UserModule
    Controllers --> OrderModule
    Controllers --> ProductModule
    Controllers --> PaymentModule
    
    OrderModule --> UserModule
    OrderModule --> ProductModule
    OrderModule --> PaymentModule
```

#### Business Process Workflow
```mermaid
graph TB
    Start[Customer Submits Order] --> Validate{Validate Order Data}
    Validate -->|Invalid| ValidationError[Return Validation Error]
    Validate -->|Valid| CheckInventory{Check Inventory}
    CheckInventory -->|Out of Stock| StockError[Return Stock Error]
    CheckInventory -->|Available| Reserve[Reserve Inventory]
    Reserve --> ProcessPayment{Process Payment}
    ProcessPayment -->|Failed| PaymentError[Payment Failed]
    ProcessPayment -->|Success| CreateOrder[Create Order Record]
    CreateOrder --> SendConfirmation[Send Confirmation Email]
    SendConfirmation --> UpdateInventory[Update Inventory]
    UpdateInventory --> QueueFulfillment[Queue for Fulfillment]
    QueueFulfillment --> End[Order Complete]
    
    PaymentError --> ReleaseInventory[Release Reserved Inventory]
    ReleaseInventory --> End
    ValidationError --> End
    StockError --> End
```

#### Integration Sequence Diagram
```mermaid
sequenceDiagram
    participant Client
    participant App
    participant PaymentGW as Payment Gateway
    participant Bank
    participant Email as Email Service
    
    Client->>App: Submit Order with Payment
    App->>App: Validate Order Data
    App->>PaymentGW: Create Payment Intent
    PaymentGW-->>App: Return Client Secret
    App-->>Client: Return Client Secret
    Client->>PaymentGW: Confirm Payment with Client
    PaymentGW->>Bank: Process Payment
    Bank-->>PaymentGW: Payment Result
    PaymentGW-->>Client: Payment Success
    PaymentGW->>App: Webhook: payment_intent.succeeded
    App->>App: Create Order Record
    App->>Email: Send Confirmation Email
    Email-->>App: Email Sent
    App-->>PaymentGW: Acknowledge Webhook
```
