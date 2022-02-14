package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.friends.Friends
import com.bitwiserain.pbbg.app.domain.model.friends.Friendship

interface FriendsUC {
    /**
     * Gets the user's friends.
     */
    fun getFriends(userId: Int): Friends

    /**
     * @throws SelfFriendshipException when the current and target users are the same.
     * @throws TargetUserDoesNotExistException when the target user does not exist.
     * @throws FriendshipNotNoneException when the current friendship is anything but [Friendship.NONE].
     */
    fun addFriend(currentUserId: Int, targetUserId: Int): Friendship

    /**
     * @throws SelfFriendshipException when the current and target users are the same.
     * @throws TargetUserDoesNotExistException when the target user does not exist.
     * @throws FriendshipNotConfirmedException when the current friendship is anything but [Friendship.CONFIRMED].
     */
    fun removeFriend(currentUserId: Int, targetUserId: Int): Friendship

    /**
     * @throws SelfFriendshipException when the current and target users are the same.
     * @throws TargetUserDoesNotExistException when the target user does not exist.
     * @throws FriendshipNotRequestReceivedException when the current friendship is anything but [Friendship.REQUEST_RECEIVED].
     */
    fun acceptRequest(currentUserId: Int, targetUserId: Int): Friendship

    /**
     * @throws SelfFriendshipException when the current and target users are the same.
     * @throws TargetUserDoesNotExistException when the target user does not exist.
     * @throws FriendshipNotRequestSentException when the current friendship is anything but [Friendship.REQUEST_SENT].
     */
    fun cancelRequest(currentUserId: Int, targetUserId: Int): Friendship
}

object SelfFriendshipException : Exception()
object FriendshipNotNoneException : Exception()
object FriendshipNotConfirmedException : Exception()
object FriendshipNotRequestReceivedException : Exception()
object FriendshipNotRequestSentException : Exception()
