# INDICE

* [FX](#fx)
* [FX-DB](#fx-db)
* [MICROLITO](#microlito)
* [LIB-PARENT](#lib-parent)
* [GATEWAY](#gateway)

# FX
[volver al indice](#indice)

x612243-sgt_sangroup: Puedes deducir que arquitectura de software se está usando en el proyecto Spring ubicado en la carpeta conac-springboot-fx? describe como está estructurado y sus características principales. Dame una descripción desde un punto de vista técnico de un nivel de desarrollador de Software SR.

GitHub Copilot: Voy a analizar la arquitectura del proyecto Spring Boot para darte una descripción técnica detallada. 

Perfecto, ahora tengo suficiente contexto para darte una descripción técnica completa de la arquitectura del proyecto.
---

## **Análisis Arquitectónico del Proyecto `conac-springboot-fx`**

### **1. Arquitectura Principal: Hexagonal (Ports & Adapters)**

El proyecto implementa una **arquitectura hexagonal** (también conocida como Ports and Adapters), combinada con principios de **Domain-Driven Design (DDD)**. Esta es evidente por la estructura de carpetas consistente en todos los módulos funcionales:

```
domain/
  ├── port/          # Interfaces (contratos)
  │   ├── service/   # Servicios de dominio
  │   ├── repository/# Puertos de persistencia
  │   └── client/    # Puertos de integraciones
  ├── dto/           # DTOs del dominio
  └── vo/            # Value Objects (cuando aplica)

application/
  ├── service/       # Implementaciones de servicios
  ├── usecase/       # Casos de uso (lógica de negocio)
  ├── mapper/        # Mapeadores de transformación
  └── exception/     # Manejo de excepciones

infrastructure/
  └── adapter/       # Adaptadores de infraestructura
      ├── rest/      # Controllers REST y DTOs de API
      ├── persistence/# Implementaciones de repositorios
      └── client/    # Clientes externos (Feign)
```

### **2. Stack Tecnológico**

#### **Framework Core:**
- **Spring Boot 3.x** (con parent personalizado `conac-springboot-lib-starter-parent:3.14.0`)
- **Spring WebFlux** - Programación reactiva (no tradicional MVC bloqueante)
- **Project Reactor** - Tipos `Mono<>` y `Flux<>` para flujos reactivos

#### **Persistencia:**
- **Spring Data JPA** - Acceso a base de datos relacional
- Hybrid approach: JPA para persistencia + Reactor para flujos reactivos

#### **Integraciones:**
- **OpenFeign** - Cliente HTTP declarativo para consumo de APIs REST
- **RSocket** - Protocolo de comunicación reactiva binaria
- **AWS S3** - Almacenamiento de objetos

#### **Herramientas:**
- **Lombok** - Reducción de boilerplate (getters/setters/builders)
- **MapStruct** - Generación automática de mappers (inferido por carpetas `mapper/`)
- **Jakarta Validation** - Validación de DTOs (`@Valid`, `@NotNull`)

### **3. Módulos Funcionales (Bounded Contexts)**

El sistema está dividido en dominios claramente separados:

- **`currency`** - Gestión de divisas, tipos de cambio, días hábiles
- **`digitalsignature`** - Firma digital de operaciones, expedientes
- **`margin`** - Cálculo de márgenes financieros
- **`risk`** - Gestión de ratios de riesgo
- **`parent`** - Componentes compartidos (STS, manejo global de excepciones)
- **`shared`** - Infraestructura transversal (autenticación, tokens)

Cada módulo es **autónomo** y sigue el mismo patrón arquitectónico.

### **4. Flujo de Datos (Inversión de Dependencias)**

```
[Cliente HTTP] 
    ↓
[RestController] (infrastructure/adapter/rest)
    ↓ (invoca)
[Service Interface] (domain/port/service)
    ↑ (implementa)
[ServiceImpl] (application/service)
    ↓ (usa)
[UseCase] (application/usecase) - Lógica de negocio pura
    ↓ (consulta)
[Repository Interface] (domain/port/repository)
    ↑ (implementa)
[RepositoryAdapter] (infrastructure/adapter/persistence)
    ↓
[JPA Repository / External Client]
```

**Principio clave:** El dominio **no depende** de la infraestructura. Las dependencias apuntan hacia adentro (Dependency Inversion Principle).

### **5. Características Técnicas Avanzadas**

#### **A. Programación Reactiva End-to-End**
```java
public Mono<TradeSignature> createOrUpdateSignature(Locale locale, String entity, TradeSignatureDto dto) {
    return tradeSignatureRepositoryClient.find(...)
        .flatMap(found -> upsertTradeSignature(found, dto, entity))
        .switchIfEmpty(Mono.defer(() -> upsertTradeSignature(null, dto, entity)));
}
```
- No hay bloqueo de threads
- Backpressure nativo
- Composición funcional de operaciones asíncronas

#### **B. Segregación de Interfaces (ISP)**
Los servicios están granularmente separados:
```
TradeSignatureServiceSave
TradeSignatureServiceGet
TradeSignatureServicePost
UpdateSignatureExpedientStatusService
ViewTradeSignatureExpedientStatusService
```

#### **C. Configuración Centralizada**
```yaml
spring.config.import: configserver:${CONFIG_URL}
```
- **Spring Cloud Config Server** para configuración externalizada
- Profiles para entornos (`local`, `dev`, `prod`)

#### **D. Separation of Concerns en DTOs**
- **API DTOs** (infrastructure/adapter/rest): Contratos externos
- **Domain DTOs** (domain/dto): Modelos internos
- **DB Entities** (desde libs compartidas): Modelos de persistencia
- **MapStruct** para transformaciones automáticas

#### **E. Manejo Global de Excepciones**
```java
@RestControllerAdvice
public class GenericExceptionHandler { ... }
```
- Intercepta excepciones de forma centralizada
- Respuestas estandarizadas

### **6. Ventajas de esta Arquitectura**

1. **Testabilidad**: El dominio es independiente, puedes hacer unit tests sin Spring
2. **Flexibilidad**: Cambiar de JPA a MongoDB solo requiere modificar adaptadores
3. **Escalabilidad**: WebFlux permite manejar alta concurrencia con pocos recursos
4. **Mantenibilidad**: Separación clara de responsabilidades por capas y módulos
5. **Evolución**: Puedes agregar nuevos bounded contexts sin afectar existentes

### **7. Patrones de Diseño Identificados**

- **Adapter Pattern**: Infraestructura se adapta a puertos del dominio
- **Repository Pattern**: Abstracción del acceso a datos
- **Service Layer**: Lógica de negocio encapsulada
- **DTO Pattern**: Transformación de datos entre capas
- **Use Case Pattern**: Acciones de negocio como componentes reutilizables
- **Dependency Injection**: Inyección por constructor con Lombok `@RequiredArgsConstructor`

### **8. Consideraciones para Desarrolladores SR**

- **Evitar lógica en controllers**: Solo deben orquestar llamadas a servicios
- **Mantener el dominio puro**: No inyectar `@Service` o `@Repository` en paquetes `domain`
- **Usar interfaces para integrations**: Facilita mocking en tests
- **Reactor operators**: Dominar `flatMap`, `switchIfEmpty`, `zipWith`, `defer`
- **Backpressure awareness**: Evitar operaciones bloqueantes en flujos reactivos
- **Exception handling**: Usar `Mono.error()` y operadores como `onErrorResume`

---

### **Conclusión**

Este es un proyecto **enterprise-grade** con una arquitectura sólida, preparado para:
- Alta concurrencia
- Mantenimiento a largo plazo
- Equipos distribuidos trabajando en diferentes bounded contexts
- Evolución de requisitos sin acoplamiento técnico

La inversión inicial en estructura se compensa con **baja deuda técnica** y **alta cohesión**.

---

# FX-DB
[volver al indice](#indice)

x612243-sgt_sangroup: Puedes deducir que arquitectura de software se está usando en el proyecto Spring ubicado en la carpeta conac-springboot-fx-db? describe como está estructurado y sus características principales. Dame una descripción desde un punto de vista técnico de un nivel de desarrollador de Software SR.

GitHub Copilot: Voy a analizar la arquitectura del proyecto Spring Boot para darte una descripción técnica detallada. 


Perfecto, ya tengo suficiente información. Déjame analizar un archivo más para completar el análisis de la arquitectura. 

Excelente. Ahora tengo una visión completa de la arquitectura. Déjame darte un análisis técnico detallado:

## Arquitectura del Proyecto: **Hexagonal (Ports & Adapters) con DDD**

### **Resumen Ejecutivo**

Este proyecto implementa una **Arquitectura Hexagonal (Ports & Adapters)** combinada con principios de **Domain-Driven Design (DDD)** y **Clean Architecture**. Es un microservicio Spring Boot que actúa como **capa de persistencia reactiva** para operaciones de Foreign Exchange (FX), exponiendo capacidades de base de datos a través de **RSocket** (protocolo reactivo binario).

---

## **Estructura Arquitectónica**

### **1. Capa de Dominio (`domain/`)**
```
domain/
└── port/
    └── persistence/
        ├── CurrencyFxRepository.java
        ├── OperationRepository.java
        ├── TradeSignatureRepository.java
        └── [17+ interfaces más]
```

**Características:**
- **Interfaces puras** (ports) que definen contratos de persistencia
- **Agnóstica de tecnología**: No depende de frameworks
- Define el **core business** sin acoplamientos externos
- Representa el **centro de la arquitectura hexagonal**
- Retorna tipos de dominio (`Currency`, `TradeSignature`, etc.) no entidades JPA

**Ejemplo:**
```java
public interface CurrencyFxRepository {
    Collection<DayOfWeek> findNonWorkingDays(Collection<String> currencies);
    Collection<LocalDate> findHolidays(Collection<String> currencies, LocalDateTime limitDateTime);
    Optional<Currency> find(String currency);
}
```

---

### **2. Capa de Infraestructura (`infrastructure/adapter/`)**

La infraestructura implementa **tres tipos de adaptadores**:

#### **2.1. Adapters de Persistencia JPA (`adapter/persistence/jpa/`)**

```
persistence/jpa/
├── crud/          # Spring Data JPA Repositories
├── dto/           # DTOs de transferencia
├── mapper/        # MapStruct mappers (Domain ↔ Entity)
├── model/         # JPA Entities (@Entity)
├── repository/    # Implementaciones de ports
└── spec/          # Specifications para queries dinámicas
```

**Patrón implementado:**
```
Port Interface (domain)
    ↓ implementa
Repository Implementation (JpaXxxRepositoryImpl)
    ↓ delega en
Spring Data JPA Repository (SpringJpaXxxRepository)
    ↓ opera sobre
JPA Entity Model (XxxModel)
```

**Ejemplo de implementación:**
```java
@Repository
@RequiredArgsConstructor
public class JpaTradeSignatureRepositoryImpl implements TradeSignatureRepository {
    private final SpringJpaTradeSignatureRepository repository;  // Spring Data JPA
    
    @Transactional
    public TradeSignature save(TradeSignature tradeSignature) {
        var model = TradeSignatureMapper.INSTANCE.fromDomain(tradeSignature);
        var saved = repository.saveAndFlush(model);
        return TradeSignatureMapper.INSTANCE.toDomain(saved);  // Mapper bidireccional
    }
}
```

**Stack técnico:**
- **JPA/Hibernate** con Jakarta Persistence API
- **Spring Data JPA** para abstracción de repositorios
- **MapStruct** para mapping eficiente (compile-time)
- **@DynamicUpdate** de Hibernate para optimización
- **Specifications** para queries dinámicas complejas
- **Transaccionalidad** gestionada con `@Transactional`

---

#### **2.2. Adapters RSocket (`adapter/rsocket/controller/`)**

**Propósito:** Exponen las capacidades de persistencia mediante **RSocket**, un protocolo de comunicación reactiva binario.

```java
@Controller
@RequiredArgsConstructor
public class CurrencyFxRSocketController implements CurrencyFxRepositoryClient {
    private final CurrencyFxRepository repository;  // Inyecta el port
    
    @Override
    public Mono<Collection<DayOfWeek>> findNonWorkingDays(@Payload CurrencyFxFindNonWorkingDaysRequest request) {
        return Mono.fromCallable(() -> repository.findNonWorkingDays(request.getCurrencies()))
            .subscribeOn(Schedulers.boundedElastic());  // Reactive wrapping
    }
}
```

**Características:**
- **Programación reactiva** con Project Reactor (`Mono<T>`, `Flux<T>`)
- Implementan interfaces de cliente generadas (`XxxRepositoryClient`)
- **Schedulers.boundedElastic()**: Ejecutan operaciones bloqueantes de DB en pool elástico
- **@Payload**: Deserialización automática de mensajes RSocket
- **Fire-and-forget, Request-Response, Request-Stream**: Soporta todos los patrones RSocket

---

#### **2.3. Adapters de Mensajería Hazelcast (`adapter/hazelcast/listener/`)**

**Propósito:** Consumidores de colas distribuidas Hazelcast para procesamiento asíncrono.

```java
@Component
public class OperationDisclaimerMessageListener extends MessageListener<Object> {
    private final JpaOperationDisclaimerRepositoryImpl repository;
    
    public OperationDisclaimerMessageListener(HazelcastInstance hazelcastInstance, ...) {
        super(hazelcastInstance, "operation-disclaimer-queue");
    }
    
    @Override
    protected void onMessage(Object message) {
        // Procesa GenericRecord de Hazelcast y persiste
    }
}
```

**Características:**
- **Event-driven architecture**: Reacciona a eventos en colas distribuidas
- **Hazelcast IMDG**: Sistema de caché/mensajería distribuida
- Procesamiento de `GenericRecord` (formato compacto de Hazelcast)
- Integración con capa de persistencia para side-effects

---

## **Patrones y Principios Aplicados**

### **1. Hexagonal Architecture (Ports & Adapters)**
- **Puerto de salida**: Interfaces en `domain/port/persistence/`
- **Adaptador primario**: RSocket controllers (driving adapters)
- **Adaptador secundario**: JPA repositories (driven adapters)
- **Inversión de dependencias**: Domain no conoce infraestructura

### **2. Domain-Driven Design (DDD)**
- Separación clara entre **dominio** e **infraestructura**
- Uso de **Value Objects** y **Entities** del dominio
- **Ubiquitous Language**: Términos de negocio FX (Trade, Currency, Operation)

### **3. Clean Architecture**
- Dependencias unidireccionales: `Infrastructure → Domain` (nunca al revés)
- Lógica de negocio aislada en el core
- Testabilidad: Puedes mockear ports fácilmente

### **4. CQRS (implícito)**
- Separación de operaciones de lectura (`find*`) y escritura (`save`)
- Vistas materializadas (`ViewTradeSignature*`) para queries complejas

### **5. Repository Pattern**
- Abstracción de la persistencia mediante interfaces
- Encapsulación de queries complejas

---

## **Stack Tecnológico**

| Capa | Tecnología |
|------|-----------|
| **Framework** | Spring Boot 3.x (Jakarta EE) |
| **Persistencia** | Spring Data JPA + Hibernate |
| **Mapping** | MapStruct (compile-time) |
| **Comunicación** | RSocket (protocolo reactivo) |
| **Programación Reactiva** | Project Reactor |
| **Mensajería** | Hazelcast IMDG |
| **Configuración** | Spring Cloud Config Server |
| **Utilities** | Lombok (reducción de boilerplate) |
| **Testing** | JUnit, Mockito, Reactor Test, PODAM |

---

## **Características Avanzadas**

### **1. Programación Reactiva**
- **Non-blocking I/O**: Controllers RSocket devuelven `Mono`/`Flux`
- **Backpressure**: Manejo de flujos de datos reactivos
- **Composición asíncrona**: Operadores de Project Reactor

### **2. Multi-Tenancy**
- Campo `entity` en todas las entidades (4 chars)
- Segregación de datos por entidad bancaria

### **3. Auditoría**
- `BaseAuditorJpa`: Trazabilidad de creación/modificación
- `@DynamicUpdate`: Optimización de updates parciales

### **4. Queries Dinámicas**
- **Specifications API** para criterios de búsqueda flexibles
- Ejemplo: `ViewTradeSignatureExpedientSpecification`

### **5. Relaciones Complejas**
- Cascades (`CascadeType.ALL`)
- Fetch strategies optimizadas (`@Fetch(FetchMode.SELECT)`)
- Manejo bidireccional de relaciones

---

## **Flujo de Datos Típico**

```
Cliente RSocket
    ↓
RSocketController (@Controller)
    ↓ Llama al port
Port Interface (domain)
    ↓ Implementada por
RepositoryImpl (@Repository)
    ↓ Usa
MapStruct Mapper (Domain ↔ Entity)
    ↓ Delega en
Spring Data JPA Repository
    ↓ Opera sobre
JPA Entity Model
    ↓
PostgreSQL/Oracle Database
```

---

## **Ventajas de esta Arquitectura**

1. **Testabilidad**: Dominio independiente, fácil de testear
2. **Mantenibilidad**: Cambios de infraestructura no afectan dominio
3. **Escalabilidad**: RSocket permite comunicación eficiente
4. **Flexibilidad**: Múltiples adapters (JPA, Hazelcast, RSocket)
5. **Performance**: Reactive streams + connection pooling
6. **Separación de concerns**: Cada capa tiene responsabilidad única

---

## **Contexto del Proyecto**

- **Dominio de negocio**: Foreign Exchange (FX) para banca corporativa
- **Rol del servicio**: **Database Gateway** - Microservicio especializado en persistencia
- **Comunicación**: Síncrona (RSocket) + Asíncrona (Hazelcast queues)
- **Entidades principales**: Operaciones FX, Firmas de Trades, Documentos, Márgenes, Fixings Bloomberg

Este diseño es típico de arquitecturas empresariales modernas en entornos bancarios que requieren **alta disponibilidad, trazabilidad completa y separación estricta de responsabilidades**.

# MICROLITO
[volver al indice](#indice)

x612243-sgt_sangroup: Puedes deducir que arquitectura de software se está usando en el proyecto Spring ubicado en la carpeta conac-springboot-microlito? describe como está estructurado y sus características principales. Dame una descripción desde un punto de vista técnico de un nivel de desarrollador de Software SR.

GitHub Copilot: Voy a analizar el proyecto Spring Boot para identificar su arquitectura y estructura. Déjame explorar los archivos clave del proyecto. 


Perfecto, ahora tengo suficiente información para elaborar una descripción técnica completa. Déjame analizar un poco más la estructura de seguridad y servicios. 

Searched text for `spring-boot-starter` (`**/pom.xml`), 16 results

Excelente. Ahora tengo toda la información necesaria para proporcionar un análisis técnico completo de la arquitectura del proyecto.

## Análisis de Arquitectura de Software - Proyecto CONAC Spring Boot Microlito

### **Arquitectura General: Monolito Híbrido con Enfoque Multi-Capa**

Este proyecto implementa una **arquitectura monolítica híbrida modular** que combina patrones tradicionales (Stripes MVC) con tecnologías modernas de Spring Boot, presentando características de un sistema de transición arquitectónica.

---

## **1. STACK TECNOLÓGICO PRINCIPAL**

### **Core Framework**
- **Spring Boot 2.7.18** (Java 17)
- **Spring Framework**: Web, Security, JPA, JMS, Web Services
- **Packaging**: WAR (desplegable en contenedor Tomcat 9.0.104)
- **Servidor embebido**: Tomcat con Jasper para JSPs

### **Frameworks de Presentación (Dual)**
1. **Stripes MVC 1.6.0** (Framework legacy principal para UI)
   - ActionBeans para controladores web tradicionales
   - JSPs ubicados en `/WEB-INF/jsp/`
   - Pattern MVC tradicional con navegación basada en acciones

2. **Spring MVC REST** (@RestController)
   - Controllers RESTful modernos bajo `/backend/api/`
   - Exposición de APIs REST documentadas con Swagger/OpenAPI

---

## **2. PATRÓN ARQUITECTÓNICO: CAPAS**

```
┌─────────────────────────────────────────────────────────┐
│                 CAPA DE PRESENTACIÓN                    │
│  ┌──────────────────┐        ┌─────────────────────┐   │
│  │  Stripes Actions │        │ REST Controllers    │   │
│  │  (*.action)      │        │ (@RestController)   │   │
│  └──────────────────┘        └─────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│              CAPA DE LÓGICA DE NEGOCIO                  │
│  ┌──────────────────────────────────────────────────┐  │
│  │           Services (@Service)                    │  │
│  │  - ~100+ servicios de dominio                    │  │
│  │  - Lógica transaccional compleja                 │  │
│  └──────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│           CAPA DE ACCESO A DATOS (DUAL)                 │
│  ┌─────────────────┐     ┌────────────────────────┐   │
│  │  MyBatis        │     │  Spring Data JPA       │   │
│  │  (@Repository   │     │  (JpaRepository)       │   │
│  │   Mappers)      │     │  + Hibernate Envers    │   │
│  └─────────────────┘     └────────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│               CAPA DE INTEGRACIÓN                       │
│  ┌────────────┬──────────┬─────────────┬──────────┐   │
│  │ Feign      │ SOAP/CXF │ JMS Tibco   │ SFTP     │   │
│  │ Clients    │ Web Svcs │ IBM MQ      │          │   │
│  └────────────┴──────────┴─────────────┴──────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## **3. CARACTERÍSTICAS ARQUITECTÓNICAS CLAVE**

### **A. Persistencia Dual (Polimorfismo de Acceso a Datos)**

**MyBatis (Predominante)**
```java
@Repository
@MapperScan(basePackages = {
    "com.isb.acelera.persistence",
    "com.isb.acelera.repository",
    "com.isb.equity.repository",
    "com.isb.calypso.repository",
    "com.isb.rf.repository"
})
```
- **Uso**: Operaciones CRUD complejas, consultas nativas SQL
- **XML Mappers**: Mapeo explícito de resultados
- **Interceptors**: PageHelper para paginación, VersionInterceptor personalizado
- **Caché**: EhCache integrado con MyBatis

**Spring Data JPA / Hibernate**
- **Uso**: Entidades JPA con auditoría (Hibernate Envers)
- **Repositorios**: Para operaciones estándar
- **Auditoría**: `@EnableJpaAuditing` para tracking de cambios

### **B. Integración Multifacética**

**1. Clientes HTTP Declarativos (OpenFeign)**
```java
@FeignClient(
    name = "MifidRestApi",
    url = "${services.conac-springboot-integration-service.url}",
    configuration = AceleraFeignAutoConfiguration.class
)
```
- **Propagación JWT**: Interceptor `JwtPropagationInterceptorFeign`
- **Circuit Breaker**: Netflix Hystrix (@EnableCircuitBreaker)
- **Fallbacks**: FallbackFactory para degradación controlada
- **Logging**: Nivel FULL en Feign

**2. SOAP Web Services (Apache CXF 3.5.7)**
```java
@Bean
public ServletRegistrationBean<CXFServlet> cXFServletRegistrationBean() {
    return new ServletRegistrationBean<>(new CXFServlet(), "/ws/*");
}
```
- **Cliente SOAP**: Consumo de servicios legacy bancarios
- **JAXWS Handlers**: `CustomSOAPHandler`, `CustomSddSOAPHandler`
- **Configuraciones WS**: 20+ endpoints (SIGA, SDD, RLA, ACFACSEG, etc.)

**3. Mensajería Asíncrona (JMS)**
- **Providers**: Tibco Rendezvous, IBM MQ
- **Listeners**: 
  - `@JmsListener` para Sentinel, Murex, Abacus, Windfall
  - Configuraciones separadas por sistema (JmsListenerSentinelConfig, etc.)
- **Senders**: JmsSenderTibcoConfig, JmsSenderMurexConfig

**4. Protocolos Adicionales**
- **SFTP**: Spring Integration SFTP para transferencia de archivos
- **FIX Protocol**: QuickFIX/J 1.6.3 para operaciones financieras (Bloomberg)

### **C. Seguridad Multicapa**

**Spring Security + Custom SSO**
```java
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MultiSecurityConfig
```

**Características:**
- **Autenticación**: JWT + LDAP + NUAR (Santander Auth Library)
- **LDAP**: Spring Security LDAP para directorio corporativo
- **JWT Management**:
  - `JwtRefreshService` para renovación de tokens
  - `JwtObjectRepository` para persistencia
  - `PublicKeyService` para validación de firmas
- **Autorización basada en roles**: `@RolesAllowed`, SecurityInterceptor custom
- **OWASP**: ESAPI 2.3, encoder-jsp, XSS filters

### **D. Observabilidad y Resiliencia**

**Spring Boot Actuator + Admin**
```yaml
spring:
  application:
    name: conac-springboot-microlito
  boot:
    admin:
      client: # Conectado a Spring Boot Admin Server
```

**Distributed Tracing**
- **Spring Cloud Sleuth 3.1.8**: Correlación de requests
- **Zipkin Integration**: Export de trazas

**Resiliencia**
- **Hystrix**: Circuit Breaker para llamadas externas
- **ShedLock 6.9.2**: Sincronización de scheduled tasks en cluster
- **Hazelcast 5.5.0**: Caché distribuida + lock provider

### **E. Mapeo de Objetos (Multiple Strategies)**

1. **MapStruct 1.6.3**: Code generation en compile-time
2. **Orika**: Mapeo dinámico con Spring Boot Starter
3. **ModelMapper 3.2.3**: Mapeo por convención

### **F. Generación de Código (Swagger Codegen)**

```xml
<plugin>
    <groupId>io.swagger.codegen.v3</groupId>
    <artifactId>swagger-codegen-maven-plugin</artifactId>
</plugin>
```

**Clientes generados**:
- `swagger-fx-api`
- `swagger-asset-master-api`
- `swagger-integration-api`
- `swagger-dfd-api`
- Y múltiples más (~10 clientes generados)

---

## **4. ORGANIZACIÓN MODULAR (PACKAGE BY FEATURE)**

```
com.isb/
├── acelera/           # Core del sistema
│   ├── controller/    # REST endpoints
│   ├── service/       # Lógica de negocio
│   ├── repository/    # JPA repos
│   ├── persistence/   # MyBatis mappers
│   ├── domain/        # Entidades
│   ├── dto/           # Data Transfer Objects
│   ├── mapper/        # Object mappers
│   ├── config/        # Configuraciones Spring
│   ├── security/      # Seguridad
│   └── web/actions/   # Stripes ActionBeans
├── fx/                # Foreign Exchange (Cambios)
├── rf/                # Renta Fija
├── equity/            # Equity/Bolsa
├── rlap/              # RLAP (Asset Master, MiFID)
├── campanyas/         # Campañas
├── safe/              # SAFE (flujos transaccionales)
├── calypso/           # Integración Calypso
├── bloomberg/         # Integración Bloomberg
├── tibportal/         # Tibco Portal
├── sso/               # Single Sign-On
└── wsdl/              # SOAP Web Services
```

**Patrón**: **Package by Feature** con separación por dominio de negocio financiero.

---

## **5. MANEJO DE CONFIGURACIÓN**

**Spring Cloud Config Client**
```yaml
spring:
  config:
    import: configserver:${CONFIG_URL:http://localhost:8888}
  profiles:
    active: local
```

- **Externalización**: Configuración centralizada en Config Server
- **Encriptación**: Jasypt con `encrypt.key`
- **Profiles**: Soporte multi-ambiente (local, dev, pre, pro)

---

## **6. CARACTERÍSTICAS AVANZADAS**

### **Testing**
- **Coverage**: JaCoCo para UT + IT
- **Sonar Integration**: Reportes XML para SonarQube
- **Test Frameworks**: JUnit 5, Mockito 3.4, AssertJ
- **Test containers**: H2, HSQLDB (in-memory)
- **GreenMail**: Test de email
- **Fake SFTP Server**: Test de transferencias

### **Aspectos (AOP)**
- **AspectJ Weaver 1.9.20**: Interceptores transversales
- **Custom Aspects**: Logging, auditoría de operaciones

### **Documentación de APIs**
- **SpringDoc OpenAPI 1.8.0**: Documentación automática
- **Swagger UI**: Interfaz interactiva de APIs
- **Múltiples specs**: Separadas por módulo (Backend, Broker, FX, RF)

### **Internacionalización**
- **ResourceBundles**: `StripesResources_en.properties`, `_es.properties`
- **LocaleResolver**: `AceleraDefaultLocalePicker`
- **Multi-idioma**: Español, Inglés

### **Generación de Documentos**
- **Docx4j 6.1.1**: Manipulación de Word
- **Apache POI 4.1.2**: Excel/Word
- **XDocReport 2.0.2**: Templates + conversión a PDF
- **Velocity 1.7**: Generación de plantillas

---

## **7. CARACTERÍSTICAS DE LEGACY/MODERNIZACIÓN**

**Indicadores de Sistema Legacy en Migración:**

1. **Dual MVC Framework**: Coexistencia Stripes + Spring MVC
2. **WAR Deployment**: No es 100% cloud-native (aunque embeddable)
3. **JSPs**: UI tradicional (no SPA)
4. **Múltiples librerías de mapeo**: Indica evolución gradual
5. **SOAP Heavy**: Gran dependencia de Web Services legacy

**Modernizaciones Implementadas:**
1. ✅ OpenFeign para nuevas integraciones
2. ✅ REST APIs para nuevos endpoints
3. ✅ Spring Cloud Config
4. ✅ Distributed Tracing (Sleuth/Zipkin)
5. ✅ Circuit Breakers
6. ✅ Caché distribuida (Hazelcast)

---

## **8. CONSIDERACIONES DE DEPLOYMENT**

- **Artifact**: `acelera.war` (v6.40.0)
- **Context Path**: `/ACELER_ACELERA`
- **Java Version**: 17 (LTS)
- **Embedded Server**: Tomcat 9 con JSP support
- **Build Tool**: Maven 3.x
- **Profiles**: Configuración multi-ambiente

---

## **CONCLUSIÓN TÉCNICA**

Este proyecto representa un **sistema financiero empresarial crítico** en proceso de modernización arquitectónica. Mantiene una arquitectura **monolítica modular** con separación clara de responsabilidades y alta cohesión por dominio de negocio.

**Fortalezas:**
- Separación clara de capas
- Integración exhaustiva (SOAP, REST, JMS, SFTP, FIX)
- Seguridad robusta multi-nivel
- Observabilidad completa
- Resiliencia con Circuit Breakers
- Testing bien estructurado

**Deudas Técnicas:**
- Dual persistence frameworks (aumenta complejidad)
- Dependencia de Stripes (framework discontinuado)
- WAR deployment (no containerizable sin servidor app)
- JSPs (UI no reactiva)
- Alta acoplamiento con sistemas bancarios legacy

**Estrategia Recomendada**: Migración gradual tipo "Strangler Pattern" hacia arquitectura de microservicios, manteniendo APIs REST como contratos estables y extrayendo módulos independientes (fx, rf, equity) como servicios autónomos.

---

# LIB-PARENT
[volver al indice](#indice)

x612243-sgt_sangroup: Puedes deducir que arquitectura de software se está usando en el proyecto Spring ubicado en la carpeta conac-springboot-LIB-STARTER-PARENT? describe como está estructurado y sus características principales. Dame una descripción desde un punto de vista técnico de un nivel de desarrollador de Software SR.

GitHub Copilot: Voy a analizar la arquitectura del proyecto Spring Boot para darte una descripción técnica detallada. 

Excelente. Ahora tengo toda la información necesaria para darte una descripción técnica completa y detallada de la arquitectura.

---

## Análisis Arquitectónico del Proyecto CONAC Spring Boot Lib Starter Parent

### **Patrón Arquitectónico Principal: Multi-Module Spring Boot Starter Library (Custom BOM + Auto-Configuration)**

Este proyecto implementa una **arquitectura de bibliotecas modulares reutilizables** siguiendo el patrón de **Spring Boot Starters**, diseñada para proporcionar capacidades pre-configuradas y estandarizadas a aplicaciones empresariales del ecosistema Acelera/Santander.

---

## **1. ESTRUCTURA ARQUITECTÓNICA**

### **1.1 Arquitectura de Multi-Módulo Maven (BOM Pattern)**

El proyecto sigue el patrón **Bill of Materials (BOM)** con un POM padre que:
- Hereda de `spring-boot-starter-parent:3.5.8`
- Define 15 módulos especializados
- Centraliza gestión de dependencias mediante `<dependencyManagement>`
- Versión unificada: `3.22.0-SNAPSHOT`
- Java 21 como target runtime

**Características técnicas clave:**
```xml
- Spring Boot: 3.5.8
- Spring Cloud: 2025.0.0
- Java: 21
- Arquitectura: Reactive (WebFlux) + Tradicional (JPA)
```

### **1.2 Módulos y Responsabilidades (Separation of Concerns)**

El proyecto se organiza en **5 categorías funcionales**:

#### **A. Domain Modules (DDD - Domain-Driven Design)**
- **conac-springboot-lib-core-domain**: Objetos de dominio core compartidos
  - DTOs genéricos (`PageDto`, `Range`, `DateRange`, `RawFile`)
  - Value Objects transversales
  - Abstracciones de servicios CRUD
  - Configuración de cache reactivo con AOP

- **conac-springboot-lib-broker-domain**: Modelos de dominio para integraciones
  - Interfaces de clientes (ports) para Web Services
  - Request/Response DTOs para sistemas externos (Murex, SIGA, etc.)
  - **Arquitectura Hexagonal implícita**: Define ports sin implementaciones

#### **B. Infrastructure/Integration Modules (Hexagonal Architecture - Adapters)**
- **conac-springboot-lib-broker**: Implementaciones de adaptadores de comunicación
  - Adapters para RSocket, REST, JMS, Tibco EMS
  - Subdominios especializados: FX, Assets, CSO, Regulatory
  - **Patrón: Adapter para diferentes protocolos de comunicación**

- **conac-springboot-lib-starter-webflux**: Configuración HTTP reactiva
  - WebFlux auto-configurations
  - Swagger/OpenAPI integration
  - Gestión de locales y traducciones
  - Security filters y exception handlers
  - **Stack tecnológico**: Netty + Reactor

- **conac-springboot-lib-starter-rsocket**: Comunicación RSocket
  - Protocolo de comunicación reactivo bidireccional
  - Seguridad RSocket
  - Interceptors para tracing y logging

- **conac-springboot-lib-starter-data-jpa**: Capa de persistencia
  - JPA/Hibernate configurations
  - Base entities con auditoría
  - Converters personalizados
  - **Patrón Repository** (implícito por uso de Spring Data)

- **conac-springboot-lib-starter-s3**: Integración AWS S3
  - Client configurado para almacenamiento objeto
  - AWS SDK 2.x

- **conac-springboot-lib-starter-filenet**: Integración IBM FileNet
  - Cliente para gestión documental empresarial

- **conac-springboot-lib-starter-cache**: Abstracción de caché
  - Hazelcast como provider (5.5.0)
  - Distributed caching
  - Lock management reactivo

#### **C. Cross-Cutting Concern Modules**
- **conac-springboot-lib-starter-core**: Funcionalidades transversales
  - Propagación JWT entre servicios
  - Internacionalización (i18n)
  - WebClient customizations
  - Error handling base

- **conac-springboot-lib-security**: Security framework
  - OAuth2 Resource Server
  - JWT processing (Nimbus JOSE+JWT)
  - Spring Security WebFlux integration
  - **Patrón: Security Filter Chain**

- **conac-springboot-lib-starter-admin-client-webflux**: Observabilidad
  - Spring Boot Admin Client (3.5.6)
  - Integración con actuator

#### **D. Utility Modules**
- **conac-springboot-lib-template**: Motor de plantillas
  - FreeMarker templates
  - SpEL expressions
  - Number to words conversion

- **conac-springboot-lib-docs**: Documentación API
  - SpringDoc OpenAPI (2.8.8)
  - Swagger UI

#### **E. Testing Module**
- **conac-springboot-lib-test**: Testing utilities
  - Helpers para tests de seguridad
  - Mocks y fixtures

---

## **2. PATRONES ARQUITECTÓNICOS IDENTIFICADOS**

### **2.1 Spring Boot Auto-Configuration Pattern**
Cada módulo implementa el mecanismo de auto-configuración:
```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

**Ejemplos encontrados:**
- `WebSecurityAutoConfig`
- `SwaggerAutoConfig`
- `CacheClientAutoConfig`
- `MessageSourceAutoConfig`

**Características:**
- `@AutoConfiguration` en clases de configuración
- `@ConditionalOnProperty` para activación condicional
- Orden de configuración controlado con `before/after`

### **2.2 Hexagonal Architecture (Ports & Adapters)**
Separación clara entre:
- **Ports**: Interfaces en módulos `*-domain` (ej: `SendToMurexClient`, `WindfallAutomaticQuoteClient`)
- **Adapters**: Implementaciones en conac-springboot-lib-broker

### **2.3 Reactive Programming**
- **Paradigma**: Programación reactiva con Project Reactor
- **Stack**: Spring WebFlux + Netty + RSocket
- **Tipos**: `Mono<T>` y `Flux<T>` como primitivas
- **Backpressure**: Manejo nativo de presión hacia atrás

### **2.4 Proxy Pattern para HTTP Clients**
Clase `WebClientProxyHandler` implementa:
- `InvocationHandler` para crear proxies dinámicos
- Mapea anotaciones Spring Web (`@GetMapping`, `@PostMapping`, etc.)
- Genera llamadas WebClient automáticamente
- **Patrón: Dynamic Proxy + Annotation-Driven**

### **2.5 Aspect-Oriented Programming (AOP)**
`ReactorCacheableAspect`:
- Intercepta métodos reactivos
- Aplica caching transparente
- Integración con Hazelcast

### **2.6 Strategy Pattern en Comunicaciones**
Múltiples estrategias de comunicación:
- REST (WebClient)
- RSocket
- JMS/Tibco EMS
- SOAP (CXF)
- Cada una encapsulada en su módulo

---

## **3. CARACTERÍSTICAS TÉCNICAS AVANZADAS**

### **3.1 Stack Tecnológico Enterprise**

**Backend Frameworks:**
- Spring Boot 3.5.8 (última generación)
- Spring Cloud 2025.0.0 (microservicios)
- Spring Security OAuth2 Resource Server
- Spring Data JPA + Hibernate

**Reactive Stack:**
- Spring WebFlux
- Project Reactor
- RSocket protocol
- Netty (servidor no-bloqueante)

**Integraciones Enterprise:**
- IBM FileNet (gestión documental)
- AWS S3 SDK 2.x
- Apache CXF (SOAP/WSDL)
- QuickFIX/J (protocolo FIX financiero)
- Tibco EMS (enterprise messaging)

**Caché Distribuido:**
- Hazelcast 5.5.0 (IMDG)

**Observabilidad:**
- Spring Boot Admin
- Logbook (HTTP logging)
- Actuator endpoints

**API Documentation:**
- SpringDoc OpenAPI 2.8.8
- Swagger UI

### **3.2 Code Generation & Processing**

**Annotation Processing:**
- Lombok (reducción boilerplate)
- MapStruct 1.6.3 (mapeo objetos)
- Hibernate JPA Model Generator

**Code Generation:**
- OpenAPI Generator Maven Plugin
- Apache CXF Codegen (WSDL → Java)

### **3.3 Quality & Testing**

**Testing Stack:**
- JUnit 5
- Reactor Test
- Spring Security Test
- H2 in-memory database
- Awaitility (async testing)
- Podam (test data generation)
- EqualsVerifier
- MockWebServer

**Code Coverage:**
- JaCoCo plugin configurado
- Reportes para unit tests e integration tests
- Integración con SonarQube

---

## **4. PRINCIPIOS DE DISEÑO APLICADOS**

### **4.1 SOLID Principles**
- **Single Responsibility**: Cada módulo con responsabilidad única
- **Open/Closed**: Extensible vía auto-configuration
- **Dependency Inversion**: Interfaces en *-domain, implementaciones en *-impl

### **4.2 Convention over Configuration**
- Auto-configuraciones con defaults sensibles
- Properties con prefijos estandarizados (`amazon.s3`, `hazelcast.cluster`, etc.)

### **4.3 Don't Repeat Yourself (DRY)**
- Lógica común centralizada en `*-core` y `*-core-domain`
- Configuraciones compartidas en POM padre

### **4.4 Separation of Concerns**
- Dominio separado de infraestructura
- Cross-cutting concerns en módulos dedicados

---

## **5. CARACTERÍSTICAS DE SEGURIDAD**

- **OAuth2 + JWT**: Bearer token authentication
- **OWASP Encoder**: Prevención XSS
- **Spring Security**: Filter chain reactivo
- **JWT Propagation**: Entre microservicios (headers)
- **Security Contexts**: Reactive security context holder

---

## **6. PATRONES DE INTEGRACIÓN**

### **Enterprise Integration Patterns (EIP) Implementados:**
- **Message Channel**: JMS/Tibco
- **Service Activator**: RSocket endpoints
- **Content Enricher**: JWT propagation filters
- **Idempotent Receiver**: Potencial en cache layer

---

## **7. DEPLOYMENT & BUILD**

**Build System:**
- Maven multi-module
- Java 21 compilation target
- Source attachments habilitados
- Failsafe + Surefire (unit + integration tests)

**Artifact Publication:**
- Versión SNAPSHOT (`3.22.0-SNAPSHOT`)
- Group ID: `com.acelera`
- Modelo de distribución: Dependency libraries

---

## **CONCLUSIÓN TÉCNICA**

Este proyecto representa una **plataforma de bibliotecas empresariales moderna** que implementa:

1. **Arquitectura Modular**: Separación clara de responsabilidades en 15 módulos especializados
2. **Reactive-First**: Stack completo no-bloqueante (WebFlux + RSocket)
3. **Hexagonal Architecture**: Separación dominio/infraestructura con ports & adapters
4. **Enterprise Integration**: Múltiples protocolos (REST, RSocket, SOAP, JMS, FIX)
5. **Cloud-Native Ready**: Spring Cloud, distributed caching, OAuth2
6. **Developer Experience**: Auto-configuration, code generation, comprehensive testing utilities
7. **Production-Grade**: Observability, security, error handling estandarizados

**Destinado a**: Acelerar desarrollo de microservicios financieros en ecosistema Santander con estándares empresariales pre-configurados y patrones arquitectónicos consistentes.

---

# GATEWAY
[Volver al indice](#indice)

x612243-sgt_sangroup: Puedes deducir que arquitectura de software se está usando en el proyecto Spring ubicado en la carpeta acelera-springboot-gateway? describe como está estructurado y sus características principales. Dame una descripción desde un punto de vista técnico de un nivel de desarrollador de Software SR.

GitHub Copilot: Voy a analizar la arquitectura del proyecto Spring Boot ubicado en `acelera-springboot-gateway`. Déjame examinar los archivos clave para entender su estructura. 


Basándome en el análisis exhaustivo del código, te presento una descripción técnica de nivel Senior Developer sobre la arquitectura del proyecto:

## **Arquitectura del Proyecto: acelera-springboot-gateway**

### **1. Patrón Arquitectónico Principal: API Gateway con Spring Cloud Gateway**

Este proyecto implementa un **API Gateway reactivo** utilizando **Spring Cloud Gateway** sobre el stack WebFlux (programación reactiva no-bloqueante). Actúa como punto de entrada único (Single Entry Point) para múltiples microservicios corporativos, proporcionando capacidades de enrutamiento, autenticación, autorización y transformación de peticiones.

### **2. Stack Tecnológico Core**

```
- Spring Boot 3.x (basado en el parent conac-springboot-lib-starter-parent v3.0.0)
- Spring Cloud Gateway (routing reactivo)
- Spring WebFlux (programación reactiva)
- Spring Security (seguridad)
- Project Reactor (Mono/Flux para programación reactiva)
- Java (inferido como Java 17 o superior por Spring Boot 3.x)
```

### **3. Arquitectura de Capas y Componentes**

#### **a) Capa de Routing (`router/`)**
- **Patrón**: Functional Routing (RouterFunction)
- **Componente principal**: `CorporateRoutes`
- Define endpoints expuestos funcionalmente usando `RouterFunctions` y `ServerResponse`
- Endpoints clave:
  - `/corporate/credential/refresh` - Renovación de credenciales
  - `/corporate/credential/expiration` - Verificación de expiración
  - `/corporate/login` y `/corporate/logoff` - Gestión de sesiones

#### **b) Capa de Filtros (`filter/`)**
- **Patrón**: Chain of Responsibility con GlobalFilter
- **Arquitectura**: Basada en `AbstractFilter` que implementa `GlobalFilter` + `Ordered`
- **Filtros identificados**:
  - `PreGlobalFilter` - Pre-procesamiento global
  - `CorporateFilter` - Validación de credenciales corporativas (JWT/Cookies)
  - `FSEFilter` - Frontend Security Enhancement
  - `RedirectorFilter` - Redirecciones basadas en estados HTTP

**Características destacadas**:
- Orden de ejecución controlado mediante `Ordered`
- Habilitación condicional por ruta mediante metadata
- Procesamiento asíncrono con `Mono<Void>`

#### **c) Capa de Servicios (`service/`)**
- **Patrón**: Service Layer con inyección de dependencias
- **Servicios críticos**:
  - `STSService` - Security Token Service (conversión de tokens corporativos a JWT)
  - `PKMService` - Public Key Manager (gestión de claves públicas)
  - `FSEService` - Frontend Security Enhancement
  - `LogOffService` - Gestión de cierre de sesión
  - `ApiDocsService` - Agregación de documentación OpenAPI/Swagger

**Características**:
- Comunicación HTTP reactiva mediante `WebClient`
- Integración con Resilience4j para retry patterns
- Cacheo distribuido con Hazelcast

#### **d) Capa de Configuración (`config/`)**
- **Patrón**: Externalized Configuration con `@ConfigurationProperties`
- **Configuraciones principales**:
  - `GlobalGatewayConfig` - Configuración global del gateway
  - `CorporateConfig` - Configuración de autenticación corporativa
  - `FSEConfig` - Configuración de seguridad frontend
  - `CacheGatewayConfig` - Configuración de caché distribuido (Hazelcast)
  - `ApplicationConfig` - Configuración de beans principales (WebClient, CircuitBreaker)

#### **e) Seguridad Anti-hacking (`antihacking/`)**
- **Submódulos**:
  - `filter/sanitization/` - Sanitización de inputs
  - `security/sanitize/` - Validación con expresiones regulares
- Protección contra XSS, SQL Injection y otros ataques

#### **f) Manejo de Errores (`error/`)**
- `GlobalErrorWebExceptionHandler` - Manejo global de excepciones
- `GlobalErrorAttributes` - Customización de atributos de error
- `GlobalErrorRedirectWebExceptionHandler` - Redirección en errores específicos
- **Orden de precedencia**: `@Order(Ordered.HIGHEST_PRECEDENCE)`

### **4. Patrones de Diseño Implementados**

| Patrón | Aplicación |
|--------|-----------|
| **Gateway Pattern** | Todo el proyecto es un API Gateway |
| **Chain of Responsibility** | Sistema de filtros encadenados |
| **Template Method** | `AbstractFilter` con método `processFilter()` abstracto |
| **Strategy** | Diferentes estrategias de autenticación (JWT/Cookie/Token) |
| **Dependency Injection** | Constructor-based DI en toda la aplicación |
| **Circuit Breaker** | Resilience4j para llamadas a servicios externos |
| **Cache Aside** | Cacheo de tokens, claves públicas y permisos |

### **5. Características de Resiliencia**

```java
- Resilience4j Circuit Breaker (fallback automático)
- Retry con backoff exponencial
- Time Limiter para timeout de operaciones
- Micrometer para métricas (Prometheus)
- Distributed Tracing con OpenTelemetry
```

### **6. Modelo de Seguridad**

- **Multi-modal authentication**:
  - JWT Tokens (Bearer)
  - Corporate Cookies
  - Corporate Tokens (conversión a JWT vía STS)
- **Validación de tokens**: Usando Nimbus JOSE + JWT
- **Public Key Infrastructure**: Cacheo de claves públicas para validación de firmas
- **Sanitización de inputs**: Validación y limpieza de payloads mediante regex patterns

### **7. Cacheo Distribuido**

```java
Cache Layer: Hazelcast
Cache Stores identificados:
- pkm-public-key (1 hora TTL)
- sts-corporate-to-jwt (1 hora TTL)
- sts-corporate-exp (1 hora TTL)
- fse-useraccess (1 hora TTL)
- fse-usecases (1 hora TTL)
```

**Patrón**: Cache Aside con wrapper `CacheMono` para integración reactiva

### **8. Observabilidad**

- **Spring Boot Actuator** - Health checks y métricas
- **Micrometer** - Métricas con exportación a Prometheus
- **OpenTelemetry** - Distributed tracing
- **Spring Boot Admin Client** - Monitorización centralizada
- **Resilience4j Micrometer** - Métricas de circuit breakers

### **9. Gestión de Documentación API**

- **SpringDoc OpenAPI** - Generación automática de specs OpenAPI 3.0
- **Agregación de Swagger** - Consolidación de documentación de múltiples servicios backend
- Configuración mediante `SwaggerConfig` y `SpringdocConfig`

### **10. Testing Strategy**

```
- Unit Tests: Mockeado con Mockito
- Integration Tests: @SpringBootTest con WebEnvironment
- Mock Server: MockServer JUnit Jupiter
- Reactive Testing: reactor-test (StepVerifier)
- Test Data Generation: Podam para DTOs
```

### **11. Configuración Externa**

- **Spring Cloud Config** - Configuración centralizada
- **Profiles**: Múltiples perfiles (local, test-cookie, test-token, test-fse, etc.)
- **Property binding**: Mediante `@ConfigurationProperties` con validación

### **12. Características Empresariales**

- **Multi-tenancy support** mediante routing dinámico
- **CORS configuration** personalizable
- **Rate limiting** (inferido por el patrón de gateway)
- **Request/Response transformation** mediante filtros
- **Centralización de logging** con SLF4J + Logback

### **Conclusión Técnica**

Este gateway implementa una **arquitectura hexagonal reactiva** donde:
- El core business (autenticación/autorización) está aislado en la capa de servicios
- Los adaptadores (filtros, routers) manejan la entrada/salida
- La programación reactiva garantiza alta concurrencia con bajo uso de threads
- Los patrones de resiliencia aseguran fault tolerance
- El cacheo distribuido reduce latencia y carga en servicios backend

Es un gateway **production-ready** con capacidades enterprise como observabilidad completa, seguridad multi-capa, y manejo robusto de errores, diseñado para escenarios de alta disponibilidad en entornos bancarios/financieros (Santander).