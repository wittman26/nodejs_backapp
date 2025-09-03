package com.isb.acelera.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.SerializationUtils;

import com.isb.fx.model.SsccLiqOperRenegociacion;

import lombok.Getter;
import lombok.Setter;

/**
 * The type Seguro cambio fx.
 */
public class SeguroCambioFx extends OperacionFx {

	private ComboBean tipoOperacion;
	private ComboBean tipoBarrera;
	private Date fechaEjecucion;

	private XMLGregorianCalendar timestampContratacion;

	private Cliente cliente;
	private String propuesta;

	private FxSpotData spot = new FxSpotData();

	private OpcionFx opcion1 = new OpcionFx();
	private OpcionFx opcion2 = new OpcionFx();

	private PreciosSeguroCambioFx precio;

	private String contrato;
	private String ccc;
	private String cccPartenon;
	
	@Getter
	@Setter
	private List<SsccLiqOperRenegociacion> renegociaciones;

	@Getter
	@Setter
	private boolean renegociacion = false;

	@Getter
	@Setter
	private String portfolio;

	@Getter
	@Setter
	private ComboBean destino;

	/**
	 * Gets contrato.
	 *
	 * @return the contrato
	 */
	public String getContrato() {

		return contrato;
	}

	/**
	 * Sets contrato.
	 *
	 * @param contrato the contrato
	 */
	public void setContrato(String contrato) {

		this.contrato = contrato;
	}

	/**
	 * Gets precio.
	 *
	 * @return the precio
	 */
	public PreciosSeguroCambioFx getPrecio() {

		return precio;
	}

	@Override
	public String getTitular() {

		return getCliente() != null ? getCliente().getCodigoJ() : null;
	}

	@Override
	public Double getNominal() {

		return spot.getNominal() != null ? spot.getNominal().getAmount() : null;
	}

	/**
	 * Sets precio.
	 *
	 * @param precio the precio
	 */
	public void setPrecio(PreciosSeguroCambioFx precio) {

		this.precio = precio;
	}

	/**
	 * Sets tipo producto.
	 *
	 * @param tipoProducto the tipo producto
	 */
	public void setTipoProducto(TipoProducto tipoProducto) {

		opcion1.setTipoProducto(tipoProducto);
		opcion2.setTipoProducto(tipoProducto);
	}

	/**
	 * Gets fecha ejecucion.
	 *
	 * @return the fecha ejecucion
	 */
	public Date getFechaEjecucion() {

		return SerializationUtils.clone(fechaEjecucion);
	}

	/**
	 * Sets fecha ejecucion.
	 *
	 * @param fechaEjecucion the fecha ejecucion
	 */
	public void setFechaEjecucion(Date fechaEjecucion) {

		this.fechaEjecucion = SerializationUtils.clone(fechaEjecucion);
	}

	/**
	 * Gets cliente.
	 *
	 * @return the cliente
	 */
	public Cliente getCliente() {

		return cliente;
	}

	/**
	 * Gets spot.
	 *
	 * @return the spot
	 */
	public FxSpotData getSpot() {

		return spot;
	}

	/**
	 * Sets spot.
	 *
	 * @param spot the spot
	 */
	public void setSpot(FxSpotData spot) {

		this.spot = spot;
	}

	/**
	 * Gets opcion 1.
	 *
	 * @return the opcion 1
	 */
	public OpcionFx getOpcion1() {

		return opcion1;
	}

	/**
	 * Sets opcion 1.
	 *
	 * @param opcion1 the opcion 1
	 */
	public void setOpcion1(OpcionFx opcion1) {

		this.opcion1 = opcion1;
	}

	/**
	 * Gets opcion 2.
	 *
	 * @return the opcion 2
	 */
	public OpcionFx getOpcion2() {

		return opcion2;
	}

	/**
	 * Sets opcion 2.
	 *
	 * @param opcion2 the opcion 2
	 */
	public void setOpcion2(OpcionFx opcion2) {

		this.opcion2 = opcion2;
	}

	/**
	 * Sets cliente.
	 *
	 * @param cliente the cliente
	 */
	public void setCliente(Cliente cliente) {

		this.cliente = cliente;
	}

	/**
	 * Gets propuesta.
	 *
	 * @return the propuesta
	 */
	public String getPropuesta() {

		return propuesta;
	}

	/**
	 * Sets propuesta.
	 *
	 * @param propuesta the propuesta
	 */
	public void setPropuesta(String propuesta) {

		this.propuesta = propuesta;
	}

	/**
	 * Gets tipo operacion.
	 *
	 * @return the tipo operacion
	 */
	public ComboBean getTipoOperacion() {

		return tipoOperacion;
	}

	/**
	 * Sets tipo operacion.
	 *
	 * @param tipoOperacion the tipo operacion
	 */
	public void setTipoOperacion(ComboBean tipoOperacion) {

		this.tipoOperacion = tipoOperacion;

		spot.setTipoOperacion(tipoOperacion);
		opcion1.setTipoOperacion(tipoOperacion);
		opcion2.setTipoOperacion(tipoOperacion);
	}

	/**
	 * Gets tipo barrera.
	 *
	 * @return the tipo barrera
	 */
	public ComboBean getTipoBarrera() {

		return tipoBarrera;
	}

	/**
	 * Sets tipo barrera.
	 *
	 * @param tipoBarrera the tipo barrera
	 */
	public void setTipoBarrera(ComboBean tipoBarrera) {

		this.tipoBarrera = tipoBarrera;

		opcion1.setTipoBarrera(tipoBarrera);
		opcion2.setTipoBarrera(tipoBarrera);
	}

