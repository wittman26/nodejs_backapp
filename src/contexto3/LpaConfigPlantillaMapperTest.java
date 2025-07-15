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
//        public String typeAliasesPackage() {
//            return "com.isb.acelera.type,com.isb.acelera.domain";
//        }
        public String typeAliasesPackage() { return "com.isb.acumuladores.model";}

        @Bean
        public Class<?>[] typeAliases() {
            return new Class[]{};
        }

    }

    @Autowired
    private LpaConfigPlantillaMapper lpaConfigPlantillaMapper;

    @Test
    public void testGetValueTableXml() {

        List<LpaConfigPlantilla> listaConfigPorTipo = new ArrayList<LpaConfigPlantilla>();
        LpaConfigPlantilla lpaConfigPlantilla = new LpaConfigPlantilla();
        lpaConfigPlantilla.setEntidad("0049");
        lpaConfigPlantilla.setId("MUREX_RF");
        lpaConfigPlantilla.setLpaEtiqueta("idMurex");
        lpaConfigPlantilla.setTipoOrigen("ACE_ACUM_OPERACION");
        lpaConfigPlantilla.setValorOrigen("'ACE_' || LPAD ( ID_OPERACION , 12 , '0' ) ");
        listaConfigPorTipo.add(lpaConfigPlantilla);

        List<String> result = lpaConfigPlantillaMapper.getValueTableXml(
                listaConfigPorTipo, "ETIQUETA", "1", null, "CAMPO");

        assertThat(result.toString()).contains("DIVISA");
    }

}
