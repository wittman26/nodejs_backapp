package com.isb.acelera.persistence;

import com.isb.CustomDataIbatisTest;
import com.isb.acelera.domain.LpaConfigPlantilla;
import com.isb.acelera.mapper.EventoMapperTest;
import com.isb.acelera.util.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

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

    @Test
    public void testGetValueTableXml() {
        LpaConfigPlantilla lpaConfigPlantilla = new LpaConfigPlantilla();
        lpaConfigPlantilla.setEntidad("0049");
        lpaConfigPlantilla.setId("ACUM_UDF_TODOS");
        lpaConfigPlantilla.setLpaEtiqueta("ADDCHRG__character");
        lpaConfigPlantilla.setValorOrigen("DIVISA_GRIEGA || '#CT=' || RTRIM ( TO_CHAR ( ( NVL ( ABS ( MARGEN_NETO ) , 0 ) + NVL ( CVA , 0 ) + NVL ( ABS ( SALES_CREDIT ) , 0 ) + DECODE ( CLIENT_FAIR_VALUE_SCOPE , 'Y' , NVL ( ADDON_AMOUNT , 0 ) + DECODE ( RENEGOCIACION , 'Y' , NVL ( VALOR_MERCADO_TOTAL , 0 ) ,0 ) , 0 ) ) , 'FM99999999999999999990.99999999' ) , '.' ) || '#0#0#0#0#0#0#0#0#0#0#' || RTRIM ( TO_CHAR ( DECODE ( CLIENT_FAIR_VALUE_SCOPE , 'Y' , NVL ( ADDON_AMOUNT , 0 ) + DECODE ( RENEGOCIACION , 'Y' , NVL ( VALOR_MERCADO_TOTAL , 0 ) ,0 ) , 0 ) , 'FM99999999999999999990.99999999' ) , '.' ) || '#0#0#0#0#0#0#0#0#0#0#0#0'");

        List<LpaConfigPlantilla> listaConfig = List.of(lpaConfigPlantilla);

        List<String> result = lpaConfigPlantillaMapper.getValueTableXml(listaConfig, null, "1", null, null);

        assertThat(result.toString()).contains("DIVISA");
    }

}
