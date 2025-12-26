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

class StacktraceTest {
  @Test
  fun itJustRuns() {
    getCurrentStacktrace()
  }
}

class FormatTest {
  @Test
  fun itCorrectlyReplaces() {
    "hello %s i am %s".fformat(1, 2).shouldBe("hello 1 i am 2")
  }
}
