package com.isb.acelera.persistence;

import com.isb.CustomDataIbatisTest;
import com.isb.acelera.domain.LpaConfigPlantilla;
import com.isb.acelera.mapper.EventoMapperTest;
import com.isb.acelera.util.WebUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@Import(EventoMapperTest.MyBatisTestConfig.class)
@CustomDataIbatisTest(basePackages = {"com.isb.acelera.persistence"})
public class LpaConfigPlantillaMapperTest {

    @Before
    public void setUp() {
        LocaleContextHolder.setDefaultLocale(WebUtils.getDefaultLocale());
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Madrid"));
    }

    @TestConfiguration
    public static class MyBatisTestConfig {

        @Bean
        public String locationPattern() {
            return "classpath:com/isb/acelera/persistence/LpaConfigPlantillaMapper.xml";
        }

        @Bean
        public String typeAliasesPackage() {
            return "com.isb.acelera.type,com.isb.acelera.domain";
        }

        @Bean
        public Class<?>[] typeAliases() {
            return new Class[]{};
        }

    }

    @Autowired
    private LpaConfigPlantillaMapper lpaConfigPlantillaMapper;

    //Plantilla MX - etiqeuta: ADDCHRG__character
    final String VALOR_ORIGEN_MX = "DIVISA_GRIEGA || '#CT=' || RTRIM ( TO_CHAR ( ( NVL ( ABS ( MARGEN_NETO ) , 0 ) + NVL ( CVA , 0 ) + NVL ( ABS ( SALES_CREDIT ) , 0 ) + DECODE ( CLIENT_FAIR_VALUE_SCOPE , 'Y' , NVL ( ADDON_AMOUNT , 0 ) + DECODE ( RENEGOCIACION , 'Y' , NVL ( VALOR_MERCADO_TOTAL , 0 ) , 0 ) , 0 ) ) , 'FM99999999999999999990.99999999' ) , '.' ) || '#0#0#0#0#0#0#0#0#0#0#' || RTRIM ( TO_CHAR ( DECODE ( CLIENT_FAIR_VALUE_SCOPE , 'Y' , NVL ( ADDON_AMOUNT , 0 ) + DECODE ( RENEGOCIACION , 'Y' , NVL ( VALOR_MERCADO_TOTAL , 0 ) , 0 ) , 0 ) , 'FM99999999999999999990.99999999' ) , '.' ) || '#0#0#0#0#0#0#0#0#0#0#0#0'";
    //Plantilla KID  - Etiqueta: StructuralCost_pctg.entry
    final String VALOR_ORIGEN_KID = "RTRIM ( TO_CHAR ( DECODE ( CLIENT_FAIR_VALUE_SCOPE , 'Y' , ( ADDON_AMOUNT + DECODE ( RENEGOCIACION , 'Y' , NVL ( VALOR_MERCADO_TOTAL , 0 ) , 0 ) ) * '100' / NOMINAL_DIVISA_GRIEGA , '0' ) , 'FM99999999999999999990.99999999' ) , '.' )";
    //Plantilla KID  - Etiqueta: AC_CostesGastos
    final String VALOR_ORIGEN_CONTRATOS = "RTRIM ( TO_CHAR ( DECODE ( MARGEN_NETO , NULL , 0 , ABS ( MARGEN_NETO ) ) + DECODE ( CVA , NULL , 0 , CVA ) + DECODE ( SALES_CREDIT , NULL , 0 , ABS ( SALES_CREDIT ) ) + DECODE ( CLIENT_FAIR_VALUE_SCOPE , 'Y' , NVL ( ADDON_AMOUNT , 0 ) + DECODE ( RENEGOCIACION , 'Y' , NVL ( VALOR_MERCADO_TOTAL , 0 ) , 0 ) , 0 ) , 'FM99999999999999999990.99' ) , '.' )";
    //Plantilla sentinel  - Etiqueta: cost_and_charges
    final String VALOR_ORIGEN_SENTINEL = "RTRIM ( TO_CHAR ( DECODE ( MARGEN_NETO , NULL , 0 , ABS ( MARGEN_NETO ) ) + DECODE ( CVA , NULL , 0 , CVA ) + DECODE ( SALES_CREDIT , NULL , 0 , ABS ( SALES_CREDIT ) ) + DECODE ( CLIENT_FAIR_VALUE_SCOPE , 'Y' , NVL ( ADDON_AMOUNT , 0 ) + DECODE ( RENEGOCIACION , 'Y' , NVL ( VALOR_MERCADO_TOTAL , 0 ) , 0 ) , 0 ) , 'FM99999999999999999990.99' ) , '.' )";

