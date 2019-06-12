package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link MSSQLDatabase}
 */
public class MSSQLDatabaseTest extends AbstractJdbcDatabaseTest {

    public MSSQLDatabaseTest() throws Exception {
        super(new MSSQLDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "Microsoft SQL Server";
    }


    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("GETDATE()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void getDefaultDriver() {
        Database database = new MSSQLDatabase();

        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", database.getDefaultDriver("jdbc:sqlserver://localhost;databaseName=liquibase"));

        assertNull(database.getDefaultDriver("jdbc:oracle:thin://localhost;databaseName=liquibase"));
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
        assertEquals("[tableName€]", database.escapeTableName(null, null, "tableName€"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("catalogName.schemaName.tableName", database.escapeTableName("catalogName", "schemaName",
            "tableName"));
        assertEquals("[catalogName€].[schemaName€].[tableName€]", database.escapeTableName("catalogName€",
            "schemaName€", "tableName€"));
    }

//    @Test
//    public void changeDefaultSchemaToAllowedValue() throws Exception {
//        Database database = new MSSQLDatabase();
//        Database dbSpy = PowerMockito.spy(database);
//        when(dbSpy, method(MSSQLDatabase.class, "getConnectionSchemaName", null)).withNoArguments().thenReturn
//            ("myschema");
//        assertNull(dbSpy.getDefaultSchemaName());
//
//        dbSpy.setDefaultSchemaName("myschema");
//        assertEquals("myschema", dbSpy.getDefaultSchemaName());
//    }
//
//    @Test
//    public void changeDefaultSchemaToNull() throws Exception {
//        Database database = new MSSQLDatabase();
//        Database dbSpy = PowerMockito.spy(database);
//        when(dbSpy, method(MSSQLDatabase.class, "getConnectionSchemaName", null)).withNoArguments().thenReturn
//            ("myschema");
//        assertNull(dbSpy.getDefaultSchemaName());
//
//        dbSpy.setDefaultSchemaName(null);
//        assertNull("Changing the default schema to null should be successful.", dbSpy.getDefaultSchemaName());
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void changeDefaultSchemaToForbiddenValue() throws Exception {
//        Database database = new MSSQLDatabase();
//        Database dbSpy = PowerMockito.spy(database);
//        when(dbSpy, method(MSSQLDatabase.class, "getConnectionSchemaName", null)).withNoArguments().thenReturn
//            ("myschema");
//        assertNull(dbSpy.getDefaultSchemaName());
//
//        dbSpy.setDefaultSchemaName("some_other_schema");
//    }

    @Test
    public void testEscapeDataTypeName() {
        Database database = getDatabase();
        assertEquals("MySchema.MyUDT", database.escapeDataTypeName("MySchema.MyUDT"));
        assertEquals("[MySchema€].[MyUDT€]", database.escapeDataTypeName("MySchema€.MyUDT€"));
        assertEquals("MySchema.[MyUDT]", database.escapeDataTypeName("MySchema.[MyUDT]"));
        assertEquals("[MySchema].MyUDT", database.escapeDataTypeName("[MySchema].MyUDT"));
        assertEquals("[MySchema].[MyUDT]", database.escapeDataTypeName("[MySchema].[MyUDT]"));
    }

    @Test
    public void testUnescapeDataTypeName() {
        Database database = getDatabase();
        assertEquals("MySchema.MyUDT", database.unescapeDataTypeName("MySchema.MyUDT"));
        assertEquals("MySchema.MyUDT", database.unescapeDataTypeName("MySchema.[MyUDT]"));
        assertEquals("MySchema.MyUDT", database.unescapeDataTypeName("[MySchema].MyUDT"));
        assertEquals("MySchema.MyUDT", database.unescapeDataTypeName("[MySchema].[MyUDT]"));
    }

    @Test
    public void testUnescapeDataTypeString() {
        Database database = getDatabase();
        assertEquals("int", database.unescapeDataTypeString("int"));
        assertEquals("int", database.unescapeDataTypeString("[int]"));
        assertEquals("decimal(19, 2)", database.unescapeDataTypeString("decimal(19, 2)"));
        assertEquals("decimal(19, 2)", database.unescapeDataTypeString("[decimal](19, 2)"));
    }

    /**
     * Verifies the case of setting the {@link MSSQLDatabase#setDefaultSchemaName(String)}
     * when using {@link OfflineConnection}.
     */
    @Test
    public void testSetDefaultSchemaNameOnOfflineConnection() {
        Database database = getDatabase();
        OfflineConnection offlineConnection = new OfflineConnection("offline:mssql?outputLiquibaseSql=all&changeLogFile=liquibase/database/simpleChangeLog.xml", new ClassLoaderResourceAccessor());
        database.setConnection(offlineConnection);

        try {
            database.setDefaultSchemaName("SCHEMA_NAME");
            assertEquals("SCHEMA_NAME", database.getDefaultSchemaName());
        } catch (Exception rte) {
            assertNull(rte);
//            assertTrue(rte instanceof  RuntimeException);
//            assertTrue(rte.getMessage().contains("Cannot use default schema name SCHEMA_NAME on Microsoft SQL Server"));
//            throw (RuntimeException)rte;
        }

    }
}
