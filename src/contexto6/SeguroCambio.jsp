<%@ include file="../comun/IncludeTaglibs.jsp" %>
<%@ page import="com.isb.acelera.domain.TipoBarrera" %>

<stripes:layout-render name="../layout/Estandard.jsp" keyTitle="SeguroCambioFx">
	<stripes:layout-component name="html-head">
		<style type="text/css">
		*{margin:0; padding:0}
		#modalFixingBlock .datepicker{ z-index:99999 !important; }
		.reduceTexto {
		    font-size: 11px;
		    padding-top: 9px;
		    padding-bottom: 8px;
   		}
<c:if test="${actionBean.ocultoDivisasOpcion1CallPut && !actionBean.seguroCambioLiquidacionFija}">   		
   		.divDivisasOpcion1CallPut {
   			height: 50px;
   		}
   		.divDivisasOpcion1CallPut > div {
   			display: none;
   		}
</c:if>
<c:if test="${actionBean.ocultoDivisasOpcion1CallPut && actionBean.seguroCambioLiquidacionFija}">   		
   		.divDivisasOpcion1CallPut {
   			display:none;
   		}
</c:if>
<c:if test="${actionBean.ocultoStrike}">   		
   		#divStrikeOpcion1 {
   			display: none;
   		}
</c:if>
   		#panelDatosSeguro input[type="radio"] {
   			vertical-align: middle;
   		}
   		#panelDatosSeguro select[name*=currency] {
   			min-width: 70px;
   		}
   		#panelDatosSeguro select[name*=twoCurrencies] {
   			min-width: 102px;
   		}
   		#panelDatosSeguro .check-button {
   			background: white;
   			display: none;
   		}
   		#panelDatosSeguro label:after {
    		content: '';
		}
		#blockcheckRegeneracion label:after {
            content: '';
        }
   		#panelDatosSeguro label {
    		margin-bottom: 0;
		}
   		label:first-letter {
    		text-transform: uppercase;
		}
		.panel-body .btn.btn-default {
			margin-bottom: 0;
		}
		.input-group { width: 100%; }
		span.input-group-btn:empty + .form-control {
			margin-left:-1px;
		}
		#txtDivisa1, #txtDivisa2, #txtDivisa3, #txtDivisa4 {
			width: 50px;
		}
		#txtBarrera, #txtBarrera2, #cmbBarrera, #cmbBarrera2, .spanSameWidth {
			width: 60px;
		}
		.idAcelera {
			text-align: right;
		}
		.idAcelera > span {
			padding-left: 5px;
		}
		.input-group > .input-group-btn:empty {
			width: 0;
		}
		@media (min-width: 1600px) {
			input[name*="strike.amount"].form-control {
				width: 226px;
			}
		}
		.panel-body {
			padding-top: 5px;
			padding-bottom: 5px;
		}
		.countdown {
  			position: relative; 
  			color: lightblue; 
  			font-size: 16px; 
  			font-family:arial; 
  			display: none; 
  			width: 60px; 
  			margin: auto; 
  			text-align: center; 
  			background: #F5F5F5; 
  			border-radius: 10px;
  		}
		#jcountdown > span { font-size:22px; margin-left:10px; color:red; }
		.boxOK {
			background-color: green;
			height: 25px;
			text-align: center;
			border-radius: 3px;
			color: white;
			font-weight: bolder;
			border-color: green;
		}
		.boxOK.no {
			background-color: red;
			border-color: red;
		}		
		.boxOK::before {
			content: 'OK';
		}		
		.boxOK.no::before {
			content: 'KO';
		}
		.labelCliente {
			color: #80808f;
			background: whitesmoke;
    		font-weight: bold;
		}
		
		.camposUsuario{
			font-weight: 700;
			color: #808080;
			text-align:left;
		}

        .headerRenegociacion{
            text-align: center;
        }

		</style>
	</stripes:layout-component>
	<stripes:layout-component name="subheader">
		<stripes:layout-render name="../layout/Subcabecera.jsp" keyTitulo="FX" listaPestanyas="${actionBean.pestanyas}">
		</stripes:layout-render>
	</stripes:layout-component>
    <stripes:layout-component name="contents">
    <fmt:message key="seleccionarFecha" var="titleSeleccionarFecha"/>
    <stripes:messages/>
    <stripes:errors/>
	<div class="tab-content">
		<div class="tab-pane fade active in" id="tabSegurCambio">
		<stripes:form beanclass="${actionBean.nameBeanClass}" class="" id="fSeguro" method="post">
			<stripes:hidden name="botonVolver"/>
			<stripes:hidden name="seguroCambio.version"/>	
		<!-- Filtro de seleccion -->
		<div class="row pad-b-15 form-horizontal">
			<div class="panel panel-default" style="height:52px;" id="panelSeguroCambioFx">
				<div class="panel-heading">
					<div class="row" >
						<c:choose>
    					<c:when test="${actionBean.visibleComboTipoProducto}">						
							<div class="col-md-2">
								<stripes:label for="cotizar.producto"/>					
      						</div>	
      						<div class="col-md-2">
      							<stripes:select class="form-control" name="seguroCambio.producto.id" id="cmbProductosSelect">
      								<stripes:options-collection  collection="${actionBean.listaProductos}" label="descripcion" value="id" />
      							</stripes:select>	
      						</div>				
						</c:when>
						<c:otherwise>
							<div class="col-md-5">${actionBean.textoPanelHeading}</div>
							<div class="col-md-2" style="min-height: 2px;"><div class="countdown"><div id="jcountdown"></div></div></div>
							<div class="col-md-5 text-right idAcelera">
								    <stripes:hidden name="seguroCambio.propuestaId"/>
								    <stripes:hidden name="propuestaAsesoramiento.id"/>
								    <stripes:hidden name="propuestaAsesoramiento.clienteGCB"/>
								    <stripes:hidden name="propuestaAsesoramiento.fechaVigencia"/>
							<c:if test="${actionBean.visibleIdAcelera}">
								    <stripes:label for="seguroCambio.id"/><span>${actionBean.seguroCambio.id}</span>
								   	<c:if test="${not empty actionBean.seguroCambio.precio.tesoreria.idDeal}">
								   	&nbsp;/&nbsp;<stripes:label for="seguroCambio.idAbacus"/><span>${actionBean.seguroCambio.precio.tesoreria.idDeal}</span>
								   	</c:if>
								   	<c:if test="${not empty actionBean.propuestaAsesoramiento.id}">
								   	&nbsp;/&nbsp;<stripes:label for="seguroCambio.idPropuesta"/><span>${actionBean.propuestaAsesoramiento.id}</span>
								   	</c:if>
								    <stripes:hidden name="seguroCambio.id"/>
								    <stripes:hidden name="seguroCambio.precio.tesoreria.idDeal"/>
								    <stripes:hidden name="seguroCambio.precio.tesoreria.version"/>
							</c:if>
							</div>
						</c:otherwise>					
						</c:choose>
					</div>
					
				</div>
			</div>
	
			<div class="form-group">
				<stripes:label for="seguroCambio.tipoOperacion.id" class="control-label col-md-2"/>
				<div class="col-md-2">
					<sdyn:select id="cmbTipoOperacion" class="form-control" name="seguroCambio.tipoOperacion.id" required="required" disabled="${!actionBean.camposEditables}">
							<c:if test="${actionBean.listaTipoOperacion.size() > 1}">
								<stripes:option value="" selected="" disabled="disabled"><fmt:message key="SeleccioneOpcion"/></stripes:option>
							</c:if>
						<stripes:options-collection collection="${actionBean.listaTipoOperacion}" label="descripcion" value="id" />
					</sdyn:select>
				</div>
				<c:choose>
    			<c:when test="${actionBean.visibleFechaEjecucion}">
    			    
    				<stripes:label for="seguroCambio.fechaEjecucion" class="control-label col-md-2" style="display:none;"/>
					<div class="col-md-3" style="display:none;">
						<div class="input-group">
							<sdyn:text name="seguroCambio.fechaEjecucion" formatType="date" class="form-control maskDatepicker datepicker with-holidays" id="fchEjecucionExt" required="required" disabled="${!actionBean.camposEditables}"/>
							<div class="input-group-btn">
								<button type="button" class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}" ${actionBean.camposEditables? '':'disabled="disabled"'}><em class="glyphicon glyphicon-calendar"></em></button>													
							</div>
						</div>
					</div>
					
				</c:when>
				<c:otherwise>
					<stripes:label for="seguroCambio.tipoBarrera.id" class="control-label col-md-2"/>
					<div class="col-md-2">
						<sdyn:select  id="cmbTipoBarrera" class="form-control" name="seguroCambio.tipoBarrera.id" required="required" disabled="${!actionBean.camposEditables}">
							<c:if test="${actionBean.listaBarrera.size() > 1}">
								<stripes:option value="" selected="" disabled="disabled"><fmt:message key="SeleccioneOpcion"/></stripes:option>
							</c:if>
							<stripes:options-collection collection="${actionBean.listaBarrera}" label="descripcion" value="id" />
						</sdyn:select>
					</div>
				</c:otherwise>
				</c:choose>

                <c:if test="${actionBean.tipoProducto.toString() == 'SL'}">
                    <div class="col-md-2" id="blockcheckRegeneracion" style="text-align:right">
                        <label for="seguroCambio.renegociacion" class="checkbox-inline">
                            <stripes:checkbox id="checkRegeneracion"  name="seguroCambio.renegociacion" disabled="${!actionBean.camposEditables}"/>
                            <fmt:message key="seguroCambio.renegociacion"/>
                        </label>
                    </div>
                </c:if>

				<%--
				<c:if test="${actionBean.seguroCambio.estado!=null}">
				<div class="col-md-4 pull-right">
					<stripes:label for="Estado" class="control-label col-md-5"/>
					<div class="col-md-7">
						<c:if test="${actionBean.seguroCambio.estado.descripcion!=''}">
							<sdyn:text name="seguroCambio.estado.descripcion" class="form-control" disabled="disabled"/>
						</c:if>
						<c:if test="${actionBean.seguroCambio.estado.descripcion=='' && actionBean.seguroCambio.estado.id!=null}">
							<sdyn:text name="seguroCambio.estado.id" class="form-control" disabled="disabled"/>
						</c:if>
					</div>
				</div>
				</c:if>
				--%>
				</div>

			</div>
			<!-- Tabla de datos -->
