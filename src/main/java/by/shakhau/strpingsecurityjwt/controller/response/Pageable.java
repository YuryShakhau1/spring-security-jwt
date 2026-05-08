package by.shakhau.strpingsecurityjwt.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Pageable<T> {

    private Integer pageNumber;
    private Integer pageSize;
    private Integer totalPages;
}
