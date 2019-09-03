package com.ramattec.rest.repository

import com.ramattec.rest.model.EmojiPhrase
import com.ramattec.rest.model.EmojiPhrases
import com.ramattec.rest.model.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(){
        Database.connect(hikari())

        transaction {
            SchemaUtils.create(EmojiPhrases,
                Users)

        }
    }
    private fun hikari(): HikariDataSource{
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = System.getenv("JDBC_DATABASE_URL")
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.username = "rafaela"
        config.password = "rafaela"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(
        block: () -> T): T =
            withContext(Dispatchers.IO){
                transaction { block() }
            }
}