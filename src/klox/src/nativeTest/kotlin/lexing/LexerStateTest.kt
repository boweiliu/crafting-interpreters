import kotlin.test.*
import lexing.*

import io.kotest.matchers.*
import io.kotest.matchers.collections.*


class LexerStateTest {
  class computeLexerActionDatasTest {
    val startStateData = LexerStateData()

    @Test
    fun itJustRuns() {
      computeLexerActionDatas(startStateData, null, null, null)
    }

    @Test
    fun itGivesErrorsOnInvalidState() {
      val result = computeLexerActionDatas(
        LexerStateData(LexerState.EOF), null, null, null
      )
      result.stuff.shouldHaveSize(1)
      result.stuff.map { it.ty }.shouldBe(listOf("Er"))
    }

    fun simulateActions(
      charTriples: List<Triple<Char?, Char?, Char?>>,
      startingState: LexerStateData = LexerStateData()
    ): Pair<List<LDatum>, LexerStateData> {
      var state = startingState
      var acc = mutableListOf<LDatum>()
      charTriples.forEach { (a, b, c) ->
        val datas = computeLexerActionDatas(state, a, b, c).stuff
        datas.forEach { acc.add(it) }
        datas.forEach {
          if (it is LDatum.Tr)
            state = LexerStateData(it.tr.st)
          else if (it is LDatum.UpC)
            state.builder.append(it.up.ch)
          else if (it is LDatum.UpE)
            state.didError = it.up.didError
        }
      }
      return Pair(acc, state)
    }
    
    fun LexerStateData.serialize(): List<String> =
     listOf(this.state.toString(), this.builder.toString(), this.didError.toString())

    @Test
    fun itShouldChangeState() {
      val (datas, resultState) = simulateActions(listOf<Triple<Char?,Char?,Char?>>(
        Triple(null, null, null)
      ))
      datas.shouldHaveSize(2)
      datas.map { it.ty }.shouldBe(listOf("Tr", "To"))
      resultState.serialize().shouldBe(listOf("EOF", "", "false"))
    }

    @Test
    fun itShouldHandleConcludingStrings() {
      val (datas, resultState) = simulateActions(listOf<Triple<Char?,Char?,Char?>>(
        Triple('"', null, null),
      ), LexerStateData(LexerState.STRING, StringBuilder("\""), false))
      datas.shouldHaveSize(2)
      datas.map { it.ty }.shouldBe(listOf("Up", "Tr"))
      resultState.serialize().shouldBe(listOf("DEFAULT", "", "false"))
    }

    @Test
    fun itHandlesDoubleEqual() {
      val (datas, resultState) = simulateActions(listOf<Triple<Char?,Char?,Char?>>(
        Triple('=', '=', null),
        Triple('=', null, null),
      ))
      datas.shouldHaveSize(3)
      datas.map { it.ty }.shouldBe(listOf("Tr", "To", "Tr"))
      resultState.serialize().shouldBe(listOf("DEFAULT", "", "false"))
    }

    @Test
    fun itHandlesDigit() {
      val (datas, resultState) = simulateActions(listOf<Triple<Char?,Char?,Char?>>(
        Triple('1', '2', '3'),
      ))
      datas.shouldHaveSize(2)
      datas.map { it.ty }.shouldBe(listOf("Tr", "Up"))
      resultState.serialize().shouldBe(listOf("NUMBER", "1", "false"))
    }
  }
}


