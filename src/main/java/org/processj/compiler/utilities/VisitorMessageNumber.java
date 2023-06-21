package org.processj.compiler.utilities;

import java.io.File;
import java.net.URL;
import java.util.Properties;

/**
 * This enum defines error messages generated by a tree-traversal node.
 * 
 * @author ben
 * @version 09/02/2018
 * @since 1.2
 */
public enum VisitorMessageNumber implements PJMessage.MessageNumber {

    // --------------------------------------------------
    // RESOLVE IMPORTS (100-199)
    
    RESOLVE_IMPORTS_100(100, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    RESOLVE_IMPORTS_101(101, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    RESOLVE_IMPORTS_102(102, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    RESOLVE_IMPORTS_103(103, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    RESOLVE_IMPORTS_104(104, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    RESOLVE_IMPORTS_105(105, PJMessage.ErrorSeverity.WARNING, PJMessage.MessageType.PRINT_CONTINUE),
    RESOLVE_IMPORTS_106(106, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),

    // --------------------------------------------------
    // TOP LEVEL DECLARATIONS (200-299)
    
    TOP_LEVEL_DECLS_200(200, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_201(201, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_202(202, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_203(203, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_204(204, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TOP_LEVEL_DECLS_205(205, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_206(206, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_207(207, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    TOP_LEVEL_DECLS_208(208, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),

    // --------------------------------------------------
    // NAME TYPE RESOLUTION (300-399)
    
    NAME_TYPE_RESOLUTION_300(300, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),

    // --------------------------------------------------
    // NAME CHECKER (400-499)
    
    NAME_CHECKER_400(400, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_401(401, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_402(402, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_403(403, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_404(404, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_405(405, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_406(406, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_407(407, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_408(408, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_409(409, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_410(410, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_411(411, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    NAME_CHECKER_412(412, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_413(413, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_414(414, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_415(415, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_416(416, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_417(417, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_418(418, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_419(419, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_420(420, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    NAME_CHECKER_421(421, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),

    // --------------------------------------------------
    // TYPE CHECKER (600-699)
    
    TYPE_CHECKER_600(600, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_601(601, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_602(602, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_603(603, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_604(604, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_605(605, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_606(606, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_607(607, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_608(608, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_609(609, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_610(610, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_611(611, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_612(612, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_613(613, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_614(614, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_615(615, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_616(616, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_617(617, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_618(618, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_619(619, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_620(620, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_621(621, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_622(622, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_623(623, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_624(624, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_625(625, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_626(626, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_627(627, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_628(628, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_629(629, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_630(630, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_631(631, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_632(632, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_633(633, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_634(634, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_635(635, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_636(636, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_637(637, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_638(638, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_639(639, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_640(640, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_641(641, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_642(642, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_643(643, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_644(644, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_645(645, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_646(646, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_647(647, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_648(648, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_649(649, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_650(650, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_651(651, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_652(652, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_653(653, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_654(654, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_655(655, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_656(656, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_657(657, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_658(658, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),
    TYPE_CHECKER_659(659, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_STOP),

    // --------------------------------------------------
    // PARALLEL USAGE CHECKER (700-799)
    
    PARALLEL_USAGE_CHECKER_700(700, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_701(701, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_702(702, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_703(703, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_704(704, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_705(705, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_706(706, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_707(707, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_708(708, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_709(709, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_710(710, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_711(711, PJMessage.ErrorSeverity.WARNING, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_712(712, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_713(713, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_714(714, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    PARALLEL_USAGE_CHECKER_715(715, PJMessage.ErrorSeverity.WARNING, PJMessage.MessageType.PRINT_CONTINUE),

    // --------------------------------------------------
    // REACHABILITY (800-899)
    
    REACHABILITY_800(800, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_801(801, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_802(802, PJMessage.ErrorSeverity.WARNING, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_803(803, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_804(804, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_805(805, PJMessage.ErrorSeverity.WARNING, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_806(806, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_807(807, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_808(808, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_809(809, PJMessage.ErrorSeverity.WARNING, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_810(810, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_811(811, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_812(812, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REACHABILITY_813(813, PJMessage.ErrorSeverity.WARNING, PJMessage.MessageType.PRINT_CONTINUE),
    
    // --------------------------------------------------
    // SEMANTIC CHECKS (900-999)
    
    SEMATIC_CHECKS_900(900, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    SEMATIC_CHECKS_901(901, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    SEMATIC_CHECKS_902(902, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    SEMATIC_CHECKS_903(903, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    
    // --------------------------------------------------
    // REWRITERS (1000-1020)
    
    REWRITE_1000(1000, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REWRITE_1001(1001, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REWRITE_1002(1002, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REWRITE_1003(1003, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REWRITE_1004(1004, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REWRITE_1005(1005, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE),
    REWRITE_1006(1006, PJMessage.ErrorSeverity.ERROR, PJMessage.MessageType.PRINT_CONTINUE)

    ;

    
    // File loader
    private static Properties localizable;
    
    // Local path of properties file in parent directory
    private final static String PATH = "src/main/resources/properties/VisitorMessageNumber.properties";
    
    // The error number
    private final int number;
    
    // The severity level of the error message
    private PJMessage.ErrorSeverity type;
    
    // Message instructed by the compiler
    private PJMessage.MessageType message;
    
    private VisitorMessageNumber(int number, PJMessage.ErrorSeverity type, PJMessage.MessageType message) {
        this.number = number;
        this.type = type;
        this.message = message;
    }
    
    public int getNumber() {
        return number;
    }
    
    public PJMessage.ErrorSeverity getErrorSeverity() {
        return type;
    }
    
    @Override
    public String getMessage() {
        return localizable.getProperty(name());
    }
    
    @Override
    public PJMessage.MessageType getMessageType() {
        return message;
    }
    
    static {
        URL url = PropertiesLoader.getURL(PATH);
        String path = PATH;
        if (url != null)
            path = url.getFile();
        localizable = PropertiesLoader.loadProperties(new File(path));
    }
}
