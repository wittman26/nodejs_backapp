package com.acelera.fx.digitalsignature.infrastructure.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeSignatureConstants {
    public static final String VALIDATED_BO_DEFAULT = "PENDING";
    public static final String ORIGIN_EVENT = "EVENT";
    public static final String ORIGIN_TRADE = "TRADE";

    public static final String OPER_CODE_TRADE = "ACE";
    public static final String OPER_CODE_EVENT = "ACEV";
    public static final String SOURCE_APP_URL = "/v1/trades-signatures/expedients/{id}?status={status}";
    public static final String CLAUSULA_MANUSCRITA = "Y";
    public static final String DIGITALIZADOR= "OSP";
    public static final String TITULO_PRECONTRACTUAL = "Información Precontractual FX";
    public static final String TITULO_CONTRACTUAL = "Información Contractual FX";

    public static final String ACELERA = "ACELERA";
    public static final String DERIVADO_DIV = "Derivado Divisa";
    public static final String B092 = "B092";
    public static final String DIVISAS = "divisas";
    public static final String CONT_DER_DIV = "Contratación Derivado Divisa";
    public static final String CHAN_OFI = "OFI";
}
