package bloczek.pl.model

import bloczek.pl.enums.Category
import bloczek.pl.enums.Subcategory
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal

object Products : Table() {
    val id: Column<Int> = integer("product_id").autoIncrement()
    val name: Column<String> = varchar("name", 255)

    val description: Column<String?> = text("description").nullable()
    val price: Column<BigDecimal> = decimal("price", 6, 2)
    val url: Column<String> = varchar("url", 255)
    val brandId: Column<Int> = reference("brand_id", Brands.id)

    val category = enumerationByName<Category>("category", 255 )
    val subcategory = enumerationByName<Subcategory>("subcategory", 255)

    override val primaryKey = PrimaryKey(id, name="PK_Products_Id")
}

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val url: String,
    val description: String? = null,
    val brand: Brand,
    val category: Category,
    val subcategory: Subcategory
)

