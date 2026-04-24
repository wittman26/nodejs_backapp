### Ejecución de Micros
Antes se ejecutaban así las apps 
    1. acelera-config-service-server
    2. acelera-springboot-gateway
    3. conac-springboot-microlito (Microlito)

Ahora
C:\GIT_REPOS\acelera\acelera-springboot-admin-server\

    1. acelera-springboot-admin-server
    2. acelera-springboot-gateway
    3. cib-conac-sbconfig
    4. conac-springboot-microlito (Microlito)
---
    1. ConfigApplication
    2. AdminServerApplication
    3. GatewayApplication
    4. AceleraApplication

para probar tus cambios, sería ejecutar en este orden:
    1. ConfigApplication
    2. AdminServerApplication
    3. GatewayApplication
    4. FxDbApplication
    5. FxApplication
    
    RestApplication
    IntegrationApplication
---

vm options para ejecución de microlito

    -Djavax.net.ssl.trustStore=C:/Users/x612243/Documents/Programas/jdk-17/lib/security/cacerts
    -Djavax.net.ssl.trustStorePassword=changeit
    -Djavax.net.ssl.trustStoreType=jks
    --add-opens
    java.base/java.lang=ALL-UNNAMED
    --add-opens
    java.base/java.util=ALL-UNNAMED
    --add-opens
    java.base/java.text=ALL-UNNAMED
    --add-opens
    java.base/sun.security.ssl=ALL-UNNAMED
    --add-opens
    java.base/javax.crypto=ALL-UNNAMED
    --add-exports
    java.base/sun.security.ssl=ALL-UNNAMED

## Ajustes de lib-parent antes de ejecucion

<skip.unit.tests>false</skip.unit.tests>

lib-starter-core
		<dependency>
			<groupId>junit</groupId>
			<artifactId>junit</artifactId>
		</dependency>
        	</dependencies>

		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter-api</artifactId>
		</dependency>    

## Git
### fetch y stash

    git fetch origin
    git merge origin/main

    git stash push -m "cambios temporales"
    git pull origin main
    git stash pop

    git stash list
    stash@{0}: On feature/CIBCONAC-4680: cambios temporales
    stash@{1}: On main: cambios en local

    git stash apply stash@{0}

    repourl: file:../cib-conac-sbconfig/src/main/resources/config/tibco/BrokerServicios-pre.dat

### set origin url

    origin  git@github.alm.europe.cloudcenter.corp:cib-conector-acelera/conac-relational-database-oracle.git (fetch)
    origin  git@github.alm.europe.cloudcenter.corp:cib-conector-acelera/conac-relational-database-oracle.git (push)

    url = git@github.com:santander-group-scib-gln/cib-conac-cncrltndbora.git        

    git remote set-url origin git@github.com:santander-group-scib-gln/cib-conac-cncrltndbora.git


-----------------

### Tips en desarrollo Acelera

 OpenMocks -> no usar

 upsertTrade
 usar mappers

no usar response y request ni en domain ni en application


 Mensajes por BD en lugar de constantes
 Mirar micro para mensajes -> cso-admin
    ProductValidator.java
    SegmentsValidator.java

    BD -> ACELER_ENTIDADES

Domain -> borrar y crear dos services:
    un service para save
    un service para find


Refactor Save and Get TradeSignature. Update tests and mappers.


SecurityUtils

- clean a lib parent y a fx-db
- invalidate caches
- clean install a lib parent
- Reload all maven projects
- clean compile a fx-db

AceleraReactiveJwtDecoder


    1. ConfigApplication
    2. AdminServerApplication
    3. fx-db
    4. fx


##
De Object a JSON
new ObjectMapper().writeValueAsString(expedientRequest)

new com.fasterxml.jackson.databind.ObjectMapper()
    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
    .writeValueAsString(expedientRequest)

new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .writeValueAsString(expedientRequest)    

//No hay datos
ACELER.ACE_EVENT_DISCLAIMER
ACELER.ACE_OPERATION_DISCLAIMER

INSERT INTO "ACELER"."ACE_OPERATION_DISCLAIMER" (ENTITY, TRADE_ID, NAME, CONTENT) 
VALUES ('0049', '1425', 'test', 'Contenido disclaimer');

INSERT INTO "ACELER"."ACE_OPERATION_DISCLAIMER" (ENTITY, TRADE_ID, NAME, CONTENT) 
VALUES ('0049', '117451', 'test', 'Contenido disclaimer');

https://acelera.scib.dev.corp/acelera-api/fx

--ACELERA_DES_ESQUEMA_ACELER
SELECT *
FROM ACE_OPERATION_DISCLAIMER;


--ACELERA_DES_ESQUEMA_FX
SELECT *
FROM "ACELER_FX"."FX_TRADE_SIGNATURE";

ExpedientCreateCmd


model.validator.codigoj.message -> Formato de código de persona incorrecto


