// ...existing imports...

@RunWith(SpringRunner.class)
@Import(EventoMapperTest.MyBatisTestConfig.class)
@CustomDataIbatisTest(basePackages = {"com.isb.acelera.persistence"})
public class LpaConfigPlantillaMapperTest {

    // ...existing setup and config...

    @Autowired
    private LpaConfigPlantillaMapper lpaConfigPlantillaMapper;

    private void assertValueTableXml(String tipoOrigen, String valorOrigen, String operacion, String[] expectedContents) {
        List<LpaConfigPlantilla> listaConfigPorTipo = getLpaConfigPlantillas(tipoOrigen, valorOrigen);
        List<String> result = lpaConfigPlantillaMapper.getValueTableXml(
                listaConfigPorTipo, "ETIQUETA", operacion, null, null);
        String resultStr = result.toString();
        for (String expected : expectedContents) {
            assertThat(resultStr).contains(expected);
        }
    }

    @Test
    public void testGetValueTableXml_MX() {
        assertValueTableXml("ACE_ACUM_COTIZACION", VALOR_ORIGEN_MX, "27531",
                new String[]{"EUR#CT=", "#0#0#0#0#0#0#0#0#0#0#", "ETIQUETA", "SubEtiqueta"});
    }

    @Test
    public void testGetValueTableXml_KID() {
        assertValueTableXml("ACE_ACUM_COTIZACION", VALOR_ORIGEN_KID, "27531",
                new String[]{"0", "ETIQUETA", "SubEtiqueta"});
    }

    @Test
    public void testGetValueTableXml_Contratos() {
        assertValueTableXml("ACE_ACUM_COTIZACION", VALOR_ORIGEN_CONTRATOS, "27531",
                new String[]{"217", "ETIQUETA", "SubEtiqueta"});
    }

    @Test
    public void testGetValueTableXml_Sentinel() {
        assertValueTableXml("ACE_ACUM_COTIZACION", VALOR_ORIGEN_SENTINEL, "27531",
                new String[]{"217", "ETIQUETA", "SubEtiqueta"});
    }

    @NotNull
    private static List<LpaConfigPlantilla> getLpaConfigPlantillas(String tipoOrigen, String valorOrigen) {
        List<LpaConfigPlantilla> listaConfigPorTipo = new ArrayList<>();
        LpaConfigPlantilla lpaConfigPlantilla = new LpaConfigPlantilla();
        lpaConfigPlantilla.setEntidad("0049");
        lpaConfigPlantilla.setLpaEtiqueta("SubEtiqueta");
        lpaConfigPlantilla.setTipoOrigen(tipoOrigen);
        lpaConfigPlantilla.setValorOrigen(valorOrigen);
        listaConfigPorTipo.add(lpaConfigPlantilla);
        return listaConfigPorTipo;
    }
}