<c:set var="showPanelDatos" value="${!empty actionBean.seguroCambio.cliente || !empty actionBean.seguroCambio.tipoOperacion}" scope="request"/>
			
			<div id="panelDatosSeguro" class="row pad-b-12" style="display: ${showPanelDatos? 'block':'none'};">			
				<div class="panel panel-default">
				<c:if test="${!actionBean.ocultarDatosCliente}">
					<div class="panel-heading"><fmt:message key="segCamLiq.datosCliente"/></div>
					<!-- Panel datos de cliente -->
					<div class="panel-body">
					    <div class="form-group form-group-sm col-md-3 separador">
					    	<fmt:message key="Asterisco"/><fmt:message key="segCamLiq.buscadorCliente" var="buscadorCliente"/>
					    	<stripes:label for="seguroCambio.cliente.codigoJ"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.cliente.codigoJ" class="form-control maskNumeroPersona" placeholder="F-999999999" disabled="disabled"/>
								<span class="input-group-btn">
									<button id="btnBusquedaCliente" class="btn btn-default" type="button" title="${buscadorCliente}" ${actionBean.camposEditables? '':'disabled="disabled"'}><em class="glyphicon glyphicon-search"></em></button>
								</span>
							</div>
							<div id="ddNameCliente" ${actionBean.ocultoNombreCliente? 'style="display: none"':''}>
								<sdyn:text name="seguroCambio.cliente.nombre" disabled="disabled" class="form-control" style="width: 100%"/>
								<div class="input-group input-group-sm">
								  <span class="input-group-addon form-control labelCliente" style="text-align: left;"><fmt:message key="segCamLiq.contratoBasico"/>:</span>
								  <span id="clienteValidContract" class="input-group-addon boxOK ${actionBean.clienteValidContract? '':'no'}"></span>
								</div>	
								
								<stripes:layout-render name="../layout/CartaClasificacion.jsp" 
								clasesCSS = "input-group input-group-sm" 
								hidden="false" 
								datosCartaClasificacion="${actionBean.cartaClasificacionClienteAsJSON}"  
								isSpot="false" 
								clasesLabelDescripcion="input-group-addon form-control labelCliente" 			
								clasesDescripcion="input-group-addon form-control"
								styleLabelDescripcion="text-align:left;width: 45%" 
								styleDescripcion="width: 55%"
								isDescripcionAsInput= "true"/>
								
															
								<div class="input-group input-group-sm">
								  <span class="input-group-addon form-control labelCliente" style="text-align: left; width: 45%"><fmt:message key="segCamLiq.lei"/>:</span>
								  <sdyn:text name="seguroCambio.cliente.lei" class="form-control" disabled="disabled" style="width: 55%"/>
								</div>
								<div class="input-group input-group-sm">
								  <span class="input-group-addon form-control labelCliente" style="text-align: left; width: 45%"><fmt:message key="segCamLiq.clasificacionMifid"/>:</span>
								  <sdyn:text name="seguroCambio.cliente.clasificacionMIFID.descripcion" class="form-control" disabled="disabled" style="width: 55%"/>
								</div>
								<div class="input-group input-group-sm">
								  <span class="input-group-addon form-control labelCliente" style="text-align: left;"><fmt:message key="segCamLiq.validezLEI"/>:</span>
								  <span id="clienteVigenciaLEI" class="input-group-addon boxOK ${actionBean.leiVigenteEnPantalla? '':'no'}"></span>
								</div>
							</div>
							<stripes:hidden id="txtFuenteTest" name="seguroCambio.cliente.fuenteTest"/>	
							<stripes:hidden name="seguroCambio.cliente.documento"/>
							<stripes:hidden name="seguroCambio.cliente.contrapartida"/>	
							<stripes:hidden name="seguroCambio.cliente.infoRLA.origContrapartida"/>
							<stripes:hidden name="seguroCambio.cliente.segmento.id"/>	
							<stripes:hidden name="seguroCambio.cliente.domicilio"/>	
							<stripes:hidden name="seguroCambio.cliente.infoRLA.validContract"/>
							<stripes:hidden name="seguroCambio.cliente.infoRLA.validLEI"/>
							<stripes:hidden name="seguroCambio.cliente.leiVigente"/>
							<stripes:hidden name="seguroCambio.cliente.infoRLA.validNationalID"/>
							<stripes:hidden name="seguroCambio.cliente.infoRLA.dfa.action"/>
							<stripes:hidden name="seguroCambio.cliente.infoRLA.cartasClasMifi.indFirmado"/>
							<stripes:hidden name="seguroCambio.cliente.infoRLA.cartasClasMifi.tipoFirma"/>			
							<stripes:hidden name="seguroCambio.cliente.clasificacionMIFID.id"/>
						</div>
					    <div class="form-group form-group-sm col-md-2 separador">
					        <c:if test="${not actionBean.botonRFQ}">
					    	  <fmt:message key="Asterisco"/>
					    	</c:if>
					    	<fmt:message key="seguroCambio.gestor" var="placeholder"/>
					    	<stripes:label for="seguroCambio.gestor"/>
					    	<sdyn:text name="seguroCambio.gestor" class="form-control" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
					    </div>						
					    <div class="form-group form-group-sm col-md-2 separador">
					    	<fmt:message key="Asterisco"/><fmt:message key="seguroCambio.oficina" var="placeholder"/>
					    	<stripes:label for="seguroCambio.oficina"/>
					    	<sdyn:text name="seguroCambio.oficina" class="form-control maskOficina" placeholder="${placeholder}"  disabled="${!actionBean.camposEditables}"/>
					    </div>
					    <div class="form-group form-group-sm col-md-2 separador">
					    	<fmt:message key="seguroCambio.ccc" var="placeholder"/>
					    	<stripes:label for="seguroCambio.ccc"/>
					    	<div class="input-group input-group-sm">
					    	    <c:set var="txtDisabledCuenta" value="${actionBean.disabledBotonBusquedaCCC or !actionBean.camposEditables? 'disabled':''}" scope="request"/>
					    		<sdyn:text id="txtCuentaLocal" name="seguroCambio.ccc" class="form-control maskCuenta" placeholder="${placeholder}"  disabled="${txtDisabledCuenta}"/>
						    	<span class="input-group-btn">
									<button id="btnBusquedaCuentas"  class="btn btn-default" type="button" title="${seguroCambio.buscadorCuentas}"  ${txtDisabledCuenta}><em class="glyphicon glyphicon-search"></em></button>													
								</span>
					    	</div>	
					    	<stripes:hidden name="seguroCambio.cccPartenon"/>	
							<stripes:hidden name="seguroCambio.fairValueScope"/>	
							<stripes:hidden name="seguroCambio.precio.tesoreria.addonAmount"/>
							<stripes:hidden name="seguroCambio.precio.tesoreria.midPrice"/>	 	   	
					    </div>						
					    <div class="form-group form-group-sm col-md-3">
					    	<fmt:message key="segCamLiq.buscadorPropuesta" var="buscadorPropuesta"/>
					    	<fmt:message key="Asterisco"/><fmt:message key="seguroCambio.propuesta" var="placeholder"/>
					    	<stripes:label for="seguroCambio.propuesta"/>
							<div class="input-group input-group-sm">
							<c:set var="txtDisabledPropuesta" value="${actionBean.disabledBotonBusquedaPropuesta or !actionBean.camposEditables? 'disabled':''}" scope="request"/>
								<sdyn:text name="seguroCambio.propuesta" class="form-control maskPropuesta" placeholder="${placeholder}" disabled="${txtDisabledPropuesta}"/>
								<span class="input-group-btn">
									<button id="btnBusquedaPropuesta" ${actionBean.disabledBotonBusquedaPropuesta or !actionBean.camposEditables? 'disabled="disabled"':''} class="btn btn-default" type="button" title="${buscadorPropuesta}"><em class="glyphicon glyphicon-search"></em></button>
								</span>
							</div>
						</div>
					</div>
				</c:if>

                <c:if test="${actionBean.tipoProducto.toString() == 'SL'}">
                    <div class="panel-heading" id="renegociacionHeader">
                        <div class="col-md-2"><fmt:message key="seguroCambio.renegociacion"/></div>
                    </div>
                    <!-- Panel datos Renegociacion -->
                    <div class="panel-body" id="renegociacionBody" name ="renegociacionBody">
                        <div class="col-md-12" style="margin-bottom:10px">
                            <c:set var="renegociaciones" value="${actionBean.seguroCambio.renegociaciones}"/>
                            <table class="table table-striped table-hover " id="tablaFix" aria-describedby="">
                                <thead>
                                    <tr>
                                        <th scope="col"></th>
                                        <th scope="col" class="headerRenegociacion"><fmt:message key="seguroCambio.renegociacion.referencia"/></th>
                                        <th scope="col" class="headerRenegociacion"><fmt:message key="seguroCambio.renegociacion.tipoCancelacion"/></th>
                                        <th scope="col" class="headerRenegociacion"><fmt:message key="seguroCambio.renegociacion.nominal"/></th>
                                        <th scope="col" class="headerRenegociacion"><fmt:message key="seguroCambio.renegociacion.fechaContratacion"/></th>
                                        <th scope="col" class="headerRenegociacion"><fmt:message key="seguroCambio.renegociacion.vencimiento"/></th>
                                        <th scope="col" class="headerRenegociacion"><fmt:message key="seguroCambio.renegociacion.valorMercado"/></th>
                                        <th scope="col" class="headerRenegociacion"><fmt:message key="seguroCambio.renegociacion.liquidacionCliente"/></th>
                                    </tr>
                                </thead>
                                <tbody id="bodyRenegociacion">
                                    <c:forEach var="renegociacion" items="${actionBean.seguroCambio.renegociaciones}" varStatus="loop">
                                        <tr id="renegociacion${loop.index}">
                                            <td>
                                                <i style="width:10px;margin-bottom:15px;" onclick="borrarFila(${loop.index});" class="fa fa-minus-square fa-lg" aria-hidden="true"></i>
                                            </td>
                                            <td class="centrado">
                                                <input type="hidden" name="seguroCambio.renegociaciones.${loop.index}.id" value="${renegociacion.id}"/>
                                                <input type="hidden" name="seguroCambio.renegociaciones.${loop.index}.delete" id="delete${loop.index}" value="${renegociacion.delete}"/>
                                                <input type="hidden" name="seguroCambio.renegociaciones.${loop.index}.idOperacion" value="${actionBean.seguroCambio.id}"/>
                                                <input type="hidden" name="seguroCambio.renegociaciones.${loop.index}.orden" value="${loop.index}"/>
                                                <sdyn:text id="referencia${loop.index}" class="form-control" name="seguroCambio.renegociaciones.${loop.index}.referencia"/>
                                            </td>
                                            <td class="centrado">
                                                <sdyn:select class="form-control" id="tipoCancelacion${loop.index}.tipoCancelacion" name="seguroCambio.renegociaciones.${loop.index}.tipoCancelacion" >
                                                    <stripes:option value=""></stripes:option>
                                                    <stripes:options-collection collection="${actionBean.listaTipoCancelacion}" label="descripcion" value="id" />
                                                </sdyn:select>
                                            </td>
                                            <td class="centrado">
                                                <div class="input-group">
                                                    <sdyn:text id="nominal${loop.index}" class="form-control maskImporte" formatPattern="decimal" name="seguroCambio.renegociaciones.${loop.index}.nominal" style="width:50%"/>
                                                    <sdyn:select class="form-control" name="seguroCambio.renegociaciones.${loop.index}.divisaNominal" disabled="${!actionBean.camposEditables}" style="width:50%">
                                                        <stripes:option value=""></stripes:option>
                                                        <stripes:options-collection collection="${actionBean.listaDivisas}" label="descripcion" value="id" />
                                                    </sdyn:select>
                                                </div>
                                            </td>
                                            <td class="centrado">
                                                <div class="input-group">
                                                    <sdyn:text id="fechaContratacion${loop.index}" name="seguroCambio.renegociaciones.${loop.index}.fechaContratacion" formatType="date" class="form-control maskDatepicker datepicker with-holidays"/>
                                                    <div class="input-group-btn">
                                                        <button type="button" class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}"><em class="glyphicon glyphicon-calendar"></em></button>
                                                    </div>
                                                </div>
                                            </td>
                                            <td class="centrado">
                                                <div class="input-group">
                                                    <sdyn:text id="vencimiento${loop.index}" name="seguroCambio.renegociaciones.${loop.index}.fechaVencimiento" formatType="date" class="form-control maskDatepicker datepicker with-holidays"/>
                                                    <div class="input-group-btn">
                                                        <button type="button" class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}"><em class="glyphicon glyphicon-calendar"></em></button>
                                                    </div>
                                                </div>
                                            </td>
                                            <td class="centrado">
                                                <sdyn:text id="valorMercado${loop.index}" class="form-control maskImporte" name="seguroCambio.renegociaciones.${loop.index}.valorMercado"/>
                                            </td>
                                            <td class="centrado">
                                                <div class="input-group">
                                                    <sdyn:text id="liqCliente${loop.index}" class="form-control maskImporte" name="seguroCambio.renegociaciones.${loop.index}.liquidacionCliente" style="width:50%"/>
                                                    <sdyn:select class="form-control" name="seguroCambio.renegociaciones.${loop.index}.divisaLiquidacion" disabled="${!actionBean.camposEditables}" style="width:50%">
                                                        <stripes:option value=""></stripes:option>
                                                        <stripes:options-collection collection="${actionBean.listaDivisas}" label="descripcion" value="id" />
                                                    </sdyn:select>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <tr>
                                        <td colspan="12">
                                            <i style="margin-bottom:15px;border-bottom-style: none;" id="addRenegotiation" class="fa fa-plus-square fa-lg" aria-hidden="true"></i>
                                            <button name="nuevaRenegociacion" id="btnnuevaRenegociacion" type="submit" style="display:none"/>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="form-inline">
                            <div class="col-md-offset-3 col-md-3">
                                <div class="input-group">
                                    <stripes:label  for="valorMercadoTotal" class="input-group-addon labelCliente" name="seguroCambio.renegociacion.valorMercadoTotal" style="width:160px"/>
                                    <div>
                                        <sdyn:text id="valorMercadoTotal" class="form-control" name="seguroCambio.precio.tesoreria.valorMercadoTotal" disabled="disabled"/>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3 col-md-offset-1">
                                <div class="input-group">
                                    <stripes:label for="valorMercadoNeto" class="input-group-addon labelCliente" name="seguroCambio.renegociacion.margenNeto" style="width:160px"/>
                                    <div>
                                        <sdyn:text id="valorMercadoNeto" formatPattern="decimal" class="form-control maskImporteCerebro" name="seguroCambio.precio.tesoreria.margenNeto" disabled="${!actionBean.botonContratar}" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>
					<div id="panelActaMifidDepoSwap">
						<div class="panel-default">
							<div class="panel-heading">
								<fmt:message key="actaMifid.actaMifid"/>
							</div>
							<div class="panel-body">
								<!-- Grabación llamada -->
								<dl class="dl-horizontal col-md-4 col-lg-2">
									<fmt:message key="actaMifid.grabarLlamada" var="placeholder"/>
									<stripes:hidden name="seguroCambio.grabarLlamadaMifid"/>
									<fmt:message key="radioButton.NO" var="NO"/>
									<fmt:message key="radioButton.SI" var="SI"/>
									<dt><label> ${placeholder}</label></dt>
									<dd>
											<span class="labelRadioDinamica"><stripes:radio
													name="seguroCambio.grabarLlamadaMifidRadio" value="N"
													onchange="toggleActaMiFIDField();"/> ${NO}</span>
									</dd>
									<dd>
											<span class="labelRadioDinamica"><stripes:radio
													name="seguroCambio.grabarLlamadaMifidRadio" value="S"
													onchange="toggleActaMiFIDField();"/> ${SI}</span>
									</dd>
								</dl>
								<!-- Acta MiFID -->
								<dl class="dl-horizontal col-md-4 col-lg-3">
									<dt>
										<label for="actaMifid">
											<fmt:message key="actaMifid.actaMifid"/>
											<i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" title="Copiar y pegar el ID del Acta (esquina inferior izquierda). No olvides confirmar la entrega del Acta">
											</i>
										</label>
									</dt>
									<dd>
										<sdyn:text id="actaMifid" name="seguroCambio.actaMifid"
										disabled="disabled" class="input-sm form-control"
										pattern="[A-Za-z0-9]{17}"
										title="Debe contener exactamente 17 caracteres alfanumericos sin caracteres especiales"/>
                                        <small class="text-muted">Ej ID N021936635JKKL001</small>
									</dd>
								</dl>
							</div>
						</div>
					</div>
                <div class="panel-heading">
                    <div class="col-md-2"><fmt:message key="segCamLiq.forward"/></div>
                    <div class="col-md-10" id="txtCV0"></div>
                </div>
                <!-- Panel datos spot -->
                <div class="panel-body">
                    <div class="form-group form-group-sm col-md-3 separador">
                        <fmt:message key="Asterisco"/><fmt:message key="segCamLiq.nominal" var="placeholder"/>
                        <stripes:label for="seguroCambio.spot.nominal.amount" name="segCamLiq.nominal"/>
                        <div class="input-group input-group-sm">
                            <sdyn:text name="seguroCambio.spot.nominal.amount" formatPattern="decimal" class="form-control" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
                            <span class="input-group-btn"></span>
                            <sdyn:select class="form-control" name="seguroCambio.spot.nominal.currency" disabled="${!actionBean.camposEditables}">
                                <stripes:option value=""></stripes:option>
                                <stripes:options-collection collection="${actionBean.listaDivisas}" label="descripcion" value="id" />
                            </sdyn:select>
                        </div>
                    </div>
                    <div class="form-group form-group-sm col-md-3 separador">
                        <fmt:message key="Asterisco"/><fmt:message key="segCamLiq.precioAsegurado" var="placeholder"/>
                        <stripes:label for="seguroCambio.spot.precioAsegurado.amount" name="segCamLiq.precioAsegurado"/>
                        <div class="input-group input-group-sm">
                            <sdyn:text name="seguroCambio.spot.precioAsegurado.amount" formatPattern="decimal" class="form-control maskImporteCerebro" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
                            <span class="input-group-btn"></span>
                            <sdyn:select class="form-control holidays-check-trigger" name="seguroCambio.spot.precioAsegurado.twoCurrencies" disabled="${!actionBean.camposEditables}">
                                <stripes:option value=""></stripes:option>
                                <stripes:options-collection collection="${actionBean.listaParesDivisas}" label="codigoDivisa" value="codigoDivisa" />
                            </sdyn:select>
                        </div>
                    </div>
                    <div class="form-group form-group-sm col-md-3">
                        <fmt:message key="Asterisco"/> <stripes:label for="seguroCambio.spot.entrega" name="segCamLiq.entrega"/>
                        <div class="input-group input-group-sm">
                            <sdyn:text name="seguroCambio.spot.plazoEntrega" class="form-control maskPlazo" placeholder="9d, 9w, 9m, 9y" disabled="${!actionBean.camposEditables}" />
                            <span class="input-group-btn"></span>
                            <sdyn:text name="seguroCambio.spot.entrega" formatType="date" class="form-control maskDatepicker datepicker with-holidays" disabled="${!actionBean.camposEditables}"/>
                            <div class="input-group-btn">
                                <button type="button" class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}" ${actionBean.camposEditables? '':'disabled="disabled"'}><em class="glyphicon glyphicon-calendar"></em></button>
                            </div>
                        </div>
                    </div>
                    <div class="form-group form-group-sm col-md-3">
						<label for="seguroCambio.destino.id"><fmt:message key="Asterisco"/> <fmt:message key="acumulador.destino"/></label>
						<div class="input-group input-group-sm">
							<sdyn:select class="form-control" name="seguroCambio.destino.id" disabled="${!actionBean.camposEditables}">
                                <stripes:option value=""></stripes:option>
                                <stripes:options-collection collection="${actionBean.listaDestino}" label="descripcion" value="id" />
                            </sdyn:select>
						</div>						
					</div>
                    <div class="form-group form-group-sm col-md-1">
                    </div>
                    <c:if test="${not empty actionBean.seguroCambio.precio.tesoreria.spot}">
                    <div class="form-group form-group-sm col-md-1 separador">
                        <stripes:label for="seguroCambio.spot"/>
                        <sdyn:text name="seguroCambio.precio.tesoreria.spot" class="form-control" disabled="disabled" />
                    </div>
                    </c:if>
                    <c:if test="${not empty actionBean.seguroCambio.precio.tesoreria.puntos}">
                    <div class="form-group form-group-sm col-md-1">
                        <stripes:label for="seguroCambio.puntos"/>
                        <sdyn:text name="seguroCambio.precio.tesoreria.puntos" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled" />
                    </div>
                    </c:if>
                </div>
					<div class="panel-heading">
						<div class="col-md-2">${actionBean.textoOptionFx}</div>
						<div class="col-md-1" id="txtCV1"></div>
						<div class="col-md-9" id="opcionFx"></div>
					</div>				
					<!-- Panel datos fx option (1a. linea) -->
					<div class="panel-body">
					<c:choose>
					 <c:when test="${actionBean.visibleTouch}">
					  <div class="form-group form-group-sm col-md-3 separador">
							<fmt:message key="Asterisco"/><fmt:message key="segCamLiq.liqFij" var="placeholder"/>
							<stripes:label for="seguroCambio.opcion1.nominal.amount" name="segCamLiq.liqFij"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion1.nominal.amount" formatPattern="decimal" class="form-control" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
								<span class="input-group-btn"></span>
								<sdyn:select class="form-control" name="seguroCambio.opcion1.nominal.currency" style="margin-left:-1px;" disabled="${!actionBean.camposEditables}">
									<stripes:option value=""></stripes:option>
									<stripes:options-collection collection="${actionBean.listaDivisas}" label="descripcion" value="id" />
								</sdyn:select>
							</div>      
						</div>
						</c:when>
						<c:otherwise>
						<div class="form-group form-group-sm col-md-3 separador">
							<fmt:message key="Asterisco"/><fmt:message key="segCamLiq.nominal" var="placeholder"/>
							<stripes:label for="seguroCambio.opcion1.nominal.amount" name="segCamLiq.nominal"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion1.nominal.amount" formatPattern="decimal" class="form-control maskImporte" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
								<span class="input-group-btn"></span>
								<sdyn:select class="form-control" name="seguroCambio.opcion1.nominal.currency" style="margin-left:-1px;" disabled="${!actionBean.camposEditables}">
									<stripes:option value=""></stripes:option>
									<stripes:options-collection collection="${actionBean.listaDivisas}" label="descripcion" value="id" />
								</sdyn:select>
							</div>      
						</div>
						</c:otherwise>
						</c:choose>
						<div class="form-group form-group-sm col-md-3 separador" id="divStrikeOpcion1">
							<div>
								<fmt:message key="Asterisco"/><fmt:message key="segCamLiq.strike" var="placeholder"/>
								<stripes:label for="seguroCambio.opcion1.strike.amount" name="segCamLiq.strike"/>
								<div class="input-group input-group-sm">
									<sdyn:text name="seguroCambio.opcion1.strike.amount" formatPattern="decimal" class="form-control maskImporteCerebro" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
									<span class="input-group-btn"></span>
									<sdyn:text class="form-control" name="seguroCambio.opcion1.strike.twoCurrencies" disabled="disabled" style="margin-left:-1px; text-indent: 4px"/>								
								</div>
							</div>
						</div>
						<c:if test="${actionBean.visibleTouch}">
						<div class="form-group form-group-sm col-md-3 separador">
							<fmt:message key="segCamLiq.strike2" var="placeholder"/>
							<stripes:label for="seguroCambio.opcion2.strike.amount" name="segCamLiq.strike2"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion2.strike.amount" formatPattern="decimal" class="form-control maskImporteCerebro" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
								<span class="input-group-btn"></span>
								<sdyn:text class="form-control" name="seguroCambio.opcion2.strike.twoCurrencies" disabled="disabled" style="margin-left:-1px; text-indent: 4px"/>								
							</div>      
						</div>
						</c:if>
						<div class="form-group form-group-sm col-md-3 separador">
							<fmt:message key="Asterisco"/><fmt:message key="segCamLiq.barrera" var="placeholder"/>
							<stripes:label for="seguroCambio.opcion1.barrera" name="segCamLiq.barrera"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion1.barrera" formatPattern="decimal" class="form-control maskImporteCerebro" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
								<span class="input-group-addon" id="cmbBarrera"></span>
								<span class="input-group-addon" id="txtBarrera"></span>
							</div>
						</div>
						<div class="form-group form-group-sm col-md-3 divDivisasOpcion1CallPut">
							<div>
								<fmt:message key="segCamLiq.put" var="PUT" scope="request"/>						
								<stripes:label for="seguroCambio.opcion1.callPut" name="segCamLiq.divisa"/>
							    <div class="input-group input-group-sm">
							    	<stripes:text id="cmbDivisa1" name="seguroCambio.opcion1.strike.currencyDomestic" disabled="disabled" class="form-control"/>
							    	<span  id="txtDivisa1" class="input-group-addon"></span>
								    <span class="input-group-addon check-button">
								    	<sdyn:radio id="chkDivisa1" value="${PUT}" name="seguroCambio.opcion1.callPut" required="required" disabled="${!actionBean.camposEditables}"/>
								    </span>
							    </div>							
							</div>
						</div>
					</div>
					<!-- Panel datos fx option (2a. linea) -->
					<div class="panel-body">
						<div class="form-group form-group-sm col-md-3 separador">
							<fmt:message key="Asterisco"/> <stripes:label for="seguroCambio.opcion1.vencimiento" name="segCamLiq.vencimiento"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion1.plazoVencimiento" class="form-control maskPlazo" placeholder="9d, 9w, 9m, 9y" disabled="${!actionBean.camposEditables}" />
								<span class="input-group-btn"></span>
								<sdyn:text name="seguroCambio.opcion1.vencimiento" formatType="date" class="form-control maskDatepicker datepicker with-holidays" disabled="${!actionBean.camposEditables}"/>
								<div class="input-group-btn">
									<button type="button" class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}" ${actionBean.camposEditables? '':'disabled="disabled"'}><em class="glyphicon glyphicon-calendar"></em></button>													
								</div>
							</div>         
						</div>
						<div class="form-group form-group-sm col-md-3 separador">
							<fmt:message key="Asterisco"/> <stripes:label for="seguroCambio.opcion1.entrega" name="segCamLiq.entrega"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion1.plazoEntrega" class="form-control maskPlazo" placeholder="9d, 9w, 9m, 9y" disabled="${!actionBean.camposEditables}" />
								<span class="input-group-btn"></span>
								<sdyn:text name="seguroCambio.opcion1.entrega" formatType="date" class="form-control maskDatepicker datepicker with-holidays" disabled="${!actionBean.camposEditables}"/>
								<div class="input-group-btn">
									<button type="button" class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}" ${actionBean.camposEditables? '':'disabled="disabled"'}><em class="glyphicon glyphicon-calendar"></em></button>													
								</div>
							</div>         
						</div>
						<c:choose>
                            <c:when test="${actionBean.visibleBarrera2}">
                                <div class="form-group form-group-sm col-md-3 separador">
                                    <fmt:message key="segCamLiq.barrera" var="placeholder"/>
                                    <stripes:label for="seguroCambio.opcion1.barrera2"/>
                                    <div class="input-group input-group-sm">
                                        <sdyn:text name="seguroCambio.opcion1.barrera2" formatPattern="decimal" class="form-control maskPercent" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
                                        <span class="input-group-addon spanSameWidth"><fmt:message key="segCamLiq.out"/></span>
                                        <span class="input-group-addon spanSameWidth"><fmt:message key="segCamLiq.down"/></span>
                                    </div>
                                </div>
                            </c:when>
                            <c:when test="${actionBean.visibleFixingBlock}">
                                            <div class="form-group form-group-sm col-md-3 separador">
                                                <fmt:message key="segCamLiq.fixingBlock" var="placeholder"/>
                                                <stripes:label for="seguroCambio.opcion1.fixingBlock" name="segCamLiq.fixingBlock"/>
                                                <div class="input-group input-group-sm">
                                                    <sdyn:text name="seguroCambio.opcion1.fixingBlock" formatType="date" class="form-control maskDatepicker datepicker with-holidays" id="fchFixingBlock" disabled="${!actionBean.camposEditables}"/>
                                                    <span class="input-group-btn">
                                                        <button id="btnBusquedaBlock" class="btn btn-default" type="button" title="${placeholder}" data-toggle="modal" data-target="#modalFixingBlock" disabled="${!actionBean.camposEditables}"><em class="glyphicon glyphicon-search"></em></button>
                                                    </span>
                                                </div>
                                            </div>
                            </c:when>
                            <c:otherwise>
                        	</c:otherwise>
                        </c:choose>
                        <c:if test="${actionBean.visibleFixingReference}">
                            <div class="form-group form-group-sm col-md-3 separador">
                                <stripes:label for="seguroCambio.opcion1.fixingReference.id" name="segCamLiq.fixing"/>
                                <sdyn:select class="form-control" name="seguroCambio.opcion1.fixingReference.id" disabled="${!actionBean.camposEditables}">
                                    <stripes:option value=""></stripes:option>
                                    <stripes:options-collection collection="${actionBean.listaFixing}" label="descripcion" value="id" />
                                </sdyn:select>
                            </div>
                        </c:if>
						<div class="form-group form-group-sm col-md-3 divDivisasOpcion1CallPut">
							<div>
								<fmt:message key="segCamLiq.call" var="CALL" scope="request"/>						
								<br>
								<div class="input-group input-group-sm">
									<stripes:text id="cmbDivisa2" name="seguroCambio.opcion1.strike.currencyForeign" disabled="disabled" class="form-control"/>
									<span  id="txtDivisa2" class="input-group-addon"></span>
									<span class="input-group-addon check-button">
										<sdyn:radio id="chkDivisa2" value="${CALL}" name="seguroCambio.opcion1.callPut" required="required" disabled="${!actionBean.camposEditables}"/>
									</span>
								</div>        
							</div>
						</div>
						<c:if test="${actionBean.visibleFixingReferenceExtensible}">
                            <div class="form-group form-group-sm col-md-3 separador">
                                <stripes:label for="seguroCambio.opcion1.fixingReference.id" name="segCamLiq.fixing"/>
                                <sdyn:select class="form-control" name="seguroCambio.opcion1.fixingReference.id" disabled="${!actionBean.camposEditables}">
                                    <stripes:option value=""></stripes:option>
                                    <stripes:options-collection collection="${actionBean.listaFixing}" label="descripcion" value="id" />
                                </sdyn:select>
                            </div>
                        </c:if>
					</div>


