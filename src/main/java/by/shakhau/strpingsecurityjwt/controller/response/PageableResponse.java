package by.shakhau.strpingsecurityjwt.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
@Getter
public class PageableResponse<T> {

    private Collection<T> content;
    private Pageable<T> pageable;
}