{
    "sourceApp": {
        "operCode": "ACETRADE",
        "code": "ACELERA",
        "url": "/v1/trades-signatures/expedients/{id}?status="
    },
    "startDate": "2025-07-06T11:23:20.943Z",  
    "endDate": "2025-09-06T11:23:20.943Z",  
    
    "startDate": "2025-08-07T14:22:56.1052008+02:00",        
    "endDate": "2025-08-12T14:22:56.1052008+02:00",
        
    "centre": "0001",
    "typeReference": "Derivado Divisa",
    "indicatorBusinnessMailBox": false,
    "indicatorParticularMailBox": false,
    "clauses": [],
    "customerId": "A28269983",
    "typeBox": "B092",
    "catBox": "divisas",
    "productDesc": "Derivado Divisa",
    "descExp": "Contratación Derivado Divisa",
    "channel": "OFI",
    "docs": []
}

----
5. Obtener titular -> 

                productId = ‘AN’, ‘IN’, ‘PC’ o ‘PS’
                    ACE_EVENTO campos TITULAR, NOMBRE_TITULAR, DOCUMENTO_TITULAR e  IDCENT
            Else  
                productId = AC
                    ACE_ACUM_CLIENTE campos HOSTID, NOMBRE y DOCUMENTO
                productId != AC
                    ACE_OPERACION campos TITULAR e IDCENT
                    ACE_OPERACION_TITULARES campos NOMBRE y DOCUMENTO
6. Obtener el listado de cláusulas de la operación a incluir en el expediente.
    Convertir en list

7. Generar el expediente de firma de la operación

- "endDate": "2025-02-27T22:55:57.371Z",  → + 
        X días sobre startDate (en UTC), parámetro que se saca de ACELER_ENTIDADES.SAFE_VARIABLE.VALOR cuando ACELER_ENTIDADES.SAFE_VARIABLE.NOMBRE = "FX_SIGNATURE_VALIDITY_DAYS"  