<c:if test="${actionBean.visibleTouch}">

<c:set var="txtDisabledFechasBarrera" value="${actionBean.disabledFechasBarrera? 'disabled=\"disabled\"':''}" scope="request"/>
<c:set var="txtDisabledFechasBarrera2" value="${actionBean.disabledFechasBarrera? 'disabled':''}" scope="request"/>

					<!-- Panel datos fx option (3a. linea) -->
					<div class="panel-body">
						<div class="form-group form-group-sm col-md-3 separador">
							<stripes:label for="seguroCambio.opcion1.inicioBarrera"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion1.plazoInicioBarrera" class="form-control maskPlazo" placeholder="9d, 9w, 9m, 9y" disabled="${txtDisabledFechasBarrera2 and action.camposEditables}"/>
								<span class="input-group-btn"></span>
								<sdyn:text name="seguroCambio.opcion1.inicioBarrera" formatType="date" class="form-control maskDatepicker datepicker with-holidays" disabled="${txtDisabledFechasBarrera2 and action.camposEditables}"/>
								<div class="input-group-btn">
									<button type="button" ${txtDisabledFechasBarrera} class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}" ${actionBean.camposEditables? '':'disabled="disabled"'}><em class="glyphicon glyphicon-calendar"></em></button>													
								</div>
							</div>         
						</div>
						<div class="form-group form-group-sm col-md-3 separador">
							<stripes:label for="seguroCambio.opcion1.finBarrera"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion1.plazoFinBarrera" class="form-control maskPlazo" placeholder="9d, 9w, 9m, 9y" disabled="${txtDisabledFechasBarrera2 and action.camposEditables}"/>
								<span class="input-group-btn"></span>
								<sdyn:text name="seguroCambio.opcion1.finBarrera" formatType="date" class="form-control maskDatepicker datepicker with-holidays" disabled="${txtDisabledFechasBarrera2 and action.camposEditables}"/>
								<div class="input-group-btn">
									<button type="button" ${txtDisabledFechasBarrera} class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}" ${actionBean.camposEditables? '':'disabled="disabled"'}><em class="glyphicon glyphicon-calendar"></em></button>													
								</div>
							</div>         
						</div>
						<div class="form-group form-group-sm col-md-3 separador">
							<stripes:label for="seguroCambio.opcion1.frecuenciaBarrera.id"/>
							<sdyn:select class="form-control" name="seguroCambio.opcion1.frecuenciaBarrera.id" disabled="${!actionBean.camposEditables}">
								<stripes:options-collection collection="${actionBean.listaFrecuenciaBarrera}" label="descripcion" value="id" />
							</sdyn:select>         
						</div>
						<div class="form-group form-group-sm col-md-3">
							<stripes:label for="seguroCambio.opcion1.payOff.id"/>
							<sdyn:select class="form-control" name="seguroCambio.opcion1.payOff.id" disabled="${!actionBean.camposEditables}">
								<stripes:options-collection collection="${actionBean.listaPayOff}" label="descripcion" value="id" />
							</sdyn:select>         
						</div>
					</div>
</c:if>					

<c:if test="${actionBean.visibleOptionFx2}">
					<div class="panel-heading">
						<div class="col-md-2"><fmt:message key="segCamLiq.optionFx2"/></div>
						<div class="col-md-1" id="txtCV2">${seguroCambio.cv2}</div>
						<div class="col-md-9" id="opcionFx2"></div>
					</div>
					<!-- Panel datos fx option 2 (1a. linea) -->
					<div class="panel-body">
						<div class="form-group form-group-sm col-md-3 separador">
							<fmt:message key="segCamLiq.nominal" var="placeholder"/>
							<stripes:label for="seguroCambio.opcion2.nominal.amount" name="segCamLiq.nominal"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion2.nominal.amount" formatPattern="decimal" class="form-control" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
								<span class="input-group-btn"></span>
								<sdyn:select class="form-control" name="seguroCambio.opcion2.nominal.currency" style="margin-left:-1px;" disabled="${!actionBean.camposEditables}">
									<stripes:option value=""></stripes:option>
									<stripes:options-collection collection="${actionBean.listaDivisas}" label="descripcion" value="id" />
								</sdyn:select>
							</div>      
						</div>
						<div class="form-group form-group-sm col-md-3 separador">
							<fmt:message key="segCamLiq.strike" var="placeholder"/>
							<stripes:label for="seguroCambio.opcion2.strike.amount" name="segCamLiq.strike"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion2.strike.amount" formatPattern="decimal" class="form-control maskImporteCerebro" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
								<span class="input-group-btn"></span>
								<sdyn:text class="form-control" name="seguroCambio.opcion2.strike.twoCurrencies" disabled="disabled" style="margin-left:-1px; text-indent: 4px"/>								
							</div>      
						</div>
						<div class="form-group form-group-sm col-md-3 separador">
							<fmt:message key="segCamLiq.barrera" var="placeholder"/>
							<stripes:label for="seguroCambio.opcion2.barrera" name="segCamLiq.barrera"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion2.barrera" formatPattern="decimal" class="form-control maskImporteCerebro" placeholder="${placeholder}" disabled="${!actionBean.camposEditables}" />
								<span class="input-group-addon" id="cmbBarrera2"></span>
								<span class="input-group-addon" id="txtBarrera2"></span>
							</div>      
						</div>
						<div class="form-group form-group-sm col-md-3">
							<stripes:label for="seguroCambio.opcion2.callPut" name="segCamLiq.divisa"/>
						    <div class="input-group input-group-sm">
						    	<stripes:text id="cmbDivisa3" name="seguroCambio.opcion2.strike.currencyDomestic" disabled="disabled" class="form-control"/>
						    	<span  id="txtDivisa3" class="input-group-addon"></span>
							    <span class="input-group-addon check-button">
							    	<sdyn:radio id="chkDivisa3" value="${PUT}" name="seguroCambio.opcion2.callPut" required="required" disabled="${!actionBean.camposEditables}"/>
							    </span>
						    </div>							
						</div>
					</div>
					<!-- Panel datos fx option 2 (2a. linea) -->
					<div class="panel-body">
						<div class="form-group form-group-sm col-md-3 separador">
							<stripes:label for="seguroCambio.opcion2.vencimiento" name="segCamLiq.vencimiento"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion2.plazoVencimiento" class="form-control maskPlazo" placeholder="9d, 9w, 9m, 9y" disabled="${!actionBean.camposEditables}"/>
								<span class="input-group-btn"></span>
								<sdyn:text name="seguroCambio.opcion2.vencimiento" formatType="date" class="form-control maskDatepicker datepicker with-holidays" disabled="${!actionBean.camposEditables}"/>
								<div class="input-group-btn">
									<button type="button" class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}" ${actionBean.camposEditables? '':'disabled="disabled"'}><em class="glyphicon glyphicon-calendar"></em></button>													
								</div>
							</div>         
						</div>
						<div class="form-group form-group-sm col-md-3 separador">
							<stripes:label for="seguroCambio.opcion2.entrega" name="segCamLiq.entrega"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion2.plazoEntrega" class="form-control maskPlazo" placeholder="9d, 9w, 9m, 9y" disabled="${!actionBean.camposEditables}" />
								<span class="input-group-btn"></span>
								<sdyn:text name="seguroCambio.opcion2.entrega" formatType="date" class="form-control maskDatepicker datepicker with-holidays" disabled="${!actionBean.camposEditables}"/>
								<div class="input-group-btn">
									<button type="button" class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}"><em class="glyphicon glyphicon-calendar"></em></button>													
								</div>
							</div>         
						</div>

						<div class="form-group form-group-sm col-md-3 separador">
							<fmt:message key="segCamLiq.fixingBlock" var="placeholder"/>
							<stripes:label for="seguroCambio.opcion2.fixingBlock" name="segCamLiq.fixingBlock"/>
							<div class="input-group input-group-sm">
								<sdyn:text name="seguroCambio.opcion2.fixingBlock" formatType="date" class="form-control maskDatepicker datepicker with-holidays" id="fchFixingBlock2" disabled="${!actionBean.camposEditables}"/>
								<span class="input-group-btn">
									<button id="btnBusquedaBlock" class="btn btn-default" type="button" title="${placeholder}" data-toggle="modal" data-target="#modalFixingBlock" disabled="${!actionBean.camposEditables}"><em class="glyphicon glyphicon-search"></em></button>
								</span>
							</div>        
						</div>
						<div class="form-group form-group-sm col-md-3">
							<br>
							<div class="input-group input-group-sm">
								<stripes:text id="cmbDivisa4" name="seguroCambio.opcion2.strike.currencyForeign" disabled="disabled" class="form-control"/>
								<span  id="txtDivisa4" class="input-group-addon"></span>
								<span class="input-group-addon check-button">
									<sdyn:radio id="chkDivisa4" value="${CALL}" name="seguroCambio.opcion2.callPut" required="required" disabled="${!actionBean.camposEditables}"/>
								</span>
							</div>        
						</div>
						<div class="form-group form-group-sm col-md-3 separador">
                            <stripes:label for="seguroCambio.opcion2.fixingReference.id" name="segCamLiq.fixing"/>
                            <sdyn:select class="form-control" name="seguroCambio.opcion2.fixingReference.id" disabled="${!actionBean.camposEditables}">
                                <stripes:option value=""></stripes:option>
                                <stripes:options-collection collection="${actionBean.listaFixing}" label="descripcion" value="id" />
                            </sdyn:select>
                        </div>
					</div>
