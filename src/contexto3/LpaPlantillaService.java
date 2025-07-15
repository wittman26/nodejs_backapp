package com.isb.acelera.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.util.StringUtil;
import com.google.gson.Gson;
import com.isb.acelera.component.CampanyasClientMifid;
import com.isb.acelera.connection.rest.LpaCreateDocument;
import com.isb.acelera.domain.*;
import com.isb.acelera.domain.format.FormatUtils;
import com.isb.acelera.domain.format.XmlUtils;
import com.isb.acelera.persistence.LpaConfigPlantillaMapper;
import com.isb.acelera.persistence.LpaPlantillaMapper;
import com.isb.acelera.persistence.OrdenanteMapper;
import com.isb.acelera.persistence.XpathConfigPlantillaMapper;
import com.isb.acumuladores.service.AcumuladoresMifidService;
import com.isb.campanyas.backend.repository.ClienteOperacionRepository;
import com.isb.wsdl.domain.lpa.DocumentDownloadResponse;
import com.isb.wsdl.domain.lpa.Error;
import com.isb.wsdl.domain.lpa.WorkflowResponse;
import com.tibco.sdk.MException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xpath.CachedXPathAPI;
import org.dom4j.DocumentException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static com.isb.acelera.util.CiberDeferUtils.neutralize;

/**
 * Contiene logica de negocio para generar documentacion LPA
 */
