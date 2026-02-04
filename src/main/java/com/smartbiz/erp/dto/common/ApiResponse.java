package com.smartbiz.erp.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
	private boolean success;
	private T data;
	private String message;
	
	public static <T> ApiResponse<T> success(T data){
		return ApiResponse.<T>builder()
				.success(true)
				.data(data)
				.message("요청이 성공적으로 처리되었습니다.")
				.build();
	}

	public static <T> ApiResponse<T> success(T data, String message) {
	    return ApiResponse.<T>builder()
	            .success(true)
	            .data(data)
	            .message(message)
	            .build();
	}
	
	public static <T> ApiResponse<T> fail(String message){
		return ApiResponse.<T>builder()
				.success(false)
				.data(null)
				.message(message)
				.build();
	}
}