</c:if>
					
				</div>
			</div>
			
			<!-- Tabla de datos -->
<c:set var="showPrecios" value="${!empty actionBean.seguroCambio.precio.tesoreria.pinRisk}" scope="request"/>
			<div id="panelDatos" class="modal-footer" style="display: ${showPrecios? 'block':'none'};">
				<div class="row">
				    <c:choose>
				    <c:when test="${actionBean.notGestor}">
					<dl class="col-md-1">
						<dt class="centrado"><fmt:message key="segCamLiq.premium"/>:</dt>
					</dl>
					<dl class="col-md-1">
						<dt class="centrado"><fmt:message key="segCamLiq.pinRisk"/>:</dt>
					</dl>
					<dl class="col-md-1">
						<dt class="centrado"><fmt:message key="segCamLiq.vega"/>:</dt>
					</dl>
					<dl class="col-md-1">
						<dt class="centrado"><fmt:message key="segCamLiq.delta"/>:</dt>
					</dl>
					<dl class="col-md-1">
						<dt class="centrado"><fmt:message key="segCamLiq.cva"/>:</dt>
					</dl>
					<dl  ${actionBean.perfilGestorBasico? 'class="col-md-2"':'class="col-md-1"'}>
						<dt class="centrado"><fmt:message key="segCamLiq.credit"/>:</dt>
					</dl>
					<dl class="col-md-2">
						<dt class="centrado"><fmt:message key="segCamLiq.destinoOper"/>:</dt>
					</dl>
					<dl class="col-md-1" ${actionBean.perfilGestorBasico? 'style="display: none"':''}>
						<dt class="centrado"><fmt:message key="segCamLiq.margenPipos"/>:</dt>
					</dl>
					</c:when>
					<c:otherwise>
					  <dl class="col-md-9">
					  </dl>
					</c:otherwise>
					</c:choose>
					<dl class="col-md-2">
						<dt class="centrado"><fmt:message key="segCamLiq.beneficio"/>:</dt>
					</dl>
				</div>
<c:if test="${not empty actionBean.seguroCambio.precio.gestor.pinRisk}">
				<div class="row">
				

					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1">
						<sdyn:text name="seguroCambio.precio.gestor.premium" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1">
						<sdyn:text name="seguroCambio.precio.gestor.pinRisk" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1">
						<sdyn:text name="seguroCambio.precio.gestor.vega" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1">
						<sdyn:text name="seguroCambio.precio.gestor.delta" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1">
						<fmt:message key="segCamLiq.cva" var="placeholder"/>
						<sdyn:text name="seguroCambio.precio.gestor.cva" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} ${actionBean.perfilGestorBasico? 'class="col-md-2"':'class="col-md-1"'}>
						<sdyn:text name="seguroCambio.precio.gestor.credit" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-2">
						<sdyn:select class="form-control" name="seguroCambio.precio.gestor.destino.id" disabled="disabled">
							<stripes:options-collection collection="${actionBean.listaDestinoOperacion}" label="descripcion" value="id"/>
						</sdyn:select>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1" ${actionBean.perfilGestorBasico? 'style="display: none"':''}>
						<sdyn:text name="seguroCambio.precio.gestor.margenPipos" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="${!actionBean.camposEditables}"/>
					</dl>


					  <dl ${actionBean.notGestor? 'style="display: none"':''} class="col-md-9">
					  </dl>

					
					<dl class="col-md-1">
						<sdyn:text name="seguroCambio.precio.gestor.beneficio" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl class="col-md-1">
						<sdyn:text name="seguroCambio.precio.gestor.porcentaje" formatPattern="decimal" class="form-control maskPercent" disabled="disabled"/>
					</dl>
					<dl class="col-md-1">
						<dt><fmt:message key="segCamLiq.precioGestor"/></dt>
					</dl>
					<stripes:hidden name="seguroCambio.precio.gestor.idDeal"/>
					<stripes:hidden name="seguroCambio.solicitud.tmsp" formatType="datetime"/>
				</div>
</c:if>				
<c:if test="${not empty actionBean.seguroCambio.precio.tesoreria.pinRisk}">
				<div class="row">

					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1">
						<sdyn:text name="seguroCambio.precio.tesoreria.premium" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1">
						<sdyn:text name="seguroCambio.precio.tesoreria.pinRisk" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1">
						<sdyn:text name="seguroCambio.precio.tesoreria.vega" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1">
						<sdyn:text name="seguroCambio.precio.tesoreria.delta" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1">
						<sdyn:text name="seguroCambio.precio.tesoreria.cva" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="${!actionBean.camposEditables}"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''} ${actionBean.perfilGestorBasico? 'class="col-md-2"':'class="col-md-1"'}>
						<sdyn:text name="seguroCambio.precio.tesoreria.credit" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="${!actionBean.camposEditables}"/>
					</dl>
					<dl ${not actionBean.notGestor? 'style="display: none"':''}  class="col-md-2">
						<sdyn:select class="form-control" name="seguroCambio.precio.tesoreria.destino.id" disabled="${!actionBean.camposEditables}">
							<stripes:options-collection collection="${actionBean.listaDestinoOperacion}" label="descripcion" value="id" />
						</sdyn:select>
					</dl>
					
					<dl ${not actionBean.notGestor? 'style="display: none"':''} class="col-md-1" ${actionBean.perfilGestorBasico? 'style="display: none"':''}>
						<sdyn:text name="seguroCambio.precio.tesoreria.margenPipos" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="${!actionBean.camposEditables}"/>
					</dl>

                       <dl ${actionBean.notGestor? 'style="display: none"':''} class="col-md-9">
					   </dl>

					<dl class="col-md-1">
						<sdyn:text name="seguroCambio.precio.tesoreria.beneficio" formatPattern="decimal" class="form-control maskImporteCerebro" disabled="disabled"/>
					</dl>
					<dl class="col-md-1">
						<sdyn:text name="seguroCambio.precio.tesoreria.porcentaje" class="form-control maskPercent" disabled="disabled"/>
					</dl>
					<dl class="col-md-1">
						<dt><fmt:message key="segCamLiq.precioTesoreria"/></dt>
					</dl>
					
					<stripes:hidden name="seguroCambio.precio.tesoreria.idSentinel"/>
					<stripes:hidden name="seguroCambio.precio.tesoreria.puntos"/>
					<stripes:hidden name="seguroCambio.precio.tesoreria.spot"/>
					<stripes:hidden name="seguroCambio.solicitud.tmsp" formatType="datetime" formatPattern="dd/MM/yyyy HH:mm:ss"/>
				</div>
</c:if>				
			    <c:if test="${actionBean.botonContratar and actionBean.notGestor and actionBean.camposEditables}">
					<div class="col-md-2 pull-right">
						<stripes:submit name="validarSSCC" id="btnValidarPrecioEstructuras" class="btn btn-default btn-default-san btn-block"><fmt:message key="Validar"/></stripes:submit>
					</div>
                </c:if>
                <c:if test="${actionBean.seguroCambio.modif.usuario!=null}">
                <div class="form-group">
					<div class="col-md-4 camposUsuario">
					Usuario: ${actionBean.seguroCambio.modif.usuario}  /  Timestamp: ${actionBean.seguroCambio.modif.tmsp}
					</div>
				</div>
				</c:if>
			</div>
			
			<div class="modal-footer" id="footerFxEstructuras" style="display: none;">
				<div class="row">
<c:if test="${actionBean.botonVolver}">
					<div class="col-md-2">
						<stripes:link beanclass="${actionBean.nameBeanClass}" event="volver" id="btnVolver" class="btn btn-primary btn-primary-san btn-block"><fmt:message key="Volver"/></stripes:link>
					</div>
</c:if>
<c:if test="${actionBean.camposEditables}">
					<div class="col-md-2">
						<stripes:link beanclass="${actionBean.nameBeanClass}" event="anular" id="btnAnular" class="btn btn-primary btn-primary-san btn-block"><fmt:message key="Limpiar"/></stripes:link>
					</div>
</c:if>
<c:if test="${actionBean.botonEnviar and actionBean.camposEditables}">
					<div class="col-md-2 pull-right">
						<stripes:submit name="enviar" id="btnEnviar" class="btn btn-default btn-default-san btn-block"><fmt:message key="segCamLiq.enviar"/></stripes:submit>
					</div>
</c:if>
<c:if test="${actionBean.botonEnviarTesoreria and actionBean.camposEditables}">
					<div class="col-md-2 pull-right">
						<stripes:submit name="enviarTesoreria" id="btnEnviarTesoreria" class="btn btn-default btn-default-san btn-block"><fmt:message key="segCamLiq.enviarTesoreria"/></stripes:submit>
					</div>
</c:if>
<c:if test="${actionBean.botonContratar and actionBean.camposEditables}">
					<div class="col-md-2 pull-right">						
						<button type="button"  id="btnConfirmarPrecioEstructuras" class="btn btn-default btn-default-san btn-block"><fmt:message key="Contratar"/></button>
						<stripes:submit style="display: none;" name="contratar" id="btnConfirmarPrecioEstructurasSubmit" class="btn btn-default btn-default-san btn-block"></stripes:submit>						
					</div>				
</c:if>
<c:if test="${actionBean.botonDocumentacionContractual and actionBean.camposEditables}">
					<div class="col-md-2 pull-right">
						<stripes:submit name="generarDocumentoContractual" id="btnGenerarDocumentacionContractual" class="btn btn-default btn-default-san btn-block reduceTexto"><fmt:message key="segCamLiq.docContractual"/></stripes:submit>
					</div>					
</c:if>
<c:if test="${actionBean.botonDocumentacionPrecontractual and actionBean.camposEditables}">
					<div class="col-md-2 pull-right">
						<stripes:submit name="generarDocumentoPreContractual" id="btnGenerarDocumentacionPrecontractual" class="btn btn-default btn-default-san btn-block reduceTexto"><fmt:message key="segCamLiq.docPrecontractual"/></stripes:submit>
					</div>
</c:if>
<c:if test="${actionBean.botonGuardarPropuesta and actionBean.camposEditables}">
					<div class="col-md-2 pull-right">
						<stripes:submit name="guardarPropuesta" id="btnGuardarPropuesta" class="btn btn-default btn-default-san btn-block"><fmt:message key="segCamLiq.guardarPropuesta"/></stripes:submit>
					</div>
</c:if>
<c:if test="${actionBean.botonGenerarPropuesta and actionBean.camposEditables}">
					<div class="col-md-2 pull-right">
						<stripes:submit name="generarPropuesta" id="btnGenerarPropuesta" class="btn btn-default btn-default-san btn-block"><fmt:message key="segCamLiq.generarPropuesta"/></stripes:submit>
					</div>
</c:if>				
<c:if test="${actionBean.botonCotizar and !actionBean.perfilConsultaSoporte}">
					<div class="col-md-2 pull-right">
						<stripes:submit name="cotizar" id="btnCotizarEstructuras" class="btn btn-default btn-default-san btn-block"><fmt:message key="segCamLiq.cotizar"/></stripes:submit>
					</div>
</c:if>
<c:if test="${!actionBean.botonCotizar && actionBean.botonCotizarPrecio}">
					<div class="col-md-2 pull-right">
						<stripes:submit name="cotizarPrecio" id="btnCotizarEstructurasPrecio" class="btn btn-default btn-default-san btn-block"><fmt:message key="segCamLiq.cotizar"/></stripes:submit>
					</div>
</c:if>
<c:if test="${actionBean.botonRFQ and !actionBean.disabledFields and actionBean.camposEditables}">
					<div class="col-md-2 pull-right">
						<stripes:submit name="rfq" id="btnValidarEstructuras" class="btn btn-default btn-default-san btn-block"><fmt:message key="segCamLiq.rfq"/></stripes:submit>
					</div>
