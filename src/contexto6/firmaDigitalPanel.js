console.log('firmaDigitalPanel.js cargado');
console.log('btnBusquedaEnabledParam: ', '${btnBusquedaEnabledParam}');
console.log('digitalSignatureEnabled:', digitalSignatureEnabled);
console.log('digitalSignatureCompaniesEnabled:', digitalSignatureCompaniesEnabled);
console.log('digitalSignatureGlobalCustomerEnabled:', digitalSignatureGlobalCustomerEnabled);

// Usa las variables en tu lógica
if (digitalSignatureEnabled === 'true') {
    console.log('Firma digital habilitada');
} else {
    console.log('Firma digital deshabilitada');
}

// Función para toggle del bloque de firmantes
$("input[name='firmaDigitalRadio']").on('change', function() {
    console.log('CAMBIO RADIO FIRMA DIGITAL');
    var firmaDigitalSeleccionada = $("input[name='firmaDigitalRadio']:checked").val();
    $('[name="radioPanelFirmaDigital"]').val(firmaDigitalSeleccionada);

    console.log('Valor seleccionado:', firmaDigitalSeleccionada);

    if (firmaDigitalSeleccionada === 'S') {
        $('#bloqueFirmantes').show();
    } else {
        $('#bloqueFirmantes').hide();
        $('#tablaFirmantes tbody').empty();
    }    
});

// Ejecuta función openBusquedaCliente() de BuscadorClientes.jsp
$('#btnBusquedaClienteFirma').click(function() {
    console.log('OPEN CLIENT FIRMA');
    openBusquedaCliente(null, 'firmaDigital');
    console.log('AFTER CLIENT FIRMA');
});

/*  No se usa el collapse de bootstrap porque no funciona con contenido dinamico */
function onClickCollapseTabla(event, idTargetToShowHide) {
    console.log('CLICK COLLAPSE:', idTargetToShowHide);
    if ($(event.target).hasClass('glyphicon-menu-down')) {
        $(event.target).removeClass('glyphicon-menu-down');
        $(event.target).addClass('glyphicon-menu-right');
        $('#' + idTargetToShowHide).hide();
    } else {
        $(event.target).removeClass('glyphicon-menu-right');
        $(event.target).addClass('glyphicon-menu-down');
        $('#' + idTargetToShowHide).show();
    }
}

// Variables para firma digital
var firmaDigitalEnabled = false;
var isClienteJuridico = false;
var isClienteGlobal = false;
var destinoActual = '';


function mostrarFormularioFirmante(tipo) {
    console.log('MOSTRAR FORMULARIO FIRMANTE TIPO:', tipo);
    if (tipo === 'cliente') {
        $('#formularioClienteFirmante').show();
        $('#formularioManualFirmante').hide();
    } else if (tipo === 'manual') {
        loadTipoDocumentosFirmaManual();
        $('#formularioClienteFirmante').hide();
        $('#formularioManualFirmante').show();
    }
}

// Carga tipos de documentos para firmantes tipo manual
function loadTipoDocumentosFirmaManual() {
    console.log('CARGAR TIPOS DOCUMENTO FIRMANTE MANUAL');
    if( !$('#tipoDocumentoFirmaManual').data('loaded') ) {

        return ajax('#frmBuscadorClientes', {
            url: $('#frmBuscadorClientes').attr('action') + '?getJsonListaTiposDocumento=',	
            data: {
                onlyJuridicas: onlyJuridicas
            } 
        },
        function(data) {
              // Elimina todas las opciones excepto la vacía
              $('#tipoDocumentoFirmaManual').find('option:not([value=""])').remove();
              console.log('Datos recibidos W:', data);
            $.each(data, function(val, text) {
                console.log('Descripcion W:', this.descripcion);
                $('#tipoDocumentoFirmaManual').append( $('<option/>').attr('value', this.id).text(this.descripcion) );
            });
        },
        true
        ).then(function() {
            $('#tipoDocumentoFirmaManual').data('loaded', true);
        });
        
    }
};


/* COMPORTAMIENTO DEL PANEL */

// Actualizar cuando se carga un cliente
function onClienteLoaded() {
    if (verificarMostrarFirmaDigital()) {
        actualizarEstadoFirmaDigital();
    }
}

