package com.example.security_token.api.product;

import com.example.security_token.api.product.dto.ProductRequest;
import com.example.security_token.api.product.exception.ProductException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Operation(summary = "get All Products", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ') or hasRole('ADMIN')")
    public ResponseEntity<String> getAllProducts() {
        try {
            // Tu lógica para obtener productos
            return ResponseEntity.ok("Lista de productos (requiere PRODUCT_READ o ROLE_ADMIN)");
        } catch (Exception e) {
            // Si quieres manejar la excepción aquí en lugar de en el GlobalExceptionHandler
            throw new ProductException("Error al obtener los productos", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Create Product", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        try {
            // Validar el producto
            if (productRequest.getName() == null || productRequest.getName().trim().isEmpty()) {
                throw new ProductException("El nombre del producto no puede estar vacío", HttpStatus.BAD_REQUEST);
            }

            // Tu lógica para crear productos
            return ResponseEntity.ok("Producto creado: " + productRequest.getName() +
                    " (requiere PRODUCT_WRITE o ROLE_ADMIN)");
        } catch (ProductException e) {
            // Deja que el GlobalExceptionHandler maneje esta excepción
            throw e;
        } catch (Exception e) {
            throw new ProductException("Error al crear el producto", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get Product by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_READ') or hasRole('ADMIN')")
    public ResponseEntity<String> getProductById(@PathVariable Long id) {
        // Ejemplo de cómo utilizar la excepción personalizada
        if (id <= 0) {
            throw new ProductException("ID de producto inválido", HttpStatus.BAD_REQUEST);
        }

        // Simular producto no encontrado
        if (id > 1000) {
            throw new jakarta.persistence.EntityNotFoundException("Producto con ID " + id + " no encontrado");
        }

        return ResponseEntity.ok("Producto con ID: " + id);
    }
}