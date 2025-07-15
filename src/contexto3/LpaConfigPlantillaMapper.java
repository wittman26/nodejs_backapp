package com.isb.acelera.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.isb.acelera.domain.LpaConfigOrigen;
import com.isb.acelera.domain.LpaConfigPlantilla;
import com.isb.acelera.domain.LpaPlantilla;

/**
 * Interface LpaConfigPlantillaMapper. Interfaz de operaciones contra tabla
 * ACE_LPA_CONFIG_PLANT
 */
@Repository
public interface LpaConfigPlantillaMapper {

    /**
     * Obtiene el tipo origen de la plantilla.
     *
     * @param idEntidad   la entidad
     * @param idPlantilla el id de plantilla
     * @return tipo origen de la plantilla
     */
    List<LpaConfigOrigen> getTipoOrigenPlantilla(@Param("id_entidad") String idEntidad,
            @Param("id_plantilla") String idPlantilla);

    /**
     * Obtiene el id de la plantilla hija.
     *
     * @param idEntidad   la entidad
     * @param idPlantilla el id de plantilla
     * @return id plantilla hija
     */
    List<String> getEtiquetaPlantilla(@Param("id_entidad") String idEntidad, @Param("id_plantilla") String idPlantilla);

    /**
     * Obtiene la configuracion de la plantilla.
     *
     * @param idEntidad   la entidad
     * @param idPlantilla el id de plantilla
     * @param tipoOrigen  el tipo de origen
     * @return la configuracion de la plantilla
     */
    List<LpaConfigPlantilla> getConfigPlantilla(@Param("id_entidad") String idEntidad,
            @Param("id_plantilla") String idPlantilla, @Param("tipo_origen") String tipoOrigen,
            @Param("condicion") String condicion);

    /**
     * @param plantilla LpaConfigPlantilla
     */
    void deleteConfigPlantilla(LpaConfigPlantilla plantilla);

    /**
     * @param plantilla LpaConfigPlantilla
     */
    void editConfigPlantilla(LpaConfigPlantilla plantilla);

    /**
     * @param plantilla LpaConfigPlantilla
     */
    void addConfigPlantilla(LpaConfigPlantilla plantilla);

    /**
     * Obtiene el valor de la table xml para campañas.
     *
     * @param plantillaTipo tipo de plantilla
     * @param etiquetaLista etiqueta de lista
     * @param idOperacion   la operacion
     * @param idAlternativo el idAlternativo
     * @param idSgcDocCamp  el idSgcDocCamp
     * @return valor tabla xml
     */
    List<String> getValueTableXml(@Param("listalpaConfigPlantilla") List<LpaConfigPlantilla> plantillaTipo,
            @Param("etiquetaLista") String etiquetaLista, @Param("idOperacion") String idOperacion,
            @Param("idAlternativo") String idAlternativo, @Param("idSgcDocCamp") String idSgcDocCamp);

    /**
     * Obtiene tabla.
     *
     * @param plantilla la plantilla
     * @param id        la operacion
     * @return la tabla
     */
    String getValueTable(@Param("lpaConfigPlantilla") LpaConfigPlantilla plantilla, @Param("idOperacion") String id,  @Param("idAlternativo") String idAlternativo);
       

    /**
     * @param idCampanya el idCampanya
     * @param nombre     el nombre
     * @return valueTableCampanyas
     */
    String getValueTableCampanyas(@Param("idCampanya") String idCampanya, @Param("nombre") String nombre);

    /**
     * @param plantilla LpaPlantilla
     */
    void deleteAllConfigPlantilla(LpaPlantilla plantilla);

    List<String> getStringsContainingCamposDisponiblesFromPlantillasCampanyas(String tipoProductoPlantillaCampanya);
    
    /**
     * @param plantilla plantilla
     * @param etiqueta etiqueta
     * @return valor
     */
    String getValueFromPlantillaAndEtiqueta(@Param("plantilla") String plantilla, @Param("etiqueta") String etiqueta);

}
