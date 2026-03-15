package com.ecommerce.product_service.service;

import com.ecommerce.product_service.model.request.CreateCategoryRequest;
import com.ecommerce.product_service.model.request.UpdateCategoryRequest;
import com.ecommerce.product_service.model.response.CategoryResponse;
import com.ecommerce.product_service.model.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getCategoryTree();
    CategoryResponse       getCategoryById(String id);
    Page<ProductResponse>  getProductsByCategory(String id, int page, int size);
    CategoryResponse       createCategory(CreateCategoryRequest request);
    CategoryResponse       updateCategory(String id, UpdateCategoryRequest request);
    void                   deleteCategory(String id);
    CategoryResponse       reorderCategory(String id, Integer displayOrder);
}