    @Test
    public void testGetValueTableXml_MX() {
        final String TIPO_ORIGEN = "ACE_ACUM_COTIZACION";

        List<LpaConfigPlantilla> listaConfigPorTipo = getLpaConfigPlantillas(TIPO_ORIGEN, VALOR_ORIGEN_MX);

        List<String> result = lpaConfigPlantillaMapper.getValueTableXml(
                listaConfigPorTipo, "ETIQUETA", "27531", null, null);

        assertThat(result.toString()).contains("EUR#CT=");
        assertThat(result.toString()).contains("#0#0#0#0#0#0#0#0#0#0#");
        assertThat(result.toString()).contains("ETIQUETA");
        assertThat(result.toString()).contains("SubEtiqueta");
    }

    @Test
    public void testGetValueTableXml_KID() {
        final String TIPO_ORIGEN = "ACE_ACUM_COTIZACION";

        List<LpaConfigPlantilla> listaConfigPorTipo = getLpaConfigPlantillas(TIPO_ORIGEN, VALOR_ORIGEN_KID);

        List<String> result = lpaConfigPlantillaMapper.getValueTableXml(
                listaConfigPorTipo, "ETIQUETA", "27531", null, null);

        assertThat(result.toString()).contains("0");
        assertThat(result.toString()).contains("ETIQUETA");
        assertThat(result.toString()).contains("SubEtiqueta");
    }

    @Test
    public void testGetValueTableXml_Contratos() {
        final String TIPO_ORIGEN = "ACE_ACUM_COTIZACION";
        List<LpaConfigPlantilla> listaConfigPorTipo = getLpaConfigPlantillas(TIPO_ORIGEN, VALOR_ORIGEN_CONTRATOS);

        List<String> result = lpaConfigPlantillaMapper.getValueTableXml(
                listaConfigPorTipo, "ETIQUETA", "27531", null, null);

        assertThat(result.toString()).contains("217");
        assertThat(result.toString()).contains("ETIQUETA");
        assertThat(result.toString()).contains("SubEtiqueta");
    }

    @Test
    public void testGetValueTableXml_Sentinel() {
        final String TIPO_ORIGEN = "ACE_ACUM_COTIZACION";

        List<LpaConfigPlantilla> listaConfigPorTipo = getLpaConfigPlantillas(TIPO_ORIGEN, VALOR_ORIGEN_SENTINEL);

        List<String> result = lpaConfigPlantillaMapper.getValueTableXml(
                listaConfigPorTipo, "ETIQUETA", "27531", null, null);

        assertThat(result.toString()).contains("217");
        assertThat(result.toString()).contains("ETIQUETA");
        assertThat(result.toString()).contains("SubEtiqueta");
    }

    @NotNull
    private static List<LpaConfigPlantilla> getLpaConfigPlantillas(String TIPO_ORIGEN, String VALOR_ORIGEN) {

        List<LpaConfigPlantilla> listaConfigPorTipo = new ArrayList<>();
        LpaConfigPlantilla lpaConfigPlantilla = new LpaConfigPlantilla();
        lpaConfigPlantilla.setEntidad("0049");
        lpaConfigPlantilla.setLpaEtiqueta("SubEtiqueta");
        lpaConfigPlantilla.setTipoOrigen(TIPO_ORIGEN);
        lpaConfigPlantilla.setValorOrigen(VALOR_ORIGEN);
        listaConfigPorTipo.add(lpaConfigPlantilla);
        return listaConfigPorTipo;
    }

}