@Service
@RequiredArgsConstructor
public class LpaPlantillaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LpaPlantillaService.class);
    private static final String CLAUSULAS_MIFID = "clausulasMiFID";
    private static final String OTRAS_CLAUSULAS = "otrasClausulas";
    private static final String NO_DATETIME_FOUND = "TZ";

    private final LpaCreateDocument lpaCreateDocument;
    private final LpaPlantillaMapper lpaPlantillaMapper;
    private final LpaConfigPlantillaMapper lpaConfigPlantillaMapper;
    private final XpathConfigPlantillaMapper xpathConfigPlantillaMapper;
    private final MifidService mifidService;
    private final EliminarAdvertenciasMifidService eliminarAdvertenciasMifidService;
    private final @Lazy AcumuladoresMifidService acumuladoresMifidService;
    private final CampanyasClientMifid campanyasClientMifid;
    private final ClienteOperacionRepository clienteOperacionRepository;
    private final OperacionEventoService operacionEventoService;
    private final EstructurasFxService estructurasFxService;
    private final OperacionFwdService operacionFwdService;
    private final UsuarioService usuarioService;
    private final VariablesService variablesService;
    private final DetalleOperacionFxService operacionFx;
    private final PersonaService personaService;
    private final ParesDivisasService paresDivisasService;
    private final OrdenanteMapper ordenanteMapper;

    public List<ComboBean> getPlantillaByTipoDocumento(String entidad, String tipoDocumento) {
        return lpaPlantillaMapper.getPlantillaByTipoDocumento(entidad, tipoDocumento);
    }

    public List<ComboBean> getPlantillaByProducto(String entidad, String idProducto) {
        return lpaPlantillaMapper.getPlantillaByProducto(entidad, idProducto);
    }

    public List<LpaPlantilla> getPlantilla(String entidad, String idProducto, String tipoDocumento) {
        return lpaPlantillaMapper.getPlantilla(entidad, idProducto, tipoDocumento);
    }

    /**
     * Devuelve en formato definido por nosotros la cadena fix para luego convertir
     * 
     * @param empresa       : bank entity
     * @param idOperacion   : operacion ID
     * @param tipoProducto  : product type of ace_lpa_config_plant
     * @param tipoDocumento : document type of ace_lpa_config_plant
     * @return cadenaFix
     */
    public String buildMessageFromLPA(String empresa, String idOperacion, String tipoProducto, String tipoDocumento) {
        List<LpaPlantilla> listaLpaPlantilla = lpaPlantillaMapper.getPlantilla(empresa, tipoProducto, tipoDocumento);
        return getXmlFromDataBase(listaLpaPlantilla.get(0), idOperacion, null, null, null);
    }

    /**
     * Para sentinel a parte de generar el xml luego tenemos que modificar algun
     * valor de los nodos con los que nos vienen por parametro
     *
     * @param operacion Operacion
     * @param evento    evento
     * @param idUsuario idUsuario
     * @param trader    trader
     * @return xml
     */
    public String getXmlSentinel(Operacion operacion, String evento, String idUsuario, String trader) {

        try {

            List<LpaPlantilla> listaLpaPlantilla = lpaPlantillaMapper.getPlantilla(operacion.getEntidad(),
                    operacion.getProducto().getId(), TipoDocumento.SENTINEL.toString());
            if (listaLpaPlantilla == null || listaLpaPlantilla.isEmpty()) {
                throw new IllegalStateException("LpaPlantilla from DB cannot be null or empty");
            }
            Document documento = XmlUtils
                    .parseXmlText(new InputSource(new StringReader(getXmlFromDataBase(listaLpaPlantilla.get(0),
                            operacion.getId().toString(), null, idUsuario, null))));

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            XPathExpression expr;

            expr = xpath.compile("//AC_TipoSentinel");
            Node node = (Node) expr.evaluate(documento, XPathConstants.NODE);
            if (node != null)
                node.setTextContent(evento);

            expr = xpath.compile("//AC_TipoSentinelFX");
            node = (Node) expr.evaluate(documento, XPathConstants.NODE);
            if (node != null)
                node.setTextContent(evento);

            expr = xpath.compile("//AC_EstadoOperacion");
            node = (Node) expr.evaluate(documento, XPathConstants.NODE);
            if (node != null)
                node.setTextContent(operacion.getEstado().getId());

            // actualizamos la etiqueta con el usuario que lanza el evento
            expr = xpath.compile("//AC_Ventas");
            node = (Node) expr.evaluate(documento, XPathConstants.NODE);
            if (node != null && StringUtils.isBlank(node.getTextContent())) { 
                node.setTextContent(idUsuario.toLowerCase());
                expr = xpath.compile("//AC_Trader");
                node = (Node) expr.evaluate(documento, XPathConstants.NODE);
                if (node!=null) {
                    node.setTextContent(idUsuario.toLowerCase());
                } else {                    
                    Element name = documento.createElement("AC_Trader");
                    name.setTextContent(idUsuario.toLowerCase());
                    documento.getDocumentElement().appendChild(name);
                }
            }
            
                
            expr = xpath.compile("//AC_Originador");
            node = (Node) expr.evaluate(documento, XPathConstants.NODE);
            if (node != null && trader != null)
                node.setTextContent(trader);

            String fecha = operacionEventoService.getFechaEvento(operacion.getId().toString(), operacion.getEntidad(),
                    evento);
            expr = xpath.compile("//AC_FechaHoraSentinel");
            node = (Node) expr.evaluate(documento, XPathConstants.NODE);
            if (node != null && !NO_DATETIME_FOUND.equals(fecha))
                node.setTextContent(fecha);

            return documentToString(documento);

        } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException
                | TransformerException e) {
            LOGGER.error("Error al grave al generar el xml de sentinel: ", e);
            return null;
        }

    }

    public String getXmlSentinel(String entidad, String producto, String idUsuario, Long clienteDfaId) {

        try {

            List<LpaPlantilla> listaLpaPlantilla = lpaPlantillaMapper.getPlantilla(entidad, producto,
                    TipoDocumento.SENTINEL.toString());
            if (listaLpaPlantilla == null) {
                throw new IllegalStateException("listaLpaPlantilla from DB is null");
            }
            Document documento = XmlUtils.parseXmlText(new InputSource(new StringReader(
                    getXmlFromDataBase(listaLpaPlantilla.get(0), clienteDfaId.toString(), null, idUsuario, null))));

            return documentToString(documento);

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            LOGGER.error("Error al grave al generar el xml de sentinel: ", e);
            return null;
        }

    }

    /**
     * A partir del id correspondiente de la plantilla se genera el xml a partir de
     * las etiquetas de BD. En caso de tener plantilla xml en BD entonces se realiza
     * el mapeo del xml formado de la BD con la plantilla a partir de los XPATH
     * recuperados de la BD
     *
     * @param lpaPlantilla            LpaPlantilla
     * @param idOperacion             idOperacion
     * @param idAlternativoOrCampanya idAlternativoOrCampanya
     * @param usuario                 usuario
     * @param idSgcDocCamp            idSgcDocCamp
     * @return xml
     */
    public String getXmlFromDataBase(LpaPlantilla lpaPlantilla, String idOperacion, String idAlternativoOrCampanya,
            String usuario, String idSgcDocCamp) {

        List<LpaConfigOrigen> listaTipoOrigen = lpaConfigPlantillaMapper
                .getTipoOrigenPlantilla(lpaPlantilla.getEntidad(), lpaPlantilla.getIdPlantilla());

        String xmlFinal;

        // Se genera el xml a partir de la plantilla definida en BD
        StringBuilder xml = new StringBuilder("<Acelera>");
        for (LpaConfigOrigen configOrigen : listaTipoOrigen) {
            List<LpaConfigPlantilla> listaConfigPorTipo = lpaConfigPlantillaMapper.getConfigPlantilla(
                    lpaPlantilla.getEntidad(), lpaPlantilla.getIdPlantilla(), configOrigen.getTipoOrigen(),
                    configOrigen.getCondicion());
            try {
                xml.append(insertDataXML(listaConfigPorTipo, configOrigen.getTipoOrigen(), null,
                        lpaPlantilla.getEntidad(), idOperacion, idAlternativoOrCampanya, usuario, idSgcDocCamp));
            } catch (MException e) {
                LOGGER.error("Se ha producido un error al generar el fichero XML desde la configuracion de BD:", e);
            }
        }
        xml.append("</Acelera>");

        xmlFinal = xml.toString().replace("&", "&amp;");

        // Si hay plantilla xml entonces a partir de xml origen hay que realizar los
        // mapeos a la plantilla final
        if (lpaPlantilla.getPlantillaXml() != null) {
            try {
                xmlFinal = configXpath(xmlFinal, lpaPlantilla);
            } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                LOGGER.error("Se ha producido un error al realizar los mapeos de la configuracion del Xpath:", e);
            }
        }

        // Por ultimo se realiza la transformacion xslt
        if (lpaPlantilla.getFicheroXslt() != null || lpaPlantilla.getNombreFicheroXslt() != null) {
            try {
                xmlFinal = transformXslt(xmlFinal, lpaPlantilla.getNombreFicheroXslt(), lpaPlantilla.getFicheroXslt());
            } catch (TransformerException e) {
                LOGGER.error("Se ha producido un error al realizar la transformacion XSLT", e);
            }
        }

        return xmlFinal;

    }

    /**
     * @param dataXml      dataXml
     * @param lpaPlantilla LpaPlantilla
     * @return configXpath
     * @throws ParserConfigurationException if ParserConfigurationException
     * @throws SAXException                 if ParserConfigurationException
     * @throws IOException                  if IOException
     * @throws TransformerException         if TransformerException
     */
    public String configXpath(String dataXml, LpaPlantilla lpaPlantilla)
            throws ParserConfigurationException, SAXException, IOException, TransformerException {

        Document documentoOrigen = XmlUtils.parseXmlText(dataXml);
        Document documentoDestino = XmlUtils.parseXmlText(lpaPlantilla.getPlantillaXml());

        CachedXPathAPI cachedXPathAPI = new CachedXPathAPI();

        // recorre la lista de mapeos del xpath origen a destino
        List<XpathConfigPlantilla> listaXpathConfig = xpathConfigPlantillaMapper
                .getXpathConfigPlantilla(lpaPlantilla.getEntidad(), lpaPlantilla.getIdPlantilla());
        for (XpathConfigPlantilla xpathconfig : listaXpathConfig) {

            try {

                NodeList nodeListOrigen = cachedXPathAPI.selectNodeList(documentoOrigen, xpathconfig.getXpathOrigen());
                if (nodeListOrigen == null || nodeListOrigen.getLength() == 0) {
                    LOGGER.warn("No se ha encontrado la etiqueta del nodo origen: {}",
                            neutralize(xpathconfig.getXpathOrigen()));
                    continue;
                }

                NodeList nodeListDestino = cachedXPathAPI.selectNodeList(documentoDestino,
                        xpathconfig.getXpathDestino());
                if (nodeListDestino == null || nodeListDestino.getLength() == 0) {
                    LOGGER.warn("No se ha encontrado la etiqueta del nodo destino: {}",
                            neutralize(xpathconfig.getXpathDestino()));
                    continue;
                }
                nodeListDestino.item(0).setTextContent(nodeListOrigen.item(0).getTextContent());

            } catch (Exception e) {
                LOGGER.error("Error de mapeado desde el origen [{}] al destino [{}]. Error: {}",
                        neutralize(xpathconfig.getXpathOrigen()), neutralize(xpathconfig.getXpathDestino()),
                        neutralize(e.getMessage()), e);
            }

        }
        return documentToString(documentoDestino);

    }

    /**
     * Metodo que aplica el XSLT al xml generado
     *
     * @param dataXml   dataXml
     * @param inputXslt inputXslt
     * @return xml
     * @throws TransformerException if TransformerException
     */
    public String transformXslt(String dataXml, String nombreFicheroXslt, String inputXslt)
        throws TransformerException {

        String dataXslt = getContentFrom(nombreFicheroXslt);
        XmlUtils.checkRiskyXSLT(dataXslt);
        XmlUtils.checkRiskyXML(dataXml);

        final TransformerFactory transformerFactory = getTransformerFactory();
        StreamSource xsltSource = new StreamSource(new StringReader(dataXslt));
        final Transformer transformer = transformerFactory.newTransformer(xsltSource);

        final StringWriter writer = new StringWriter();
        final StreamSource xmlSource = new StreamSource(new StringReader(dataXml));
        transformer.transform(xmlSource, new StreamResult(writer));
        return addAtributos(writer.toString());
    }

    private String getContentFrom(String nombreFicheroXslt) throws TransformerException {
        try (InputStream xsltStreamAutoCloseable = indirectPathTraversal(nombreFicheroXslt)) {
            return IOUtils.toString(xsltStreamAutoCloseable, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Error reading file xslt %s", nombreFicheroXslt), e);
        }
    }

    public InputStream indirectPathTraversal(String nombreFile) throws TransformerException {
        try {
            ClassPathResource cpr = new ClassPathResource("plantillas/" + FilenameUtils.getName(nombreFile));
            if (cpr.exists()) {
                return cpr.getInputStream();
            } else {
                throw new TransformerException("Plantilla no encontrada");
            }
        } catch (Exception e) {
            LOGGER.error("Se ha producido un error al buscar el fichero para la transformacion XSLT", e);
            throw new TransformerException("");
        }
    }

    /*
     * <Product>
     * <AtributoXslt>Product%ProductTypeId="OTCFXNOFW06XFXAF"</AtributoXslt> =>
     * <Product ProductTypeId="OTCFXNOFW06XFXAF">
     *
     * <portfolioReference/>
     * <AtributoXslt>portfolioReference%href="#PORTFOLIO"</AtributoXslt> =>
     * <portfolioReference href="#PORTFOLIO"\>
     *
     *
     * Como en el xstl capa los atributos entonces los metemos en un tag para
     * despues anadirlo de la siguiente forma: 1.- Se busca en xml todos tag que se
     * llamen AtributoXslt 2.- Dentro separamos por %: la primera parte es el nombre
     * del tag del xml donde vamos a reemplazar y la segunda parte el valor a
     * incluir como atributo 3.- Reemplazamos ese tag para anadir ademas el valor
     * del atributo 4.- Eliminamos toda la linea que hemos usado
     * (<AtributoXslt>Product%ProductTypeId="OTCFXNOFW06XFXAF"</AtributoXslt>) del
     * xml final y temporal para que no se vuelva a ejecutar
     */
    private String addAtributos(String xml) {
        try {
            String xmlTratar = xml;
            String tag = "AtributoXslt";
            String valoresAtributo = null;
            while (xmlTratar.contains(tag)) {
                int inicio = xmlTratar.indexOf(tag) + tag.length() + 1;
                int incremento = xmlTratar.substring(inicio).indexOf("<");
                valoresAtributo = xmlTratar.substring(inicio, inicio + incremento).trim();
                xmlTratar = xmlTratar.substring(inicio + incremento + tag.length());
                if (StringUtil.isNotEmpty(valoresAtributo)) {
                    String[] valores = valoresAtributo.split("%");
                    xml = xml.replace(Constantes.XML_OPEN_TAG + valores[0],
                            Constantes.XML_OPEN_TAG + valores[0] + " " + valores[1].trim());
                    xml = xml.replace(Constantes.XML_OPEN_TAG + tag + Constantes.XML_CLOSE_TAG + valoresAtributo
                            + Constantes.XML_OPEN_TAG + Constantes.SLASH + tag + Constantes.XML_CLOSE_TAG, "");
                    xmlTratar = xmlTratar
                            .replace(Constantes.XML_OPEN_TAG + tag + Constantes.XML_CLOSE_TAG + valoresAtributo
                                    + Constantes.XML_OPEN_TAG + Constantes.SLASH + tag + Constantes.XML_CLOSE_TAG, "");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Error incluyendo atributos del xslt.", e);
        }
        return xml;
    }

    private static TransformerFactory getTransformerFactory() throws TransformerConfigurationException {
        return XmlUtils.getTransformerFactorySecured();
    }

    /**
     * @param element Element
     * @param out     OutputStream
     * @throws TransformerException if TransformerException
     */
    public static void elementToStream(Element element, OutputStream out) throws TransformerException {
        DOMSource source = new DOMSource(element);
        StreamResult result = new StreamResult(out);
        TransformerFactory transFactory = getTransformerFactory();
        Transformer transformer = transFactory.newTransformer();
        transformer.transform(source, result);
    }

    /**
     * @param doc Document
     * @return documentToString
     * @throws TransformerException if TransformerException
     */
    public static String documentToString(Document doc) throws TransformerException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        elementToStream(doc.getDocumentElement(), baos);
        return baos.toString();
    }

    /**
     * Genera documentacion segun una operacion de renta fija y tipo de documento
     *
     * @param idOperacion             la operacion
     * @param idAlternativo           idAlternativo
     * @param entidad                 entidad
     * @param tipoDocumento           el tipo de documento
     * @param usuario                 usuario
     * @param idProducto              idProducto
     * @param idSgcDocCamp            idSgcDocCamp
     * @return la respuesta de LPA
     * @throws DocumentException la exception
     */
    @ExcludeAopPoincut
    public DocumentacionWrapper generarDocumentacion(String idOperacion, String idAlternativo, String entidad,
            String tipoDocumento, String usuario, String idProducto, String idSgcDocCamp) throws DocumentException {

        LpaResponse lpaResponse;
        List<LpaPlantilla> listaLpaPlantilla = lpaPlantillaMapper.getPlantilla(entidad, idProducto, tipoDocumento);
        DocumentacionWrapper docWrapper = new DocumentacionWrapper();

        final Optional<String> idPlantillaClientDataCamp = listaLpaPlantilla.stream()
                .filter(x -> x.getIdPlantilla().startsWith("CLIENTDATA_CAMP")).findFirst()
                .map(LpaPlantilla::getIdPlantilla);

        String formatoEnvio = listaLpaPlantilla.get(0).getFormatoEnvio();
        String formatoRespuesta = listaLpaPlantilla.get(0).getFormatoRespuesta();
        String resultado;
        String request;
        JSONObject jsonRequest = null;

        if (formatoEnvio.contains(MediaType.APPLICATION_XML_VALUE)) {
            request = getXmlFromDataBase(listaLpaPlantilla.get(0), idOperacion, idAlternativo, usuario,
                    idSgcDocCamp);
        } else {
            jsonRequest = generarJsonRequest(idOperacion, idAlternativo, entidad, tipoDocumento, usuario,
                    idProducto, idSgcDocCamp);
            request = jsonRequest.toJSONString();
        }

        if (formatoRespuesta.contains(MediaType.APPLICATION_XML_VALUE)) {

            resultado = lpaCreateDocument.executePost(request, listaLpaPlantilla.get(0).getPath(), formatoEnvio,
                    formatoRespuesta);
            WorkflowResponse workflowResponse = getXmlFromResponse(resultado);
            if (!workflowResponse.isSuccess()) {
                final Error error = workflowResponse.getErrors().getError();
                LOGGER.error("Error: {} - {}", neutralize(error.getErrorCode()), neutralize(error.getMessage()));
                throw new DocumentException(error.getMessage());
            }

            docWrapper.setWorkflowId(Integer.valueOf(workflowResponse.getWorkflowResponseData().getId()));
            DocumentDownloadResponse documento = getDocumentFromResponse(workflowResponse,
                    listaLpaPlantilla.get(0).getNombreDocumento());
            if (documento != null) /*
                                    * En los acumuladores la primera vez no tenemos documento solo el id del
                                    * workflow
                                    */ {
                docWrapper.setDatos(documento.getDocumentData().getDocumentContent());
                docWrapper.setDocumentoId(documento.getDocumentData().getId());
            }

        } else {

            JSONObject jsonResul = lpaCreateDocument.executePost(jsonRequest, listaLpaPlantilla.get(0).getPath());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            try {
                lpaResponse = objectMapper.readValue(jsonResul.toJSONString(), LpaResponse.class);
            } catch (Exception e) {
                LOGGER.error("Error: ", e);
                throw new DocumentException("");
            }

            if (lpaResponse.getErrors() != null && !lpaResponse.getErrors().isEmpty()) {
                final Optional<LpaError> error = Optional.of(lpaResponse).map(LpaResponse::getErrors)
                        .map(x -> x.get(0));

                final String message = error.map(LpaError::getMessage).orElse("");
                final String errorCode = error.map(LpaError::getErrorCode).orElse("");

                LOGGER.error("Error: {} - {}", neutralize(errorCode), neutralize(message));
                throw new DocumentException(message);
            }
            docWrapper.setDatos(lpaResponse.getDocuments().get(0).getSerializedDoc());
            docWrapper.setDocumentoId(lpaResponse.getDocuments().get(0).getDocumentId() + "");

            if (isCampanyasAndWorkflowIdNotPresentInResponse(lpaResponse, jsonRequest, idPlantillaClientDataCamp)) {
                docWrapper.setWorkflowId(Integer.parseInt((String) jsonRequest.get("WorkflowId")));
            } else {
                docWrapper.setWorkflowId(lpaResponse.getWorkflowId());
            }

            docWrapper.setOperacionId(lpaResponse.getOperationId());
        }
        return docWrapper;
    }

    /**
     * @param resultado resultado
     * @return WorkflowResponse
     * @throws DocumentException if DocumentException
     */
    public WorkflowResponse getXmlFromResponse(String resultado) throws DocumentException {

        JAXBContext jaxbContext;

        try {
            resultado = resultado.replace("a:", "");

            Document document = XmlUtils.parseXmlText(resultado);

            jaxbContext = JAXBContext.newInstance(WorkflowResponse.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            return jaxbUnmarshaller.unmarshal(document, WorkflowResponse.class).getValue();

        } catch (SAXException | IOException | JAXBException | ParserConfigurationException e) {
            LOGGER.error("Error:", e);
            throw new DocumentException("");
        }
    }

    /**
     * @param workflowResponse WorkflowResponse
     * @param nombreDocumento  nombreDocumento
     * @return DocumentDownloadResponse
     */
    public DocumentDownloadResponse getDocumentFromResponse(WorkflowResponse workflowResponse, String nombreDocumento) {

        final List<DocumentDownloadResponse> documentDownloadResponses = workflowResponse.getDocuments()
                .getDocumentDownloadResponse();
        if (nombreDocumento != null) {
            for (DocumentDownloadResponse documentDownloadResponse : documentDownloadResponses) {
                if (documentDownloadResponse.isSuccess()
                        && (nombreDocumento.contains(documentDownloadResponse.getDocumentData().getName()))) {
                    return documentDownloadResponse;
                }
            }
        }

        return !documentDownloadResponses.isEmpty() ? documentDownloadResponses.get(0) : null;

    }

    /**
     * @param idOperacion   idOperacion
     * @param idAlternativo idAlternativo
     * @param entidad       entidad
     * @param tipoDocumento tipoDocumento
     * @param idUsuario     idUsuario
     * @param idProducto    idProducto
     * @return JSONObject
     */
    public JSONObject generarJsonRequest(String idOperacion, String idAlternativo, String entidad, String tipoDocumento,
            String idUsuario, String idProducto, String idSgcDocCamp) {

        List<LpaPlantilla> listaLpaPlantilla = lpaPlantillaMapper.getPlantilla(entidad, idProducto, tipoDocumento);
        Map<String, Object> entrada = new HashMap<>();

        final Optional<String> idPlantillaClientData = listaLpaPlantilla.stream()
                .filter(x -> x.getIdPlantilla().endsWith("CLIENTDATA_XML")).findFirst()
                .map(x -> x.getIdPlantilla().substring(0, x.getIdPlantilla().length() - 4));

        final Optional<String> idPlantillaClientDataCamp = listaLpaPlantilla.stream()
                .filter(x -> x.getIdPlantilla().startsWith("CLIENTDATA_CAMP")).findFirst()
                .map(LpaPlantilla::getIdPlantilla);
        
        final Optional<String> idPlantillaClientDataOrden = listaLpaPlantilla.stream()
                .filter(x -> x.getIdPlantilla().startsWith(TipoPlantilla.CLIENTDATA_ORDEN.toString())).findFirst()
                .map(LpaPlantilla::getIdPlantilla);

        JSONObject jsonObject;
        if (idPlantillaClientDataCamp.isPresent()) {
            jsonObject = formatClientData(entidad, idOperacion, idPlantillaClientDataCamp, null);
            int index = IntStream.range(0, listaLpaPlantilla.size())
                    .filter(x -> listaLpaPlantilla.get(x).getIdPlantilla().startsWith("CLIENTDATA_CAMP")).findFirst()
                    .orElse(-1);
            listaLpaPlantilla.remove(index);
        }else if(idPlantillaClientDataOrden.isPresent()) {
            jsonObject = formatClientData(entidad, idOperacion, idPlantillaClientDataOrden, idAlternativo);
            int index = IntStream.range(0, listaLpaPlantilla.size()).filter(x -> listaLpaPlantilla.get(x)
                    .getIdPlantilla().startsWith(TipoPlantilla.CLIENTDATA_ORDEN.toString())).findFirst().orElse(-1);
            listaLpaPlantilla.remove(index);
        }else {
            jsonObject = formatClientData(entidad, idOperacion, idPlantillaClientData, null);
        }
        
        
        
        if (!jsonObject.isEmpty()) {
            entrada.put(TipoPlantilla.CLIENTDATA.toString(), jsonObject);
        }

        JSONArray jsonDocument = formatDocumentConfigWrapper(idOperacion, entidad, tipoDocumento, idProducto);
        // Not add array is empty
        if (!jsonDocument.isEmpty()) {
            entrada.put(TipoPlantilla.DOCUMENTCONFIGNAMES.toString(), jsonDocument);
        }

        if (jsonDocument.isEmpty() && idPlantillaClientDataCamp.isPresent()) {
            entrada.put(TipoPlantilla.DOCUMENTCONFIGNAMES.toString(),
                    Arrays.asList(listaLpaPlantilla.get(0).getNombreDocumento()));
        }

        for (LpaPlantilla lpaPlantilla : listaLpaPlantilla) {
            String dato = getXmlFromDataBase(lpaPlantilla, idOperacion, idAlternativo, idUsuario, idSgcDocCamp);
            if ("JSON".equals(lpaPlantilla.getFormato())) {
                JSONObject jsonAuxObject = new Gson().fromJson(dato, JSONObject.class);
                entrada.put(lpaPlantilla.getElemento(), jsonAuxObject.get(lpaPlantilla.getElemento()));
            } else {
                entrada.put(lpaPlantilla.getElemento(), dato);
            }
        }

        return new JSONObject(entrada);

    }

    private JSONArray formatDocumentConfigWrapper(String idOperacion, String entidad, String tipoDocumento,
            String idProducto) {
        JSONArray jsonDocument = formatDocumentConfig(
                getNameDocumentConfigClasificacionMifid(idOperacion, entidad, tipoDocumento, idProducto), entidad);
        if (jsonDocument.isEmpty()) {
            jsonDocument = formatDocumentConfig(tipoDocumento + "_" + idProducto, entidad);
        }
        if (jsonDocument.isEmpty()) {
            jsonDocument = formatDocumentConfig(tipoDocumento, entidad);
        }
        return jsonDocument;
    }

    /**
     * @param listaPlantilla          List<LpaConfigPlantilla>
     * @param tipoOrigen              tipoOrigen
     * @param etiquetaLista           etiquetaLista
     * @param entidad                 entidad
     * @param idOperacion             idOperacion
     * @param idAlternativoOrCampanya idAlternativoOrCampanya
     * @param usuario                 usuario
     * @param idSgcDocCamp            idSgcDocCamp
     * @return insertDataXML
     * @throws MException if MException
     */
    protected String insertDataXML(List<LpaConfigPlantilla> listaPlantilla, String tipoOrigen, String etiquetaLista,
            String entidad, String idOperacion, String idAlternativoOrCampanya, String usuario, String idSgcDocCamp)
            throws MException {

        StringBuilder xml = new StringBuilder();

        if (tipoOrigen.equals(TipoOrigenPlantilla.FIJO.toString())) {

            // Recorremos todos los fijos para la plantilla correspondiente y la incluimos
            // en el xml
            for (LpaConfigPlantilla plantilla : listaPlantilla) {
                xml.append(formatTag(plantilla.getLpaEtiqueta(), plantilla.getValorOrigen()));
            }
            return xml.toString();

        } else if (tipoOrigen.equals(TipoOrigenPlantilla.ETIQUETA.toString())) {

            return getDataXMLEtiqueta(listaPlantilla, entidad, idOperacion, idAlternativoOrCampanya, usuario,
                    idSgcDocCamp, xml);

        } else if (tipoOrigen.equals(TipoOrigenPlantilla.LISTA.toString())) {

            return getDataXMLLista(listaPlantilla, entidad, idOperacion, idAlternativoOrCampanya, usuario, idSgcDocCamp,
                    xml);

        } else if (tipoOrigen.equals(TipoOrigenPlantilla.SERVICIO.toString())) {

            return getDataXMLServicio(listaPlantilla, etiquetaLista, entidad, idOperacion, idAlternativoOrCampanya,
                    usuario, idSgcDocCamp, xml);

        } else if (tipoOrigen.equals(TipoOrigenPlantilla.SERV_CAMP_VARIABLE.toString())) {

            processDataXMLServCampVariable(listaPlantilla, idAlternativoOrCampanya);

        }

        // En caso de que sea una tabla buscamos los valores correspondientes por tabla
        // (n columnas) y para listas se anade la etiquetaLista
        List<String> listaDatos = lpaConfigPlantillaMapper.getValueTableXml(listaPlantilla, etiquetaLista, idOperacion,
                idAlternativoOrCampanya, idSgcDocCamp);
        processListaDatosXml(entidad, idOperacion, xml, listaDatos);
        return xml.toString();

    }

    private void processListaDatosXml(String entidad, String idOperacion, StringBuilder xml, List<String> listaDatos) {
        for (String dato : listaDatos) {
            if (dato.contains(Constantes.TIPO_DOCUMENTOID)) {
                List<Persona> persona = ordenanteMapper.getListaOrdenantesRentaFija(idOperacion, entidad, "es");
                if (!persona.isEmpty() && FormatUtils.isValidIdentidad(persona.get(0).getDocumento(),
                        TipoIdentidad.CIF.getTipoDocumento())) {
                    dato = dato.replace(Constantes.TIPO_DOCUMENTOID, "S");
                } else if (!persona.isEmpty() && FormatUtils.isValidIdentidad(persona.get(0).getDocumento(),
                        TipoIdentidad.TARJETA_RESIDENCIA.getTipoDocumento())) {
                    dato = dato.replace(Constantes.TIPO_DOCUMENTOID, "C");
                } else {
                    dato = dato.replace(Constantes.TIPO_DOCUMENTOID, "N");
                }
            }
            xml.append(dato);
        }
    }

    /**
     * Procesa el servicio para campañas con datos dinamicos de variables
     * 
     * @param listaPlantilla          the listaPlantilla
     * @param idAlternativoOrCampanya
     */
    private void processDataXMLServCampVariable(List<LpaConfigPlantilla> listaPlantilla,
            String idAlternativoOrCampanya) {
        // Lo que empieza por CD_ es que es una variable dinamica que hay que recuperar
        // el dato uno a uno
        for (LpaConfigPlantilla plantilla : listaPlantilla) {
            StringBuilder valorOrigenFinal = new StringBuilder();          
            // separamos la sentencia por espacios en blancos
            String[] partes = plantilla.getValorOrigen().replaceAll("\\s+", " ").split(" ");
            for (int i = 0; i < partes.length; i++) {
                if (partes[i].contains("CD_")) {
                    // buscar el valor y lo metemos para que se pueda ejecutar la formula
                    // posteriormente
                    valorOrigenFinal.append(lpaConfigPlantillaMapper.getValueTableCampanyas(idAlternativoOrCampanya,
                            partes[i].replace("CD_", "")).replace(" ", "&")).append(" ");
                } else {
                    valorOrigenFinal.append(partes[i]).append(" ");
                }
            }
            plantilla.setValorOrigen(valorOrigenFinal.toString());
            // Para hacer join de las tablas de campanas
            plantilla.setTipoOrigen("CAMP_CAMPANYAS");
        }
    }

    private String getDataXMLServicio(List<LpaConfigPlantilla> listaPlantilla, String etiquetaLista, String entidad,
            String idOperacion, String idAlternativoOrCampanya, String usuario, String idSgcDocCamp, StringBuilder xml)
            throws MException {
        for (LpaConfigPlantilla plantilla : listaPlantilla) {

            if (plantilla.getValorOrigen().startsWith("MIFID")) {
                processDataXMLServicioPlantillaMifid(etiquetaLista, entidad, idOperacion, idAlternativoOrCampanya,
                        idSgcDocCamp, xml, plantilla);
            }

            if (plantilla.getValorOrigen().startsWith("MAIL"))
                processDataXmlServicioMail(entidad, usuario, xml, plantilla);

            if (plantilla.getValorOrigen().startsWith("PERSONA_EVENTO"))
                processDataXmlServicioPersonaEvento(entidad, idAlternativoOrCampanya, xml, plantilla);
            if (plantilla.getValorOrigen().contains("PARES_DIVISA_EVENTO")) {
                processDataXmlServicioParesDivisaEvento(entidad, idAlternativoOrCampanya, xml, plantilla);
            }            

        }

        return xml.toString();
    }

    private void processDataXmlServicioMail(String entidad, String usuario, StringBuilder xml,
            LpaConfigPlantilla plantilla) {
        String email = Optional.ofNullable(usuarioService.getUsuario(usuario)).map(UsuarioBase::getEmail).orElse("");
        if (StringUtils.isNotBlank(email)) {
            xml.append(formatTag(plantilla.getLpaEtiqueta(), email));
        } else {
            String emailBuzon = variablesService.getValor(entidad, plantilla.getValorOrigen());
            xml.append(formatTag(plantilla.getLpaEtiqueta(), emailBuzon));
        }
    }

    private void processDataXmlServicioPersonaEvento(String entidad, String idAlternativoOrCampanya, StringBuilder xml,
            LpaConfigPlantilla plantilla) {
        Evento evento = operacionFx.getEventoFx(entidad, idAlternativoOrCampanya, null);
        if (StringUtils.isNotBlank(evento.getTitular())) {
            try {
                Cliente cliente = personaService.getClienteByCodigoJ(evento.getTitular(), entidad, "es");
                xml.append(formatTag(plantilla.getLpaEtiqueta(), cliente.getDomicilio()));
            } catch (MException me) {
                LOGGER.info("No se pudo recuperar domicilio ", me);
                xml.append(formatTag(plantilla.getLpaEtiqueta(), " "));
            }
        } else {
            xml.append(formatTag(plantilla.getLpaEtiqueta(), " "));
        }
    }

    private void processDataXmlServicioParesDivisaEvento(String entidad, String idAlternativoOrCampanya,
            StringBuilder xml, LpaConfigPlantilla plantilla) {
        Evento evento = operacionFx.getEventoFx(entidad, idAlternativoOrCampanya, null);
        String[] divisas = evento.getParDivisas().getCodigoDivisa().split("/");
        if (divisas.length > 0) {
            String parDivisa = paresDivisasService.getParDivisaDominante(entidad, divisas[0], divisas[1]);
            if (StringUtils.isNotBlank(parDivisa)) {
                if (plantilla.getValorOrigen().contains("1")) {
                    xml.append(formatTag(plantilla.getLpaEtiqueta(), parDivisa.substring(0, 3)));
                } else {
                    xml.append(formatTag(plantilla.getLpaEtiqueta(), parDivisa.substring(4)));
                }
            }
        }
    }
  
    private void processDataXMLServicioPlantillaMifid(String etiquetaLista, String entidad, String idOperacion,
            String idAlternativoOrCampanya, String idSgcDocCamp, StringBuilder xml, LpaConfigPlantilla plantilla)
            throws MException {
        Mifid mifid;
        if (plantilla.getValorOrigen().contains("EVENTO")) {
            mifid = mifidService.getMifidEventoClausulas(idAlternativoOrCampanya, entidad);
        } else if (plantilla.getValorOrigen().contains("ACUM")) {
            mifid = acumuladoresMifidService.getMifidAcumuladoresClausulas(Long.valueOf(idOperacion));
        } else if (plantilla.getValorOrigen().contains("CAMP")) {
            mifid = getMifidCampanyasClausulas(idOperacion, idSgcDocCamp, xml, plantilla);
        } else {
            mifid = mifidService.getMifidOperacionClausulas(idOperacion, entidad);
        }
        if (mifid != null && StringUtils.isNotBlank(mifid.getClausula()) && null == idSgcDocCamp) {
            xml.append(formatTagOpen(etiquetaLista));
            List<String> listaClausulas = mifidService.clausulasMifid(mifid);
            for (String clausula : listaClausulas) {
                xml.append(formatTag(plantilla.getLpaEtiqueta(), clausula + "\n"));

            }
            xml.append(formatTagClose(etiquetaLista));
        }
    }

    private Mifid processPlantillaMifidCampanyas(String idOperacion, String idSgcDocCamp, StringBuilder xml,
            LpaConfigPlantilla plantilla) {
        Mifid mifid = null;
        if (null == idSgcDocCamp) {
            mifid = campanyasClientMifid.getMifid(Long.valueOf(idOperacion));
        } else {
            final Map<String, Object> mifidSGC = campanyasClientMifid.getMifidSGC(Long.valueOf(idSgcDocCamp));

            if (mifidSGC.containsKey(CLAUSULAS_MIFID) || mifidSGC.containsKey(OTRAS_CLAUSULAS))
                processClausulasMiFID(xml, plantilla, mifidSGC);
        }
        return mifid;
    }

    private void processClausulasMiFID(StringBuilder xml, LpaConfigPlantilla plantilla, Map<String, Object> mifidSGC) {
        if (mifidSGC.containsKey(CLAUSULAS_MIFID)) {
            String clausulasSgc = (String) mifidSGC.get(CLAUSULAS_MIFID);
            Mifid mifidSgc = new Mifid();
            mifidSgc.setClausula(clausulasSgc);
            mifidSgc.setEntidad("0049");
            try {
                List<String> listaClausulas = mifidService.clausulasMifid(mifidSgc);
                for (String clausula : listaClausulas) {
                    xml.append(formatTag(plantilla.getLpaEtiqueta(), clausula + "\n"));
                }
            } catch (MException e) {
                LOGGER.info("Error en la llamada al motor Mifid ", e);
                xml.append(formatTag(plantilla.getLpaEtiqueta(), " "));
            }
        }

        if (mifidSGC.containsKey(OTRAS_CLAUSULAS)) {
            @SuppressWarnings("unchecked")
            List<String> clausulasSgcTexts = (List<String>) mifidSGC.get(OTRAS_CLAUSULAS);
            for (String texto : clausulasSgcTexts) {
                xml.append(formatTag(plantilla.getLpaEtiqueta(), texto + "\n"));
            }
        }
    }
    
    private Mifid getMifidCampanyasClausulas (String idOperacion, String idSgcDocCamp, StringBuilder xml,
            LpaConfigPlantilla plantilla) {
        // Obtiene el Evento Mifid
        Mifid mifid = processPlantillaMifidCampanyas(idOperacion, idSgcDocCamp, xml, plantilla);
        if(mifid != null && StringUtils.isNotBlank(mifid.getClausula())) {
            // Obtiene los datos de CLASF_MIFID_TIT y ORIGEN_CONTRAPARTIDA_TIT del Evento dado el id_evento y la entidad.
            ClausulasMifidRequerimiento requerimiento = clienteOperacionRepository.findContrapartidaAndClasificacioMifidByIdOperacion(Long.valueOf(idOperacion));
            
            // Obtiene la variable con los requerimientos 
            String variable = variablesService.getValorByNombre(Constantes.VARIABLE_ELIMINAR_ADVERTENCIAS_MIFID);
            
            //Evalua que la variable no es null y su valor no es vacio
            List<EliminarAdvertenciasMifid> ad = eliminarAdvertenciasMifidService.variableEliminarAdvertenciasMIFIDToJson(variable);
                if(!ad.isEmpty()) {
                    for(EliminarAdvertenciasMifid ite: ad) {
                        //Si hay correspondencia entre los valores de OrigenContrapartidaTIT y los valores de ClasfMiFIDTIT
                        if(eliminarAdvertenciasMifidService.evaluarReqMifid(requerimiento, ite)) {
                            //Eliminacion de las clausulas, teniendo en cuenta si se excluyen o no
                            mifid.setClausula(eliminarAdvertenciasMifidService.evalClausulas(mifid.getClausula(), ite.getClausulas(), ite.isExcluir()));
                        }
                    }
                }
        }
        return mifid;
    }

    private String getDataXMLLista(List<LpaConfigPlantilla> listaPlantilla, String entidad, String idOperacion,
            String idAlternativoOrCampanya, String usuario, String idSgcDocCamp, StringBuilder xml) throws MException {
        // en caso de lista le pasamos la etiqueta que pertenece a la lista y buscamos
        // la plantilla hija que va a tener la lista
        for (LpaConfigPlantilla plantilla : listaPlantilla) {
            List<LpaConfigOrigen> nuevaListaTipoOrigen = lpaConfigPlantillaMapper.getTipoOrigenPlantilla(entidad,
                    plantilla.getValorOrigen());
            for (LpaConfigOrigen nuevoTipoOrigen : nuevaListaTipoOrigen) {
                List<LpaConfigPlantilla> listaConfigPorTipo = lpaConfigPlantillaMapper.getConfigPlantilla(entidad,
                        plantilla.getValorOrigen(), nuevoTipoOrigen.getTipoOrigen(), nuevoTipoOrigen.getCondicion());
                xml.append(
                        insertDataXML(listaConfigPorTipo, nuevoTipoOrigen.getTipoOrigen(), plantilla.getLpaEtiqueta(),
                                entidad, idOperacion, idAlternativoOrCampanya, usuario, idSgcDocCamp));
            }
        }
        return xml.toString();
    }

    private String getDataXMLEtiqueta(List<LpaConfigPlantilla> listaPlantilla, String entidad, String idOperacion,
            String idAlternativoOrCampanya, String usuario, String idSgcDocCamp, StringBuilder xml) throws MException {
        // Para este caso tenemos que pintar la etiqueta y volver a buscar las
        // plantillas hijas
        for (LpaConfigPlantilla plantilla : listaPlantilla) {
            xml.append(formatTagOpen(plantilla.getLpaEtiqueta()));
            List<LpaConfigOrigen> nuevaListaTipoOrigen = lpaConfigPlantillaMapper.getTipoOrigenPlantilla(entidad,
                    plantilla.getValorOrigen());
            for (LpaConfigOrigen nuevoTipoOrigen : nuevaListaTipoOrigen) {
                List<LpaConfigPlantilla> listaConfigPorTipo = lpaConfigPlantillaMapper.getConfigPlantilla(entidad,
                        plantilla.getValorOrigen(), nuevoTipoOrigen.getTipoOrigen(), nuevoTipoOrigen.getCondicion());

                xml.append(insertDataXML(listaConfigPorTipo, nuevoTipoOrigen.getTipoOrigen(), null, entidad,
                        idOperacion, idAlternativoOrCampanya, usuario, idSgcDocCamp));

            }
            xml.append(formatTagClose(plantilla.getLpaEtiqueta()));
        }
        return xml.toString();
    }

    /**
     * @param tipoDocumento tipoDocumento
     * @param entidad       entidad
     * @return JSONArray
     */
    @SuppressWarnings("unchecked")
    private JSONArray formatDocumentConfig(String tipoDocumento, String entidad) {
        JSONArray jsonDocument = new JSONArray();
        List<LpaConfigPlantilla> listaConfigPorTipo = lpaConfigPlantillaMapper.getConfigPlantilla(entidad,
                tipoDocumento, null, null);
        for (LpaConfigPlantilla plantilla : listaConfigPorTipo) {
            jsonDocument.add(plantilla.getValorOrigen());
        }
        return jsonDocument;
    }

    @SuppressWarnings("unchecked")
    private JSONObject formatClientData(String entidad, String id, Optional<String> idPlantilla, String idAlternativo) {
        JSONObject jsonClient = new JSONObject();

        final String tipoPlantilla = idPlantilla.orElse(TipoPlantilla.CLIENTDATA.toString());

        List<LpaConfigPlantilla> listaConfigPorTipo = lpaConfigPlantillaMapper.getConfigPlantilla(entidad,
                tipoPlantilla, null, null);
        for (LpaConfigPlantilla plantilla : listaConfigPorTipo) {

            if (plantilla.getTipoOrigen().equals(TipoOrigenPlantilla.FIJO.toString())) {
                jsonClient.put(plantilla.getLpaEtiqueta(), plantilla.getValorOrigen());

            } else {
                // Tabla
                String valor = StringUtils.isNotBlank(idAlternativo)
                        ? lpaConfigPlantillaMapper.getValueTable(plantilla, null, idAlternativo)
                        : lpaConfigPlantillaMapper.getValueTable(plantilla, id, null);
                // Search Table Variable plantilla_id
                String searchKey = TipoPlantilla.CLIENTDATA.toString() + '_' + plantilla.getLpaEtiqueta();
                String limit = variablesService.getValor(entidad, searchKey);
                if (valor == null)
                    valor = "NA";
                if (limit != null) {
                    jsonClient.put(plantilla.getLpaEtiqueta(), StringUtils.abbreviate(valor, Integer.parseInt(limit)));
                } else {
                    jsonClient.put(plantilla.getLpaEtiqueta(), valor);
                }
            }
        }

        return jsonClient;
    }

    /**
     * @param nombreEtiqueta nombreEtiqueta
     * @param valor          valor
     * @return formatTag
     */
    private String formatTag(String nombreEtiqueta, String valor) {
        if (valor == null)
            valor = "";
        return formatTagOpen(nombreEtiqueta) + valor + formatTagClose(nombreEtiqueta);
    }

    /**
     * @param nombreEtiqueta nombreEtiqueta
     * @return formatTagOpen
     */
    private String formatTagOpen(String nombreEtiqueta) {
        if (nombreEtiqueta == null)
            return "";
        else
            return Constantes.XML_OPEN_TAG + nombreEtiqueta + Constantes.XML_CLOSE_TAG;
    }

    /**
     * @param nombreEtiqueta nombreEtiqueta
     * @return formatTagClose
     */
    private String formatTagClose(String nombreEtiqueta) {
        if (nombreEtiqueta == null)
            return "";
        else {
            int espacio = nombreEtiqueta.indexOf(" ");
            if (espacio > 0)
                nombreEtiqueta = nombreEtiqueta.substring(0, espacio);
            return Constantes.XML_OPEN_TAG + Constantes.SLASH + nombreEtiqueta + Constantes.XML_CLOSE_TAG;
        }
    }

    /**
     * @param id            id
     * @param entidad       entidad
     * @param tipoDocumento tipoDocumento
     * @param idProducto    idProducto
     * @return nameDocumentConfigClasificacionMifid
     */
    private String getNameDocumentConfigClasificacionMifid(String id, String entidad, String tipoDocumento,
            String idProducto) {

        String documentConfig = tipoDocumento + "_" + idProducto;
        String clasificacionMIFID;
        if (!(idProducto.contains(TipoProducto.RF.toString())
                || idProducto.contains(TipoProducto.PROPUESTA.toString()))) {
            clasificacionMIFID = estructurasFxService.getClasificacionMIFID(id, entidad);
            if (clasificacionMIFID == null) {
                clasificacionMIFID = operacionFwdService.getClasificacionMIFID(id, entidad);
            }
            if (clasificacionMIFID != null) {
                documentConfig += "_" + clasificacionMIFID;
            }
        }

        return documentConfig;

    }

    public void confirma(String idOperacion, String idAlternativo, String entidad, String idUsuario,
            String tipoProducto) throws DocumentException {
        generarDocumentacion(idOperacion, idAlternativo, entidad, TipoDocumento.CONFIRMACION_PROPUESTA.toString(),
                idUsuario, tipoProducto, null);
    }

    public void rechaza(String idOperacion, String idAlternativo, String entidad, String idUsuario, String tipoProducto)
            throws DocumentException {
        generarDocumentacion(idOperacion, idAlternativo, entidad, TipoDocumento.RECHAZO_PROPUESTA.toString(), idUsuario,
                tipoProducto, null);

    }

    private boolean isCampanyasAndWorkflowIdNotPresentInResponse(LpaResponse lpaResponse, JSONObject jsonRequest,
            Optional<String> idPlantillaClientDataCamp) {
        return null == lpaResponse.getWorkflowId() && null != jsonRequest && null != jsonRequest.get("WorkflowId")
                && idPlantillaClientDataCamp.isPresent();
    }

    /**
     * Para sentinel a parte de generar el xml luego tenemos que modificar algun
     * valor de los nodos con los que nos vienen por parametro
     *
     * @param operacion Operacion
     * @param evento    evento
     * @param idUsuario idUsuario
     * @param plantilla    LpaPlantilla
     * @return xml
     */
    public String getXmlSentinelCampanyas(com.isb.campanyas.backend.model.Operacion operacion, String evento,
            String idUsuario, LpaPlantilla plantilla) {

        try {
            Document documento = XmlUtils.parseXmlText(new InputSource(
                    new StringReader(getXmlFromDataBase(plantilla, operacion.getId().toString(),
                            operacion.getComercializacion().getId().toString(), idUsuario, null))));

            final String rutaEvento = "//SGC_OL_Sentinel//Legs//eventType";
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile(rutaEvento);
            Node node = (Node) expr.evaluate(documento, XPathConstants.NODE);
            if (node != null) {
                node.setTextContent(evento);
            }

            return documentToString(documento);

        } catch (ParserConfigurationException | SAXException | IOException | TransformerException
                | XPathExpressionException e) {
            LOGGER.error("Error al grave al generar el xml de sentinel: ", e);
            return null;
        }

    }

}
