package com.pranta.ecommerce.Util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.pranta.ecommerce.Exceptions.InvalidRequestException;

@Component
public class PaginationHelper {
    private static final int MAX_PAGE_SIZE = 100;

    public Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        if (page < 1) {
            throw new InvalidRequestException("Page number must be 1 or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidRequestException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return PageRequest.of(page - 1, size, sort);
    }
}