- "clauses": [ Lista de clausulas obtenida de las tablas ACELER.ACE_OPERATION_DISCLAIMER o ACELER.ACE_EVENT_DISCLAIMER      


ACE_DOCUMENTOS
ACE_EVENTO_DOC


METADATA
    NUM_PERSONA_CLI
    J0000565695
    Quitar ceros desde J hasta numero

Valida expedientId al principio.

954274

ACELER_ENTIDADES.SAFE_VARIABLE.VALOR

## JSPs en Microlito

<% System.out.println("Mensaje de debug"); %> o <c:out>


    <div>
        <c:if test="${actionBean.mostrarFirmaDigital}">
            <stripes:layout-render name="layouts/spotFwd/FirmaDigital.jsp" />
        </c:if>
    </div>


    <stripes:layout-render name="./BusquedaClienteFirmaDig.jsp"
    			tipoProducto="${actionBean.tipoProducto.toString()}"
    			validarViaSentinel="true"
    			validarViaEmir="true"
    			validarFxCoverage="true" />

				function(data) {
                      // Elimina todas las opciones excepto la vacía
                      $('#tipoDocumento').find('option:not([value=""])').remove();
                      console.log('Datos recibidos Z:', data);
					$.each(data, function(val, text) {
					    console.log('Descripcion:', this.descripcion);
					    $('#tipoDocumento').append( $('<option/>').attr('value', this.id).text(this.descripcion) );
					});
				}                

Generación de Expediente
    MIRAR KEY DOCUMENT
SpringJpaDocumentRepository    

Estandar.jsp


    @HandlesEvent("guardarFirmante")
    public Resolution guardarFirmante() throws JsonProcessingException {
        // Crear respuesta JSON
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Firmante guardado correctamente");
        response.put("firmanteId", UUID.randomUUID().toString());
        
        return new StreamingResolution("application/json",
            new ObjectMapper().writeValueAsString(response));
    }
    
    <!-- Notación JSP/FreeMarker -->
    <script>
        var onlyJuridicas = ${onlyJuridicas?true:false};

        var digitalSignatureEnabled = ${actionBean.mostrarFirmaDigitalGenerico ? 'true' : 'false'};
        // Variable para empresas
        var digitalSignatureCompaniesEnabled = ${actionBean.mostrarFirmaDigitalCompaniesGenerico ? 'true' : 'false'};
        var digitalSignatureGlobalCustomerEnabled = ${actionBean.mostrarFirmaDigitalGlobalCustomerGenerico ? 'true' : 'false'};
        var producto = '${actionBean.producto}';

        console.log('digitalSignatureEnabled:', digitalSignatureEnabled);
        console.log('digitalSignatureCompaniesEnabled:', digitalSignatureCompaniesEnabled);
        console.log('digitalSignatureGlobalCustomerEnabled:', digitalSignatureGlobalCustomerEnabled);
        console.log('digitalSignatureGlobalCustomerEnabled:', producto);
        
        console.log('digitalSignatureGlobalCustomerEnabled: ');
        console.log('btnBusquedaEnabledParam:', '${btnBusquedaEnabledParam}');
    </script>


    // Función para guardar firmante llamando al ActionBean
function guardarFirmanteEnBackend(datosFormante) {
    console.log('Llamando a guardarFirmante en FirmaDigitalActionBean');
    
    // Construir la URL del action
    var actionUrl = $('#frmFirmaDigitalAjax').attr('action') || '';
    // Si no hay formulario oculto, construir la URL manualmente
    if (!actionUrl) {
        // Obtener la URL base del contexto
        var contextPath = '${pageContext.request.contextPath}';
        actionUrl = contextPath + '/FirmaDigital.action';
    }
    
    // Asegurarse de que la URL termine con .action
    if (!actionUrl.endsWith('.action')) {
        actionUrl = actionUrl.replace(/\/[^\/]*$/, '/FirmaDigital.action');
    }

    // Buscar cualquier formulario Stripes en la página
    var $stripesForm = $('form[action*=".action"]').first();
    var actionUrl = '';
    
    if ($stripesForm.length > 0) {
        // Obtener la URL base del formulario existente
        var baseUrl = $stripesForm.attr('action');
        // Reemplazar el action con FirmaDigital.action
        actionUrl = baseUrl.replace(/[^\/]+\.action/, 'FirmaDigital.action');
    } else {
        // Construir la URL manualmente
        actionUrl = window.location.pathname.replace(/[^\/]+$/, 'FirmaDigital.action');
    }

    console.log('URL del action:', actionUrl);
    
    $.ajax({
        url: actionUrl,
        type: 'POST',
        data: {
            // El parámetro que identifica el método @HandlesEvent
            'guardarFirmante': '',
            // Los datos del firmante
            'nombre': datosFormante.nombre,
            'tipoCodigoPersona': datosFormante.tipoCodigoPersona,
            'documento': datosFormante.documento,
            'tipoIntervencion': datosFormante.tipoIntervencion,
            'esCliente': datosFormante.escliente
        },
        dataType: 'json',
        success: function(response) {
            console.log('Respuesta del servidor:', response);
            
            if (response.success) {
                console.log('Firmante guardado exitosamente');
                // Puedes guardar el ID retornado para uso futuro
                datosFormante.id = response.firmanteId;
                
                // Agregar a la tabla visual
                agregarFirmanteATabla(datosFormante);
            } else {
                console.error('Error al guardar firmante:', response.message);
                alert('Error al guardar firmante: ' + response.message);
            }
        },
        error: function(xhr, status, error) {
            console.error('Error en la llamada AJAX:', error);
            alert('Error al comunicarse con el servidor');
        }
    });
}    


http://localhost:8000/ACELER_ACELERA/actions/FirmaDigital.action?generarExpedientesFirma=&producto=SP&originId=119295&_=1759737879254

SpotFxActionBeanTest - 129 errores
SwapFxActionBeanTest - 14 errores
SpotFwdFxActionBeanTest - 31 errores
SeguroCambioFxActionBeanTest - 85 errores
SeguroCambioLiqDownFxActionBeanTest - 18 errores
SeguroCambioLiqExtensibleFxActionBeanTest - 4 errores
SeguroCambioLiqFijaFxActionBeanTest - 2 errores
SeguroCambioLiqUpFxActionBeanTest - 3 errores
NonDeliveryFwFxActionBeanTest - 2 errores
CotizadorEstructuraFxActionBeanTest - 1 error


Genera una clase de test para FirmaDigitalService haciendo un coverage de 100% del código para pasar el SONAR; hazlo teniendo con el mismo estilo que la clase FiltroProductosServiceTest y manejando mejores prácticas.

applicationContext-services-tests.xml

/**********************************/

-> firmaDigitalPanel.js



FX-DB

<version>3.9.0-SNAPSHOT</version>
ViewTradeSignerDocumentStatusModel

    @Id
    @Column(name = "GN_ID")
    @ColumnTransformer(read = "COALESCE(GN_ID, 'NONE')")
    private String gnId;


LPA
Acumuladores
parte de negociación


/************************/

    private static final String FX_PREFIX = "/acelera-api/fx";
    //private static final String FX_PREFIX = "/ACELER_ACELERA/backend/api";

    @Override
    public Mono<DocumentLpaResponse> generateDocumentLpa(DocumentLpaCreateRequest request) {
        String token = request.getToken();
        Long originId = request.getOriginId();
        request.setToken(null); // No enviamos el token en el body, solo en la cabecera
        request.setLocale(null); // No enviamos el locale en el body, no es necesario
        request.setOriginId(null); // No enviamos el originId en el body, no es necesario
        return webClient.post()
                .uri(builder -> builder.path(FX_PREFIX + "/v1/micro/trades-signatures/{originId}/documents")
                        .build(originId))
                .bodyValue(request)
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(getCleanToken(token));
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handleError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleError)
                .bodyToMono(DocumentLpaResponse.class);
    }

Limpiar llamados integration y rest de fx


- Convertir a JSon un objeto en IntelliJ debug:
org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json()
        .simpleDateFormat("yyyy-MM-dd")
        .build()
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(nombreObjeto)

---
