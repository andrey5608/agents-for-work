package steps

import io.qameta.allure.Step

class LoginApiClient {

    @Step("POST /auth/login with username \"{0}\"")
    fun login(username: String, password: String, registered: Map<String, String>): LoginResponse {
        if (username.isBlank()) {
            return LoginResponse(status = 400, errorCode = "MISSING_USERNAME")
        }
        if (password.isBlank()) {
            return LoginResponse(status = 400, errorCode = "MISSING_PASSWORD")
        }
        val stored = registered[username]
            ?: return LoginResponse(status = 404, errorCode = "USER_NOT_FOUND")
        if (stored != password) {
            return LoginResponse(status = 401, errorCode = "INVALID_PASSWORD")
        }
        return LoginResponse(status = 200, token = "session-${username.hashCode().toUInt()}")
    }
}
