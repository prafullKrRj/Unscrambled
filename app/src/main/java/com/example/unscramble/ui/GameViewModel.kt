package com.example.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.allWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState("", false))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private lateinit var currentWord: String
    private var usedWords: MutableSet<String> = mutableSetOf()

    init {
        resetGame()
    }
    
    var userGuess by mutableStateOf("")
        private set

    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle(), isGuessedWordWrong = false)
    }
    private fun pickRandomWordAndShuffle(): String {
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        }
        else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }
    private fun updateGameState(updatedScore: Int) {
        if (_uiState.value.currentWordCount == MAX_NO_OF_WORDS) {
            _uiState.update {currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        }
        else {
            _uiState.update {currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc()
                )
            }
        }

    }
    fun checkUserGuess() {
        if (userGuess.equals(currentWord, ignoreCase = true)) {
            // User's guess is correct, increase the score
            // and call updateGameState() to prepare the game for next round
           val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore = updatedScore)
        }
        else {
            // User's guess is wrong, show an error
            _uiState.update {currentState ->
                currentState.copy(
                    isGuessedWordWrong = true
                )
            }
        }
        // Reset user guess
        updateUserGuess("")
    }
    private fun shuffleCurrentWord(currentWord: String): String {
        val tempWord = currentWord.toCharArray()
        tempWord.shuffle()
        while (String(tempWord) == currentWord) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun updateUserGuess(userGuessWord: String) {
        userGuess = userGuessWord
    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        updateUserGuess("")
    }
}