package fr.chatavion.client

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import fr.chatavion.client.datastore.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

    private lateinit var context: Context
    private lateinit var repository: SettingsRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = SettingsRepository(context)
    }

    @Test
    fun testGetAndSetTheme() = runBlocking {
        val testTheme = SettingsRepository.Theme.Dark
        repository.setTheme(testTheme)
        val theme = repository.theme.first()
        assertEquals(testTheme, theme)
    }

    @Test
    fun testGetAndSetPseudo() = runBlocking {
        val testPseudo = "Test User"
        repository.setPseudo(testPseudo)
        val pseudo = repository.pseudo.first()
        assertEquals(testPseudo, pseudo)
    }

    @Test
    fun testGetAndSetLanguage() = runBlocking {
        val testLanguage = SettingsRepository.Language.English
        repository.setLanguage(testLanguage)
        val language = repository.language.first()
        assertEquals(testLanguage, language)
    }

    @Test
    fun testGetAndSetRefreshTime() = runBlocking {
        val testRefreshTime = 60L
        repository.setRefreshTime(testRefreshTime)
        val refreshTime = repository.refreshTime.first()
        assertEquals(testRefreshTime, refreshTime)
    }

    @Test
    fun testGetAndSetHistoryLoading() = runBlocking {
        val testHistoryLoading = 10
        repository.setHistoryLoading(testHistoryLoading)
        val historyLoading = repository.historyLoading.first()
        assertEquals(testHistoryLoading, historyLoading)
    }
}

