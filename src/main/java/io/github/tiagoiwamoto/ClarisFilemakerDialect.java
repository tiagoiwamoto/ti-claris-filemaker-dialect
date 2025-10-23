package io.github.tiagoiwamoto;

import org.hibernate.LockMode;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;

import static org.hibernate.type.SqlTypes.BLOB;
import static org.hibernate.type.SqlTypes.DATE;
import static org.hibernate.type.SqlTypes.DOUBLE;
import static org.hibernate.type.SqlTypes.TIME;
import static org.hibernate.type.SqlTypes.TIMESTAMP;
import static org.hibernate.type.SqlTypes.VARCHAR;

/**
 * Claris/FileMaker dialect adapted for Hibernate 7.x (7.1+).
 *
 * This implementation registers the basic DDL type names used by the
 * legacy FileMaker dialect (varchar/string, double/decimal, date, time,
 * timestamp, blob) via the DdlTypeRegistry and registers a set of SQL
 * functions using the FunctionContributions registry.
 *
 * It intentionally keeps the surface area small and conservative; if you
 * need more advanced behaviour (custom JdbcType implementations, specific
 * SQL AST translators or mutation strategies) those should be implemented
 * via additional contributors (TypeContributor, SqlAstTranslatorFactory, etc.).
 */
public class ClarisFilemakerDialect extends Dialect {

    private static final DatabaseVersion MINIMUM_VERSION = DatabaseVersion.make(1, 0, 0);

    public ClarisFilemakerDialect(DialectResolutionInfo info) {
        this(staticDetermineDatabaseVersion(info));
        registerKeywords(info);
    }

    public ClarisFilemakerDialect() {
        this(MINIMUM_VERSION);
    }

    public ClarisFilemakerDialect(DatabaseVersion version) {
        super(version);
    }

    @Override
    public DatabaseVersion determineDatabaseVersion(DialectResolutionInfo info) {
        return staticDetermineDatabaseVersion(info);
    }

    private static DatabaseVersion staticDetermineDatabaseVersion(DialectResolutionInfo info) {
        final DatabaseVersion version = info.makeCopyOrDefault(MINIMUM_VERSION);
        return info.getDatabaseVersion() != null
                ? DatabaseVersion.make(version.getMajor(), version.getMinor(), 0)
                : version;
    }

    @Override
    protected DatabaseVersion getMinimumSupportedVersion() {
        return MINIMUM_VERSION;
    }

    @Override
    protected void registerColumnTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        // delegate base registration first
        super.registerColumnTypes(typeContributions, serviceRegistry);

        final DdlTypeRegistry ddlTypeRegistry = typeContributions.getTypeConfiguration().getDdlTypeRegistry();

