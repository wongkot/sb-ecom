package com.ecommerce.project.service;

import com.ecommerce.project.payload.PaginationResponse;
import com.ecommerce.project.payload.ProductDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    PaginationResponse<ProductDTO> getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    PaginationResponse<ProductDTO> searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    PaginationResponse<ProductDTO> searchByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ProductDTO addProduct(Long categoryId, ProductDTO product);
    ProductDTO updateProduct(Long productId, ProductDTO product);
    ProductDTO deleteProduct(Long productId);
    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;
}
