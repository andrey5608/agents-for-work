package steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat

class LoginSteps(
    private val context: LoginContext,
    private val api: LoginApiClient,
) {

    @Given("a registered user with username {string} and password {string}")
    fun registerUser(username: String, password: String) {
        context.registeredUsers[username] = password
    }

    @When("the user logs in with username {string} and password {string}")
    fun logIn(username: String, password: String) {
        context.lastResponse = api.login(username, password, context.registeredUsers)
    }

    @Then("the response status is {int}")
    fun assertStatus(expected: Int) {
        val response = requireNotNull(context.lastResponse) { "No login response captured" }
        assertThat(response.status)
            .`as`("HTTP status of the login response")
            .isEqualTo(expected)
    }

    @And("the response contains a non-empty session token")
    fun assertTokenPresent() {
        val response = requireNotNull(context.lastResponse) { "No login response captured" }
        assertThat(response.token)
            .`as`("session token on successful login")
            .isNotBlank()
    }

    @And("the response error code is {string}")
    fun assertErrorCode(expected: String) {
        val response = requireNotNull(context.lastResponse) { "No login response captured" }
        assertThat(response.errorCode)
            .`as`("error code on failed login")
            .isEqualTo(expected)
    }
}
