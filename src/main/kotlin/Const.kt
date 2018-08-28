package pbbg

const val USERNAME_MIN_LENGTH = 1
const val USERNAME_MAX_LENGTH = 15
const val USERNAME_REGEX = "[A-Za-z0-9_]{$USERNAME_MIN_LENGTH,$USERNAME_MAX_LENGTH}"
const val USERNAME_REGEX_DESCRIPTION = "Username must consist of $USERNAME_MIN_LENGTH-$USERNAME_MAX_LENGTH letters, numbers, and/or underscores."

const val PASSWORD_MIN_LENGTH = 6
const val PASSWORD_REGEX = ".{$PASSWORD_MIN_LENGTH,}"
const val PASSWORD_REGEX_DESCRIPTION = "Password must consist of at least $PASSWORD_MIN_LENGTH characters."
