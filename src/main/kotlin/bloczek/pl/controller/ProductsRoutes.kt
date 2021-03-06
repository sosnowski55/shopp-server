package bloczek.pl.controller

import bloczek.pl.enums.Category
import bloczek.pl.enums.Subcategory
import bloczek.pl.service.ProductService
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.*
import org.koin.java.KoinJavaComponent.inject

@Serializable
@Resource("/products")
class ProductsRoute(
    val category: Category? = null,
    val subcategory: Subcategory? = null,
    val brandId: Int? = null,
    val random: Boolean? = null,
) {

    @Serializable
    @Resource("{id}")
    class Id(val parent: ProductsRoute = ProductsRoute(), val id: Int)
}

fun Route.productsRoutes() {
    val productService: ProductService by inject(ProductService::class.java)

    get<ProductsRoute> {
        val user = call.principal<JWTPrincipal>()

        if (user != null) {
            println(user.getClaim("username", String::class))
            println(user.payload)
        }

        it.random?.run {
            call.respond(
                if (this)
                    productService.getRandomProducts()
                else
                    productService.getProducts(
                        it.category,
                        it.subcategory,
                        it.brandId
                    ))
        } ?: run {
            call.respond(productService.getProducts(
                it.category,
                it.subcategory,
                it.brandId
            ))
        }
    }

    get<ProductsRoute.Id> {
        productService.getById(it.id)?.let { product ->
            call.respond(product)
        } ?: run {
            call.respond(HttpStatusCode.NotFound, "Product not found")
        }
    }
}