// Función para verificar si mostrar firma digital
function verificarMostrarFirmaDigital() {
    console.log('digitalSignatureEnabled:', digitalSignatureEnabled);
    return digitalSignatureEnabled;
}

// Función para verificar estado de firma digital según cliente
function actualizarEstadoFirmaDigital() {
    var clienteGenericoActualizado = $('#modalCliente').data('origenLlamada') !== 'firmaDigital';
    if (clienteGenericoActualizado) {
        console.log('Cliente genérico, actualizamos estado de firma digital');

        var cliente = $('#modalCliente').data('clientLoaded');
        if (!cliente) return;

        var codigoJ = cliente.codigoJ;
        isClienteJuridico = codigoJ.substring(0,1) === 'J';

        isClienteGlobal = cliente.global;
        destinoActual = $('#cmbDestino').val();

        bannedProductsList = ['SP', 'FW', 'FA', 'ND', 'SW'];        
        isBannedProduct = bannedProductsList.includes( producto);

        // Verificar condiciones para habilitar/deshabilitar
        if(!isClienteJuridico) {
            // Cliente físico
            habilitarFirmaDigital();
        } else if(isClienteJuridico && !digitalSignatureCompaniesEnabled) {
            // Cliente jurídico con variable deshabilitada
            deshabilitarFirmaDigital();
        } else if(isClienteJuridico && digitalSignatureCompaniesEnabled) {
            // Cliente jurídico con variable habilitada
            habilitarFirmaDigital();
        } else if(!digitalSignatureGlobalCustomerEnabled && isBannedProduct) {
            // Cliente jurídico con variable habilitada
            habilitarFirmaDigital();
        }
    }
}

// Función para deshabilitar firma digital
function deshabilitarFirmaDigital() {
    $("input[name='firmaDigitalRadio'][value='N']").prop('checked', true);
    $("input[name='firmaDigitalRadio']").prop('disabled', true);
    $('[name="radioPanelFirmaDigital"]').val('N');
    $('#bloqueFirmantes').hide();
}

// Función para habilitar firma digital
function habilitarFirmaDigital() {
    $("input[name='firmaDigitalRadio']").prop('disabled', false);
    // No marcar ninguna opción por defecto
    $("input[name='firmaDigitalRadio']").prop('checked', false);
}


// Variable global para almacenar el valor de data
var clienteDataGlobal = null; // Datos del cliente desde BuscadorClientes
var firmanteDataGlobal = {}; // Datos del firmante a agregar

$(document).ready(function () {
    //Cuando tenga la informacion del cliente cargada... aqui poner mas para la listilla a mostrar
    $('body').bind('loadedClientFromClientFinderFirma', function (event, data) {
        console.log('Evento recibido:', data);
        $('#txtIdClienteFirma').val(data.codigoJ);
        // Guardar el valor de data en la variable global
        clienteDataGlobal = data;        
    });

     // Listener para agregar firmante HAY QUE QUITAR Y PONER DENTRO DEL MODAAL TAMBIEN LIMPIAR CLIENTE
     $('#btnModalFirmante').on('click', function() {
        // Limpiar campos del modal antes de mostrarlo
            $('#txtIdClienteFirma').val('');
            $('#cmbTipoIntervencionFirmante').val('');
            $("input[name='tipoDatosFirmante']").prop('checked', false);
            $('#formularioClienteFirmante').hide();
            $('#formularioManualFirmante').hide();

            // Limpiar campos del formulario manual
            $("select[name='esClienteFirmanteManual']").val('');
            $("input[name='nombreFirmanteManual']").val('');
            $("#tipoDocumentoFirmaManual").val('');
            $("input[name='codigoDocumentoFirmanteManual']").val('');
            $("input[name='persona.codigoJ']").val('');
            $('#tipoDocumentoFirmaManual').removeData('loaded');

    });

    // Listener para cambio de destino
    $('#cmbDestino').on('change', function() {
        actualizarEstadoFirmaDigital();
    });

})

