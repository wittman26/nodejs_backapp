package com.isb.acelera.web.actions;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.Source;

import com.isb.acelera.domain.*;
import com.isb.acelera.domain.ClientValidationContext;
import com.isb.acelera.service.*;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.ws.WebServiceException;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.client.SoapFaultClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.isb.acelera.domain.format.FormatUtils;
import com.isb.acelera.persistence.OperacionFxMapper;
import com.isb.acelera.persistence.VariablesMapper;
import com.isb.acelera.util.FairValueUtils;
import com.isb.acelera.util.ListUtils;
import com.isb.fx.model.AtribucionAccionPK;
import com.isb.fx.model.AtribucionDivisaPK;
import com.isb.fx.model.AtribucionPK;
import com.isb.fx.model.AtribucionPerfilProducto;
import com.isb.fx.model.AtribucionPerfilProductoAccion;
import com.isb.fx.model.AtribucionPerfilProductoDivisa;
import com.isb.fx.model.SsccLiqOperRenegociacion;
import com.isb.fx.service.AtribucionesQueryService;
import com.isb.fx.service.AtribucionesValidateService;
import com.isb.fx.service.ClienteDFARepository;
import com.isb.fx.service.SsccLiqOperRenegociacionService;
import com.isb.jms.MurexClient;
import com.isb.jms.SentinelClient;
import com.isb.pagging.Pagina;
import com.isb.wsdl.domain.aceleracontratac.ComBanestoAlSccoreAceleracontratacMFExcGeneralAceleraContratacM;
import com.tibco.sdk.MException;

import lombok.Getter;
import lombok.Setter;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;


/**
 * ActionBean encargado de la gestion de Seguro de Cambio de Fx.
 */
