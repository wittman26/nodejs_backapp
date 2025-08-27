package com.acelera.broker.microlito.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentLpaResponse {
    private Integer idOperacion;
    private Integer idOrden;
    private Integer idEvento;
    private String entidad;
    private ComboBean tipoDocumento;
    private String nombreDocumento;
    private String nombreLPA;
    private ComboUsuarioFecha alta;
    private ComboUsuarioFecha modif;
    private String descripcionDocumento;
    private String sentido;
    private String idDocumentoLpa;
    private Integer idLpa;
    private String datos;
    private ComboBean tipoProducto;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ComboBean {
        private String descripcion;
        private String idioma;
        private String tipo;
        private String id;
        private String valorAntiguo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ComboUsuarioFecha {
        private String usuario;
        private Timestamp tmsp;
    }
}
