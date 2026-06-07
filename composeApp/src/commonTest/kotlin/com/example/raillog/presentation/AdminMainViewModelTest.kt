package com.example.raillog.presentation

import com.example.raillog.domain.model.PartCategory
import com.example.raillog.domain.model.Priority
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.model.SupplyStatus
import com.example.raillog.domain.repository.SupplyRepository
import com.example.raillog.presentation.screens.admin_main.AdminMainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AdminMainViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test filtering by search query`() = runTest {
        val items = listOf(
            createMockItem(1, "Bantalan Beton"),
            createMockItem(2, "Rel R54")
        )
        
        val mockRepo = object : EmptySupplyRepo() {
            override fun getAllItems() = flowOf(items)
        }
        
        val viewModel = AdminMainViewModel(mockRepo)
        
        // Agar StateFlow (WhileSubscribed) aktif, kita harus mengoleksinya
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            viewModel.filteredPendingItems.collect()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Test Search "Rel"
        viewModel.updateSearchQuery("Rel")
        
        // Maju melampaui debounce 300ms
        testDispatcher.scheduler.advanceTimeBy(400)
        testDispatcher.scheduler.runCurrent()
        
        val filtered = viewModel.filteredPendingItems.value
        assertEquals(1, filtered.size, "Seharusnya ada 1 item yang cocok dengan 'Rel'")
        assertTrue(filtered.first().name.contains("Rel"))
    }

    @Test
    fun `test filtering by status`() = runTest {
        val items = listOf(
            createMockItem(1, "A", SupplyStatus.PENDING),
            createMockItem(2, "B", SupplyStatus.VERIFIED)
        )
        
        val mockRepo = object : EmptySupplyRepo() {
            override fun getAllItems() = flowOf(items)
        }
        
        val viewModel = AdminMainViewModel(mockRepo)
        
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testDispatcher.scheduler)) {
            viewModel.filteredPendingItems.collect()
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Filter status VERIFIED (index 2 di AdminMainViewModel)
        viewModel.updateSelectedFilter(2)
        
        // Debounce tetap berlaku karena combine dipicu ulang
        testDispatcher.scheduler.advanceTimeBy(400)
        testDispatcher.scheduler.runCurrent()
        
        val filtered = viewModel.filteredPendingItems.value
        assertEquals(1, filtered.size, "Seharusnya ada 1 item dengan status VERIFIED")
        assertEquals(SupplyStatus.VERIFIED, filtered.first().status)
    }

    private fun createMockItem(id: Long, name: String, status: SupplyStatus = SupplyStatus.PENDING) = SupplyItem(
        id = id, partCode = "CODE-$id", name = name, category = PartCategory.INFRASTRUCTURE,
        quantity = 10, unit = "unit", supplier = "S", status = status, priority = Priority.NORMAL
    )
}

open class EmptySupplyRepo : SupplyRepository {
    override fun getAllItems() = flowOf(emptyList<SupplyItem>())
    override fun getItemById(id: Long) = flowOf(null)
    override suspend fun insertItem(item: SupplyItem) {}
    override suspend fun updateItem(item: SupplyItem) {}
    override suspend fun deleteItem(id: Long) {}
    override suspend fun updateStatus(id: Long, status: SupplyStatus) {}
    override suspend fun saveDraft(draftId: String, projectTitle: String, currentStep: Int, lastUpdated: Long, formStateJson: String) {}
    override fun getAllDrafts() = flowOf(emptyList<com.example.raillog.domain.model.DraftItem>())
    override suspend fun deleteDraft(draftId: String) {}
}