	/**
	 * Gets ccc.
	 *
	 * @return the ccc
	 */
	public String getCcc() {
		return ccc;
	}

	/**
	 * Sets ccc.
	 *
	 * @param ccc the ccc
	 */
	public void setCcc(String ccc) {
		this.ccc = ccc;
	}

	/**
	 * Gets ccc partenon.
	 *
	 * @return the ccc partenon
	 */
	public String getCccPartenon() {
		return cccPartenon;
	}

	/**
	 * Sets ccc partenon.
	 *
	 * @param cccPartenon the ccc partenon
	 */
	public void setCccPartenon(String cccPartenon) {
		this.cccPartenon = cccPartenon;
	}

	/**
	 * Gets timestamp contratacion.
	 *
	 * @return the timestamp contratacion
	 */
	public XMLGregorianCalendar getTimestampContratacion() {
		return timestampContratacion;
	}

	/**
	 * Sets timestamp contratacion.
	 *
	 * @param timestampContratacion the timestamp contratacion
	 */
	public void setTimestampContratacion(XMLGregorianCalendar timestampContratacion) {
		this.timestampContratacion = timestampContratacion;
	}
	


	public Double getMargenDocubox() {
		BigDecimal beneficio = BigDecimal.valueOf(0);
		BigDecimal valorMercadoTotal = BigDecimal.valueOf(0);
		if (Objects.nonNull(this.precio) && Objects.nonNull(this.precio.getTesoreria())) {
			if (Objects.nonNull(this.precio.getTesoreria().getBeneficio())) {
				beneficio = BigDecimal.valueOf(this.precio.getTesoreria().getBeneficio());
			}
			if (Objects.nonNull(this.precio.getTesoreria().getValorMercadoTotal())) {
				valorMercadoTotal = this.precio.getTesoreria().getValorMercadoTotal();
			}
		}
		
		
		BigDecimal precioAsegurado = BigDecimal.valueOf(0);
		if (Objects.nonNull(this.spot) && Objects.nonNull(this.spot.getPrecioAsegurado())
				&& Objects.nonNull(this.spot.getPrecioAsegurado().getAmount())) {
			precioAsegurado = BigDecimal.valueOf(this.spot.getPrecioAsegurado().getAmount());
		}
		
		BigDecimal nominal = null;
		if (Objects.nonNull(this.opcion1.getNominal()) && Objects.nonNull(this.opcion1.getNominal().getAmount())) {
			nominal = BigDecimal.valueOf(this.opcion1.getNominal().getAmount());
		} else {
			nominal = BigDecimal.valueOf(1);
		}
		
		return (beneficio.add(valorMercadoTotal).multiply(precioAsegurado)).divide(nominal, 2, RoundingMode.HALF_UP).doubleValue();
	}

	public Double getCvaDocubox() {
		BigDecimal cva = BigDecimal.valueOf(0);
		BigDecimal precioAsegurado = BigDecimal.valueOf(0);
		BigDecimal nominal = BigDecimal.valueOf(1);
		if (Objects.nonNull(this.precio) && Objects.nonNull(this.precio.getTesoreria())
				&& Objects.nonNull(this.precio.getTesoreria().getCva())) {
			cva = BigDecimal.valueOf(this.precio.getTesoreria().getCva());
		}
		if (Objects.nonNull(this.spot) && Objects.nonNull(this.spot.getPrecioAsegurado())
				&& Objects.nonNull(this.spot.getPrecioAsegurado().getAmount())) {
			precioAsegurado = BigDecimal.valueOf(this.spot.getPrecioAsegurado().getAmount());
		}
		if (Objects.nonNull(this.opcion1) && Objects.nonNull(this.opcion1.getNominal())
				&& Objects.nonNull(this.opcion1.getNominal().getAmount())) {
			nominal = BigDecimal.valueOf(this.opcion1.getNominal().getAmount());
		}
		return (cva).multiply(precioAsegurado).divide(nominal, 2, RoundingMode.HALF_UP).doubleValue();
	}

	public Double getSalesCreditDocubox() {
		BigDecimal credit = BigDecimal.valueOf(0);
		BigDecimal precioAsegurado = BigDecimal.valueOf(0);
		BigDecimal nominal = BigDecimal.valueOf(1);
		if (Objects.nonNull(this.precio) && Objects.nonNull(this.precio.getTesoreria())
				&& Objects.nonNull(this.getPrecio().getTesoreria().getCredit())) {
			credit = BigDecimal.valueOf(this.getPrecio().getTesoreria().getCredit());
		}
		if (Objects.nonNull(this.spot) && Objects.nonNull(this.spot.getPrecioAsegurado())
				&& Objects.nonNull(this.spot.getPrecioAsegurado().getAmount())) {
			precioAsegurado = BigDecimal.valueOf(this.spot.getPrecioAsegurado().getAmount());
		}
		if (Objects.nonNull(this.opcion1) && Objects.nonNull(this.opcion1.getNominal())
				&& Objects.nonNull(this.opcion1.getNominal().getAmount())) {
			nominal = BigDecimal.valueOf(this.opcion1.getNominal().getAmount());
		}
		return (credit).multiply(precioAsegurado).divide(nominal, 2, RoundingMode.HALF_UP).abs().doubleValue();
	}
	
}
