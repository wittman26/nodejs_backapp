Claro, aquí tienes un ejemplo completo de una aplicación Spring Boot que utiliza RSocket para enviar y recibir mensajes. El ejemplo incluye tanto el lado del servidor como del cliente.

---

### 🎯 Objetivo:

Implementar una aplicación donde el cliente envía una solicitud con `RSocketRequester` y el servidor responde con un `Mono<Response>`.

---

## 📁 Estructura de Archivos

```
rsocket-example/
├── src/
│   ├── main/
│   │   ├── java/com/example/rsocket/
│   │   │   ├── RSocketExampleApplication.java
│   │   │   ├── controller/
│   │   │   │   └── ProductController.java
│   │   │   ├── model/
│   │   │   │   ├── ProductRequest.java
│   │   │   │   └── ProductResponse.java
│   │   │   └── client/
│   │   │       └── ProductClient.java
│   └── test/
│       └── java/com/example/rsocket/
│           └── client/
│               └── ProductClientTest.java
└── pom.xml
```

---

## 🛠️ Paso 1: Dependencias `pom.xml`

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-rsocket</artifactId>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 🚀 Paso 2: Clase principal

```java
@SpringBootApplication
public class RSocketExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(RSocketExampleApplication.class, args);
    }

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder builder) {
        return builder.tcp("localhost", 7000);
    }
}
```

---

## 🧾 Paso 3: Modelo

**ProductRequest.java**

```java
public class ProductRequest {
    private Integer id;
    private String name;
    // getters y setters
}
```

**ProductResponse.java**

```java
public class ProductResponse {
    private String message;
    // constructor, getters y setters
}
```

---

## 📡 Paso 4: Servidor (Controller)

**ProductController.java**

```java
@Controller
public class ProductController {

    @MessageMapping("product.create")
    public Mono<ProductResponse> handleProduct(ProductRequest request) {
        String msg = "Producto recibido: " + request.getName();
        return Mono.just(new ProductResponse(msg));
    }
}
```

---

## 💻 Paso 5: Cliente RSocket

**ProductClient.java**

```java
@Component
public class ProductClient {

    private final RSocketRequester requester;

    public ProductClient(RSocketRequester requester) {
        this.requester = requester;
    }

    public Mono<ProductResponse> sendProduct(ProductRequest request) {
        return requester
                .route("product.create")
                .data(request)
                .retrieveMono(ProductResponse.class);
    }
}
```

---

## ✅ Paso 6: Test del cliente

**ProductClientTest.java**

```java
@SpringBootTest
@ActiveProfiles("test")
class ProductClientTest {

    @Autowired
    private ProductClient productClient;

    @Test
    void shouldSendProduct() {
        ProductRequest request = new ProductRequest();
        request.setId(1);
        request.setName("Tablet");

        StepVerifier.create(productClient.sendProduct(request))
            .expectNextMatches(resp -> resp.getMessage().contains("Tablet"))
            .verifyComplete();
    }
}
```

---

## 🔌 Configuración del servidor RSocket

En `application.yml`:

```yaml
spring:
  rsocket:
    server:
      port: 7000
      transport: tcp
```

---

¿Quieres que prepare también un ejemplo para comunicación bidireccional o streaming (Flux)?
