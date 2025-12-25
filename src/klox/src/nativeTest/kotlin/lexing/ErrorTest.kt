import kotlin.test.*
import lexing.*
import io.kotest.matchers.*
import io.kotest.matchers.collections.*

class ErrorTest {
  @Test
  fun itFormats() {
    val result = InterpreterErrorType.UNPARSEABLE_DOUBLE_NUMBER.fformat(13.4)
    result.shouldBe("Could not parse float '13.4', ignoring")
  }

  @Test
  fun itNoticesMissingArgs() {
    val result = InterpreterErrorType.UNPARSEABLE_DOUBLE_NUMBER.fformat()
    result.shouldBe("Could not parse float '%s', ignoring")
  }

  @Test
  fun itNoticesMissingTemplate() {
    val result = InterpreterErrorType.NEVER.fformat(13.4)
    result.shouldBe("Missing error string template for NEVER")
  }
}