        // Map the SQL types used by the legacy dialect to DDL type names
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(VARCHAR, "varchar", this));
        // The legacy dialect also used the name "string" for varchar columns
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(VARCHAR, "string", this));

        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(DOUBLE, "double", this));
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(DOUBLE, "decimal", this));

        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(DATE, "date", this));
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(TIME, "time", this));
        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(TIMESTAMP, "timestamp", this));

        ddlTypeRegistry.addDescriptor(new DdlTypeImpl(BLOB, "blob", this));
    }

    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        // keep default contributions; if you need custom JdbcType descriptors they can be added here
        super.contributeTypes(typeContributions, serviceRegistry);

        // Example: if you wanted to register a JdbcType replacement you would use:
        // final JdbcTypeRegistry jdbcTypeRegistry = typeContributions.getTypeConfiguration().getJdbcTypeRegistry();
        // jdbcTypeRegistry.addDescriptor(MyCustomJdbcType.INSTANCE);
    }

    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        super.initializeFunctionRegistry(functionContributions);

        final var registry = functionContributions.getFunctionRegistry();

        // Numeric aggregations and basic math
        registry.registerPattern("sum", "sum(?1)");
        registry.registerPattern("avg", "avg(?1)");
        registry.registerPattern("count", "count(?1)");
        registry.registerPattern("max", "max(?1)");
        registry.registerPattern("min", "min(?1)");

        // String functions
        registry.registerPattern("chr", "chr(?1)");
        registry.registerPattern("rtrim", "rtrim(?1)");
        registry.registerPattern("trim", "trim(?1)");
        registry.registerPattern("ltrim", "ltrim(?1)");
        registry.registerPattern("upper", "upper(?1)");
        registry.registerPattern("lower", "lower(?1)");
        registry.registerPattern("left", "left(?1,?2)");
        registry.registerPattern("right", "right(?1,?2)");
        registry.registerPattern("substring", "substring(?1,?2,?3)");
        registry.registerPattern("space", "space(?1)");
        registry.registerPattern("length", "length(?1)");
        registry.registerPattern("instr", "instr(?1,?2)");
        registry.registerPattern("substr", "substr(?1,?2,?3)");

        // Date/time related
        registry.registerPattern("curdate", "current_date");
        registry.registerPattern("current_date", "current_date");
        registry.registerPattern("curtime", "current_time");
        registry.registerPattern("current_time", "current_time");
        registry.registerPattern("time", "time(?1)");

        // Numeric and trig functions
        registry.registerPattern("abs", "abs(?1)");
        registry.registerPattern("atan", "atan(?1)");
        registry.registerPattern("atan2", "atan2(?1,?2)");
        registry.registerPattern("ceiling", "ceiling(?1)");
        registry.registerPattern("ceil", "ceil(?1)");
        registry.registerPattern("degrees", "degrees(?1)");
        registry.registerPattern("radians", "radians(?1)");
        registry.registerPattern("mod", "mod(?1,?2)");
        registry.registerPattern("exp", "exp(?1)");
        registry.registerPattern("floor", "floor(?1)");
        registry.registerPattern("round", "round(?1)");
        registry.registerPattern("sqrt", "sqrt(?1)");
        registry.registerPattern("sin", "sin(?1)");
        registry.registerPattern("tan", "tan(?1)");

        // Misc
        registry.registerPattern("username", "user");
        registry.registerPattern("user", "user");
        registry.registerPattern("current_user", "current_user");
        registry.registerPattern("pi", "pi()");
        registry.registerPattern("numval", "cast(?1 as double)");
        registry.registerPattern("strval", "cast(?1 as varchar)");

        // Add any other FileMaker-specific functions you relied on here following the same pattern.
    }

    // Capability flags and other behaviour mirroring the old FileMakerDialect

    @Override
    public boolean dropConstraints() {
        return false;
    }

    // The legacy dialect indicated no ALTER TABLE support for FileMaker - keep conservative defaults
    public boolean hasAlterTable() {
        return false;
    }

    public boolean supportsColumnCheck() {
        return false;
    }

    public boolean supportsCascadeDelete() {
        return false;
    }

    public boolean supportsLockTimeouts() {
        return false;
    }

    public boolean canCreateSchema() {
        return false;
    }

    public String getCurrentTimestampSelectString() {
        // Return a reasonable default SQL expression for current timestamp
        return "current_timestamp";
    }

    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }

    @Override
    public boolean supportsCurrentTimestampSelection() {
        return true;
    }

    @Override
    public String toBooleanValueString(boolean bool) {
        return bool ? "{b'true'}" : "{b'false'}";
    }

    // FileMaker did not support FOR UPDATE semantics in the legacy dialect - keep empty strings
    @Override
    public String getForUpdateString() {
        return "";
    }

    public String getForUpdateString(String aliases) {
        return "";
    }

    public String getForUpdateNowaitString() {
        return "";
    }

    public String getForUpdateNowaitString(String aliases) {
        return "";
    }

    public String getForUpdateString(LockMode lockMode) {
        return "";
    }
}
