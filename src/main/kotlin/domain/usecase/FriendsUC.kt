package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.friends.Friends

interface FriendsUC {
    /**
     * Gets the user's friends.
     */
    fun getFriends(userId: Int): Friends
}
