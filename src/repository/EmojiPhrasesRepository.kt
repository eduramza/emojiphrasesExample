package com.ramattec.rest.repository

import com.ramattec.rest.model.*
import com.ramattec.rest.repository.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class EmojiPhrasesRepository : Repository{
    override suspend fun add(userId: String, emojiV: String, phraseV: String)  =
        dbQuery {
            val insertStmt = EmojiPhrases.insert {
                it[user] = userId
                it[emoji] = emojiV
                it[phrase] = phraseV
            }
            val result = insertStmt.resultedValues?.get(0)
            if (result != null){
                toEmojiPhrase(result)
            } else {
                null
            }
        }


    override suspend fun phrase(id: Int): EmojiPhrase? = dbQuery{
        EmojiPhrases.select{
            (EmojiPhrases.id eq id)
        }.mapNotNull { toEmojiPhrase(it) }
            .singleOrNull()
    }

    override suspend fun phrase(id: String): EmojiPhrase? {
        return phrase(id.toInt())
    }

    override suspend fun phrases(): List<EmojiPhrase> = dbQuery {
        EmojiPhrases.selectAll().map { toEmojiPhrase(it) }
    }

    override suspend fun remove(id: Int): Boolean {
        if (phrase(id) == null){
            throw IllegalArgumentException("No phrase found for id $id.")
        }
        return dbQuery {
            EmojiPhrases.deleteWhere { EmojiPhrases.id eq id } > 0
        }
    }

    override suspend fun remove(id: String): Boolean {
        return remove(id.toInt())
    }

    override suspend fun clear() {
        EmojiPhrases.deleteAll()
    }

    override suspend fun user(userId: String, hash: String?): User? {
        val user = dbQuery {
            Users.select{
                (Users.id eq userId)
            }.mapNotNull { toUser(it) }
                .singleOrNull()
        }

        return when{
            user == null -> null
            hash == null -> user
            user.passwordHash == hash -> user
            else -> null
        }
    }

    override suspend fun userByEmail(email: String): User? = dbQuery {
        Users.select{ Users.email.eq(email) }
            .map { User(
                it[Users.id],
                email,
                it[Users.displayName],
                it[Users.passwordHash]
            ) }.singleOrNull()
    }

    override suspend fun userById(userId: String) = dbQuery {
        Users.select{ Users.id.eq(userId) }
            .map { User(userId, it[Users.email], it[Users.displayName], it[Users.passwordHash]) }.singleOrNull()
    }

    override suspend fun createUser(user: User) = dbQuery {
        Users.insert {
            it[id] = user.userId
            it[displayName] = user.displayName
            it[email] = user.email
            it[passwordHash] = user.passwordHash
        }
        Unit
    }

    private fun toEmojiPhrase(row: ResultRow): EmojiPhrase =
        EmojiPhrase(
            id = row[EmojiPhrases.id].value,
            userId = row[EmojiPhrases.user],
            emoji = row[EmojiPhrases.emoji],
            phrase = row[EmojiPhrases.phrase]
        )

    private fun toUser(row: ResultRow): User =
        User(
            userId = row[Users.id],
            email = row[Users.email],
            displayName = row[Users.displayName],
            passwordHash = row[Users.passwordHash]
        )
}