</c:if>

					<div class="col-md-2 pull-right" style="display: none">
						<stripes:submit name="getRfq" id="btnGetRfq" class="btn btn-default btn-default-san btn-block"></stripes:submit>
					</div>
				</div>
			</div>
			</stripes:form>
		</div>			

	</div>
	
		<!-- Modal Asesoramiento -->
				
	<div id="modalAsesoramiento" class="modal fade" tabindex="-1" role="dialog" >
		<div class="modal-dialog">

			<!-- Modal content-->				
			<div class="modal-content">
				<div class="modal-header">								
					<h4 class="modal-title">${actionBean.asesoramientoMifid.titulo}</h4>
				</div>
				<div class="modal-body">
					<p>${actionBean.asesoramientoMifid.descripcion}</p>														 
					</div>
				<div class="modal-footer">
					<div class="row">
						<div class="col-md-offset-6 col-md-3">
							<c:if test="${actionBean.asesoramientoMifid.cancelar}">										
								<input name="cancelar" id="btnCancelarAsesoramiento" type="submit" data-dismiss="modal" class="btn btn-default btn-default-san btn-block" value="Cancelar">			
							</c:if>
						</div>
						<c:if test="${not actionBean.asesoramientoMifid.bloqueanteContratacion}">
							<stripes:form id="Asesoramiento" class="form-horizontal" beanclass="${actionBean.nameBeanClass}">
							<div class="col-md-3">							 
								 <button type="button" id="btnAceptarAsesoramiento" class="btn btn-default btn-default-san btn-block"><fmt:message key="Aceptar"/></button>										 										  
							</div>
							</stripes:form>
					 	</c:if>
						<c:if test="${actionBean.asesoramientoMifid.bloqueanteContratacion}">												
							<div class="col-md-3">
								 <input name="Aceptar" id="btnAceptarInformacion" type="submit" data-dismiss="modal" class="btn btn-default btn-default-san btn-block" value="Aceptar">											 										 										  
							</div>										
						</c:if>
					</div>
				</div>
			</div>

		</div>
	</div>
	
	<!-- Modal Fixing Block -->
	<div id="modalFixingBlock" class="modal fade" tabindex="-1" role="dialog">
		<div class="modal-dialog" data-width="1200px">
			<!-- Modal content-->
			<div class="modal-content">
				<stripes:form beanclass="com.isb.acelera.web.actions.SeguroCambioFxActionBean" class="form-horizontal">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">×</button>
					<h4 class="modal-title"><fmt:message key="segCamLiq.fixingBlock"/></h4>
				</div>
				<div class="modal-body">
					<!-- Filtro -->
					<div class="modal-default">
						<div class="panel-heading" data-toggle="collapse" data-target="#panelBusqFixingBlock"><fmt:message key="segCamLiq.filtroBusqueda"/></div>
					</div>
					<div id="panelBusqFixingBlock" class="panel-collapse collapse in">
						<form id="frmFixingBlock" class="form-horizontal">
						<div class="form-group">
						    <stripes:label for="segCamLiq.fechDesde" class="control-label col-md-2"/>
							<div class="col-md-2">
								<div class="input-group">
								<sdyn:text name="filtroFixing.fechDesde" formatType="date" class="form-control maskDatepicker datepicker with-holidays" id="txtFchDesde" placeholder="dd/MM/yyyy"/>
								<div class="input-group-btn">
									<button id="fechDesde" type="button" class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}"><em class="glyphicon glyphicon-calendar"></em></button>													
								</div>
								</div>
							</div>
							<stripes:label for="segCamLiq.fechHasta" class="control-label col-md-2"/>
							<div class="col-md-2">
								<div class="input-group">
								<sdyn:text name="filtroFixing.fechHasta" formatType="date" class="form-control maskDatepicker datepicker with-holidays" id="txtFchHasta" placeholder="dd/MM/yyyy"/>
								<div class="input-group-btn">
									<button id="fechHasta" type="button" class="btn btn-default btnDatepicker" title="${titleSeleccionarFecha}"><em class="glyphicon glyphicon-calendar"></em></button>													
								</div>
								</div>
							</div>
							<stripes:label for="segCamLiq.numberDates" class="control-label col-md-2"/>
							<div class="col-md-2">
								<sdyn:text name="filtroFixing.numberDates" maxlength="4" formatType="text" class="form-control" disabled="disabled" />
							</div>
							<stripes:label for="segCamLiq.frecuency" class="control-label col-md-2"/>
							<div class="col-md-2">
								<select id="cmbFrecuency" name="filtroFixing.frecuency" class="form-control control-label-multiselect">
									  <option value="">Todos</option> 
									  <option value="1" selected >Daily (business)</option>
								</select>
							</div>
							<stripes:label for="segCamLiq.business" class="control-label col-md-2"/>
							<div class="col-md-2">
								<select id="cmbBusinessDay" name="filtroFixing.business" class="form-control control-label-multiselect">
									<option value="">Todos</option> 
									<option value="EUR" selected>EUR</option>								
								</select>
							</div>
						</div>
						</form>
						<div class="modal-footer">
							<div class="row">
								<div class="col-md-offset-8 col-md-2">
									<button type="button" id="btnLimpiarFixingBlock" class="btn btn-primary btn-primary-san btn-block"><fmt:message key="Limpiar"/></button>
								</div>
								<div class="col-md-2">
									<button type="button" id="btnBuscarFixingBlock" class="btn btn-default btn-default-san btn-block"><fmt:message key="Buscar"/></button>
								</div>
							</div>
						</div>
					</div>	
				</div>
				</stripes:form>

				<div class="modal-body">
				<!-- Resultado busqueda -->
				<div id="panelResFixingBlock" class="panel panel-default" style="display: none;">
						<div class="panel-heading"><fmt:message key="segCamLiq.dates"/></div>
						<div class="panel-body">
							<div class="row">
								<div class="col-md-12">
									<div class="panel-body">
										<table class="table table-striped table-hover" id="tablaFix" aria-describedby="">
										<thead>
											<tr>
												<th scope="col" data-field="date"><fmt:message key="segCamLiq.date"/></th>
												<th scope="col" data-field="weight"><fmt:message key="segCamLiq.weight"/></th>
												<th scope="col" data-field="value"><fmt:message key="segCamLiq.value"/></th>
											</tr>
										</thead>
										<tbody>
										</tbody>
									</table>
									<c:if test="1 > 1}">		
										<nav id="paginadorFixingBox" class="pull-right paginador" data-currentpage="1" data-numpages="1" data-url="#"></nav>
									</c:if>											
									</div>
								</div>
							</div>	
						</div>
					</div>
				</div>

				<div class="modal-footer">
					<div class="row">
						<div class="col-md-offset-10 col-md-2">
							<button type="button" id="btnVolverFixingBlock0" data-dismiss="modal" class="btn btn-primary btn-primary-san btn-block"><fmt:message key="Volver"/></button>
						</div>
					</div>
				</div>
			</div>

		</div>
	</div>

	</stripes:layout-component>  
    <stripes:layout-component name="body-script">
    	<script type="text/javascript" src="../webjars/jquery.countdown/2.2.0/dist/jquery.countdown.min.js"></script> 	
    	
		<stripes:layout-render name="../layout/AutocompleteUsuarioLdap.jsp" input="[name=\"seguroCambio.gestor\"]" container=".tab-content" min="3"/>
		<stripes:layout-render name="../layout/CalcularFechaPlazo.jsp" inputPlazo="[name=\"seguroCambio.opcion1.plazoVencimiento\"]" inputFecha="[name=\"seguroCambio.opcion1.vencimiento\"]" container=".tab-content"/>
		<stripes:layout-render name="../layout/CalcularFechaPlazo.jsp" inputPlazo="[name=\"seguroCambio.spot.plazoEntrega\"]" inputFecha="[name=\"seguroCambio.spot.entrega\"]" container=".tab-content"/>
		<stripes:layout-render name="../layout/CalcularFechaPlazo.jsp" inputPlazo="[name=\"seguroCambio.opcion1.plazoEntrega\"]" inputFecha="[name=\"seguroCambio.opcion1.entrega\"]" container=".tab-content"/>
    	<stripes:layout-render name="../layout/BuscadorClientes.jsp" tipoProducto="${actionBean.tipoProducto.toString()}" validarViaSentinel="true" validarViaEmir="true" validarFxCoverage="true"/>
<c:if test="${actionBean.visibleTouch}">
		<stripes:layout-render name="../layout/CalcularFechaPlazo.jsp" inputPlazo="[name=\"seguroCambio.opcion1.plazoInicioBarrera\"]" inputFecha="[name=\"seguroCambio.opcion1.inicioBarrera\"]" container=".tab-content"/>
		<stripes:layout-render name="../layout/CalcularFechaPlazo.jsp" inputPlazo="[name=\"seguroCambio.opcion1.plazoFinBarrera\"]" inputFecha="[name=\"seguroCambio.opcion1.finBarrera\"]" container=".tab-content"/>
</c:if>
<c:if test="${actionBean.visibleOptionFx2}">
		<stripes:layout-render name="../layout/CalcularFechaPlazo.jsp" inputPlazo="[name=\"seguroCambio.opcion2.plazoEntrega\"]" inputFecha="[name=\"seguroCambio.opcion2.entrega\"]" container=".tab-content"/>
		<stripes:layout-render name="../layout/CalcularFechaPlazo.jsp" inputPlazo="[name=\"seguroCambio.opcion2.plazoVencimiento\"]" inputFecha="[name=\"seguroCambio.opcion2.vencimiento\"]" container=".tab-content"/>
