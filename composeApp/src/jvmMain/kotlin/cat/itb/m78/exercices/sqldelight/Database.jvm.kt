package cat.itb.m78.exercices.sqldelight

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import cat.itb.m78.exercices.db.Database
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString

actual fun createDriver(): SqlDriver {
    val dbDir = System.getProperty("user.home")
    val file = Path(dbDir, "myDatabase.db")
    val driver = JdbcSqliteDriver("jdbc:sqlite:${file.absolutePathString()}")
    migrateIfNeeded(driver, Database.Schema)
    return driver
}

fun migrateIfNeeded(driver: JdbcSqliteDriver, schema: SqlSchema<QueryResult.Value<Unit>>) {
    val currentVer = readVersion(driver)
    val schemaVer = schema.version
    if (currentVer == 0L) {
        schema.create(driver)
        updateVersion(driver, schemaVer)
    } else {
        if (schemaVer > currentVer) {
            schema.migrate(driver, currentVer, schemaVer)
            updateVersion(driver, schemaVer)
        } else if(currentVer > schemaVer){
            throw UnsupportedOperationException("Database can't downgrade from $currentVer to $schemaVer")
        }
    }
}

private fun readVersion(driver: JdbcSqliteDriver): Long {
    val mapper = { cursor: SqlCursor ->
        QueryResult.Value(if (cursor.next().value) cursor.getLong(0) else null)
    }
    return driver.executeQuery(null, "PRAGMA user_version", mapper, 0, null).value ?: 0L
}

private fun updateVersion(driver: JdbcSqliteDriver, version: Long) {
    driver.execute(null, "PRAGMA user_version = $version", 0, null)
}