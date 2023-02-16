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
    fun testGetAndSetDnsTypeTransaction() = runBlocking {
        val testDnsTypeTransaction = "Test DNS Type Transaction"
        repository.setDnsTypeTransaction(testDnsTypeTransaction)
        val dnsTypeTransaction = repository.dnsTypeTransaction.first()
        assertEquals(testDnsTypeTransaction, dnsTypeTransaction)
    }

    @Test
    fun testGetAndSetProtocolChoice() = runBlocking {
        val testProtocolChoice = SettingsRepository.Protocol.Http
        repository.setProtocol(testProtocolChoice)
        val protocolChoice = repository.protocol.first()
        assertEquals(testProtocolChoice, protocolChoice)
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

    @Test
    fun testGetAndSetEncoding() = runBlocking {
        val testEncoding = "Test Encoding"
        repository.setEncoding(testEncoding)
        val encoding = repository.encoding.first()
        assertEquals(testEncoding, encoding)
    }
}

