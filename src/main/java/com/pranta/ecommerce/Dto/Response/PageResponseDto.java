package com.pranta.ecommerce.Dto.Response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PageResponseDto<T> {
    
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElement;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;

    public static<T> PageResponseDto<T> from(Page<T> page){
        PageResponseDto<T> dto = new PageResponseDto<>();
            dto.setContent(page.getContent());
            dto.setPageNumber(page.getNumber() + 1);
            dto.setPageSize(page.getSize());
            dto.setTotalElement(page.getTotalElements());
            dto.setTotalPages(page.getTotalPages());
            dto.setFirst(page.isFirst());
            dto.setLast(page.isLast());
            dto.setHasNext(page.hasNext());
            dto.setHasPrevious(page.hasPrevious());
        
            return dto;
    }

}
