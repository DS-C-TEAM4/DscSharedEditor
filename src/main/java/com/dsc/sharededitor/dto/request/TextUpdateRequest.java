package com.dsc.sharededitor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TextUpdateRequest {
    private int position;
    private int length;
    private String text;
}
