@Getter
public enum TypeEnum {
    KD("KD"),

    CO("CO"),

    KE("KE"),

    EV("EV"),

    OM("OM");

    private String value;

    TypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String value) {
        for (TypeEnum b : TypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeResponse {

    private TypeEnum type;

    private Boolean isPrecontractual;

    private String documentalType;

    private String documentalTypeCode;

    public DocumentTypeResponse type(TypeEnum type) {
        this.type = type;
        return this;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocumentParameters {
    private String documentType;

    private Boolean isPrecontractual;

    private String documentalType;

    private String documentalTypeCode;
}

@Mapper(imports = {com.acelera.fx.digitalsignature.infrastructure.entity.TypeEnum.class})
public interface ProductDocumentsMapper {
    ProductDocumentsMapper INSTANCE = Mappers.getMapper(ProductDocumentsMapper.class);

    @Mapping(target = "type", expression = "java(com.acelera.fx.digitalsignature.infrastructure.entity.TypeEnum.valueOf(productDocumentParameters.getDocumentType()))")
    DocumentTypeResponse toDocumentTypeResponse(ProductDocumentParameters productDocumentParameters);
}

public class ProductDocumentsMapperTest {
    private final ProductDocumentsMapper MAPPER = ProductDocumentsMapper.INSTANCE;

    private static final PodamFactoryImpl PODAM_FACTORY = new PodamFactoryImpl();

    @Test
    void testMapper() throws IOException {

        ProductDocumentParameters input = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);

        DocumentTypeResponse expected = new DocumentTypeResponse();

        expected.setType(TypeEnum.valueOf(input.getDocumentType()));
        expected.setIsPrecontractual(input.getIsPrecontractual());
        expected.setDocumentalType(input.getDocumentalType());
        expected.setDocumentalTypeCode(input.getDocumentalTypeCode());

        DocumentTypeResponse result = MAPPER.toDocumentTypeResponse(input);

        assertThat(result).as("ProductDocumentParameters").isNotNull().usingRecursiveComparison().isEqualTo(expected);

    }

    @Test
    void testMapper_invalidEnumValue() {
        ProductDocumentParameters input = ProductDocumentParameters.builder()
                .documentType("INVALID")
                .isPrecontractual(true)
                .documentalType("TYPE")
                .documentalTypeCode("CODE")
                .build();
        // Espera que lance IllegalArgumentException
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> MAPPER.toDocumentTypeResponse(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unexpected value");
    }
}