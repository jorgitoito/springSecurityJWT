package com.example.security_token.controller;

import com.example.security_token.controller.dto.ProductRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ') or hasRole('ADMIN')")
    public ResponseEntity<String> getAllProducts() {
        return ResponseEntity.ok("Lista de productos (requiere PRODUCT_READ o ROLE_ADMIN)");
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_WRITE') or hasRole('ADMIN')")
    public ResponseEntity<String> createProduct(@RequestBody ProductRequest productRequest) {
        return ResponseEntity.ok("Producto creado: " + productRequest.getName() +
                " (requiere PRODUCT_WRITE o ROLE_ADMIN)");
    }
}
