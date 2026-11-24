package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.PmsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AHB PMS", appName)
    }

    @Test
    fun `room repository starts empty and supports sample data plus json backup restore`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = PmsRepository(db.pmsDao())

            assertTrue(repo.buyers.first().isEmpty())
            assertTrue(repo.purchaseOrders.first().isEmpty())
            assertTrue(repo.productionEntries.first().isEmpty())

            repo.loadSampleFactoryData()
            val sampleBuyers = repo.buyers.first()
            val samplePOs = repo.purchaseOrders.first()
            val sampleEntries = repo.productionEntries.first()

            assertEquals(3, sampleBuyers.size)
            assertEquals(4, samplePOs.size)
            assertEquals(9, sampleEntries.size)

            val backupJson = repo.exportBackupJson()
            repo.clearAllData()
            assertTrue(repo.buyers.first().isEmpty())
            assertTrue(repo.productionEntries.first().isEmpty())

            val restoreResult = repo.restoreFromBackupJson(backupJson)
            assertTrue(restoreResult.isSuccess)
            assertEquals(3, repo.buyers.first().size)
            assertEquals(4, repo.purchaseOrders.first().size)
            assertEquals(9, repo.productionEntries.first().size)
        } finally {
            db.close()
        }
    }
}
