package com.matheus.gastos.data.entity

import androidx.room.*
import kotlinx.serialization.Serializable

@Entity(tableName = "categories")
@Serializable
data class Category(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String,
    val color: Int, // ARGB color int
    val icon: String, // Material icon name
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
) {
    @Transient
    var subcategories: List<Subcategory> = emptyList()
}

@Entity(
    tableName = "subcategories",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
@Serializable
data class Subcategory(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val categoryId: Long,
    val name: String,
    val sortOrder: Int = 0
)

@Entity(tableName = "credit_cards")
@Serializable
data class CreditCard(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val name: String,
    val color: Int, // ARGB color int
    val limit: Double,
    val closingDay: Int, // 1-28
    val dueDay: Int, // 1-28
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Subcategory::class,
            parentColumns = ["id"],
            childColumns = ["subcategoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CreditCard::class,
            parentColumns = ["id"],
            childColumns = ["creditCardId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("date"),
        Index("categoryId"),
        Index("creditCardId"),
        Index("isInstallment"),
        Index("installmentGroupId")
    ]
)
@Serializable
data class Transaction(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val amount: Double,
    val description: String,
    val date: Long, // timestamp in milliseconds
    val categoryId: Long?,
    val subcategoryId: Long?,
    val creditCardId: Long?,
    val isExpense: Boolean = true, // true = expense, false = income
    val isInstallment: Boolean = false,
    val installmentNumber: Int = 1, // 1-based
    val totalInstallments: Int = 1,
    val installmentGroupId: String? = null, // UUID to group installments
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budget_limits")
@Serializable
data class BudgetLimit(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val categoryId: Long,
    val amount: Double,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val isActive: Boolean = true,
    val warningThreshold: Double = 0.8 // 80% warning
) {
    @Transient
    var category: Category? = null
}

@Entity(tableName = "app_settings")
@Serializable
data class AppSettings(
    @PrimaryKey val key: String,
    val value: String
)

enum class BudgetPeriod(@Serializable(with = BudgetPeriodSerializer::class) val value: String) {
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    YEARLY("yearly")
}

class BudgetPeriodSerializer : kotlinx.serialization.KSerializer<BudgetPeriod> {
    override val descriptor = kotlinx.serialization.descriptors.PrimitiveSerialDescriptor("BudgetPeriod", kotlinx.serialization.descriptors.PrimitiveKind.STRING)

    override fun serialize(encoder: kotlinx.serialization.Encoding, value: BudgetPeriod) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: kotlinx.serialization.Decoding): BudgetPeriod {
        return BudgetPeriod.valueOf(decoder.decodeString().uppercase())
    }
}