// Funcion para validar datos de firmante y agregarlos
function altaFirmante() {
    if ($('#formularioManualFirmante').is(':visible')) {
        // Validar el formulario manual
        var isValidForm = $('#fAltaFirmante')[0].checkValidity();

        if (!isValidForm) {
            $('#fAltaFirmante').find(':submit').click();
            return;
        }

        // Obtener los datos del formulario manual
        firmanteDataGlobal.nombre               = $('#formularioManualFirmante input[name="nombreFirmanteManual"]').val();
        firmanteDataGlobal.tipoCodigoPersona    = $('#formularioManualFirmante input.maskNumeroPersona').val();
        firmanteDataGlobal.tipoDocumento        = $('#formularioManualFirmante select[name="filtroBuscadorClientes.tipoDocumento.id"]').val();
        firmanteDataGlobal.documento            = $('#formularioManualFirmante input[name="codigoDocumentoFirmanteManual"]').val();
        firmanteDataGlobal.escliente            = $('#formularioManualFirmante select[name="esClienteFirmanteManual"] option:selected').text();
        firmanteDataGlobal.codigoIntervencion   = $('#cmbTipoIntervencionFirmante').val();
        firmanteDataGlobal.tipoIntervencion     = $('#cmbTipoIntervencionFirmante option:selected').text();

        // Mostrar los datos en la consola
        console.log('Datos del formulario manual:');
        $('#modalAltaFirmante').modal('hide');
        agregarFirmante();
        // Aquí puedes agregar la lógica para procesar el formulario manual
    } else if ($('#formularioClienteFirmante').is(':visible')) {

        // Validar que se haya seleccionado una opción en cmbTipoIntervencionFirmante
        var tipoIntervencion = $('#cmbTipoIntervencionFirmante').val();
        if (!tipoIntervencion) {
            $('#cmbTipoIntervencionFirmante').focus();
            return;
        } else {
            // Restablecer el mensaje de validación si ya se seleccionó una opción
            $('#cmbTipoIntervencionFirmante')[0].setCustomValidity('');
        }
        firmanteDataGlobal.codigoIntervencion   = $('#cmbTipoIntervencionFirmante').val();
        firmanteDataGlobal.tipoIntervencion     = $('#cmbTipoIntervencionFirmante option:selected').text();        

        // Mostrar los datos cargados del cliente
        if (clienteDataGlobal) {
            firmanteDataGlobal.nombre              = clienteDataGlobal.nombre;
            firmanteDataGlobal.tipoCodigoPersona   = clienteDataGlobal.codigoJ; // Asumimos que es persona física
            firmanteDataGlobal.tipoDocumento       = "DNI"; // Asumimos que es DNI
            firmanteDataGlobal.documento           = clienteDataGlobal.documento;            
            firmanteDataGlobal.escliente           = 'Sí';
        } else {
            console.log('No hay datos del cliente cargados.');
            return;
        }
        $('#modalAltaFirmante').modal('hide');
        agregarFirmante();                
    } else {
        console.log('Ningún formulario está visible.');
    }

}

// Función para agregar firmante -- voy a tener que hacer una para el boton y otra del form
function agregarFirmante() {
    console.log('AGREGAR FIRMANTE');
    console.log('Datos de firmante: ' + JSON.stringify(firmanteDataGlobal));

    var tbody = $('#tablaFirmantes tbody');

    var cuantos = tbody.find('tr').length;
    
    var txtIn = $('[data-target=".colapsarFirmante"]').hasClass('collapsed')? '' : 'in';
    var tr = cuantos > 0? '<tr class="colapsarFirmante collapse '+txtIn+'">':'<tr>';
    
    var $tr = $( tr );

    $tr.append( $( '<td>' ).text( firmanteDataGlobal.nombre ) );
    $tr.append( $( '<td>' ).text( firmanteDataGlobal.tipoCodigoPersona ) );
    $tr.append( $( '<td>' ).attr('hidden', true )
                           .text( firmanteDataGlobal.tipoDocumento ) );
    $tr.append( $( '<td>' ).text( firmanteDataGlobal.documento ) );
    $tr.append( $( '<td>' ).text( firmanteDataGlobal.escliente ) );
    $tr.append( $( '<td>' ).text( firmanteDataGlobal.tipoIntervencion ) );

    $tr.append( $( '<td class="iconAction">' ).append( $('<a class="deleteRow" href="#">').append( $('<span class="glyphicon glyphicon-trash" aria-hidden="true">') ) ) );

    tbody.append( $tr );

    $('#tablaFirmantes').trigger('change');    
}

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