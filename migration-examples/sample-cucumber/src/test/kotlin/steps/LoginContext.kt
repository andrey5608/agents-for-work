package steps

data class LoginResponse(
    val status: Int,
    val token: String? = null,
    val errorCode: String? = null,
)

class LoginContext {
    var lastResponse: LoginResponse? = null
    val registeredUsers: MutableMap<String, String> = mutableMapOf()
}
