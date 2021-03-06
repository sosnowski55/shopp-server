package bloczek.pl.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import javax.sql.DataSource

object DatabaseFactory {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun connectAndMigrate(config: ApplicationConfig) {
        log.info("Initialising database")
        val pool = hikari(config)
        Database.connect(pool)
//        runFlyway(pool)
    }

    private fun hikari(appConfig: ApplicationConfig): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = appConfig.property("storage.driverClassName").getString()
            jdbcUrl = appConfig.property("storage.jdbcURL").getString()
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    private fun runFlyway(datasource: DataSource) {
        val flyway = Flyway.configure()
            .dataSource(datasource)
            .locations("bloczek/pl/db/migrations")
            .lockRetryCount(100)
            .load()
        try {
            flyway.info()
            flyway.migrate()
        } catch (e: Exception) {
            log.error("Exception running flyway migration", e)
            throw e
        }
        log.info("Flyway migration has finished")
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T
    ): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}