</c:if>
    	<script>
    	$(document).ready(function() {
    		$('#fchEjecucionExt').val( $.formatDate(new Date() ));
    		$('body').bind('loadedClientFromClientFinder', function(event, data) {
    			$('[name="seguroCambio.cliente.infoRLA.validContract"]').val(data.validContract? 'S':'N'); 
				$('[name="seguroCambio.cliente.infoRLA.validLEI"]').val(data.infoRLA.validLEI);
				$('[name="seguroCambio.cliente.leiVigente"]').val( data.leiVigente, $f);
				$('[name="seguroCambio.cliente.infoRLA.validNationalID"]').val(data.infoRLA.validNationalID);
				$('[name="seguroCambio.cliente.infoRLA.dfa.action"]').val(data.infoRLA.dfa.action);	
				$('[name="seguroCambio.cliente.infoRLA.cartasClasMifi.indFirmado"]').val( data.infoRLA.cartasClasMifi?data.infoRLA.cartasClasMifi.indFirmado: '', $f);
				$('[name="seguroCambio.cliente.infoRLA.cartasClasMifi.tipoFirma"]').val( data.infoRLA.cartasClasMifi?data.infoRLA.cartasClasMifi.tipoFirma: '', $f);
				if(data.aviso!=null && data.aviso!=''){
					addAlert('.tab-content', data.aviso, 'alert-warning');				
				}
				$('[name="seguroCambio.cliente.infoRLA.origContrapartida"]').val(data.infoRLA.origContrapartida); 
				
    			$('[name="seguroCambio.cliente.codigoJ"]').val(data.codigoJ).trigger('change');
    			$('[name="seguroCambio.cliente.nombre"]').val(data.nombre);
    			$('#clienteValidContract').removeClass('no').addClass(data.validContract? '':'no');
    			$('#clienteVigenciaLEI').removeClass('no').addClass(data.leiVigenteEnPantalla? '':'no');
    			$('[name="seguroCambio.cliente.lei"]').val(data.lei);
    			$('[name="seguroCambio.cliente.clasificacionMIFID.descripcion"]').val(data.clasificacionMIFID? data.clasificacionMIFID.descripcion:'');
    			$('[name="seguroCambio.cliente.clasificacionMIFID.id"]').val(data.clasificacionMIFID? data.clasificacionMIFID.id:'');
    			
    			var $f = $('#fSeguro');
    			
    			attachInput('seguroCambio.cliente.documento', data.documento, $f);
    			attachInput('seguroCambio.cliente.contrapartida', data.contrapartida, $f);
    			attachInput('seguroCambio.cliente.segmento.id', data.segmento? data.segmento.id:'', $f);    			
    			attachInput('seguroCambio.cliente.domicilio', data.domicilio, $f);

    			
    			$('#ddNameCliente').show();
    			$('[name="seguroCambio.propuesta"]').val('').trigger('change');
    			$('#btnBusquedaPropuesta,[name="seguroCambio.propuesta"]').attr('disabled',false);
    			$('#btnBusquedaCuentas').attr('disabled',false);
    			$('#txtCuentaLocal').attr('disabled',false);
    			
    			var contrapartida = data.contrapartida;
    			if (contrapartida!=null){
    			   ajaxWrapper('.tab-content',
    		 	      	{
    		      			url: $('#fSeguro').attr('action') + '?preCargaClienteDirecto=',
    		 	      		data: {
      			 	      			"seguroCambio.cliente.contrapartida": data.contrapartida,
      			 	      		    "seguroCambio.cliente.codigoJ": data.codigoJ,
      			 	      	        "seguroCambio.cliente.documento": data.documento,
    		 	          	}, 
    		 		    },
    		 		    function(data) {
    		 		    	$('[name="seguroCambio.gestor"]').val(data.gestor);
    		 		    	$('[name="seguroCambio.oficina"]').val(data.oficina);
    		 		    	$('[name="seguroCambio.propuesta"]').val(data.propuesta);
    		 		    	$('[name="seguroCambio.ccc"]').val(data.ccc);
  
           			 	})
    			}
    			var codigoJ = data.codigoJ;
    			if(codigoJ.substring(0,1) === 'J') {
       		      	
    				ajaxWrapper('.tab-content',
		 	      	{
		      			url: $('#fSeguro').attr('action') + '?isClienteAsesorable=',
		 	      		data: {
  			 	      			"seguroCambio.cliente.codigoJ": data.codigoJ,
		 	          	}, 
		 		    },
		 		    function(data) {
		 		    	debugger;
						if(data.isClienteAsesorable) {
							$('#txtFuenteTest').val(data.fuenteTest);
							mostrarPropuestasAsesoramiento(codigoJ, '${actionBean.tipoProducto}', $('#modalCliente').data('clientLoaded').profesional);						
						} else {
		    				$('#btnGuardarPropuesta').parent().hide();
		    				$('#btnGenerarPropuesta').parent().hide();
						}  
       			 	})
    			} else {
    				$('#btnGuardarPropuesta').parent().hide();
    				$('#btnGenerarPropuesta').parent().hide();
				}  
    		});
    		
    		$('#btnBusquedaCliente').click(function() {
    			openBusquedaCliente();
    		});
    	});
    	</script>
    	<stripes:layout-render name="../layout/BuscadorPropuestas.jsp"/>
    	<script>
    	$(document).ready(function() {
    		$('body').bind('selectedPropuesta', function(event, propuesta, divisa, importe) {
    			$('[name="seguroCambio.propuesta"]').val(propuesta).trigger('change');
    		});
   			$('#btnBusquedaPropuesta').click(function() {
   				resetAlertFormErrors('#fSeguro');
   				mostrarPropuestas($('[name="seguroCambio.cliente.codigoJ"]').val(), '${actionBean.tipoProducto}');
   			});
    	});
    	</script>
    	<stripes:layout-render name="../layout/BuscadorPropuestasAsesoramiento.jsp"/>
    	<script>
    	$(document).ready(function() {
		$('body').bind('selectedPropuestaAsesoramiento', function(event, data) {
			var action = $(location).attr("href").split("/").pop();
			if(action.indexOf("?") !== -1){
				action = action.substring(0, action.indexOf("?"))
			}
			ajaxWrapper('.tab-content',
		 	      	{
		      			url: $('#fSeguro').attr('action') + '?viewPropuesta=',
		 	      		data: {
		 	      			'propuestaAsesoramiento.id': data.id,
		 	          	}, 
		 		    },
		 		    function(data) {
		 		    	var action = data.urlDestino.split("/").pop();
		 		    	var indice1 = action.indexOf("?");
		 		    	var indice2 = action.lastIndexOf("?");
			            if(indice1!==indice2){
			               action = action.substring(0, indice2);
		                 }
		              $(location).attr('href', action);
		 		    },
		 		    true)
		});		
    		$('body').bind('aceptadoModalPropuestasAsesoramiento', function(event, data) {
    			
    			if(data && data.deseaAsesorar) {
    				var $f = $('#fSeguro');
    				attachInput('propuestaAsesoramiento.clienteGCB', data.checkedGCB, $f);
    				attachInput('propuestaAsesoramiento.fechaVigencia', data.vigenciaPropuesta, $f);
    			} else {
    				$('#btnGuardarPropuesta').parent().hide();
    				$('#btnGenerarPropuesta').parent().hide();
    			}
    		});
    	});

        var ACTAMIFID_FORMATO_NO_VALIDO = '<fmt:message key="FormatoActaMifidNoValido"/>';

         function validateActaMiFID() {
            var actaMifid = $('#actaMifid').val();
            var pattern = /^[A-Za-z0-9]{17}$/;
            if (pattern.test(actaMifid)) {
                resetAlertFormErrors('.tab-content');
                return true;
            } else {
                mostrarError('.tab-content', ACTAMIFID_FORMATO_NO_VALIDO);
                return false;
            }
         }

		function toggleActaMiFIDField() {
			$('[name="seguroCambio.grabarLlamadaMifid"]').val($("input[name='seguroCambio.grabarLlamadaMifidRadio']:checked").val());
			if ($("input[name='seguroCambio.grabarLlamadaMifidRadio']:checked").val() == 'N') {
				$('#actaMifid').prop('disabled', false);
				$('#actaMifid').on('blur', function() {
                    if (!validateActaMiFID()) {
                        $('#actaMifid').focus();
                    }
                });
			} else {
				$('#actaMifid').prop('disabled', true);
				$('[name="seguroCambio.actaMifid"]').val('');
			}
		}

    	</script>
    	
    	<stripes:layout-render name="../layout/BuscadorCuentas.jsp"/>
    	<script>
    	$(document).ready(function() {
    		$('body').bind('selectedCuenta', function(event, cuenta, divisa, cuentaPartenon) {
    			$('[name="seguroCambio.ccc"]').val(cuenta).trigger('change');
    			$('[name="seguroCambio.cccPartenon"]').val(cuentaPartenon);
    		});
   			$('#btnBusquedaCuentas').click(function() {
   				resetAlertFormErrors('#fSeguro');
   				mostrarCuentas($('[name="seguroCambio.cliente.codigoJ"]').val(),'selectedCuenta',null);
   				
   			});
    	});
    	</script>

	<fmt:message key="segCamLiq.compra" var="COMPRA"/>
	<fmt:message key="segCamLiq.venta" var="VENTA"/>
	<fmt:message key="segCamLiq.above" var="ABOVE"/>
	<fmt:message key="segCamLiq.up" var="UP"/>
	<fmt:message key="segCamLiq.below" var="BELOW"/>
	<fmt:message key="segCamLiq.down" var="DOWN"/>
	<fmt:message key="segCamLiq.out" var="OUT"/>
	<fmt:message key="segCamLiq.in" var="IN"/>
	<fmt:message key="segCamLiq.bareuropea" var="BAREUROPEA"/>
	<fmt:message key="segCamLiq.barsimple" var="BARSIMPLE"/>
	<fmt:message key="segCamLiq.bartouch" var="TOUCH"/>
	<c:set var="showCountDown" value="${!empty actionBean.countDown}"/>
	<c:set var="tipoBarreraDefecto" value="${actionBean.tipoBarreraDefecto}"/>
	
	<c:set var="indiceMonedaOpcion1" value="${actionBean.tratamientoDivisasSeguroCambioLiquidacionFija? 1 : 0}"/>
    <c:set var="valorGestor" value="${actionBean.seguroCambio.gestor}"/>  
    
    <!-- Logica para mostrar mensaje en caso de cotizar/RFQ y campo CCC vacío -->
    <script type="text/javascript">

		$(document).ready(function() {	
			var MENSAJE_CUENTA_CORRIENTE_VACIA = '${actionBean.mensajeCuentaCorrienteVacia}' 
			var IS_CALLED_RFQ_WITH_SUCCESS = ${actionBean.calledRFQWithSuccess};
			var IS_CALLED_GET_RFQ_WITH_SUCCESS = ${actionBean.calledGetRFQWithSuccess};
			var IS_CALLED_COTIZAR_WITH_SUCCESS = ${actionBean.calledCotizarWithSuccess};
			var IS_CUENTA_CORRIENTE_VACIA = ${actionBean.cuentaCorrienteVacia};
			var MENSAJE_WARNING = '${actionBean.mensajeWarning}';
			if(   (IS_CALLED_GET_RFQ_WITH_SUCCESS || IS_CALLED_COTIZAR_WITH_SUCCESS) && IS_CUENTA_CORRIENTE_VACIA){
				addAlert('.tab-content',MENSAJE_CUENTA_CORRIENTE_VACIA, 'alert-warning')
			}
			if (MENSAJE_WARNING!='X'){
				addAlert('.tab-content',MENSAJE_WARNING, 'alert-warning')	
			}
		})

	</script>
	
	
	 <!-- Logica cambio de margen pipos -->
    <script type="text/javascript">
    
	 $(document).ready(function() {	    
		    $("[name='seguroCambio.precio.tesoreria.margenPipos']").on('change', function(){
				var margenPipos = $("[name='seguroCambio.precio.tesoreria.margenPipos']").val().replace('.',',');
				var parDivisa = $('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]').val();
				var spot = $("[name='seguroCambio.precio.tesoreria.spot']").val()/*.replace('.',',');*/
				var puntos = $("[name='seguroCambio.precio.tesoreria.puntos']").val().replace('.',',');
				var idOperacion = $("[name='seguroCambio.id']").val();
				
				ajaxWrapper('.tab-content',
			      	{
					    url: $('#fSeguro').attr('action') + '?calcularActualizarPrecioAsegurado=',			      					      		
			      		data: {		      			
			      			'seguroCambio.precio.tesoreria.margenPipos':margenPipos,			      			
			      			'seguroCambio.spot.precioAsegurado.twoCurrencies': parDivisa,
			      			'seguroCambio.precio.tesoreria.spot':spot,
			      			'seguroCambio.precio.tesoreria.puntos':puntos,
			      			'seguroCambio.id': idOperacion
			          	}
				    },
				    function(data) {		    	
				    	$("[name='seguroCambio.spot.precioAsegurado.amount']").val(data.precioAsegurado);
				    	return data;		    		    	
				    }).then(function(data) {				    	
						return data;			
		 			}
		 		)
				
		    })		
	    })
    </script>
    
    
    <script type="text/javascript">

		function updateAndTriggerChange(objectToUpdate, value) {
			let jqueryObj = (objectToUpdate instanceof jQuery) ? objectToUpdate : $(objectToUpdate);
			jqueryObj.val(value);
			jqueryObj.trigger('change', true);
		}

		$(document).ready(function() {
		
			function filterFrecuenciaBarrera(v) {

				if(v) {
					return $('[name="seguroCambio.opcion1.frecuenciaBarrera.id"]').find('option').filter( '[value="' + v.join('"],[value="') + '"]' );
				}
				
				return null;
			}
			
			var INDICE_MONEDA_OPCION1 = ${indiceMonedaOpcion1};
			var JSON_FRECUENCIA_BARRERA_AMERICANA = ${actionBean.jsonFiltroFrecuenciaBarreraAmericana};
			var JSON_FRECUENCIA_BARRERA_EUROPEA = ${actionBean.jsonFiltroFrecuenciaBarreraEuropea};
			
			var OPTION_DIVISAS = $('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]').find('option').not('[value=""]');
			var OPTION_FRECUENCIA_BARRERA_AMERICANA =  filterFrecuenciaBarrera( JSON_FRECUENCIA_BARRERA_AMERICANA );
			var OPTION_FRECUENCIA_BARRERA_EUROPEA = filterFrecuenciaBarrera( JSON_FRECUENCIA_BARRERA_EUROPEA );
		    setDivisas();

		    $("#txtCuentaLocal").on('change',function(){ 
				$("input[name='seguroCambio.cccPartenon']").val('');					
			});
		    
<c:if test="${actionBean.disabledFields}">
			$('input,select,button').not('[type="submit"],[type="hidden"]').attr('disabled', true);
			var disable=${actionBean.disabledBotonBusquedaPropuesta};
			$('#btnBusquedaPropuesta,[name="seguroCambio.propuesta"]').attr('disabled', disable);
			$('#btnBusquedaPropuesta').attr('disabled', disable);
			var disableOficina=${actionBean.disabledOficina};
			$('[name="seguroCambio.oficina"]').attr('disabled', disableOficina);
</c:if>

			var vJsonDatos = $.parseJSON('${actionBean.jsonTextosOpcionBarrera}');

		    $('input[placeholder!=""]').attr('placeholder', function() {
	    		var txt = $(this).attr('placeholder');
	    		return txt ? txt.capitalizeFirstLetter():'';
		   	});

<c:if test="${showPanelDatos}">
			$('#fSeguro').data('seleccionadoCriterios', true);
	<c:if test="${showCountDown}">
					$('#btnConfirmarPrecioEstructuras').attr('disabled',false);
					$('#btnValidarPrecioEstructuras').attr('disabled',false);
					$('.countdown').show();
					$('#jcountdown').countdown('${actionBean.countDown}', function(event) {
					    var $this = $(this).html(event.strftime('<span>%M:%S</span>'));
					});
					$('#jcountdown').bind('finish.countdown', function() {
						quitarBoton();
					});
	</c:if>
			$('#footerFxEstructuras').show();
            var tEje = (!$.isEmptyObject( $('#fchEjecucionExt').val() ) && $('#fchEjecucionExt').val() !== '')
            				//o que estemos en la página de Cotizador con el caso Extensible
            				|| ($('#cmbProductosSelect') && $('#cmbProductosSelect').val()=='SE');
            if(tEje) {
            	setValues_x_OperacionBarrera($('#cmbTipoOperacion').val(),'${tipoBarreraDefecto}');
	         }
            else{
            	var tipoBarrera='${tipoBarreraDefecto}';
            	//para evitar pete en el onload al cambiar de extensible a otro tipo de producto
            	if($('#cmbTipoBarrera') && $('#cmbTipoBarrera').val()!=null && $('#cmbTipoBarrera').val()!=''){
            		setValues_x_OperacionBarrera($('#cmbTipoOperacion').val(), $('#cmbTipoBarrera').val());
            	}
         	}
			$('[name="seguroCambio.opcion1.callPut"]').val( ['${actionBean.seguroCambio.opcion1.callPut}'] );
			$('[name="seguroCambio.opcion2.callPut"]').val( ['${actionBean.seguroCambio.opcion2.callPut}'] );
</c:if>

            if( $('[name="seguroCambio.opcion1.callPut"]:checked').length === 0 ) {
            	$('[name="seguroCambio.opcion1.callPut"]:first').prop('checked', true);	
            }
<c:if test="${actionBean.visibleOptionFx2}">
			if( $('[name="seguroCambio.opcion2.callPut"]:checked').length === 0 ) {
				$('[name="seguroCambio.opcion2.callPut"]:first').prop('checked', true);	
			}
</c:if>

setInicioBarrera();
setFrecuenciaBarrera();
iniciarMascaraNominal();
<c:if test="${actionBean.botonContratar}">
			$('#panelDatosSeguro').find('input,select').not('[name="seguroCambio.cliente.codigoJ"],[name="seguroCambio.gestor"],[name="seguroCambio.oficina"],[name="seguroCambio.propuesta"],[name*="filtroBuscadorClientes"],[name*="seguroCambio.precio.tesoreria.margenNeto"]').bind('change keyup',function() {
				quitarBoton();
			});
			$('#checkRegeneracion').bind('change click',function() {
                quitarBoton();
            });
            $('#bodyRenegociacion').find('i').bind('change click',function() {
                quitarBoton();
            });
</c:if>
		    function cambioCriterios() {
		    	
				var $f = $('<form>').attr('method','post').attr( 'action', $('#fSeguro').attr('action') ).hide();
				$f.append( $('<input name="refresh" type="submit" id="cambioCriterios">') );
				
		    	attachInput('seguroCambio.tipoOperacion.id', $('[name="seguroCambio.tipoOperacion.id"]').val(), $f);
		    	attachInput('seguroCambio.tipoBarrera.id', $('[name="seguroCambio.tipoBarrera.id"]').val(), $f);
		    	attachInput('seguroCambio.fechaEjecucion', $('[name="seguroCambio.fechaEjecucion"]').val(), $f);
		    	attachInput('seguroCambio.producto.id', $('[name="seguroCambio.producto.id"]').val(), $f);
		    	attachInput('seguroCambio.gestor', "${valorGestor}", $f);
		    	attachInput('seguroCambio.renegociacion', $('#checkRegeneracion').is(':checked'), $f);

		    	$('body').append( $f );

		    	$('#cambioCriterios').click();
		    };
		    
		    function setInicioBarrera() {
	        	if($('[name="seguroCambio.tipoBarrera.id"]').val() === '${TipoBarrera.AMERICANA.toString()}') {
	        		if( '${actionBean.fechaInicioBarrera}' !== '' ) {
	        			$('[name="seguroCambio.opcion1.inicioBarrera"]').val( '${actionBean.fechaInicioBarrera}' );
	        		}
	        	}
		    }
			    
		    function setFrecuenciaBarrera() {
		    	if($('[name="seguroCambio.tipoBarrera.id"]').val() === '${TipoBarrera.AMERICANA.toString()}') {
		        	$('[name="seguroCambio.opcion1.frecuenciaBarrera.id"]').html( OPTION_FRECUENCIA_BARRERA_AMERICANA );
		    	}
		    	if($('[name="seguroCambio.tipoBarrera.id"]').val() === '${TipoBarrera.EUROPEA.toString()}') {
		        	$('[name="seguroCambio.opcion1.frecuenciaBarrera.id"]').html( OPTION_FRECUENCIA_BARRERA_EUROPEA );
		    	}
		    }
		    
		    <c:if test="${actionBean.visibleFechaEjecucion}">
		    
		  		 $('[name="seguroCambio.opcion1.strike.amount"],[name="seguroCambio.opcion1.fixingReference.id"],[name="seguroCambio.opcion1.barrera"]').change(function(){	
			    	$('[name="seguroCambio.opcion2.strike.amount"]').val($('[name="seguroCambio.opcion1.strike.amount"]').val());
			    	$('[name="seguroCambio.opcion2.barrera"]').val($('[name="seguroCambio.opcion1.barrera"]').val());
			    	$('select[name="seguroCambio.opcion2.fixingReference.id"]').val($('select[name="seguroCambio.opcion1.fixingReference.id"]').val());					
				});
			   				
			    
			    $('[name="seguroCambio.opcion2.strike.amount"],[name="seguroCambio.opcion2.fixingReference.id"],[name="seguroCambio.opcion2.barrera"]').change(function(){				
					$('[name="seguroCambio.opcion1.strike.amount"]').val($('[name="seguroCambio.opcion2.strike.amount"]').val());
					$('[name="seguroCambio.opcion1.barrera"]').val($('[name="seguroCambio.opcion2.barrera"]').val());
					$('select[name="seguroCambio.opcion1.fixingReference.id"]').val($('select[name="seguroCambio.opcion2.fixingReference.id"]').val());
				});	
			
			</c:if>
						    
		    $('#cmbTipoOperacion, #cmbTipoBarrera, #cmbProductosSelect, #fchEjecucionExt').bind('change',function(e, state) {
				if (typeof state!='undefined' && state) return false; // infinite recursion check

	        	if($('#cmbProductosSelect')){
	        		if($(this).attr("id") =='cmbProductosSelect'){
			            $('#panelDatosSeguro').hide();
			            $('#panelDatos').hide();
			            $('#footerFxEstructuras').hide();
	        		}
	        	}
		    	if($('#fSeguro').data('seleccionadoCriterios') ) {
		    		//vuelvo a mostrar
		    		if($('#cmbProductosSelect')){
			    		$('#panelDatosSeguro').show();
			            <c:if test="${showPrecios}">
			            	$('#panelDatos').show();
			            </c:if>
	     		        $('#footerFxEstructuras').show();
		    		}
		    		cambioCriterios();
		    		return;
		    	}
		    	
		        var tOper = !$.isEmptyObject( $('#cmbTipoOperacion').val() );
		        var tBar = !$.isEmptyObject( $('#cmbTipoBarrera').val() );
		        var tEje = !$.isEmptyObject( $('#fchEjecucionExt').val() ) && $('#fchEjecucionExt').val() !== '';
		        if(tOper && (tBar || tEje)){
		        	$('#fSeguro').data('seleccionadoCriterios', true);

		        	setInicioBarrera();
		        	setFrecuenciaBarrera();
		        	
		        	var valorDivisaAsegurado = $('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]').val().split('/');	
		        	$('[name="seguroCambio.opcion1.strike.twoCurrencies"]').val( $('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]').val() );
		        	$('[name="seguroCambio.opcion2.strike.twoCurrencies"]').val( $('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]').val() );
		        	$('#cmbDivisa1').val( valorDivisaAsegurado[0] );
		        	$('#cmbDivisa2').val( valorDivisaAsegurado[1] );
		        	$('[name="seguroCambio.opcion1.nominal.currency"]').val( valorDivisaAsegurado[ INDICE_MONEDA_OPCION1 ] );
		        	$('[name="seguroCambio.opcion2.nominal.currency"]').val( valorDivisaAsegurado[0] );
		        	$('[name="seguroCambio.spot.nominal.currency"]').val( valorDivisaAsegurado[0] );
		        	if(tEje){
		        		$('#txtDivisaStrike2').val( valorDivisaAsegurado );
		        		$('#cmbDivisa3').val( valorDivisaAsegurado[0] );
		        		$('#cmbDivisa4').val( valorDivisaAsegurado[1] );
		        		$('#divisaNominal2').val( valorDivisaAsegurado[0] );
		        	}
		        	
		            $('#panelDatosSeguro').show();
<c:if test="${showPrecios}">
		            	$('#panelDatos').show();
		            	$('#btnConfirmarPrecioEstructuras').attr('disabled',false);
		            	$('#btnValidarPrecioEstructuras').attr('disabled',false);
</c:if>
		            $('#footerFxEstructuras').show();
		            if(tEje){
		            	setValues_x_OperacionBarrera($('#cmbTipoOperacion').val(),'${tipoBarreraDefecto}');
			         }
		            else{
		            	setValues_x_OperacionBarrera($('#cmbTipoOperacion').val(), $('#cmbTipoBarrera').val());
			         }
		            //jerome
		            //submit del form para que busque con los tres criterios
		            if($('#cmbProductosSelect')){
			            $('#fSeguro').data('seleccionadoCriterios', true);
		            	cambioCriterios();
		            }
		        } else {
		        	//paso a false para que al cambiar algun criterio no me haga submit del form. Y que lo haga solo si estan los criterios con valor
		        	if($('#cmbProductosSelect')){
		        		$('#fSeguro').data('seleccionadoCriterios', false);//jerome 
		        	}
		            $('#panelDatosSeguro').hide();
		            $('#panelDatos').hide();
		            $('#footerFxEstructuras').hide();
		        }
		    });

			$('[name="seguroCambio.opcion1.fixingBlock"]').bind('change', function(e, state) {
				if (typeof state!='undefined' && state) return false;

				$('[name="seguroCambio.opcion2.fixingBlock"]').val( $(this).val() );
			});
			
			$('[name="seguroCambio.opcion2.fixingBlock"]').bind('change', function(e, state) {
				if (typeof state!='undefined' && state) return false;

				$('[name="seguroCambio.opcion1.fixingBlock"]').val( $(this).val() );
			});
			
			$('[name="seguroCambio.opcion1.vencimiento"]').bind('change', function(e, state) {
				if (typeof state!='undefined' && state) return false;
			
				var p;	
				p = calcularFechaPlazoByDate($(this).val(), '2d', '.tab-content');				
				
				if(p) {
					p.then(function(data) {
						const antes = $('[name="seguroCambio.spot.entrega"]').val();

						function triggerUpdates() {
							updateAndTriggerChange($('[name="seguroCambio.opcion1.plazoEntrega"]'), '');
							updateAndTriggerChange($('[name="seguroCambio.opcion1.entrega"]'), data);
							 <c:if test="${!actionBean.visibleFechaEjecucion}">
								updateAndTriggerChange($('[name="seguroCambio.spot.plazoEntrega"]'), '');
							 	updateAndTriggerChange($('[name="seguroCambio.spot.entrega"]'), data);
							 </c:if>
						}

						if( !antes  ) {
							triggerUpdates();
						} else if( $.parseDate( data ).getTime() > $.parseDate( antes ).getTime() ) {
							triggerUpdates();
						}
					});
				}
			});
							
			$('[name="seguroCambio.opcion2.vencimiento"]').bind('change', function(e, state) {
				if (typeof state!='undefined' && state) return false; // infinite recursion check

				
				var p;				
				p = calcularFechaPlazoByDate($(this).val(), '2d', '.tab-content');
               
				if(p) {
					p.then(function(data) {
						const antes = $('[name="seguroCambio.spot.entrega"]').val();

						function triggerUpdates() {
							updateAndTriggerChange($('[name="seguroCambio.opcion2.plazoEntrega"]'), '');
							updateAndTriggerChange($('[name="seguroCambio.opcion2.entrega"]'), data);
						}

						if( !antes  ) {
							triggerUpdates();
						} else if( $.parseDate( data ).getTime() > $.parseDate( antes ).getTime() ) {
							triggerUpdates();
						}
					});
				}
			});
				
			$('[name="seguroCambio.spot.entrega"]').bind('change', function(e, state) {
				if (typeof state!='undefined' && state) return false; // infinite recursion check

				var esExtensible=false;
				var p;
				<c:if test="${actionBean.visibleFechaEjecucion}">
				    esExtensible=true;
                </c:if>
				if( $(this).val() === '' ) {
					$('[name="seguroCambio.spot.plazoEntrega"]').val('');
				}
				updateAndTriggerChange($('[name="seguroCambio.opcion1.plazoEntrega"],[name="seguroCambio.opcion2.plazoEntrega"],[name="seguroCambio.opcion1.plazoFixingBlock"],[name="seguroCambio.opcion2.plazoFixingBlock"]'),
						$('[name="seguroCambio.spot.plazoEntrega"]').val() );
				if (esExtensible){
					updateAndTriggerChange($('[name="seguroCambio.opcion1.fixingBlock"],[name="seguroCambio.opcion2.fixingBlock"]'), $(this).val() );
				}else{
					updateAndTriggerChange($('[name="seguroCambio.opcion1.entrega"],[name="seguroCambio.opcion2.entrega"],[name="seguroCambio.opcion1.fixingBlock"],[name="seguroCambio.opcion2.fixingBlock"]'),
							$(this).val() );
				  p = calcularFechaPlazoByDate($(this).val(), '-2d', '.tab-content');
				}
				if(p) {
					p.then(function(data) {
						const antes = $('[name="seguroCambio.opcion1.vencimiento"]').val();

						function triggerUpdates() {
							updateAndTriggerChange($('[name="seguroCambio.opcion1.plazoVencimiento"]'), '');
							updateAndTriggerChange($('[name="seguroCambio.opcion1.vencimiento"]'), data);
						}

						if( !antes  ) {
							triggerUpdates();
						} else if( $.parseDate( data ).getTime() < $.parseDate( antes ).getTime() ) {
							triggerUpdates();
						}
					});
				}
			});

			$('[name="seguroCambio.opcion1.entrega"]').bind('change', function(e, state) {
				if (typeof state!='undefined' && state) return false; // infinite recursion check

				
				var p;
				               
				updateAndTriggerChange($('[name="seguroCambio.opcion1.plazoFixingBlock"],[name="seguroCambio.opcion2.plazoFixingBlock"]'),
						$('[name="seguroCambio.opcion1.plazoEntrega"]').val() );
		
				<c:if test="${!actionBean.visibleFechaEjecucion}">
						updateAndTriggerChange($('[name="seguroCambio.spot.plazoEntrega"]'), '');
					 	updateAndTriggerChange($('[name="seguroCambio.spot.entrega"]'), $(this).val());
				</c:if>
			   p = calcularFechaPlazoByDate($(this).val(), '-2d', '.tab-content');
               
				if(p) {
					p.then(function(data) {
						const antes = $('[name="seguroCambio.opcion1.vencimiento"]').val();

						function triggerUpdates() {
							updateAndTriggerChange($('[name="seguroCambio.opcion1.plazoVencimiento"]'), '');
							updateAndTriggerChange($('[name="seguroCambio.opcion1.vencimiento"]'), data);
						}

						if( !antes  ) {
							triggerUpdates();
						} else if( $.parseDate( data ).getTime() < $.parseDate( antes ).getTime() ) {
							triggerUpdates();
						}
					});
				}
			});
				
			$('[name="seguroCambio.opcion2.entrega"]').bind('change', function(e, state) {
				if (typeof state!='undefined' && state) return false; // infinite recursion check
				
				var p;
				p = calcularFechaPlazoByDate($(this).val(), '-2d', '.tab-content');
              
				if(p) {
					p.then(function(data) {
						const antes = $('[name="seguroCambio.opcion1.vencimiento"]').val();

						function triggerUpdates() {
							updateAndTriggerChange($('[name="seguroCambio.opcion2.plazoVencimiento"]'), '');
							updateAndTriggerChange($('[name="seguroCambio.opcion2.vencimiento"]'), data);
						}

						if( !antes  ) {
							triggerUpdates();
						} else if( $.parseDate( data ).getTime() < $.parseDate( antes ).getTime() ) {
							triggerUpdates();
						}
					});
				}
			});
			
			$('#btnConfirmarPrecioEstructuras').bind('click', function() {
	    		$('#btnConfirmarPrecioEstructuras').attr('disabled',true);
	    		$('#btnConfirmarPrecioEstructurasSubmit').trigger("click");
   			});
			
			$('#fSeguro').bind('submit', function() {
	    		$('body').addClass('loading');
   			});
   			
   		    $('#btnLimpiarFixingBlock').click(function() {  

   		        $('#panelBusqFixingBlock').addClass( 'in' );
   		        $('#panelResFixingBlock').removeClass( 'in' );
   		        $('#panelResFixingBlock').hide();

   		        clearForm( $(this).closest('.modal-dialog').find('form') );

   		    });

   		 	$('#modalFixingBlock').on('show.bs.modal', function(e) {    
   		     	idBotonFixing = e.relatedTarget.id;

   		  	});
	
	   		$('#btnBuscarFixingBlock').click(function() {
   				resetAlertFormErrors('#fSeguro');
   		      	ajaxWrapper('.tab-content',
		 	      	{
		      			url: $('#fSeguro').attr('action') + '?searchFixingBlock=',
		 	      		data: {
		 	      			fechDesde: $('[name="filtroFixing.fechDesde"]').val(),
		 	      			fechHasta: $('[name="filtroFixing.fechHasta"]').val(),
		 	      			frecuency: $('[name="filtroFixing.frecuency"]').val(),
		 	      			business: $('[name="filtroFixing.business"]').val(),
		 	          	}, 
		 		    },
		 		    function(data) {
	 		    		mostrarFixingBlock(data);
		 		    },
		 		    true)
	   		});

	   		function mostrarFixingBlock(datos) {
     				$('#tablaFix').removeAllRows();
     				var $tbody = $('#tablaFix tbody');      				
     	            var html = [];
     	          	for (i = 0; i < datos.length; i++) {
     	            	html.push('<tr onClick="escogerFixingBlock(this);">');
     	            	html.push('<td>' + datos[i].fecha + '</td>');
     	            	html.push('<td>' + datos[i].peso + '</td>');
     	            	html.push('<td>' + datos[i].valor + '</td>');
     	            	html.push('</tr>');
     	          	}
     	            
     	          $tbody.append( html.join('') );
     	          $('#panelResFixingBlock').show();
     		}
     		 	
		    $('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]').bind('change',function(e, state) {

                var parDivisa = $(this).val();
		        //set the value into field fixingReference
                var encontrado = false;
                var duplicado = false;
                $('select[name="seguroCambio.opcion1.fixingReference.id"] > option').each(function(i){
                     if($(this).text().indexOf(parDivisa) >= 0){
                        $('select[name="seguroCambio.opcion1.fixingReference.id"]').val($(this).val());
                        $('select[name="seguroCambio.opcion2.fixingReference.id"]').val($(this).val());
                        if(encontrado){
                            duplicado = true;
                        }
                        else{
                            encontrado = true;
                        }

                     }
                });
                if(!encontrado || duplicado){
                    $('select[name="seguroCambio.opcion1.fixingReference.id"]').val("");
                    $('select[name="seguroCambio.opcion2.fixingReference.id"]').val("");
                }

				if (typeof state!='undefined' && state) return false; // infinite recursion check

		        var aux = parDivisa? parDivisa.split('/') : ['',''];
		        $('#cmbDivisa1,#cmbDivisa3').val(aux[0]);
		        $('#cmbDivisa2,#cmbDivisa4').val(aux[1]);
		        
		        if( !$('[name="seguroCambio.opcion1.nominal.currency"]').val() ) {
		        	$('[name="seguroCambio.opcion1.nominal.currency"]').val(aux[ INDICE_MONEDA_OPCION1 ]);
		        	cargarMascaraNominal($('[name="seguroCambio.opcion1.nominal.currency"]'), $('[name="seguroCambio.opcion1.nominal.amount"]'));
		        	
		        }
		        if( !$('[name="seguroCambio.spot.nominal.currency"]').val() ) {
			        $('[name="seguroCambio.spot.nominal.currency"]').val(aux[0]).trigger('change');
			        cargarMascaraNominal($('[name="seguroCambio.spot.nominal.currency"]'), $('[name="seguroCambio.spot.nominal.amount"]'));
		        }
		        if( !$('[name="seguroCambio.opcion2.nominal.currency"]').val() ) {
			        $('[name="seguroCambio.opcion2.nominal.currency"]').val(aux[0]);
			        cargarMascaraNominal($('[name="eguroCambio.opcion2.nominal.currency"]'), $('[name="eguroCambio.opcion2.nominal.amount"]'));
		        }
		        $('[name="seguroCambio.opcion1.strike.twoCurrencies"]').val( parDivisa );
		        $('[name="seguroCambio.opcion2.strike.twoCurrencies"]').val( parDivisa );

		    });


			$('[name="seguroCambio.spot.precioAsegurado.amount"]').change(function() {
				$('[name="seguroCambio.opcion1.strike.amount"],[name="seguroCambio.opcion2.strike.amount"]').val( $(this).val() );
			});

<c:if test="${actionBean.fechaFinBarreraFechaVencimientoHanDeSerIguales}">
$('[name="seguroCambio.opcion1.finBarrera"]').change(function(e, state) {
	if (typeof state!='undefined' && state) return false; // infinite recursion check
	$('[name="seguroCambio.opcion1.vencimiento"]').val( $(this).val() );

	if($('[name="seguroCambio.tipoBarrera.id"]').val() === '${TipoBarrera.EUROPEA.toString()}') {
		$('[name="seguroCambio.opcion1.inicioBarrera"]').val( $(this).val() );
	}
});

$('[name="seguroCambio.opcion1.vencimiento"]').change(function() {
	$('[name="seguroCambio.opcion1.finBarrera"]').val( $(this).val() );
	
	if($('[name="seguroCambio.tipoBarrera.id"]').val() === '${TipoBarrera.EUROPEA.toString()}') {
		$('[name="seguroCambio.opcion1.inicioBarrera"]').val( $(this).val() );
	}
});

$('[name="seguroCambio.opcion1.inicioBarrera"]').change(function() {
	
	if($('[name="seguroCambio.tipoBarrera.id"]').val() === '${TipoBarrera.EUROPEA.toString()}') {
		$('[name="seguroCambio.opcion1.finBarrera"]').val( $(this).val() );
		$('[name="seguroCambio.opcion1.vencimiento"]').val( $(this).val() );
	}
});
</c:if>

<c:if test="${actionBean.nominalesHanDeSerIguales}">
				$('[name="seguroCambio.spot.nominal.amount"]').change(function() {
    				$('[name="seguroCambio.opcion1.nominal.amount"],[name="seguroCambio.opcion2.nominal.amount"]').val( $(this).val() );
    			});

    			$('[name="seguroCambio.opcion1.nominal.amount"]').change(function() {
    				$('[name="seguroCambio.spot.nominal.amount"],[name="seguroCambio.opcion2.nominal.amount"]').val( $(this).val() );
    			});
    			
    			$('[name="seguroCambio.opcion2.nominal.amount"]').change(function() {
    				$('[name="seguroCambio.spot.nominal.amount"],[name="seguroCambio.opcion1.nominal.amount"]').val( $(this).val() );
    			});
</c:if>

<c:if test="${not actionBean.nominalesHanDeSerIguales}">
    $('[name="seguroCambio.spot.precioAsegurado.amount"]').change(function() {
    	var nominal=$('[name="seguroCambio.spot.nominal.amount"]').val();
    	 var liq=$('[name="seguroCambio.opcion1.nominal.amount"]').val();
    	 var strike2=$('[name="seguroCambio.opcion2.strike.amount"]').val();
    	if (nominal){
    		if(strike2){
    			$('[name="seguroCambio.opcion1.nominal.amount"]').trigger('change');	
    		}
            if(liq){
            	$('[name="seguroCambio.opcion2.strike.amount"]').trigger('change');  	
            }
    	}else{
    		$('[name="seguroCambio.opcion1.nominal.amount"]').val('');
            $('[name="seguroCambio.opcion2.strike.amount"]').val('');
    	}
     });
      $('[name="seguroCambio.spot.nominal.amount"]').change(function() {
    	  var strike=$('[name="seguroCambio.opcion1.strike.amount"]').val();
    	  var liq=$('[name="seguroCambio.opcion1.nominal.amount"]').val();
     	  var strike2=$('[name="seguroCambio.opcion2.strike.amount"]').val();
    	  if (strike){
    		  if(strike2){
    	        $('[name="seguroCambio.opcion1.nominal.amount"]').trigger('change');
    		  }
    		  if(liq){
    	        $('[name="seguroCambio.opcion2.strike.amount"]').trigger('change');
    		  }
    	  }else{
    		$('[name="seguroCambio.opcion1.nominal.amount"]').val('');
            $('[name="seguroCambio.opcion2.strike.amount"]').val('');
    	  }
      });
      $('[name="seguroCambio.opcion1.nominal.amount"]').change(function() {
    	  recalcularStrike2();
      });
      $('[name="seguroCambio.opcion2.strike.amount"]').change(function() {
    	  recalcularLiqFija();
      });
      function recalcularStrike2() {
    	  var sentido=$('[name="seguroCambio.tipoOperacion.id"]').val()?$('[name="seguroCambio.tipoOperacion.id"]').val().replace(',','.'):0;
    	  var nominal=$('[name="seguroCambio.spot.nominal.amount"]').val()?$('[name="seguroCambio.spot.nominal.amount"]').val().replace(',','.'):0;
    	  var strike=$('[name="seguroCambio.opcion1.strike.amount"]').val()?$('[name="seguroCambio.opcion1.strike.amount"]').val().replace(',','.'):0;
    	  var liqFija=$('[name="seguroCambio.opcion1.nominal.amount"]').val()?$('[name="seguroCambio.opcion1.nominal.amount"]').val().replace(',','.'):0;
    	  var resultado=0;
    	  var division=strike==0?0:nominal/strike;
    	  if (sentido=='IMP'){
    		  var parcial=division-parseFloat(liqFija);
    		  resultado=parcial==0?0:nominal / parcial;
    	  }else{
    		  var parcial=division+parseFloat(liqFija);
    		  resultado=nominal / parcial;
    	  }
    	  $('[name="seguroCambio.opcion2.strike.amount"]').val(resultado);
      }
      
      function recalcularLiqFija() {
    	  var sentido=$('[name="seguroCambio.tipoOperacion.id"]').val()?$('[name="seguroCambio.tipoOperacion.id"]').val().replace(',','.'):0;
    	  var nominal=$('[name="seguroCambio.spot.nominal.amount"]').val()?$('[name="seguroCambio.spot.nominal.amount"]').val().replace(',','.'):0;
    	  var strike=$('[name="seguroCambio.opcion1.strike.amount"]').val()?$('[name="seguroCambio.opcion1.strike.amount"]').val().replace(',','.'):0;
    	  var strike2=$('[name="seguroCambio.opcion2.strike.amount"]').val()?$('[name="seguroCambio.opcion2.strike.amount"]').val().replace(',','.'):0;
    	  var resultado=0;
    	  var divisionnomStrike=strike==0?0:nominal/strike;
    	  var divisionnomStrike2=strike2==0?0:nominal/strike2;
    	  if (sentido=='IMP'){
    		  resultado=divisionnomStrike-divisionnomStrike2;
    	  }else{
    		  resultado=divisionnomStrike2-divisionnomStrike;
    	  }
    	  $('[name="seguroCambio.opcion1.nominal.amount"]').val(resultado);
      }
</c:if>

<c:if test="${not actionBean.tratamientoDivisasSeguroCambioLiquidacionFija}">
				$('[name="seguroCambio.spot.nominal.currency"]').change(function() {
					cargarMascaraNominal($('[name="seguroCambio.spot.nominal.currency"]'), $('[name="seguroCambio.spot.nominal.amount"]'));
    				$('[name="seguroCambio.opcion1.nominal.currency"]').val( $(this).val() );
    				$('[name="seguroCambio.opcion2.nominal.currency"]').val( $(this).val() );
					cargarMascaraNominal($('[name="seguroCambio.opcion1.nominal.currency"]'), $('[name="seguroCambio.opcion1.nominal.amount"]'));
					cargarMascaraNominal($('[name="seguroCambio.opcion2.nominal.currency"]'), $('[name="seguroCambio.opcion2.nominal.amount"]'));
					let precioAseguradoSelect = $('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]');
					if( $(this).val() ) {
        				precioAseguradoSelect.html( OPTION_DIVISAS.filter('[value*="'+ $(this).val() +'"]').sort(compareParDivisas)  );
						precioAseguradoSelect.trigger('change', true);
					} else {
        				precioAseguradoSelect.html( OPTION_DIVISAS );
    				}
					var parDivisa = precioAseguradoSelect.val();
					var aux = parDivisa? parDivisa.split('/') : ['',''];
					$('#cmbDivisa1,#cmbDivisa3').val(aux[0]);
					$('#cmbDivisa2,#cmbDivisa4').val(aux[1]);
					$('[name="seguroCambio.opcion1.strike.twoCurrencies"]').val( parDivisa );
					$('[name="seguroCambio.opcion2.strike.twoCurrencies"]').val( parDivisa );
    			});
				function compareParDivisas(valor1, valor2) {
    				var aux1 = valor1? valor1.innerHTML.split('/') : ['',''];
    				var aux2 = valor2? valor2.innerHTML.split('/') : ['',''];
    				if (aux1[0]=='EUR'){
    					return -1;
    				}
    				if (aux2[0]=='EUR'){
    					return 1;
    				}
    				if (aux1[1]=='EUR'){
    					return -1;
    				}
    				if (aux2[1]=='EUR'){
    					return 1;
    				}
    				return 0;
    			}
			    function setDivisas() {
					
			    	var valor = $('[name="seguroCambio.spot.nominal.currency"]').val();
			    	if(valor) {
						let precioAseguradoSelector = '[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]';
						var antes = $(precioAseguradoSelector).val();
	    				$(precioAseguradoSelector).html( OPTION_DIVISAS.filter('[value*="'+ valor +'"]') );
	    				if( antes ) {
	    					$(precioAseguradoSelector).val(antes);
	    				}
						$(precioAseguradoSelector).trigger('change', true)
			    	}
			    }
</c:if>

<c:if test="${actionBean.tratamientoDivisasSeguroCambioLiquidacionFija}">
    			$('[name="seguroCambio.spot.nominal.currency"]').change(function() {
    				cargarMascaraNominal($('[name="seguroCambio.spot.nominal.currency"]'),$('[name="seguroCambio.spot.nominal.amount"]'));
					var segundaDivisa = $('[name="seguroCambio.opcion1.nominal.currency"]').val();
					if(!segundaDivisa) {
						segundaDivisa = '';
					}
					
					var primeraDivisa = $(this).val()? $(this).val() : '';

					let precioAseguradoSelector =  $('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]');
					if(primeraDivisa || segundaDivisa) {
						var par1 = primeraDivisa + '/'+ segundaDivisa;
						var par2 = segundaDivisa +'/'+ primeraDivisa;
						
	    				$(precioAseguradoSelector).html( OPTION_DIVISAS.filter('[value*="'+ par1 +'"],[value*="'+ par2 +'"]').sort(compareParDivisas) );
	    				// document.getElementsByName("seguroCambio.spot.precioAsegurado.twoCurrencies")[0].selectedIndex = 0;
					} else {
	    				$(precioAseguradoSelector).html( OPTION_DIVISAS );
					}
					var parDivisa = $(precioAseguradoSelector).val();
					var aux = parDivisa? parDivisa.split('/') : ['',''];
					$('#cmbDivisa1,#cmbDivisa3').val(aux[0]);
					$('#cmbDivisa2,#cmbDivisa4').val(aux[1]);
					$('[name="seguroCambio.opcion1.strike.twoCurrencies"]').val( parDivisa );
					$('[name="seguroCambio.opcion2.strike.twoCurrencies"]').val( parDivisa );

					$(precioAseguradoSelector).trigger('change', true)
    			});
    			function compareParDivisas(valor1, valor2) {
    				var aux1 = valor1? valor1.innerHTML.split('/') : ['',''];
    				var aux2 = valor2? valor2.innerHTML.split('/') : ['',''];
    				if (aux1[0]=='EUR'){
    					return -1;
    				}
    				if (aux2[0]=='EUR'){
    					return 1;
    				}
    				if (aux1[1]=='EUR'){
    					return -1;
    				}
    				if (aux2[1]=='EUR'){
    					return 1;
    				}
    				return 0;
    			}
			    function setDivisas() {
					
			    	var primeraDivisa = $('[name="seguroCambio.spot.nominal.currency"]').val();
			    	primeraDivisa = primeraDivisa? primeraDivisa : '';
					var segundaDivisa = $('[name="seguroCambio.opcion1.nominal.currency"]').val();
					segundaDivisa = segundaDivisa? segundaDivisa : '';

					if(primeraDivisa || segundaDivisa) {
						var par1 = primeraDivisa + '/'+ segundaDivisa;
						var par2 = segundaDivisa +'/'+ primeraDivisa;
						var antes = $('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]').val();
	    				$('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]').html( OPTION_DIVISAS.filter('[value*="'+ par1 +'"],[value*="'+ par2 +'"]') );
	    				if( antes ) {
	    					$('[name="seguroCambio.spot.precioAsegurado.twoCurrencies"]').val(antes);
	    				}
					}
			    }
</c:if>    				
    			
<c:if test="${not actionBean.tratamientoDivisasSeguroCambioLiquidacionFija}">
    			$('[name="seguroCambio.opcion1.nominal.currency"]').change(function(e, state) {
					if (typeof state!='undefined' && state) return false; // infinite recursion check

					cargarMascaraNominal($('[name="seguroCambio.opcion1.nominal.currency"]'),$('[name="seguroCambio.opcion1.nominal.amount"]'));
					updateAndTriggerChange($('[name="seguroCambio.spot.nominal.currency"]'), $(this).val() );
    			});
</c:if>

<c:if test="${actionBean.tratamientoDivisasSeguroCambioLiquidacionFija}">
				$('[name="seguroCambio.opcion1.nominal.currency"]').change(function(e, state) {
					if (typeof state!='undefined' && state) return false; // infinite recursion check

					cargarMascaraNominal($('[name="seguroCambio.opcion1.nominal.currency"]'),$('[name="seguroCambio.opcion1.nominal.amount"]'));
					$('[name="seguroCambio.spot.nominal.currency"]').trigger('change', true);
    			});
</c:if>    				

<c:if test="${actionBean.visibleOptionFx2}">    			
    			$('[name="seguroCambio.opcion2.nominal.currency"]').change(function(e, state) {
					if (typeof state!='undefined' && state) return false; // infinite recursion check

    				cargarMascaraNominal($('[name="seguroCambio.opcion2.nominal.currency"]'),$('[name="seguroCambio.opcion2.nominal.amount"]'));
					updateAndTriggerChange($('[name="seguroCambio.spot.nominal.currency"]'), $(this).val() );
    			});
</c:if>    			
        		function quitarBoton() {
		            $('.countdown').hide();
        			$('#btnConfirmarPrecioEstructuras').attr('disabled',true);
        			$('#btnValidarPrecioEstructuras').attr('disabled',true);
        			$('#btnCotizarEstructuras').attr('disabled',true);
        			$('#btnValidarEstructuras').attr('disabled',true);
    			}
    			
			    function AddValues(opc) {
			        $.each(vJsonDatos[opc], function (name, value) {
			            var obj = $(name);
			            if (obj.is('input') || obj.is('select') ) {
			                obj.val(value).trigger('change');
			            }
			            else{
			                obj.html(value).trigger('change');
			            } 
			       });  
			    }

			    function setValues_x_OperacionBarrera(argOper, argBar){
			        
	                AddValues( argOper );
	                AddValues( argBar );
	                AddValues( String(argOper).concat(':',argBar) );
			    }
		});
		var idBotonFixing='';
	   	function escogerFixingBlock(dato) {
		    	var fixing = dato.firstChild.innerText;
		    	if (idBotonFixing==='btnBusquedaBlock'){
		    		$('#fchFixingBlock').val( fixing );	
		    	}else if (idBotonFixing==='btnBusquedaBlock2'){
		    		$('#fchFixingBlock2').val( fixing );
		    	}
		       	
		      	$('#modalFixingBlock').modal('hide');
	   	};
	   	function cargarMascaraNominal(datoDivisa,datoNominal) {
	   		if(datoDivisa.val()==null){
	   			return false;
	   		}
	      	return ajaxWrapper('.tab-content',
 	      	{
      			url: $('#fSeguro').attr('action') + '?getDecimales=',
      			data: {
	 	      			"divisa": datoDivisa.val(),
	          	}, 
 		    },
 		    function(data) {
 		    	var indice=data.indexOf('=');
 		    	 data=data.substring(indice+1);
 		    	 asignarMascara(datoNominal,parseInt(data));
 		    },
 		    true);
		};
		function iniciarMascaraNominal(){

			if($('[name="seguroCambio.spot.nominal.currency"]').val()!=null && $('[name="seguroCambio.spot.nominal.currency"]').val()!=''){
				cargarMascaraNominal($('[name="seguroCambio.spot.nominal.currency"]'),$('[name="seguroCambio.spot.nominal.amount"]'));
			}else{
				asignarMascara($('[name="seguroCambio.spot.nominal.amount"]'),2);
			}
			if($('[name="seguroCambio.opcion1.nominal.currency"]').val()!=null && $('[name="seguroCambio.opcion1.nominal.currency"]').val()!=''){
				cargarMascaraNominal($('[name="seguroCambio.opcion1.nominal.currency"]'),$('[name="seguroCambio.opcion1.nominal.amount"]'));
			}else{
				asignarMascara($('[name="seguroCambio.opcion1.nominal.amount"]'),2);
			}
			if($('[name="seguroCambio.opcion2.nominal.currency"]').val()!=null && $('[name="seguroCambio.opcion2.nominal.currency"]').val()!=''){
				cargarMascaraNominal($('[name="seguroCambio.opcion2.nominal.currency"]'),$('[name="seguroCambio.opcion2.nominal.amount"]'));
			}else{
			    asignarMascara($('[name="seguroCambio.opcion2.nominal.amount"]'),2);
			}
		};
	</script>
	
	
	<!-- ajaxWrapper para control de versiones de operacion -->
	<script>
		function ajaxWrapper(placeError, jsonSend, processJSON, noloader) {	
			
			var versionOperacion = $("input[name='seguroCambio.version']").val()
			var idOperacion = $("input[name='seguroCambio.id']").val()		
			
			var data = {	
				'data':{
					'seguroCambio.version': versionOperacion,
					'seguroCambio.id': idOperacion
				}				
			}
			if(!jsonSend.data){					
				$.extend(jsonSend,data);	
			}else if(typeof jsonSend.data == 'object'){
				$.extend(jsonSend.data,data.data);	
			}
			return ajax(placeError, jsonSend, processJSON, noloader); 	
		};
		

	</script>

	<!-- funciones para evitar timeout Abacus -->
	<script>			
		function hayRfq(){			
			var idOperacion = $("[name='seguroCambio.id']").val();			
			var idDeal = "${actionBean.seguroCambio.precio.tesoreria.idDeal}";			
			return ajaxWrapper('.tab-content',
		      	{
				    url: $('#fSeguro').attr('action') + '?hayRfq=',			      					      		
		      		data: {
		      			'seguroCambio.id': idOperacion,
		      			'seguroCambio.precio.tesoreria.idDeal': idDeal
		          	}
			    },
			    function(data) { 
			    	return data;		    		    	
			    }).then(function(data) {
			    	if(data.errores){
			    		$('body').removeClass('keepLoading loading');
			    	}
					return data;			
	 			}
	 		)
		}
		
		
		function hayRfqWrapper(){	
			$('body').addClass('keepLoading');
			hayRfq().then(function(data){
				if(!data.errores && !data.hayRfq){
					$("body").trigger("noHayRfq")						
				}else if(!data.errores && data.hayRfq){
					$('body').removeClass('keepLoading loading');
					$('#btnGetRfq').trigger('click');
				}				
			})
		}

		function borrarFila(fila){
		    $('#delete'+fila).val('true');
		    $('#renegociacion'+fila).hide();
		}
		
		
		$('body').on('noHayRfq', function(){		
			 sleepFor(1000).then(function(){hayRfqWrapper()})				
		 })
		 
		 $(document).ready(function() {			 
			if('${actionBean.calledRFQWithSuccess}' === 'true'){				
				hayRfqWrapper();
			} 
		 })
		
		$(document).ready(function() {
		    let renegociacionVisible = '<c:out value="${actionBean.seguroCambio.renegociacion}"/>';

            if(renegociacionVisible !== 'false'){
                $('#renegociacionHeader').show();
                $('#renegociacionBody').show();
            }
            else{
                $('#renegociacionHeader').hide();
                $('#renegociacionBody').hide();
            }

            $("input[id^='delete']").filter(function() {
                return $( this ).attr( "value" ) === "true";
            }).parent().parent().hide();

			$(document).unbind( "ajaxStop" );
		    $(document).on({		       
		        ajaxStop: function() { 
		        	$('body').data('ajaxStart', false); 
		        	if(!$('body').hasClass('keepLoading')){
		        		$('body:not(.noloader)').removeClass('loading');		        		
		        	}
		        	 
		        }
		    });
		})

        $('#addRenegotiation').click(function() {
            $('#btnnuevaRenegociacion').trigger('click');
            var tbody = $('#bodyRenegociacion');
            var $tr = tbody.find('tr').last();
        });

        $('#checkRegeneracion').bind('change', function() {
            if($('#checkRegeneracion').is(':checked')){
                $('#renegociacionHeader').show();
                $('#renegociacionBody').show();
        	}
        	else{
        	    $('#renegociacionHeader').hide();
                $('#renegociacionBody').hide();
        	}
        });

	</script>
	<script src="${pageContext.request.contextPath}/js/panelClientes/CartaClasificacion.js"></script>
	<script src="${pageContext.request.contextPath}/js/util/Constantes.js"></script>
	
 	</stripes:layout-component>   
</stripes:layout-render>
