package com.bitwiserain.pbbg.db.repository

import org.jetbrains.exposed.sql.*

object FriendsTable : Table() {
    val userId1 = reference("user_id_1", UserTable).primaryKey()
    val userId2 = reference("user_id_2", UserTable).primaryKey()
    val confirmed = bool("confirmed")

    fun areFriends(userId1: Int, userId2: Int): Boolean = select(exists(
        select {
            ((FriendsTable.userId1.eq(userId1) and FriendsTable.userId2.eq(userId2)) or (FriendsTable.userId1.eq(userId2) and FriendsTable.userId2.eq(userId1)))
                .and(FriendsTable.confirmed.eq(true))
        }
    )).any()
}