public class SeguroCambioFxActionBean extends AbstractPestanyasVersionInterceptorActionBean
        implements ValidationErrorHandler {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SeguroCambioFxActionBean.class);

    /** The Constant VIEW. */
    private static final String VIEW = "/WEB-INF/jsp/fx/SeguroCambio.jsp";
    public static final String VALIDATION_REQUIRED_VALUE_NOT_PRESENT = "validation.required.valueNotPresent";
    
    /** The variables service. */
    @SpringBean
    private transient VariablesService variablesService;

    /** The divisa fx service. */
    @SpringBean
    private transient DivisaFxService divisaFxService;

    @SpringBean
    private transient AtribucionesValidateService atribucionesService;// AEEV-106

    @SpringBean
    private transient AtribucionesQueryService atribucionesQueryService;// AEEV-106

    /** The persona service. */
    @SpringBean
    private transient PersonaService personaService;

    /** The combo service. */
    @SpringBean
    private transient ComboService comboService;

    /** The fixing service. */
    @SpringBean
    private transient ArchivingGroupFxService fixingService;

    @SpringBean
    private transient MifidService mifidService;

    @SpringBean
    private transient MurexClient murexClient;

    /** The documento service. */
    @SpringBean
    private transient DocumentoService documentoService;

    /** The clientes directos fx service. */
    @SpringBean
    private transient ClienteDirectoFxService clientesDirectosFxService;

    @SpringBean
    private transient SentinelClient sentinelClient;

    @SpringBean
    private transient ClienteDFARepository clienteDFARepository;

    /** The estructuras fx service. */
    @SpringBean
    private transient EstructurasFxService estructurasFxService;

    /** The mail service. */
    @SpringBean
    private transient MailService mailService;

    /** The variables mapper. */
    @SpringBean
    private VariablesMapper variablesMapper;

    @SpringBean
    private transient SegmentosService segmentosService;

    @SpringBean
    private transient ParesDivisasService paresDivisasService;

    @SpringBean
    protected transient OperacionFxVersionService operacionFxVersionService;

    @SpringBean
    protected transient OperacionFxMapper operacionFxMapper;

    @SpringBean
    protected transient SsccLiqOperRenegociacionService ssccLiqOperRenegociacionService;

    @SpringBean
    private transient Validator validator;

    @SpringBean
    private WindfallFxService windfallFxService;

    private List<ComboBean> listaTipoCancelacion;
    
    @SpringBean
    private RlaValidationService rlaValidationService;

    /** The seguro cambio. */
    @ValidateNestedProperties({
            @Validate(field = "id",
                    required = true,
                    on = { "viewOperacion", "generarPropuesta", "calcularActualizarPrecioAsegurado" }),
            @Validate(field = "tipoOperacion.id", required = true, on = { "rfq", "cotizar", "guardarPropuesta" }),
            @Validate(field = "cliente.codigoJ",
                    required = true,
                    on = { "rfq", "cotizar", "isClienteAsesorable", "guardarPropuesta" }),
            @Validate(field = "oficina", required = true, maxlength = 4, on = { "rfq", "cotizar", "guardarPropuesta" }),
            @Validate(field = "gestor", maxlength = 10, on = { "rfq", "cotizar", "guardarPropuesta" }),
            @Validate(field = "ccc", maxlength = 24, on = { "rfq", "cotizar", "guardarPropuesta" }),
            @Validate(field = "propuesta", required = true, on = { "rfq", "cotizar" }),
            @Validate(field = "spot.nominal.amount",
                    required = true,
                    on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "spot.nominal.currency",
                    required = true,
                    on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "opcion1.nominal.amount",
                    required = true,
                    on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "opcion1.nominal.currency",
                    required = true,
                    on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "spot.precioAsegurado.amount",
                    required = true,
                    on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "spot.precioAsegurado.twoCurrencies",
                    required = true,
                    mask = Constantes.EXPRESION_REGULAR_PARDIVISA,
                    on = { "rfq", "cotizar", "guardarPropuesta", "calcularActualizarPrecioAsegurado" }),
            @Validate(field = "opcion1.strike.amount",
                    required = true,
                    on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "opcion1.strike.twoCurrencies",
                    required = true,
                    mask = Constantes.EXPRESION_REGULAR_PARDIVISA,
                    on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "opcion1.vencimiento",
                    required = true,
                    on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "opcion1.entrega",
                    required = true,
                    on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "opcion1.barrera",
                    required = true,
                    on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "destino.id",
    				required = true,
    				on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta" }),
            @Validate(field = "precio.tesoreria.spot", required = true, on = { "calcularActualizarPrecioAsegurado" }),
            @Validate(field = "precio.tesoreria.puntos", required = true, on = { "calcularActualizarPrecioAsegurado" }),
            @Validate(field = "grabarLlamadaMifid", required = true, on = { "rfq", "cotizar", "cotizarPrecio", "guardarPropuesta"  }),
            @Validate(field = "precio.tesoreria.margenPipos",
                    required = true,
                    on = { "calcularActualizarPrecioAsegurado" }) })
    private SeguroCambioFx seguroCambio;

    /** The is called cotizar with success. */
    private boolean isCalledCotizarWithSuccess = false;

    /** The is called RFQ with success. */
    private boolean isCalledGetRFQWithSuccess = false;

    /** The is called RFQ with success. */
    private boolean isCalledRFQWithSuccess = false;

    private String mensajeWarning = "X";

    //
    /**
     * Inits the.
     *
     * @return the resolution
     */
    @DefaultHandler
    public Resolution init() {

        return refresh();
    }

    /**
     * Refresh.
     *
     * @return the resolution
     */
    @HandlesEvent("refresh")
    public Resolution refresh() {

        return view();
    }

    /**
     * View.
     *
     * @return the resolution
     */
    @HandlesEvent("view")
    public Resolution view() {

        getInfoRLA();

        if (seguroCambio != null && seguroCambio.getPropuestaId() != null) {
            if (propuestaAsesoramiento == null) {
                propuestaAsesoramiento = new PropuestaFx();
            }
            propuestaAsesoramiento.setId(seguroCambio.getPropuestaId());
        }
        if (seguroCambio == null) {
            seguroCambio = new SeguroCambioFx();
            seguroCambio.setEntidad(getContext().getUsuario().getUserData().getCorpCodEmpresaLocal());
            seguroCambio.setIdioma(getContext().getLocale().getLanguage());
            seguroCambio.setGestor(getContext().getUsuario().getUsername());           
        } else {
            if (seguroCambio.getEstado() != null && StringUtils.isBlank(seguroCambio.getEstado().getDescripcion())) {
                seguroCambio.getEstado()
                        .setDescripcion(EstadoOperacion.createFrom(seguroCambio.getEstado().getId()).name());
            }
            initRenegociacion();
        }

        if (!Optional.ofNullable(seguroCambio).map(SeguroCambioFx::getId).isPresent()) {
            if (seguroCambio == null) {
                seguroCambio = new SeguroCambioFx();
            }
            seguroCambio.setVersion(operacionFxVersionService.getDefaultVersionOperacion());
        }

        return new ForwardResolution(VIEW);
    }

    private void initRenegociacion() {
        if (TipoProducto.LIQUIDACION.equals(getTipoProducto()) && seguroCambio.getRenegociaciones() == null) {
            if (seguroCambio.isRenegociacion() && seguroCambio.getId() != null) {
                seguroCambio.setRenegociaciones(
                        ssccLiqOperRenegociacionService.findByOperacion(seguroCambio.getId()));
            } else {
                seguroCambio.setRenegociaciones(
                        Arrays.asList(SsccLiqOperRenegociacion.builder().delete(false).build()));
            }
        }
    }

    /**
     * Informacion sobre el asesoramiento en caso de que no lo haya devuelto el
     * motor mifid.
     */
    private AsesoramientoMifid asesoramientoMifid;

    /**
     * Gets the asesoramiento mifid.
     *
     * @return asesoramientoMifid
     */
    public AsesoramientoMifid getAsesoramientoMifid() {

        return asesoramientoMifid;
    }

    /**
     * Sets the asesoramiento mifid.
     *
     * @param asesoramientoMifid the new asesoramiento mifid
     */
    public void setAsesoramientoMifid(AsesoramientoMifid asesoramientoMifid) {

        this.asesoramientoMifid = asesoramientoMifid;
    }

    /**
     * Validate rla titular.
     *
     * @return true, if successful
     */
    private boolean validateRlaTitular() {

        if (getInfoRLA() != null) {

            personaService.setVigenciaLei(seguroCambio.getCliente(),
                    getContext().getUsuario().getUserData().getCorpCodEmpresaLocal());
            CollectionGlobalErrorException ce = personaService.getMensajesRLA(getContext().getEntidad(),
                    seguroCambio.getCliente(), true, true);

            for (GlobalErrorException e : ce.getErrores()) {
                addValidationError("seguroCambio.cliente.codigoJ", e.getIdError(), e.getArgumentos());
            }

            if (!ce.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks for validate mifid.
     *
     * @param tipoIndicador the tipo indicador
     * @return true, if successful
     */
    private boolean hasValidateMifid(String tipoIndicador) {

        try {

            asesoramientoMifid = new AsesoramientoMifid();
            boolean isValido = false;
            if (Constantes.S.equals(variablesMapper.getValor(getContext().getEntidad(), "SALTAR_MIFID_ESTRUCT"))) {
                isValido = true;
            } else {
                isValido = estructurasFxService.validar(seguroCambio, getContext().getUsuario(),
                        getContext().getLocale(), tipoIndicador, asesoramientoMifid, getTipoProducto());
            }

            if (isValido) {
                addMessage("validarOperacion.ok", seguroCambio.getId(), seguroCambio.getMotivoEstado());
            } else {
                addGlobalError("validarOperacion.error", seguroCambio.getMotivoEstado());
            }

            return isValido;

          } catch (MException e) {
            LOGGER.info("Error al intentar validar MIFID un seguro de cambio", e);
            addGlobalError("validarOperacion.errorTibco");
            return false;
          } catch (Exception e) {
            LOGGER.info("Error al intentar validar MIFID un seguro de cambio", e);
            addGlobalError("validarOperacion.errorServicio");
            return false;
          }

        }

    /**
     * View propuesta.
     *
     * @return the resolution
     */
    @HandlesEvent("viewPropuesta")
    public Resolution viewPropuesta() {
        mensajeWarning = "X";
        PropuestaFx propuesta = estructurasFxService.getPropuestaAsesoramiento(getContext().getEntidad(),
                propuestaAsesoramiento.getId());

        seguroCambio = new SeguroCambioFx();
        seguroCambio.setId(propuesta.getIdOperacion());
        
        RedirectResolution urlDestino= viewOperacion( isBotonVolver() ).
        		addParameter( "propuestaAsesoramiento.id", propuesta.getId() );
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put( "urlDestino", urlDestino.getUrl(getContext().getLocale()) );

        return new StreamingResolution( MediaType.APPLICATION_JSON, node.toString() );
    }

    /**
     * Evento de viewOperacion.
     *
     * @return the resolution
     */
    @HandlesEvent("viewOperacion")
    public Resolution viewOperacion() {
        mensajeWarning = "X";
        return viewOperacion(true);
    }

    /**
     * View operacion.
     *
     * @param btVolver the bt volver
     * @return the redirect resolution
     */
    private RedirectResolution viewOperacion(boolean btVolver) {

        operacionFxVersionService.setNewVersionOperacion(seguroCambio, operacionFxMapper);

        TipoProducto tipoProducto = estructurasFxService.getTipoProducto(getContext().getEntidad(),
                seguroCambio.getId(), getContext().getLocale().getLanguage());

        Class<? extends AbstractActionBean> clase = getClass();
        if (tipoProducto.equals(TipoProducto.EXTENSIBLE)) {
            clase = SeguroCambioLiqExtensibleFxActionBean.class;
        } else if (tipoProducto.equals(TipoProducto.DOWNOUT)) {
            clase = SeguroCambioLiqDownFxActionBean.class;
        } else if (tipoProducto.equals(TipoProducto.LIQUIDACIONFIJA)) {
            clase = SeguroCambioLiqFijaFxActionBean.class;
        } else if (tipoProducto.equals(TipoProducto.UPOUT)) {
            clase = SeguroCambioLiqUpFxActionBean.class;
        }

        seguroCambio.setEntidad(getContext().getEntidad());

        return new RedirectResolution(clase).addParameter("seguroCambio", seguroCambio).addParameter("botonVolver",
                btVolver);
    }

    /**
     * Gets the tipo barrera defecto.
     *
     * @return the tipo barrera defecto
     */
    public String getTipoBarreraDefecto() {

        if (TipoProducto.EXTENSIBLE.equals(getTipoProducto())) {
            return TipoBarrera.AMERICANA.toString();
        }

        return "";
    }

    public boolean isOcultarDatosCliente() {
        return false;
    }

    /**
     * Evento volver.
     *
     * @return the resolution
     */
    @HandlesEvent("volver")
    public Resolution volver() {
        mensajeWarning = "X";
        return new RedirectResolution(ConsultaFxActionBean.class).addParameter("refresh");
    }

    /**
     * Evento enviar tesoreria.
     *
     * @return the resolution
     */
    @HandlesEvent("enviarTesoreria")
    public Resolution enviarTesoreria() {
        mensajeWarning = "X";
        if (!validarAtribuciones(Accion.SOLICITAR.toString(), false)) {
            return view();
        }

        try {
            estructurasFxService.enviarTesoreria(seguroCambio, getContext().getEntidad(),
                    getContext().getUsuario().getUsername());
            addMessage("solicitarTesoreria.ok");
        } catch (GlobalErrorException e) {
            LOGGER.info("Error al enviar a tesoreria un seguro de cambio", e);
            addGlobalError(e);
            return getContext().getSourcePageResolution();
        }

        return view();
    }

    /**
     * Evento nueva Renegociacion
     *
     * @return the resolution
     */
    @HandlesEvent("nuevaRenegociacion")
    public Resolution nuevaRenegociacion() {
        if (seguroCambio.getRenegociaciones() == null) {
            seguroCambio.setRenegociaciones(Arrays.asList(SsccLiqOperRenegociacion.builder().delete(false).build()));
        } else {
            seguroCambio.getRenegociaciones().add(SsccLiqOperRenegociacion.builder().delete(false).build());
        }
        return view();
    }

    /**
     * Evento enviar.
     *
     * @return the resolution
     */
    @HandlesEvent("enviar")
    public Resolution enviar() {
        mensajeWarning = "X";

        if (!validarAtribuciones(Accion.CONTRATAR.toString(), false)) {
            return view();
        }

        String uid = getContext().getUsuario().getUsername();
        String entidad = getContext().getEntidad();
        String idioma = getContext().getLocale().getLanguage();
        seguroCambio.setModoSimulacionSSCC(false);
        try {
            estructurasFxService.setPropuestaToGanada(seguroCambio, entidad, uid, getTipoProducto());

            estructurasFxService.altaSeguroCambioReenvio(seguroCambio, entidad, uid, idioma, getTipoProducto());

        } catch (DatatypeConfigurationException e) {

            LOGGER.error("Error en reenvio al intentar dar de alta el seguro de cambio", e);

            estructurasFxService.updateEstadoOperacion(seguroCambio, EstadoOperacion.RECHAZADASSCC, uid);
            mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);
            addGlobalError("globalError.enviar.altaSeguroCambio");

            return getContext().getSourcePageResolution();
        } catch (WebServiceException e) {

            LOGGER.error("Error en reenvio al intentar llamar ws al dar de alta el seguro de cambio", e);
            estructurasFxService.updateEstadoOperacion(seguroCambio, EstadoOperacion.RECHAZADASSCC, uid);
            ComBanestoAlSccoreAceleracontratacMFExcGeneralAceleraContratacM errJaxb = getError(e);

            if (errJaxb != null) {
                String descError = errJaxb.getCodError();
                // Consulta tabla de descripcion de errores ACE_ERRORES_SEGURO_CAMBIO
                String errorSSCC = estructurasFxService.getDescripcion(entidad, descError.trim(), Application.ACELERA,
                        idioma);

                if (errorSSCC != null && !"".equals(errorSSCC)) {
                    descError = errorSSCC;
                } else if (errJaxb.getDescError() != null && !"".equals(errJaxb.getDescError())) {
                    descError = errJaxb.getDescError();
                }
                addGlobalError("globalError.enviar.soapFault", descError);
            } else {
                addGlobalError("globalError.enviar.soapFault", e.getMessage());
            }

            mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);
            return getContext().getSourcePageResolution();
        } catch (GlobalErrorException ge) {

            LOGGER.error(
                    "Error en reenvio al intentar dar de alta el seguro de cambio. Error controlado global error",
                    ge);

            estructurasFxService.updateEstadoOperacion(seguroCambio, EstadoOperacion.RECHAZADASSCC, uid);
            mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);
            addGlobalError(ge);

            return getContext().getSourcePageResolution();
        } catch (DocumentException e) {
            LOGGER.error("Error al intentar pasar a ganada la propuesta de seguro de cambio durante el reenv?o", e);
            addGlobalError("globalError.enviar.propuestaGanada", e.getMessage());
            return getContext().getSourcePageResolution();
        }

        try {

            estructurasFxService.envioMurexReenvio(seguroCambio, entidad, uid, idioma);

        } catch (GlobalErrorException e) {

            LOGGER.error("Error en reenvio al intentar enviar a murex. Error controlado global error", e);

            estructurasFxService.updateEstadoOperacion(seguroCambio, EstadoOperacion.FINALIZADAERRORMUREX, uid);
            mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);
            addGlobalError(e);

            return getContext().getSourcePageResolution();
        } catch (Exception e) {

            LOGGER.error("Error grave en reenvio al intentar enviar a murex", e);

            estructurasFxService.updateEstadoOperacion(seguroCambio, EstadoOperacion.FINALIZADAERRORMUREX, uid);
            mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);
            addGlobalError("globalError.enviar.envioMurex");

            return getContext().getSourcePageResolution();
        }

        if (seguroCambio.getContrato() == null) {
            addMessage("message.enviar.ok", seguroCambio.getId().toString());
        } else {
            addMessage("message.enviar.okContrato", seguroCambio.getId().toString(),
                    FormatUtils.formatContrato(seguroCambio.getContrato()));
        }

        mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);
        return view();
    }

    private ComBanestoAlSccoreAceleracontratacMFExcGeneralAceleraContratacM getError(WebServiceException e) {
        ComBanestoAlSccoreAceleracontratacMFExcGeneralAceleraContratacM errJaxb = null;
        if (e instanceof SoapFaultClientException) {
            SoapFaultDetail soapFaultDetail = ((SoapFaultClientException) e).getSoapFault().getFaultDetail();

            if (soapFaultDetail != null) {
                Iterator<SoapFaultDetailElement> iterator = soapFaultDetail.getDetailEntries();
                while (iterator.hasNext()) {
                    final Source errSource = iterator.next().getSource();
                    JAXBContext jc;

                    try {
                        jc = JAXBContext
                                .newInstance(ComBanestoAlSccoreAceleracontratacMFExcGeneralAceleraContratacM.class);
                        Unmarshaller unmarshaller = jc.createUnmarshaller();
                        errJaxb = (ComBanestoAlSccoreAceleracontratacMFExcGeneralAceleraContratacM) unmarshaller
                                .unmarshal(errSource);
                        break;
                    } catch (JAXBException exceptionUnMarshall) {
                        LOGGER.error(exceptionUnMarshall.getMessage(), exceptionUnMarshall);
                    }
                }
            }
        }
        return errJaxb;
    }

    /**
     * Evento Rfq.
     *
     * @return the resolution
     */
    @HandlesEvent("rfq")
    public Resolution rfq() {
        mensajeWarning = "X";

        if(Boolean.FALSE.equals(validarEstadoOperacion(Accion.COTIZAR))) {
            return view();
        }
        
        if (!validarAtribuciones(Accion.COTIZAR.toString(), false)) {

            return view();
        }

        try {
            estructurasFxService.addSeguroCambio(seguroCambio, getTipoProducto(), getContext().getEntidad(),
                    getContext().getUsuario().getUsername(), getContext().getLocale().getLanguage(), true);

        } catch (DuplicateKeyException e) {

            LOGGER.error("Error en rfq al intentar guardar duplicado en bbdd", e);

            seguroCambio.setPrecio(null);

            addGlobalError(Constantes.GLOBAL_ERROR_SEGURO_CAMBIO_DUPLICADO);

            return getContext().getSourcePageResolution();
        }

        if (!hasValidateMifid(TipoIndicadorMifid.SIMULACION.toString())) {
            return view();
        }

        try {
            PreciosSeguroCambioFx preciosAutomaticos = estructurasFxService.cotizar(seguroCambio, getTipoProducto(),
                    getContext().getUsuario().getUsername(), isPerfilGestorBasico(), false, true);

            estructurasFxService.rfq(getContext().getEntidad(), getContext().getLocale(), seguroCambio,
                    preciosAutomaticos.getIdDeal(), getPerfilSegmento(seguroCambio.getCliente().getSegmento().getId()));

            isCalledRFQWithSuccess = true;
        } catch (GlobalErrorException e) {

            LOGGER.error("Error en rfq de seguro de cambio. Error global controlado", e);

            seguroCambio.setPrecio(null);

            addGlobalError(e);

            return getContext().getSourcePageResolution();
        } catch (WebServiceException e) {

            LOGGER.error("Error en rfq al llamar al ws de cotizacion", e);

            seguroCambio.setPrecio(null);

            addGlobalError(Constantes.GLOBAL_ERROR_ABACUS_SEND_ACELERA_SOAP_FAULT);

            return getContext().getSourcePageResolution();
        }

        return view();
    }

    @HandlesEvent("hayRfq")
    public Resolution hayRfq() {
        try {
            boolean hayRfq = estructurasFxService.hayRfq(getContext().getEntidad(), seguroCambio.getId(),
                    seguroCambio.getPrecio().getTesoreria().getIdDeal());
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("hayRfq", hayRfq);
            return new StreamingResolution(Constantes.APPLICATION_JSON, node.toString());
        } catch (GlobalErrorException e) {
            LOGGER.warn("Error en hayRfq. Error global controlado", e);
            return getJsonResolutionGlobalError(e);
        }
    }

    @HandlesEvent("getRfq")
    public Resolution getRfq() {

        try {
            seguroCambio.setProducto(new ComboBean(getTipoProducto().toString(), "", ""));
            seguroCambio.setEntidad(getContext().getEntidad());
            PreciosSeguroCambioFx precio = estructurasFxService.getRfq(seguroCambio,
                    getContext().getUsuario().getUsername(), isPerfilGestorBasico());

            seguroCambio.setPrecio(precio);

            seguroCambio.getPrecio().getTesoreria().setDestino(getListaDestinoOperacion().get(0));
            estructurasFxService.addPrecio(seguroCambio, getContext().getEntidad());

            if (seguroCambio.getPrecio() == null) {
                addGlobalError("globalError.rfq");

                return getContext().getSourcePageResolution();
            }

            startCountDown = isBotonContratar(false);
            
            calcularCountDown();

            isCalledGetRFQWithSuccess = true;

            if (!hasValidateMifid(TipoIndicadorMifid.SIMULACION.toString())) {
                return view();
            }

            return view();

        } catch (Exception e) {

            LOGGER.error("Error en rfq en la respuesta de la cotizacion", e);

            seguroCambio.setPrecio(null);

            addGlobalError("globalError.graveRfq");

            return getContext().getSourcePageResolution();
        }

    }

    /**
     * Evento de validarSSCC.
     *
     * @return the resolution
     */
    @HandlesEvent("validarSSCC")
    public Resolution validarSSCC() {
        mensajeWarning = "X";
        startCountDown = true;
        if (simulacionSSCC()) {
            addMessage("altaSSCCSimulacion.ok");
            return getContext().getSourcePageResolution();
        }
        return view();
    }

    /**
     * Evento de validarSSCC.
     *
     * @return the resolution
     */
    private boolean simulacionSSCC() {
        String uid = getContext().getUsuario().getUsername();
        String entidad = getContext().getEntidad();
        String idioma = getContext().getLocale().getLanguage();
        seguroCambio.setModoSimulacionSSCC(true);
        try {
            estructurasFxService.altaSeguroCambio(seguroCambio, entidad, uid, idioma, getTipoProducto());
        } catch (DatatypeConfigurationException e) {
            LOGGER.error("Error al simular un seguro de cambio al llamar al ws de alta del contrato", e);
            addGlobalError("globalError.contratar.altaSeguroCambio");

            return false;
        } catch (WebServiceException e) {

            LOGGER.error("Error soap al simular un seguro de cambio al llamar al ws de alta del contrato", e);

            ComBanestoAlSccoreAceleracontratacMFExcGeneralAceleraContratacM errJaxb = getError(e);

            if (errJaxb != null) {
                String descError = errJaxb.getCodError();
                // Consulta tabla de descripcion de errores ACE_ERRORES_SEGURO_CAMBIO
                String errorSSCC = estructurasFxService.getDescripcion(entidad, descError.trim(), Application.ACELERA,
                        idioma);
                if (errorSSCC != null && !"".equals(errorSSCC)) {
                    descError = errorSSCC;
                } else if (errJaxb.getDescError() != null && !"".equals(errJaxb.getDescError())) {
                    descError = errJaxb.getDescError();
                }
                if (isNotGestor()) {
                    mensajeWarning = descError;
                    return false;
                }
                addGlobalError("globalError.enviar.soapFault", descError);
            } else {
                addGlobalError("globalError.enviar.soapFault", e.getMessage());
            }
            return false;
        }
        return true;
    }

    /** The loaded info RLA. */
    private boolean loadedInfoRLA = false;

    /** The info RLA. */
    private InfoRLA infoRLA;

    /**
     * Gets the info RLA.
     *
     * @return the info RLA
     */
    private InfoRLA getInfoRLA() {

        if ((!loadedInfoRLA || infoRLA == null) && (seguroCambio != null && seguroCambio.getCliente() != null
                && StringUtils.isNotEmpty(seguroCambio.getCliente().getCodigoJ()))) {
            try {
                var rlaData = ClientValidationContext.builder().cliente(seguroCambio.getCliente())
                  .tipoProducto(getTipoProducto().toString()).entidad(getContext().getEntidad())
                  .idioma(getContext().getLocale().getLanguage()).validarDfa(true).build();
                rlaValidationService.validateFiocAndKyc(rlaData);
                
                infoRLA = seguroCambio.getCliente().getInfoRLA();
                
                loadedInfoRLA = true;
            } catch (CollectionGlobalErrorException e) {
                LOGGER.warn("CGEE: {}", e.getMessage());
                e.getErrores().forEach(error -> {
                    if (error.getIdError().startsWith("aviso.")) {
                        mensajeWarning = getMessage(error.getIdError(), error.getArgumentos());
                    } else {
                        addGlobalError(error.getIdError(), error.getArgumentos());
                    }
                });
            } finally {
            	Optional.ofNullable(seguroCambio.getCliente().getInfoRLA()).ifPresent(x -> reloadFairValueScope(x.getMifidCat(), x.getOrigContrapartida()));
            }

        }

        return infoRLA;
    }
    
    private void reloadFairValueScope(String mifidCat, String origContrapartida) {
    	boolean isFairValueEnabled = StringUtils.equalsIgnoreCase("S", variablesService.getValor(getContext().getEntidad(), "FAIR_VALUE_ENABLED_FX_STRUCTURES"));
    	boolean fairValueScope = FairValueUtils.shouldAddFairValue(mifidCat, origContrapartida, isFairValueEnabled);
    	seguroCambio.setFairValueScope(fairValueScope);
    }

    /**
     * Evento de Generar documento contractual.
     *
     * @return the resolution
     */
    @HandlesEvent("generarDocumentoContractual")
    public Resolution generarDocumentoContractual() {
        mensajeWarning = "X";

        try {
            estructurasFxService.addSeguroCambio(seguroCambio, getTipoProducto(), getContext().getEntidad(),
                    getContext().getUsuario().getUsername(), getContext().getLocale().getLanguage(), false);
            estructurasFxService.generarDocumentacion(seguroCambio, getTipoProducto(), getContext().getEntidad(),
                    getContext().getUsuario().getUsername(), getContext().getLocale().getLanguage(),
                    TipoDocumento.CONTRATO);

        } catch (Exception e) {
            LOGGER.error("Se ha producido un error al generar contrato de un seguro de cambio", e);

            addGlobalError("contractualOperacion.error", e.getMessage());

            return getContext().getSourcePageResolution();
        }

        addMessage("contractualOperacion.ok", seguroCambio.getId());

        return view();
    }

    /**
     * Evento de Generar documento pre contractual.
     *
     * @return the resolution
     */
    @HandlesEvent("generarDocumentoPreContractual")
    public Resolution generarDocumentoPreContractual() {
        mensajeWarning = "X";

        try {

            estructurasFxService.addPrecio(seguroCambio, getContext().getEntidad());
            estructurasFxService.generarDocumentacion(seguroCambio, getTipoProducto(), getContext().getEntidad(),
                    getContext().getUsuario().getUsername(), getContext().getLocale().getLanguage(), TipoDocumento.KID);
        } catch (Exception e) {
            LOGGER.error("Se ha producido un error al generar precontrato de un seguro de cambio", e);

            addGlobalError("PrecontractualOperacion.error", e.getMessage());

            return getContext().getSourcePageResolution();
        }

        addMessage("PrecontractualOperacion.ok", seguroCambio.getId());

        return view();
    }

    /**
     * Precarga valores dados de alta en Cliente directo.
     *
     * @return the resolution
     * @throws JsonProcessingException the json processing exception
     */
    @HandlesEvent("preCargaClienteDirecto")
    public Resolution preCargaClienteDirecto() throws JsonProcessingException {
        mensajeWarning = "X";

        Optional<ClienteDirectoGlcsFx> optionalClienteDirecto = clientesDirectosFxService.getClienteDirectoGlcsFx(
                getContext().getEntidad(), getContext().getLocale().getLanguage(), seguroCambio.getCliente());

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("gestor", optionalClienteDirecto.map(ClienteDirectoGlcsFx::getGestor).orElse(""));
        mapa.put("oficina", optionalClienteDirecto.map(ClienteDirectoGlcsFx::getOficina).orElse(""));
        mapa.put("propuesta", optionalClienteDirecto.map(ClienteDirectoGlcsFx::getPropuestaRiesgo).orElse(""));
        mapa.put("ccc", optionalClienteDirecto.map(ClienteDirectoGlcsFx::getCcc).orElse(""));

        String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapa);
        return new StreamingResolution(Constantes.APPLICATION_JSON, jsonResult);
    }

    /**
     * Validate contratar.
     */
    @ValidationMethod(on = { "contratar" })
    public void validateContratar() {

        validarPerfilGestor();

        validateDatosSeguroCambio();// AEEV-106

        validateRlaTitular();
    }

    /**
     * Validate datos seguro cambio. AEEV-106
     */

    public void validateDatosSeguroCambio() {

        validarPerfilGestor();

        getInfoRLA();

        validateDatosSeguroCambioComunes();

        validateDatosSeguroCambioOpcional();

        validarClienteDirecto();// AEEV-106

        validarRenegociacion();
    }

    private void validarRenegociacion() {

        if (seguroCambio != null && seguroCambio.isRenegociacion()) {

            if (seguroCambio.getRenegociaciones() == null || seguroCambio.getRenegociaciones().isEmpty()
                    || seguroCambio.getRenegociaciones().stream().allMatch(SsccLiqOperRenegociacion::isDelete)) {
                addValidationError("validation.renegociacion.list", "validation.renegociacion");
            } else {
                seguroCambio.getRenegociaciones().forEach(renegociacion -> {
                    if (!renegociacion.isDelete()) {
                        final Set<ConstraintViolation<SsccLiqOperRenegociacion>> errores =
                                validator.validate(renegociacion);
                        errores.forEach(error -> addValidationError(error.getPropertyPath() + ": " +
                                error.getMessage(), "validation.renegociacion"));
                    }
                });
            }
        }
    }

    @ValidationMethod(on = { "cotizar", "rfq", "guardarPropuesta" })
    public void validarDatosSeguroCambio() {
        validateDatosSeguroCambio();
    }

    @ValidationMethod(on = { "cotizarPrecio" })
    public void validateDatosSeguroCambioNew() {

        validarPerfilGestor();
        getInfoRLA();
        validateDatosSeguroCambioComunes();
        validateDatosSeguroCambioOpcional();
    }

    /**
     * Validate atribuciones.
     */
    private void validarClienteDirecto() {

        AtribucionPK atribucionPK = new AtribucionPK(getContext().getEntidad(), getContext().getRolesUsuario().get(0),
                getTipoProducto().toString());
        AtribucionPerfilProducto atribucion = atribucionesService.getAtribucionPerfilProducto(atribucionPK);

        if (atribucion != null && clientesDirectosFxService.getCliente(seguroCambio.getCliente().getCodigoJ(),
                getContext().getEntidad()) != null && !atribucion.getClientesDirectos()) {
            addValidationError("seguroCambio.cliente.codigoJ", "validation.clienteDirecto");
        }
    }

    /*
     * private void validateCentro() {
     * 
     * String propuesta=seguroCambio.getPropuesta(); String
     * centro=seguroCambio.getOficina();
     * 
     * if( propuesta != null && centro!=null) { if(
     * !propuesta.substring(4,8).equals(centro)) { addValidationError(
     * "seguroCambio.oficina", "validation.centro" ); }
     * 
     * } }
     */

    /**
     * Validate datos seguro cambio comunes.
     */
    private void validateDatosSeguroCambioComunes() {

        Date hoy = new Date();
        String fechaMin = null;
        if(Constantes.N.equals(seguroCambio.getGrabarLlamadaMifid())&& seguroCambio.getActaMifid()==null){
            addValidationError("seguroCambio.actaMifid", VALIDATION_REQUIRED_VALUE_NOT_PRESENT);
        }
        try {
            if (!DateUtils.isSameDay(hoy, seguroCambio.getSpot().getEntrega())
                    && seguroCambio.getSpot().getEntrega().before(hoy)) {
                if (StringUtils.isEmpty(fechaMin)) {
                    fechaMin = FormatUtils.getDateFormat(FormatUtils.DATE, FormatUtils.MEDIUM, getContext().getLocale()).format(hoy);
                }
                
                addValidationError("seguroCambio.spot.entrega", Constantes.VALIDATION_MINVALUE_VALUE_BELOW_MINIMUM, fechaMin);
            }
        }catch (IllegalArgumentException e) {
            addValidationError("seguroCambio.spot.entrega", Constantes.VALIDATION_REQUIRED_VALUE_NOT_PRESENT, fechaMin);
        }

        if (!DateUtils.isSameDay(hoy, seguroCambio.getOpcion1().getVencimiento())
                && seguroCambio.getOpcion1().getVencimiento().before(hoy)) {
            if (StringUtils.isEmpty(fechaMin)) {
                fechaMin = FormatUtils.getDateFormat(FormatUtils.DATE, FormatUtils.MEDIUM, getContext().getLocale()).format(hoy);
            }

            addValidationError("seguroCambio.opcion1.vencimiento", Constantes.VALIDATION_MINVALUE_VALUE_BELOW_MINIMUM, fechaMin);
        }

        if (seguroCambio.getSpot().getPrecioAsegurado() == null
                || seguroCambio.getSpot().getPrecioAsegurado().getTwoCurrencies() == null
                || seguroCambio.getOpcion1().getStrike() == null
                || seguroCambio.getOpcion1().getStrike().getTwoCurrencies() == null
                || !seguroCambio.getSpot().getPrecioAsegurado().getTwoCurrencies()
                        .equals(seguroCambio.getOpcion1().getStrike().getTwoCurrencies())) {
            addValidationError("seguroCambio.spot.precioAsegurado.twoCurrencies",
                    Constantes.VALIDATION_EXPRESSION_VALUE_FAILED_EXPRESSION);
            addValidationError("seguroCambio.opcion1.strike.twoCurrencies",
                    Constantes.VALIDATION_EXPRESSION_VALUE_FAILED_EXPRESSION);
        }

        if (isNominalesHanDeSerIguales() && 
            !seguroCambio.getSpot().getNominal().getAmount()
            .equals(seguroCambio.getOpcion1().getNominal().getAmount())) {
                addValidationError("seguroCambio.spot.nominal.amount", Constantes.VALIDATION_EXPRESSION_VALUE_FAILED_EXPRESSION);
                addValidationError("seguroCambio.opcion1.nominal.amount",
                        Constantes.VALIDATION_EXPRESSION_VALUE_FAILED_EXPRESSION);
        }
    }

    /**
     * Validate datos seguro cambio opcional.
     */
    protected void validateDatosSeguroCambioOpcional() {
        if (seguroCambio.getTipoBarrera() == null || StringUtils.isEmpty(seguroCambio.getTipoBarrera().getId())) {
            addValidationError("seguroCambio.tipoBarrera.id", Constantes.VALIDATION_REQUIRED_VALUE_NOT_PRESENT);
        }

        if (getSeguroCambio().getOpcion1().getFixingReference() == null
                || StringUtils.isEmpty(getSeguroCambio().getOpcion1().getFixingReference().getId())) {
            addValidationError("seguroCambio.opcion1.fixingReference.id", Constantes.VALIDATION_REQUIRED_VALUE_NOT_PRESENT);
        }
    }

    /**
     * Evento de Cotizar desde CotizadorEstructuraFxActionBean.
     *
     * @return the resolution
     */
    @HandlesEvent("cotizarPrecio")
    public Resolution cotizarPrecio() {
        try {
            seguroCambio.setProducto(new ComboBean(getTipoProducto().toString(), "", ""));
            seguroCambio.setPrecio(estructurasFxService.cotizar(seguroCambio, getTipoProducto(),
                    getContext().getUsuario().getUsername(), isPerfilGestorBasico(), true, false));

            seguroCambio.getPrecio().getTesoreria().setDestino(getListaDestinoOperacion().get(0));

            startCountDown = isBotonContratar(false);
            calcularCountDown();
        } catch (GlobalErrorException e) {

            LOGGER.error("Error global controlado al cotizar un seguro de cambio al llamar al ws de abacus", e);

            seguroCambio.setPrecio(null);

            addGlobalError(e);

            return getContext().getSourcePageResolution();
        } catch (WebServiceException e) {

            LOGGER.error("Error al cotizar un seguro de cambio al llamar al ws de cotizacion", e);

            seguroCambio.setPrecio(null);

            addGlobalError(Constantes.GLOBAL_ERROR_ABACUS_SEND_ACELERA_SOAP_FAULT);

            return getContext().getSourcePageResolution();
        }
        return view();
    }

    /**
     * Evento de Cotizar.
     *
     * @return the resolution
     */
    @HandlesEvent("cotizar")
    public Resolution cotizar() {
        mensajeWarning = "X";

        if(Boolean.FALSE.equals(validarEstadoOperacion(Accion.COTIZAR))) {
            return view();
        }
        
        if (!validarAtribuciones(Accion.COTIZAR.toString(), false)) {
            return view();
        }

        try {
            estructurasFxService.addSeguroCambio(seguroCambio, getTipoProducto(), getContext().getEntidad(),
                    getContext().getUsuario().getUsername(), getContext().getLocale().getLanguage(), true);

        } catch (DuplicateKeyException e) {

            LOGGER.error("Error al cotizar un seguro de cambio cuando guardamos los datos del seguro de cambio", e);

            seguroCambio.setPrecio(null);

            addGlobalError(Constantes.GLOBAL_ERROR_SEGURO_CAMBIO_DUPLICADO);

            return getContext().getSourcePageResolution();

        } catch (GlobalErrorException ge) {

            LOGGER.error(
                    "Error global controlado al cotizar un seguro de cambio cuando guardamos los datos del seguro de cambio",
                    ge);

            seguroCambio.setPrecio(null);

            addGlobalError(ge);

            return getContext().getSourcePageResolution();
        }

        if (!hasValidateMifid(TipoIndicadorMifid.SIMULACION.toString())) {
            return view();
        }

        try {
            seguroCambio.setPrecio(estructurasFxService.cotizar(seguroCambio, getTipoProducto(),
                    getContext().getUsuario().getUsername(), isPerfilGestorBasico(), false, false));

            seguroCambio.getPrecio().getTesoreria().setDestino(getListaDestinoOperacion().get(0));// AEEV-106
            estructurasFxService.addPrecio(seguroCambio, getContext().getEntidad());

            if (!isNotGestor() && !simulacionSSCC()) {
                seguroCambio.setPrecio(null);
                return getContext().getSourcePageResolution();
            }

            startCountDown = isBotonContratar(false);
            calcularCountDown();

        } catch (GlobalErrorException e) {

            LOGGER.error("Error global controlado al cotizar un seguro de cambio al llamar al ws de abacus", e);

            seguroCambio.setPrecio(null);

            addGlobalError(e);

            return getContext().getSourcePageResolution();
        } catch (WebServiceException e) {

            LOGGER.error("Error al cotizar un seguro de cambio al llamar al ws de cotizacion", e);

            seguroCambio.setPrecio(null);

            addGlobalError(Constantes.GLOBAL_ERROR_ABACUS_SEND_ACELERA_SOAP_FAULT);

            return getContext().getSourcePageResolution();
        }

        isCalledCotizarWithSuccess = true;

        return view();
    }

    private Boolean validarEstadoOperacion(final Accion accion) {
        Boolean valid = true;
        
        if(Accion.COTIZAR.equals(accion) && null == seguroCambio.getId()) {
            return true;
        }
        seguroCambio.setEstado(estructurasFxService.getEstado(seguroCambio.getId(), getContext().getEntidad(),
                getContext().getLocale().getLanguage()));

        final Optional<String> estadoOp = Optional.ofNullable(seguroCambio.getEstado()).map(ComboBean::getId);

        switch (accion) {
        case COTIZAR:
            // Si la operación tiene estado y es diferente a cotizada, no se puede cotizar
            if (estadoOp.isPresent() && (!estadoOp.get().equals(EstadoOperacion.COTIZADAAUTOMATICA.toString())
                    && !estadoOp.get().equals(EstadoOperacion.COTIZADAMANUAL.toString())
                    && !estadoOp.get().equals(EstadoOperacion.XX.toString()))) {
                valid = false;
                addGlobalError("globalError.seguroCambioNoCotizado");
            }
            break;
        case CONTRATAR:
            // Si la operación no tiene estado o no está cotizada, no se puede contratar
            if (!estadoOp.isPresent() || (!estadoOp.get().equals(EstadoOperacion.COTIZADAAUTOMATICA.toString())
                    && !estadoOp.get().equals(EstadoOperacion.COTIZADAMANUAL.toString()))) {
                valid = false;
                addGlobalError("globalError.seguroCambioNoCotizado");
            }
            break;
        default:
            break;
        }

        return valid;

    }

    /**
     * Evento de Contratar.
     *
     * @return the resolution
     */
    @HandlesEvent("contratar")
    public Resolution contratar() {
        mensajeWarning = "X";
        String docuboxText = null;

        if(Boolean.FALSE.equals(validarEstadoOperacion(Accion.CONTRATAR))) {
            return view();
        }
        
        if (!isBotonContratar(false)) {
            return view();
        }

        String uid = getContext().getUsuario().getUsername();
        String entidad = getContext().getEntidad();
        String idioma = getContext().getLocale().getLanguage();
        seguroCambio.setModoSimulacionSSCC(false);
        seguroCambio.setEntidad(entidad);
        seguroCambio.setProducto(new ComboBean(getTipoProducto().toString(), "", idioma));

        if (!hasValidateMifid(TipoIndicadorMifid.PRECONTRACTUAL.toString())) {
            return view();
        }

        try {
            estructurasFxService.addPrecio(seguroCambio, entidad);

            estructurasFxService.setPropuestaToGanada(seguroCambio, entidad, uid, getTipoProducto());

            try {
                mailService.sendMailCerrarOperacion(seguroCambio.getCliente().getCodigoJ(), getContext().getLocale(),
                        seguroCambio.getId().toString(), seguroCambio.getOficina(), entidad,
                        getPerfilSegmento(seguroCambio.getCliente().getSegmento().getId()));
            } catch (Exception e) {
                LOGGER.error("Error al enviar mail: " + e.getMessage());
            }
            // antiguamente llamada a Partenon aqui
        } catch (DocumentException e) {
            LOGGER.error("Error al intentar pasar a ganada la propuesta de seguro de cambio", e);
            addGlobalError("globalError.contratar.propuestaGanada", e.getMessage());
            return getContext().getSourcePageResolution();
        }
        
        try {          
            if (isCotizacionWindfall()) {
                seguroCambio.setTipoProducto(getTipoProducto());
                windfallFxService.contratarWindfall(seguroCambio, true);
                windfallFxService.comprobarEstadoContratacion(seguroCambio.getId().toString());
            }            
        }catch (GlobalErrorException e) {
           LOGGER.error("Error al intentar confirmar con windfall para contratar", e);
           addGlobalError(e);
           return getContext().getSourcePageResolution();
       }
                
        try {
            
            estructurasFxService.altaSeguroCambio(seguroCambio, entidad, uid, idioma, getTipoProducto());

        } catch (DatatypeConfigurationException e) {

            LOGGER.error("Error al contratar un seguro de cambio al llamar al ws de alta del contrato", e);

            estructurasFxService.updateEstadoOperacion(seguroCambio, EstadoOperacion.RECHAZADASSCC, uid);

            addGlobalError("globalError.contratar.altaSeguroCambio");

            mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);
            
            return getContext().getSourcePageResolution();
        } catch (WebServiceException e) {

            LOGGER.error("Error soap al contratar un seguro de cambio al llamar al ws de alta del contrato", e);

            estructurasFxService.updateEstadoOperacion(seguroCambio, EstadoOperacion.RECHAZADASSCC, uid);
            mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);
            ComBanestoAlSccoreAceleracontratacMFExcGeneralAceleraContratacM errJaxb = getError(e);

            if (errJaxb != null) {
                String descError = errJaxb.getCodError();
                // Consulta tabla de descripcion de errores ACE_ERRORES_SEGURO_CAMBIO
                String errorSSCC = estructurasFxService.getDescripcion(entidad, descError.trim(), Application.ACELERA,
                        idioma);
                if (errorSSCC != null && !"".equals(errorSSCC)) {
                    descError = errorSSCC;
                } else if (errJaxb.getDescError() != null && !"".equals(errJaxb.getDescError())) {
                    descError = errJaxb.getDescError();
                }
                addGlobalError("globalError.enviar.soapFault", descError);
            } else {
                addGlobalError("globalError.enviar.soapFault", e.getMessage());
            }

            return getContext().getSourcePageResolution();
        } catch (GlobalErrorException ge) {
            LOGGER.error(
                    "Error global controlado al contratar un seguro de cambio al llamar al ws de alta del contrato",
                    ge);

            estructurasFxService.updateEstadoOperacion(seguroCambio, EstadoOperacion.RECHAZADASSCC, uid);

            mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);

            addGlobalError(ge);

            return getContext().getSourcePageResolution();
        }
        try {

            estructurasFxService.envioMurex(seguroCambio, uid);

        } catch (GlobalErrorException e) {

            LOGGER.error("Error al intentar enviar a murex un seguro de cambio al intentar contratar", e);

            estructurasFxService.updateEstadoOperacion(seguroCambio, EstadoOperacion.FINALIZADAERRORMUREX, uid);

            mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);
            
            addGlobalError(e);

            return getContext().getSourcePageResolution();
        } catch (Exception e) {

            LOGGER.error("Error grave al intentar enviar a murex un seguro de cambio al intentar contratar", e);

            mensajeWarning = estructurasFxService.getDocuboxText(seguroCambio);
            
            estructurasFxService.updateEstadoOperacion(seguroCambio, EstadoOperacion.FINALIZADAERRORMUREX, uid);

            addGlobalError("globalError.contratar.envioMurex");

            return getContext().getSourcePageResolution();
        }

        addMessage("message.contratar.ok", seguroCambio.getId().toString(),
                FormatUtils.formatContrato(seguroCambio.getContrato()));

        docuboxText = estructurasFxService.getDocuboxText(seguroCambio);
        if (StringUtils.isNotBlank(docuboxText)) {
            mensajeWarning = docuboxText;
        }
        return view();
    }

    /**
     * Evento de Anular.
     *
     * @return the resolution
     */
    @HandlesEvent("anular")
    public Resolution anular() {
        mensajeWarning = "X";
        return new RedirectResolution(getClass());
    }

    /** The propuesta asesoramiento. */
    @ValidateNestedProperties({ @Validate(field = "id", required = true, on = { "generarPropuesta", "viewPropuesta" }),
            @Validate(field = "clienteGCB", required = true, on = { "guardarPropuesta" }),
            @Validate(field = "fechaVigencia", required = true, on = { "guardarPropuesta" }), })
    private PropuestaFx propuestaAsesoramiento;

    /**
     * Gets the propuesta asesoramiento.
     *
     * @return the propuesta asesoramiento
     */
    public PropuestaFx getPropuestaAsesoramiento() {

        return propuestaAsesoramiento;
    }

    /**
     * Sets the propuesta asesoramiento.
     *
     * @param propuestaAsesoramiento the new propuesta asesoramiento
     */
    public void setPropuestaAsesoramiento(PropuestaFx propuestaAsesoramiento) {

        this.propuestaAsesoramiento = propuestaAsesoramiento;
    }

    /**
     * Generar propuesta.
     *
     * @return the resolution
     * @throws Exception the exception
     */
    @HandlesEvent("generarPropuesta")
    public Resolution generarPropuesta() throws Exception {
        mensajeWarning = "X";

        if (!validarAtribuciones(Accion.SOLICITAR.toString(), false)) {
            return view();
        }
        try {
            seguroCambio.setPropuestaId(propuestaAsesoramiento.getId());

            estructurasFxService.generarDocumentacion(seguroCambio, getTipoProducto(), getContext().getEntidad(),
                    getContext().getUsuario().getUsername(), getContext().getLocale().getLanguage(),
                    TipoDocumento.PROPUESTA_COBERTURA);
        } catch (Exception e) {
            LOGGER.error("Se ha producido un error al generar propuesta de un seguro de cambio", e);

            addGlobalError("globalError.generarPropuesta.error", e.getMessage());

            return getContext().getSourcePageResolution();
        }

        addMessage("message.generarPropuesta.ok", propuestaAsesoramiento.getId(), seguroCambio.getId());

        return view();
    }

    /**
     * Guardar propuesta.
     *
     * @return the resolution
     * @throws Exception the exception
     */
    @HandlesEvent("guardarPropuesta")
    public Resolution guardarPropuesta() throws Exception {
        mensajeWarning = "X";

        try {
            propuestaAsesoramiento = estructurasFxService.guardarPropuesta(seguroCambio, getTipoProducto(),
                    getContext().getEntidad(), getContext().getUsuario().getUsername(),
                    getContext().getLocale().getLanguage(), propuestaAsesoramiento.getClienteGCB(),
                    propuestaAsesoramiento.getFechaVigencia());

            seguroCambio.setPropuestaId(propuestaAsesoramiento.getId());

        } catch (DuplicateKeyException e) {
            LOGGER.error("Se ha producido un error al intentar guardar una propuesta duplicada", e);
            seguroCambio.setPrecio(null);
            addGlobalError(Constantes.GLOBAL_ERROR_SEGURO_CAMBIO_DUPLICADO);
            return getContext().getSourcePageResolution();

        } catch (GlobalErrorException ge) {
            LOGGER.error("Error global controlado cuando se intentaba guardar una propuesta", ge);
            seguroCambio.setPrecio(null);
            addGlobalError(ge);
            return getContext().getSourcePageResolution();
        }

        addMessage("message.guardarPropuesta.ok", propuestaAsesoramiento.getId(), seguroCambio.getId());

        return view();
    }

    /**
     * Checks if is cliente asesorable.
     *
     * @return the resolution
     * @throws JsonProcessingException the json processing exception
     */
    @HandlesEvent("isClienteAsesorable")
    public Resolution isClienteAsesorable() throws JsonProcessingException {
        mensajeWarning = "X";

        try {
            ObjectNode node = estructurasFxService.isClienteAsesorable(seguroCambio.getCliente().getCodigoJ(),
                    getContext().getEntidad(), getTipoProducto());
            return new StreamingResolution("application/json", node.toString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return getJsonResolutionGlobalError(getGlobalError("globalError.validarSiClienteAsesorable"));
        }

    }

    /** The bloques. */
    @Getter @Setter
    private List<FixingBlockFx> bloques;

    /**
     * Evento de Busqueda de fixing block.
     *
     * @return the resolution
     * @throws JsonProcessingException the json processing exception
     */
    @HandlesEvent("searchFixingBlock")
    public Resolution searchFixingBlock() throws JsonProcessingException {
        mensajeWarning = "X";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date fechDesde = Calendar.getInstance().getTime();
        Date fechHasta = Calendar.getInstance().getTime();
        try {
            fechDesde = formatter.parse(getContext().getRequest().getParameter("fechDesde"));
            fechHasta = formatter.parse(getContext().getRequest().getParameter("fechHasta"));
        } catch (ParseException e) {

        }

        List<FixingBlockFx> bloques = new ArrayList<>();

        FixingBlockFx block = new FixingBlockFx();

        DateFormat formatFechas = FormatUtils.getDateFormat(FormatUtils.DATE, FormatUtils.MEDIUM, getContext().getLocale());

        block.setFecha(formatFechas.format(fechDesde));
        block.setValor(2.2);
        block.setPeso(1.1);

        bloques.add(block);
        block = new FixingBlockFx();
        block.setFecha(formatFechas.format(fechHasta));
        block.setValor(3.2);
        block.setPeso(3.1);

        bloques.add(block);
        Pagina<FixingBlockFx> paginaBloques = new Pagina<>(bloques, "");
        paginaBloques.setPageNum(1);
        paginaBloques.setPages(1);
        return new StreamingResolution("application/json", new ObjectMapper().writeValueAsString(bloques));
    }

    @HandlesEvent("calcularActualizarPrecioAsegurado")
    public Resolution calcularActualizarPrecioAsegurado() throws JsonProcessingException {
        mensajeWarning = "X";
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();

            seguroCambio.setEntidad(getContext().getUsuario().getUserData().getCorpCodEmpresaLocal());

            node.put("precioAsegurado", estructurasFxService.calcularActualizarPrecioAsegurado(seguroCambio, null));

            return new StreamingResolution("application/json", node.toString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return getJsonResolutionGlobalError(getGlobalError("globalError.calcularActualizarPrecioAsegurado"));
        }
    }

    /** The lista tipo operacion. */
    private List<ComboBean> listaTipoOperacion;

    /**
     * Obtiene lista de tipo de operacion.
     *
     * @return lista de tipo de operacion
     */
    public List<ComboBean> getListaTipoOperacion() {

        if (listaTipoOperacion == null) {
            listaTipoOperacion = comboService.getListaComboSentidoFx(getContext().getEntidad(),
                    getContext().getLocale().getLanguage(), getListaFiltroTipoOperacion());
        }
        return listaTipoOperacion;
    }

    /**
     * Gets the lista filtro tipo operacion.
     *
     * @return the lista filtro tipo operacion
     */
    protected List<String> getListaFiltroTipoOperacion() {
        return null;
    }

    /** The lista barrera. */
    private List<ComboBean> listaBarrera;

    /**
     * Obtiene lista de barrera.
     *
     * @return lista de barrera
     */
    public List<ComboBean> getListaBarrera() {

        if (listaBarrera == null) {
            listaBarrera = comboService.getListaComboTipoBarreraFx(getContext().getEntidad(),
                    getContext().getLocale().getLanguage(), getListaFiltroBarrera());
        }
        return ListUtils.clone(listaBarrera);
    }

    /**
     * Gets the lista filtro barrera.
     *
     * @return the lista filtro barrera
     */
    protected List<String> getListaFiltroBarrera() {
        // refractor
        if (TipoProducto.DOWNOUT.equals(getTipoProducto())) {
            return null;
        } else {
            if (TipoProducto.LIQUIDACIONFIJA.equals(getTipoProducto())) {
                return null;
            } else {
                if (TipoProducto.EXTENSIBLE.equals(getTipoProducto())) {
                    return Arrays.asList(TipoBarrera.AMERICANA.toString());

                } else {
                    return null;
                }
            }

        }
    }

    /**
     * Gets the lista frecuencia barrera.
     *
     * @return the lista frecuencia barrera
     */

    public List<ComboBean> getListaFrecuenciaBarrera() {

        String id = seguroCambio != null && seguroCambio.getTipoBarrera() != null
                ? seguroCambio.getTipoBarrera().getId()
                : null;

        return getListaFrecuenciaBarrera(id);
    }

    /**
     * Gets the json filtro frecuencia barrera americana.
     *
     * @return the json filtro frecuencia barrera americana
     * @throws JsonProcessingException the json processing exception
     */
    public String getJsonFiltroFrecuenciaBarreraAmericana() throws JsonProcessingException {

        return new ObjectMapper().writeValueAsString(getFiltroFrecuenciaBarrera(TipoBarrera.AMERICANA));
    }

    /**
     * Gets the json filtro frecuencia barrera europea.
     *
     * @return the json filtro frecuencia barrera europea
     * @throws JsonProcessingException the json processing exception
     */
    public String getJsonFiltroFrecuenciaBarreraEuropea() throws JsonProcessingException {

        return new ObjectMapper().writeValueAsString(getFiltroFrecuenciaBarrera(TipoBarrera.EUROPEA));
    }

    /**
     * Gets the lista frecuencia barrera.
     *
     * @param idTipoBarrera the id tipo barrera
     * @return the lista frecuencia barrera
     */
    private List<ComboBean> getListaFrecuenciaBarrera(String idTipoBarrera) {

        List<String> filtro = getFiltroFrecuenciaBarrera(
                StringUtils.isNotBlank(idTipoBarrera) ? TipoBarrera.createFrom(idTipoBarrera) : null);

        return comboService.getListaComboFrecuenciaBarreraFx(getContext().getEntidad(),
                getContext().getLocale().getLanguage(), filtro);
    }

    /**
     * Gets the filtro frecuencia barrera.
     *
     * @param tipoBarrera the tipo barrera
     * @return the filtro frecuencia barrera
     */
    private List<String> getFiltroFrecuenciaBarrera(TipoBarrera tipoBarrera) {

        if (TipoBarrera.AMERICANA.equals(tipoBarrera)) {
            return Arrays.asList("CONTINUOUS");
        }
        if (TipoBarrera.EUROPEA.equals(tipoBarrera)) {
            return Arrays.asList("DISCRETE");
        }

        return null;
    }
    
    /** The lista destino. */
    private List<ComboBean> listaDestino;
    
    /**
     * Obtiene lista de destino.
     *
     * @return lista de destino
     */
    public List<ComboBean> getListaDestino() {

        if (listaDestino == null) {
            listaDestino = comboService.getListaComboDestinoRouting(getContext().getEntidad(), getContext().getLocale().getLanguage());
        }
        return ListUtils.clone(listaDestino);
    }

    /**
     * Obtiene la lista de payoff.
     *
     * @return la lista de payoff
     */
    public List<ComboBean> getListaPayOff() {

        return comboService.getListaComboPayOffFx(getContext().getEntidad(), getContext().getLocale().getLanguage());
    }

    /** The lista pares divisas. */
    private List<ParesDivisas> listaParesDivisas;

    /**
     * Obtiene lista de pares de divisas.
     *
     * @return lista de pares de divisas
     */
    public List<ParesDivisas> getListaParesDivisas() {

        if (listaParesDivisas == null) {
            if (isPerfilConsultaSoporte()) {
                listaParesDivisas = paresDivisasService.getListaParesDivisas(getContext().getEntidad());
            } else {
                listaParesDivisas = estructurasFxService.getListaParesDivisas(getContext().getEntidad(),
                        getContext().getRolesUsuario(), getTipoProducto());
            }
        }

        return ListUtils.clone(listaParesDivisas);
    }

    /** The lista divisas. */
    private List<ComboBean> listaDivisas;

    /**
     * Obtiene lista de divisas.
     *
     * @return lista de divisas
     */
    public List<ComboBean> getListaTipoCancelacion() {
        if (listaTipoCancelacion == null) {
            listaTipoCancelacion = comboService.getListaComboFromTipo(TipoCombo.TIPO_CANCELACION,
                    getContext().getEntidad(), getContext().getLocale().getLanguage());
        }

        return ListUtils.clone(listaTipoCancelacion);
    }

    /**
     * Obtiene lista de divisas.
     *
     * @return lista de divisas
     */
    public List<ComboBean> getListaDivisas() {
        if (listaDivisas == null) {
            if (isPerfilConsultaSoporte()) {
                listaDivisas = divisaFxService.getListaDivisas(getContext().getEntidad());
            } else {
                listaDivisas = estructurasFxService.getListaDivisas(getContext().getEntidad(),
                        getContext().getRolesUsuario(), getTipoProducto());
            }
        }

        return ListUtils.clone(listaDivisas);
    }

    /**
     * Obtiene lista de combo barrera.
     *
     * @return lista de combo barrera
     */
    public List<ComboBean> getListaComboBarrera() {

        return comboService.getListaComboBarreraFx(getContext().getEntidad(), getContext().getLocale().getLanguage());
    }

    /** The lista destino operacion. */
    private List<ComboBean> listaDestinoOperacion;

    /**
     * Obtiene lista de destino de operacion.
     *
     * @return lista destino de operacion
     */
    public List<ComboBean> getListaDestinoOperacion() {

        if (listaDestinoOperacion == null) {
            String entidad = getContext().getEntidad();
            String idioma = getContext().getLocale().getLanguage();

            if (isPerfilConsultaSoporte()) {
                listaDestinoOperacion = comboService.getListaComboMesasFx(entidad, idioma);
            } else {
                listaDestinoOperacion = atribucionesQueryService.getListaComboBeanMesa(
                        new AtribucionPK(entidad, getContext().getRolesUsuario().get(0), getTipoProducto().toString()),
                        idioma);
            }

        }

        return ListUtils.clone(listaDestinoOperacion);
    }

    /**
     * Obtiene lista fixing.
     *
     * @return lista fixing
     */
    public List<ComboBean> getListaFixing() {

        return fixingService.getListaComboArchivingGroup(getContext().getEntidad());
    }

    /**
     * Obtiene tipo de seguro.
     *
     * @return tipo de seguro
     */
    public TipoProducto getTipoProducto() {

        return TipoProducto.LIQUIDACION;

    }

    /**
     * Obtiene seguro de cambio.
     *
     * @return seguro de cambio
     */
    public SeguroCambioFx getSeguroCambio() {

        return seguroCambio;
    }

    /**
     * Establece seguro de cambio.
     *
     * @param seguroCambio nuevo seguro de cambio
     */
    public void setSeguroCambio(SeguroCambioFx seguroCambio) {

        this.seguroCambio = seguroCambio;
    }

    /**
     * Obtiene el texto panel heading.
     *
     * @return el texto en cuestion
     */
    public String getTextoPanelHeading() {
        String id = "segCamLiq.segCamLiq";
        return getMessage(id);
    }

    /**
     * Obtiene el texto option fx.
     *
     * @return el texto en cuestion
     */
    public String getTextoOptionFx() {

        return getMessage(
                TipoProducto.EXTENSIBLE.equals(getTipoProducto()) ? "segCamLiq.optionFx1" : "segCamLiq.optionFx");
    }

    /**
     * Comprueba si es visible opcion touch.
     *
     * @return true, si es visible
     */
    public boolean isVisibleTouch() {
        if (TipoProducto.DOWNOUT.equals(getTipoProducto())) {
            return false;
        } else {
            if (TipoProducto.LIQUIDACIONFIJA.equals(getTipoProducto())) {
                return true;

            } else {
                return false;
            }

        }
    }

    public boolean isVisibleComboTipoProducto() {

        return false;
    }

    /**
     * Comprueba si es visible la barrera 2.
     *
     * @return true, si es visible
     */
    public boolean isVisibleBarrera2() {
        if (TipoProducto.DOWNOUT.equals(getTipoProducto())) {
            return false;
        } else {
            if (TipoProducto.LIQUIDACIONFIJA.equals(getTipoProducto())) {
                return true;
            } else {
                return false;
            }

        }
    }

    /**
     * Comprueba si es visible la fecha de ejecucion.
     *
     * @return true, si es visible
     */
    public boolean isVisibleFechaEjecucion() {

        return TipoProducto.EXTENSIBLE.equals(getTipoProducto());
    }

    /**
     * Comprueba si es visible fixing reference.
     *
     * @return true, si es visible
     */
    public boolean isVisibleFixingReference() {

        return TipoProducto.LIQUIDACION.equals(getTipoProducto()) || TipoProducto.LIQUIDACIONFIJA.equals(getTipoProducto()) 
        		|| TipoProducto.DOWNOUT.equals(getTipoProducto()) || TipoProducto.UPOUT.equals(getTipoProducto());
    }

    public boolean isVisibleFixingReferenceExtensible() {

        return TipoProducto.EXTENSIBLE.equals(getTipoProducto());
    }

    /**
     * Comprueba si es visible fixing block.
     *
     * @return true, si es visible
     */
    public boolean isVisibleFixingBlock() {

        return TipoProducto.EXTENSIBLE.equals(getTipoProducto());
    }

    /**
     * Comprueba si es visible option fx 2.
     *
     * @return true, si es visible
     */
    public boolean isVisibleOptionFx2() {

        return TipoProducto.EXTENSIBLE.equals(getTipoProducto());
    }

    /** The start count down. */
    private boolean startCountDown = false;
    
  
    private String countDown = null;

    /**
     * Gets the count down.
     *
     * @return the count down
     */
    public void calcularCountDown() {

        Integer timeout = 0;
        
        if (!startCountDown || !isVisibleIdAcelera()) {
            setCountDown(null);
        }
 
        if (!isCotizacionWindfall()) {     
            timeout = Integer.parseInt(
                    variablesService.getValor(getContext().getEntidad(), Constantes.VARIABLE_PRECIO_VALIDO_ABACUS));
        } else {
            timeout = Optional.ofNullable(seguroCambio.getPrecio()).map(PreciosSeguroCambioFx::getTesoreria)
                    .map(ItemPreciosSeguroCambioFx::getTimeout).orElse(BigDecimal.valueOf(0)).intValue();
        }

        if (timeout>0) {
            Calendar aux = Calendar.getInstance();
            aux.add(Calendar.SECOND,timeout);
            setCountDown(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(aux.getTime()));
        }else {
            startCountDown = false;
        }
        
        
    }

    private boolean isCotizacionWindfall() {
        return "W".equalsIgnoreCase(variablesService.getValor(getContext().getEntidad(),
                Constantes.VARIABLE_COTIZACION_WINDFALL_ABACUS + getTipoProducto()));
    }

    /**
     * Checks if is visible id acelera.
     *
     * @return true, if is visible id acelera
     */
    public boolean isVisibleIdAcelera() {

        return seguroCambio != null && seguroCambio.getId() != null;
    }

    /**
     * Comprueba si el nombre del cliente va oculto.
     *
     * @return true, si debe ir oculto
     */
    public boolean isOcultoNombreCliente() {

        return seguroCambio == null || seguroCambio.getCliente() == null
                || StringUtils.isEmpty(seguroCambio.getCliente().getNombre());
    }

    /** The boton volver. */
    private boolean botonVolver;

    /**
     * Sets the boton volver.
     *
     * @param volver the new boton volver
     */
    public void setBotonVolver(boolean volver) {

        this.botonVolver = volver;
    }

    /**
     * Checks if is boton volver.
     *
     * @return true, if is boton volver
     */
    public boolean isBotonVolver() {

        return botonVolver;
    }

    /**
     * Checks if is boton generar propuesta.
     *
     * @return true, if is boton generar propuesta
     */
    public boolean isBotonGenerarPropuesta() {

        getInfoRLA();

        boolean validarLeiJuridica = "S".equalsIgnoreCase(
                variablesService.getValor(getContext().getEntidad(), Constantes.VARIABLE_VALIDAR_FX_LEI));

        return seguroCambio != null && seguroCambio.getCliente() != null
                && (seguroCambio.getCliente().isPersonaJuridica() && !validarLeiJuridica
                        || seguroCambio.getCliente().isValidLei())
                && isEstadoPropuestaAsesoramiento(EstadoPropuesta.BORRADOR);
    }

    /**
     * Checks if is boton guardar propuesta.
     *
     * @return true, if is boton guardar propuesta
     */
    public boolean isBotonGuardarPropuesta() {

        return propuestaAsesoramiento != null && propuestaAsesoramiento.getId() == null
                && isEstado(EstadoOperacion.COTIZADAAUTOMATICA, EstadoOperacion.COTIZADAMANUAL);

    }

    /**
     * Checks if is boton enviar.
     *
     * @return true, if is boton enviar
     */
    public boolean isBotonEnviar() {

        return isEstado(EstadoOperacion.FINALIZADAERRORMUREX, EstadoOperacion.RECHAZADASSCC);
    }

    /** The enviar tesoreria. */
    private boolean enviarTesoreria = false;

    /**
     * Checks if is boton enviar tesoreria.
     *
     * @return true, if is boton enviar tesoreria
     */
    public boolean isBotonEnviarTesoreria() {

        return enviarTesoreria && !isPerfilVentas();
    }

    /**
     * Checks if is boton documentacion precontractual.
     *
     * @return true, if is boton documentacion precontractual
     */
    public boolean isBotonDocumentacionPrecontractual() {

        return isEstado(EstadoOperacion.FINALIZADAOKMUREX, EstadoOperacion.RECHAZADASSCC,
                EstadoOperacion.COTIZADAAUTOMATICA, EstadoOperacion.COTIZADAMANUAL,
                EstadoOperacion.FINALIZADAERRORMUREX, EstadoOperacion.ALTASSCC, EstadoOperacion.ENVIADAMUREX,
                EstadoOperacion.FINALIZADAFIN);
    }

    /**
     * Checks if is boton documentacion contractual.
     *
     * @return true, if is boton documentacion contractual
     */
    public boolean isBotonDocumentacionContractual() {

        return isEstado(EstadoOperacion.FINALIZADAOKMUREX, EstadoOperacion.FINALIZADAERRORMUREX,
                EstadoOperacion.ENVIADAMUREX, EstadoOperacion.FINALIZADAFIN);
    }

    /**
     * Checks if is cliente valid contract.
     *
     * @return true, if is cliente valid contract
     */
    public boolean isClienteValidContract() {

        getInfoRLA();

        return seguroCambio != null && seguroCambio.getCliente() != null && seguroCambio.getCliente().isValidContract();
    }

    public boolean isLeiVigenteEnPantalla() {

        getInfoRLA();

        return seguroCambio != null && seguroCambio.getCliente() != null
                && seguroCambio.getCliente().isLeiVigenteEnPantalla();
    }

    /**
     * Checks if is cliente valid lei.
     *
     * @return true, if is cliente valid lei
     */
    public boolean isClienteValidLei() {

        getInfoRLA();

        boolean validarLeiJuridica = "S".equalsIgnoreCase(
                variablesService.getValor(getContext().getEntidad(), Constantes.VARIABLE_VALIDAR_FX_LEI));

        return seguroCambio != null && seguroCambio.getCliente() != null
                && (seguroCambio.getCliente().isPersonaJuridica() && !validarLeiJuridica
                        || seguroCambio.getCliente().isValidLei());
    }

    /**
     * Checks if is boton RFQ.
     *
     * @return true, if is boton RFQ
     */
    public boolean isBotonRFQ() {

        List<String> roles = getContext().getRolesUsuario();

        if (roles.contains(new Perfil(FirstPerfil.GESTOR, Subperfil.AVANZADO).toString())
                || roles.contains(new Perfil(FirstPerfil.GESTOR, Subperfil.MEDIO).toString())
                || roles.contains(new Perfil(FirstPerfil.GESTOR, Subperfil.BASICO).toString())) {
            return false;
        }

        return true;
    }

    /**
     * Checks if is not gestor.
     *
     * @return true, if is not gestor
     */
    public boolean isNotGestor() {
        return isBotonRFQ();
    }

    /**
     * Checks if is perfil ventas.
     *
     * @return true, if is perfil ventas
     */
    public boolean isPerfilVentas() {
        return getContext().isPerfil(FirstPerfil.VENTAS);
    }

    public boolean isPerfilConsultaSoporte() {
        return getContext().isPerfil(FirstPerfil.CONSULTA) || getContext().isPerfil(FirstPerfil.SOPORTE);
    }

    public boolean isCamposEditables() {
        if (isPerfilConsultaSoporte()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks if is boton cotizarPrecio.
     *
     * @return false
     */
    public boolean isBotonCotizarPrecio() {
        return false;
    }

    /**
     * Checks if is boton cotizar.
     *
     * @return true, if is boton cotizar
     */
    public boolean isBotonCotizar() {

        if (seguroCambio == null || seguroCambio.getEstado() == null || seguroCambio.getId() == null) {
            return true;
        }

        return isEstado(EstadoOperacion.XX, EstadoOperacion.COTIZADAAUTOMATICA, EstadoOperacion.ENVIADATESORERIA,
                EstadoOperacion.COTIZADAMANUAL);
    }

    /**
     * Checks if is boton contratar.
     *
     * @param enCuentaCountDown the en cuenta count down
     * @return true, if is boton contratar
     */
    private boolean isBotonContratar(boolean enCuentaCountDown) {
        if (enCuentaCountDown) {
            return startCountDown && validarAtribuciones(Accion.CONTRATAR.toString(), true) && getInfoRLA() != null
                    && personaService.isValidRlaEstructuras(getContext().getEntidad(), seguroCambio.getCliente(), true);
        } else {
            return validarAtribuciones(Accion.CONTRATAR.toString(), true) && getInfoRLA() != null
                    && personaService.isValidRlaEstructuras(getContext().getEntidad(), seguroCambio.getCliente(), true);
        }
    }

    /**
     * Checks if is boton contratar.
     *
     * @return true, if is boton contratar
     */
    public boolean isBotonContratar() {

        return isBotonContratar(true);
    }

    /**
     * Checks if is estado propuesta asesoramiento.
     *
     * @param estado the estado
     * @return true, if is estado propuesta asesoramiento
     */
    private boolean isEstadoPropuestaAsesoramiento(EstadoPropuesta... estado) {

        if (propuestaAsesoramiento != null && propuestaAsesoramiento.getId() != null
                && StringUtils.isBlank(propuestaAsesoramiento.getEstado())) {
            propuestaAsesoramiento = estructurasFxService.getPropuestaAsesoramiento(getContext().getEntidad(),
                    propuestaAsesoramiento.getId());
        }

        if (propuestaAsesoramiento == null || StringUtils.isBlank(propuestaAsesoramiento.getEstado())) {
            return false;
        }

        Set<String> estados = new HashSet<String>();

        for (EstadoPropuesta e : estado) {
            estados.add(e.toString());
        }

        return estados.contains(propuestaAsesoramiento.getEstado());
    }

    /**
     * Checks if is estado.
     *
     * @param estado the estado
     * @return true, if is estado
     */
    private boolean isEstado(EstadoOperacion... estado) {

        if (seguroCambio != null && seguroCambio.getId() != null
                && (seguroCambio.getEstado() == null || StringUtils.isEmpty(seguroCambio.getEstado().getId()))) {
            seguroCambio.setEstado(estructurasFxService.getEstado(seguroCambio.getId(), getContext().getEntidad(),
                    getContext().getLocale().getLanguage()));
        }

        if (seguroCambio == null || seguroCambio.getEstado() == null) {
            return false;
        }

        Set<String> estados = new HashSet<String>();

        for (EstadoOperacion e : estado) {
            estados.add(e.toString());
        }

        return estados.contains(seguroCambio.getEstado().getId());
    }

    /**
     * Comprueba si el boton busqueda propuesta debe ir disabled.
     *
     * @return true, si debe ir disabled
     */
    public boolean isDisabledBotonBusquedaPropuesta() {
        return isPerfilConsultaSoporte() || seguroCambio == null || seguroCambio.getCliente() == null
                || StringUtils.isEmpty(seguroCambio.getCliente().getNombre())
                || (!isEstado(EstadoOperacion.RECHAZADASSCC, EstadoOperacion.FINALIZADAERRORMUREX)
                        && !StringUtils.isEmpty(seguroCambio.getPropuesta()));
    }

    /**
     * Comprueba si el boton busqueda ccc debe ir disabled.
     *
     * @return true, si debe ir disabled
     */
    public boolean isDisabledBotonBusquedaCCC() {
        return isPerfilConsultaSoporte() || seguroCambio == null || seguroCambio.getCliente() == null
                || StringUtils.isEmpty(seguroCambio.getCliente().getNombre());
    }

    /**
     * Checks if is disabled fields.
     *
     * @return true, if is disabled fields
     */
    public boolean isDisabledFields() {

        return !isCamposEditables() || isEstado(EstadoOperacion.RECHAZADASSCC, EstadoOperacion.FINALIZADAERRORMUREX,
                EstadoOperacion.FINALIZADAFIN, EstadoOperacion.FINALIZADAOKMUREX,
                EstadoOperacion.DOCPRECONTRACTUALGENERADA, EstadoOperacion.FIRMADA, EstadoOperacion.ALTASSCC,
                EstadoOperacion.ENVIADAMUREX);
    }

    /**
     * Checks if is disabled oficina.
     *
     * @return true, if is disabled oficina
     */
    public boolean isDisabledOficina() {

        return !isCamposEditables() || !isEstado(EstadoOperacion.RECHAZADASSCC, EstadoOperacion.FINALIZADAERRORMUREX);
    }

    /**
     * Gets the json textos opcion barrera.
     *
     * @return the json textos opcion barrera
     * @throws JsonProcessingException the json processing exception
     */
    public String getJsonTextosOpcionBarrera() throws JsonProcessingException {

        Map<String, Map<String, String>> textos = new HashMap<String, Map<String, String>>();

        addTextos(SentidoFx.IMP.toString(), textos, getMapEachOperacion(true));
        addTextos(SentidoFx.EXP.toString(), textos, getMapEachOperacion(false));

        addTextos(TipoBarrera.EUROPEA.toString(), textos, getMapEachBarrier(false));
        addTextos(TipoBarrera.AMERICANA.toString(), textos, getMapEachBarrier(true));

        addTextos(SentidoFx.IMP + ":" + TipoBarrera.EUROPEA, textos, getMapEachImport(false));
        addTextos(SentidoFx.IMP + ":" + TipoBarrera.AMERICANA, textos, getMapEachImport(true));

        addTextos(SentidoFx.EXP + ":" + TipoBarrera.EUROPEA, textos, getMapEachExport(false));
        addTextos(SentidoFx.EXP + ":" + TipoBarrera.AMERICANA, textos, getMapEachExport(true));

        return new ObjectMapper().writeValueAsString(textos);
    }

    /**
     * Adds the textos.
     *
     * @param key    the key
     * @param textos the textos
     * @param aux    the aux
     */
    private void addTextos(String key, Map<String, Map<String, String>> textos, Map<String, String> aux) {

        if (aux != null) {
            textos.put(key, aux);
        }
    }

    /** The Constant UP. */
    protected static final String UP = "segCamLiq.up";

    /** The Constant DOWN. */
    protected static final String DOWN = "segCamLiq.down";

    /** The Constant IN. */
    protected static final String IN = "segCamLiq.in";

    /** The Constant OUT. */
    protected static final String OUT = "segCamLiq.out";

    /** The Constant ABOVE. */
    protected static final String ABOVE = "segCamLiq.above";

    /** The Constant BELOW. */
    protected static final String BELOW = "segCamLiq.below";

    /** The Constant SIMPLE. */
    protected static final String SIMPLE = "segCamLiq.barsimple";

    /** The Constant EUROPEA. */
    protected static final String EUROPEA = "segCamLiq.bareuropea";

    /** The Constant PUT. */
    protected static final String PUT = "segCamLiq.put";

    /** The Constant CALL. */
    protected static final String CALL = "segCamLiq.call";

    /** The Constant COMPRA. */
    protected static final String COMPRA = "segCamLiq.compra";

    /** The Constant VENTA. */
    protected static final String VENTA = "segCamLiq.venta";

    /**
     * Gets the map each export.
     *
     * @param isAmerican the is american
     * @return the map each export
     */
    protected Map<String, String> getMapEachExport(boolean isAmerican) {
        Map<String, String> aux = new HashMap<String, String>();

        aux.put("#cmbBarrera", getMessage(OUT));
        aux.put("#txtBarrera", getMessage(isAmerican ? DOWN : BELOW));

        return aux;
    }

    /**
     * Gets the map each import.
     *
     * @param isAmerican the is american
     * @return the map each import
     */
    protected Map<String, String> getMapEachImport(boolean isAmerican) {
        // refractor
        Map<String, String> aux = new HashMap<String, String>();

        aux.put("#cmbBarrera", getMessage(OUT));
        aux.put("#txtBarrera", getMessage(isAmerican ? UP : ABOVE));

        return aux;
    }

    /**
     * Gets the map each barrier.
     *
     * @param isAmerican the is american
     * @return the map each barrier
     */
    protected Map<String, String> getMapEachBarrier(boolean isAmerican) {
        Map<String, String> aux = new HashMap<String, String>();
        aux.put("#opcionFx", getMessage(isAmerican ? SIMPLE : EUROPEA));
        return aux;
    }

    /**
     * Gets the map each operacion.
     *
     * @param isImport the is import
     * @return the map each operacion
     */
    protected Map<String, String> getMapEachOperacion(boolean isImport) {
        Map<String, String> aux = new HashMap<String, String>();
        aux.put("#txtCV0", getMessage(isImport ? COMPRA : VENTA));
        aux.put("#txtDivisa1", getMessage(isImport ? CALL : PUT));
        aux.put("#chkDivisa1", aux.get("#txtDivisa1"));
        aux.put("#txtDivisa2", getMessage(isImport ? PUT : CALL));
        aux.put("#chkDivisa2", aux.get("#txtDivisa2"));
        aux.put("#txtCV1", getMessage(COMPRA));

        return aux;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.stripes.validation.ValidationErrorHandler#
     * handleValidationErrors(net.sourceforge.stripes. validation.ValidationErrors)
     */
    @Override
    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {

        return handleValidationErrors(Arrays.asList("isClienteAsesorable", "calcularActualizarPrecioAsegurado"),
                errors);
    }

    /**
     * Checks if is nominales han de ser iguales.
     *
     * @return true, if is nominales han de ser iguales
     */
    public boolean isNominalesHanDeSerIguales() {

        return !isSeguroCambioLiquidacionFija();
    }

    /**
     * Checks if is oculto strike.
     *
     * @return true, if is oculto strike
     */
    public boolean isOcultoStrike() {

        return isSeguroCambioLiquidacionFija();
    }

    /**
     * Checks if is seguro cambio liquidacion fija.
     *
     * @return true, if is seguro cambio liquidacion fija
     */
    public boolean isSeguroCambioLiquidacionFija() {

        return TipoProducto.LIQUIDACIONFIJA.equals(getTipoProducto());
    }

    /**
     * Checks if is fecha fin barrera fecha vencimiento han de ser iguales.
     *
     * @return true, if is fecha fin barrera fecha vencimiento han de ser iguales
     */
    public boolean isFechaFinBarreraFechaVencimientoHanDeSerIguales() {

        return isSeguroCambioLiquidacionFija();
    }

    /**
     * Checks if is tratamiento divisas seguro cambio liquidacion fija.
     *
     * @return true, if is tratamiento divisas seguro cambio liquidacion fija
     */
    public boolean isTratamientoDivisasSeguroCambioLiquidacionFija() {

        return isSeguroCambioLiquidacionFija();
    }

    /**
     * Checks if is oculto divisas opcion 1 call put.
     *
     * @return true, if is oculto divisas opcion 1 call put
     */
    public boolean isOcultoDivisasOpcion1CallPut() {

        return isSeguroCambioLiquidacionFija();
    }

    /**
     * Checks if is disabled fechas barrera.
     *
     * @return true, if is disabled fechas barrera
     */
    public boolean isDisabledFechasBarrera() {

        return isSeguroCambioLiquidacionFija();
    }

    /**
     * Gets the fecha inicio barrera.
     *
     * @return the fecha inicio barrera
     */
    public String getFechaInicioBarrera() {

        if (!isSeguroCambioLiquidacionFija()) {
            return "";
        }

        return FormatUtils.getDateFormat(FormatUtils.DATE, null, getContext().getLocale()).format(new Date());
    }

    /**
     * Checks if is called cotizar with success.
     *
     * @return true, if is called cotizar with success
     */
    public boolean isCalledCotizarWithSuccess() {
        return isCalledCotizarWithSuccess;
    }

    /**
     * Checks if is called RFQ with success.
     *
     * @return true, if is called RFQ with success
     */
    public boolean isCalledRFQWithSuccess() {
        return isCalledRFQWithSuccess;
    }

    public boolean isCalledGetRFQWithSuccess() {
        return isCalledGetRFQWithSuccess;
    }

    /**
     * Gets the mensaje cuenta corriente vacia.
     *
     * @return the mensaje cuenta corriente vacia
     */
    public String getMensajeCuentaCorrienteVacia() {
        return getMessage("message.contratar.cuentaCorrienteVacia");
    }

    /**
     * Checks if is cuenta corriente vacia.
     *
     * @return true, if is cuenta corriente vacia
     */
    public boolean isCuentaCorrienteVacia() {

        if (seguroCambio == null || (seguroCambio != null && StringUtils.isBlank(seguroCambio.getCcc()))) {
            return true;
        }

        return false;
    }

    /**
     * Validar gestor obligatorio si existe.
     */
    private void validarPerfilGestor() {
        List<String> roles = getContext().getRolesUsuario();
        if (    (
                    roles.contains(new Perfil(FirstPerfil.GESTOR, Subperfil.AVANZADO).toString())
                    || roles.contains(new Perfil(FirstPerfil.GESTOR, Subperfil.MEDIO).toString())
                    || roles.contains(new Perfil(FirstPerfil.GESTOR, Subperfil.BASICO).toString())
                )
                && StringUtils.isEmpty(seguroCambio.getGestor())
            ) {
                addValidationError("seguroCambio.gestor", Constantes.VALIDATION_REQUIRED_VALUE_NOT_PRESENT);
        }
    }

    /**
     * Checks if is perfil gestor basico.
     *
     * @return true, if is perfil gestor basico
     */
    public boolean isPerfilGestorBasico() {
        List<String> roles = getContext().getRolesUsuario();
        if (roles.contains(new Perfil(FirstPerfil.GESTOR, Subperfil.BASICO).toString())) {
            return true;
        }
        return false;
    }

    /**
     * Devuelve el perfil del segmento.
     *
     * @return perfil
     */
    public String getPerfilSegmento(String segmento) {
        if ("BG".equals(segmento)) {
            return Subperfil.MRG.toString();
        } else if ("BP".equals(segmento)) {
            return Subperfil.INSTITUCIONAL.toString();
        }

        return Subperfil.BCE.toString();
    }

    /**
     * Gets the mensaje warning.
     *
     * @return the mensaje warning
     */
    public String getMensajeWarning() {
        return mensajeWarning;
    }

    /**
     * @return the decimales
     */
    @HandlesEvent("getDecimales")
    public Resolution getDecimales() throws JsonProcessingException {
        String divisa = getContext().getRequest().getParameter("divisa");
        String json = new ObjectMapper().writeValueAsString("decimal=" + divisaFxService.getDecimalDivisa(divisa));
        return new StreamingResolution("application/json", json);
    }

    public boolean validarAtribuciones(String accion, boolean isWarning) {

        List<String> mensajes = new ArrayList<String>();

        if (!isWarning) {
            if (accion.equals(Accion.COTIZAR.toString())) {
                enviarTesoreria = true;
            } else {
                enviarTesoreria = false;
            }
        }

        AtribucionAccionPK atribucionAccionPK = new AtribucionAccionPK(getContext().getEntidad(),
                getContext().getRolesUsuario().get(0), getTipoProducto().toString(), accion);
        if (!atribucionesService.isHasAtribucionesAlguna(atribucionAccionPK)) {
            mensajes.add(getGlobalError("globalError.noAtribuciones.accionNoPermitida"));

        } else {

            AtribucionDivisaPK atribucionDivisaPK = new AtribucionDivisaPK(getContext().getEntidad(),
                    getContext().getRolesUsuario().get(0), getTipoProducto().toString(), accion,
                    seguroCambio.getSpot().getNominal().getCurrency());
            AtribucionPerfilProductoDivisa atribucionPerfilProductoDivisa = atribucionesService
                    .getAtribucionPerfilProductoDivisa(atribucionDivisaPK);

            if (atribucionPerfilProductoDivisa == null) {
                addGlobalError(Constantes.GLOBAL_ERROR_NO_ATRIBUCIONES_DIVISA_NO_PERMITIDA,
                        seguroCambio.getSpot().getNominal().getCurrency());
                return false;
            }

            if (!atribucionesService.validarNominal(atribucionPerfilProductoDivisa, seguroCambio.getNominal())) {
                if (isWarning) {
                    mensajes.add(getGlobalError("globalError.noAtribuciones.nominalNoValido",
                            atribucionPerfilProductoDivisa.getNominalMinimo(),
                            atribucionPerfilProductoDivisa.getNominalMaximo()));
                } else {
                    addGlobalError("globalError.noAtribuciones.nominalNoValido",
                            atribucionPerfilProductoDivisa.getNominalMinimo(),
                            atribucionPerfilProductoDivisa.getNominalMaximo());
                }
            }

            AtribucionPerfilProductoAccion atribucionPerfilProductoAccion = atribucionesService
                    .getAtribucionPerfilProductoAccion(new AtribucionAccionPK(getContext().getEntidad(),
                            getContext().getRolesUsuario().get(0), getTipoProducto().toString(), accion));
            if (accion.equals(Accion.CONTRATAR.toString())) {
                if (!atribucionesService.validarMargen(atribucionPerfilProductoAccion,
                        seguroCambio.getPrecio().getTesoreria().getPorcentaje())) {
                    if (isWarning) {
                        mensajes.add(getGlobalError("globalError.noAtribuciones.margenNoValido",
                                atribucionPerfilProductoAccion.getMargenMinimo(),
                                atribucionPerfilProductoAccion.getMargenMaximo()));
                    } else {
                        addGlobalError("globalError.noAtribuciones.margenNoValido",
                                atribucionPerfilProductoAccion.getMargenMinimo(),
                                atribucionPerfilProductoAccion.getMargenMaximo());
                        return false;
                    }
                }
            }

            if (!atribucionesService.validarPlazoMaximo(atribucionPerfilProductoAccion,
                    seguroCambio.getOpcion1().getVencimiento())) {
                if (isWarning) {
                    mensajes.add(getGlobalError("globalError.noAtribuciones.plazoNoValido",
                            atribucionPerfilProductoAccion.getPlazoMaximo()));
                } else {
                    addGlobalError("globalError.noAtribuciones.plazoNoValido",
                            atribucionPerfilProductoAccion.getPlazoMaximo());
                }
                return false;
            }

            String par = seguroCambio.getSpot().getPrecioAsegurado().getTwoCurrencies();

            String div1 = par.substring(0, 3);
            AtribucionDivisaPK atribucionDivisa1PK = new AtribucionDivisaPK(getContext().getEntidad(),
                    getContext().getRolesUsuario().get(0), getTipoProducto().toString(), accion, div1);
            AtribucionPerfilProductoDivisa atribucionPerfilProductoDivisa1 = atribucionesService
                    .getAtribucionPerfilProductoDivisa(atribucionDivisa1PK);
            if (atribucionPerfilProductoDivisa1 == null) {
                mensajes.add(getGlobalError(Constantes.GLOBAL_ERROR_NO_ATRIBUCIONES_DIVISA_NO_PERMITIDA, div1));
            }
            String div2 = par.substring(4);
            AtribucionDivisaPK atribucionDivisa2PK = new AtribucionDivisaPK(getContext().getEntidad(),
                    getContext().getRolesUsuario().get(0), getTipoProducto().toString(), accion, div2);
            AtribucionPerfilProductoDivisa atribucionPerfilProductoDivisa2 = atribucionesService
                    .getAtribucionPerfilProductoDivisa(atribucionDivisa2PK);
            if (atribucionPerfilProductoDivisa2 == null) {
                mensajes.add(getGlobalError(Constantes.GLOBAL_ERROR_NO_ATRIBUCIONES_DIVISA_NO_PERMITIDA, div2));
            }

        }

        if (!mensajes.isEmpty()) {
            if (isWarning)
                mensajeWarning = getGlobalError("globalError.noAtribuciones." + accion, String.join(" ", mensajes));
            else
                addGlobalError("globalError.noAtribuciones." + accion, String.join(" ", mensajes));
            return false;
        } else {
            return true;
        }

    }
    
    public String getCartaClasificacionClienteAsJSON() {
        return Optional.ofNullable(seguroCambio).map(SeguroCambioFx::getCliente).map(Cliente::getCartaClasificacion)
                .map(
                        carta->new ObjectMapper().valueToTree(carta).toString()
                )
                .orElse("");
    }

    @Override
    public boolean hayVersionPosteriorOperacion() {
        return operacionFxVersionService.hayVersionPosteriorOperacion(seguroCambio, operacionFxMapper);
    }

    @Override
    public Long getVersionOperacion() {
        return Optional.ofNullable(seguroCambio).map(SeguroCambioFx::getVersion)
                .orElse(operacionFxVersionService.getDefaultVersionOperacion());
    }

    @Override
    public boolean isSetDefaultVersionOperacion() {
        return !Optional.ofNullable(seguroCambio).map(SeguroCambioFx::getVersion).isPresent();
    }

    public String getCountDown() {
        return countDown;
    }

    public void setCountDown(String countDown) {
        this.countDown = countDown;
    }


}
