package com.ecommerce.project.service;

import com.ecommerce.project.payload.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaginationService {
    public Pageable getPageable(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = Sort.by(
                sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy
        );
        return PageRequest.of(pageNumber, pageSize, sortByAndOrder);
    }

    public <T> PaginationResponse<T> getPaginationResponse(List<T> content, Page<?> pageData) {
        return new PaginationResponse<T>(
                content,
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.isLast()
        );
    }
}
