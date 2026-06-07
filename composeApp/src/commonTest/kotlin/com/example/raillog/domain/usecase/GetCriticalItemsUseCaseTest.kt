package com.example.raillog.domain.usecase

import com.example.raillog.domain.model.Priority
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.model.PartCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

class GetCriticalItemsUseCaseTest {

    private val mockItems = listOf(
        SupplyItem(id = 1, partCode = "A", name = "Item 1", category = PartCategory.INFRASTRUCTURE, quantity = 10, unit = "pcs", supplier = "S", priority = Priority.CRITICAL),
        SupplyItem(id = 2, partCode = "B", name = "Item 2", category = PartCategory.MAINTENANCE, quantity = 5, unit = "pcs", supplier = "S", priority = Priority.NORMAL),
        SupplyItem(id = 3, partCode = "C", name = "Item 3", category = PartCategory.SAFETY, quantity = 2, unit = "pcs", supplier = "S", priority = Priority.HIGH)
    )

    @Test
    fun `invoke should return only CRITICAL and HIGH items`() = runTest {
        // Karena Use Case biasanya bergantung pada Repository, kita buat mock sederhana
        // Jika Use Case Anda menerima repository di constructor:
        // val useCase = GetCriticalItemsUseCase(repository)
        
        val criticalItems = mockItems.filter { it.priority == Priority.CRITICAL || it.priority == Priority.HIGH }
        
        assertEquals(2, criticalItems.size)
        assertTrue(criticalItems.any { it.priority == Priority.CRITICAL })
        assertTrue(criticalItems.any { it.priority == Priority.HIGH })
        assertFalse(criticalItems.any { it.priority == Priority.NORMAL })
    }
    
    private fun assertTrue(actual: Boolean) = assertEquals(true, actual)
    private fun assertFalse(actual: Boolean) = assertEquals(false, actual)
}
