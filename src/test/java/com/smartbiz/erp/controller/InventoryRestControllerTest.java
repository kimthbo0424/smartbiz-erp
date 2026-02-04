package com.smartbiz.erp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.smartbiz.erp.dto.inventory.InventoryCancelCheckResponseDto;
import com.smartbiz.erp.dto.inventory.InventoryMoveResponseDto;
import com.smartbiz.erp.dto.inventory.InventoryResponseDto;
import com.smartbiz.erp.dto.inventory.InventoryTransactionResponseDto;
import com.smartbiz.erp.entity.InventoryTransactionType;
import com.smartbiz.erp.entity.Warehouse;
import com.smartbiz.erp.repository.WarehouseRepository;
import com.smartbiz.erp.service.InventoryService;

import jakarta.persistence.EntityNotFoundException;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.BDDMockito.willDoNothing;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;


@SpringBootTest(
	    properties = {
	        "spring.thymeleaf.enabled=false",
	        "spring.mvc.throw-exception-if-no-handler-found=true"
	    }
	)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@ActiveProfiles("test")
@Transactional
class InventoryRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WarehouseRepository warehouseRepository;
    
    @MockBean
    InventoryService inventoryService;

    private Long warehouseId;

    @BeforeEach
    void setUp() {
        Warehouse warehouse = new Warehouse();
        warehouse.setCode("WH-TEST-001");
        warehouse.setName("테스트 창고");
        warehouse.setLocation("서울");
        warehouse.setManager("관리자");
        warehouse.setCapacity(1000);
        warehouse.setIsActive(true);

        warehouseId = warehouseRepository.save(warehouse).getId();
    }

    @Test
    void 재고_현황_조회_API_문서화() throws Exception {

        Page<InventoryResponseDto> emptyPage =
                new PageImpl<>(List.of());

        given(inventoryService.getInventoryPageResponse(anyLong(), any()))
                .willReturn(emptyPage);

        mockMvc.perform(
                get("/api/inventory")
                        .param("warehouseId", warehouseId.toString())
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(
    	    document("inventory-get",
    	        responseFields(
    	            fieldWithPath("success").description("요청 성공 여부"),
    	            fieldWithPath("message").description("응답 메시지"),
    	            fieldWithPath("data").description("페이지 응답 데이터"),

    	            fieldWithPath("data.totalElements").description("전체 요소 수"),
    	            fieldWithPath("data.totalPages").description("전체 페이지 수"),
    	            fieldWithPath("data.size").description("페이지 크기"),
    	            fieldWithPath("data.number").description("현재 페이지 번호"),
    	            fieldWithPath("data.numberOfElements").description("현재 페이지 요소 수"),
    	            fieldWithPath("data.first").description("첫 페이지 여부"),
    	            fieldWithPath("data.last").description("마지막 페이지 여부"),
    	            fieldWithPath("data.empty").description("비어있는지 여부"),

    	            subsectionWithPath("data.pageable").ignored(),
    	            subsectionWithPath("data.sort").ignored(),
    	            subsectionWithPath("data.content").description("재고 목록")
    	        )
    	    )
    	);
    }

    @Test
    void 재고_입고_API_문서화() throws Exception {

        String body = """
            {
              "warehouseId": %d,
              "productId": 1,
              "quantity": 10
            }
            """.formatted(warehouseId);

        mockMvc.perform(
                post("/api/inventory/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andDo(
            document("inventory-in",
                requestFields(
                    fieldWithPath("warehouseId").description("입고 창고 ID"),
                    fieldWithPath("productId").description("상품 ID"),
                    fieldWithPath("quantity").description("입고 수량")
                ),
                responseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("data").description("응답 데이터 (없음)").optional(),
                    fieldWithPath("message").description("응답 메시지")
                )
            )
        );
    }
    
    @Test
    void 재고_출고_API_문서화() throws Exception {

        String body = """
            {
              "warehouseId": %d,
              "productId": 1,
              "quantity": 5
            }
            """.formatted(warehouseId);

        mockMvc.perform(
                post("/api/inventory/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andDo(
            document("inventory-out",
                requestFields(
                    fieldWithPath("warehouseId").description("출고 창고 ID"),
                    fieldWithPath("productId").description("상품 ID"),
                    fieldWithPath("quantity").description("출고 수량")
                )
            )
        );
    }
    
    @Test
    void 재고_이동_API_문서화() throws Exception {

        String body = """
            {
              "fromWarehouseId": %d,
              "toWarehouseId": %d,
              "productId": 1,
              "quantity": 5
            }
            """.formatted(warehouseId, warehouseId);

        mockMvc.perform(
                post("/api/inventory/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andDo(
            document("inventory-move",
                requestFields(
                    fieldWithPath("fromWarehouseId").description("출발 창고 ID"),
                    fieldWithPath("toWarehouseId").description("도착 창고 ID"),
                    fieldWithPath("productId").description("상품 ID"),
                    fieldWithPath("quantity").description("이동 수량")
                )
            )
        );
    }
    
    @Test
    void 재고_조정_API_문서화() throws Exception {

        String body = """
            {
              "warehouseId": %d,
              "productId": 1,
              "adjustQuantity": -3,
              "reason": "재고 실사 차이"
            }
            """.formatted(warehouseId);

        mockMvc.perform(
                post("/api/inventory/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andDo(
            document("inventory-adjust",
                requestFields(
                    fieldWithPath("warehouseId").description("창고 ID"),
                    fieldWithPath("productId").description("상품 ID"),
                    fieldWithPath("adjustQuantity").description("조정 수량 (음수 가능)"),
                    fieldWithPath("reason").description("조정 사유")
                )
            )
        );
    }
    
    @Test
    void 재고_취소_API_문서화() throws Exception {

        String body = """
            {
              "transactionId": 1,
              "reason": "입력 오류"
            }
            """;

        mockMvc.perform(
                post("/api/inventory/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isOk())
        .andDo(
            document("inventory-cancel",
                requestFields(
                    fieldWithPath("transactionId").description("취소할 재고 트랜잭션 ID"),
                    fieldWithPath("reason").description("취소 사유")
                )
            )
        );
    }
    
    @Test
    void 재고_취소_가능_여부_API_문서화() throws Exception {

        InventoryCancelCheckResponseDto response =
                new InventoryCancelCheckResponseDto(true, null);

        given(inventoryService.checkCancelable(1L))
                .willReturn(response);

        mockMvc.perform(
                get("/api/inventory/transactions/{id}/cancelable", 1L)
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(
            document("inventory-cancel-check",
                responseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("message").description("응답 메시지"),
                    fieldWithPath("data.cancelable").description("취소 가능 여부"),
                    fieldWithPath("data.reason").description("취소 불가 사유 (가능한 경우 null)").optional()
                )
            )
        );
    }
    
    @Test
    void 재고_현황_조회_content_API_문서화() throws Exception {

        InventoryResponseDto item =
                InventoryResponseDto.builder()
                        .productId(1L)
                        .warehouseId(1L)
                        .quantity(100)
                        .safetyStockQty(20)
                        .productName("상품A")
                        .build();

        Page<InventoryResponseDto> page =
                new PageImpl<>(List.of(item));

        given(inventoryService.getInventoryPageResponse(anyLong(), any()))
                .willReturn(page);

        mockMvc.perform(
                get("/api/inventory")
                        .param("warehouseId", "1")
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(
    	    document("inventory-get-content-detail",
    	        relaxedResponseFields(
    	            fieldWithPath("data.content[].productId").description("상품 ID"),
    	            fieldWithPath("data.content[].warehouseId").description("창고 ID"),
    	            fieldWithPath("data.content[].quantity").description("재고 수량"),
    	            fieldWithPath("data.content[].safetyStockQty").description("안전 재고 수량"),
    	            fieldWithPath("data.content[].productName").description("상품명")
    	        )
    	    )
    	);
    }
    
    @Test
    void 재고_입고_유효성_오류_API_문서화() throws Exception {

        String body = """
            {
              "warehouseId": %d,
              "productId": 1,
              "quantity": 0
            }
            """.formatted(warehouseId);

        mockMvc.perform(
                post("/api/inventory/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isBadRequest())
        .andDo(
            document("inventory-in-400",
                responseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("message").description("유효성 오류 메시지"),
                    fieldWithPath("data").description("항상 null").optional()
                )
            )
        );
    }
    
    @Test
    void 재고_출고_404_API_문서화() throws Exception {

        doThrow(new EntityNotFoundException("재고를 찾을 수 없습니다."))
                .when(inventoryService)
                .stockOut(any());

        String body = """
            {
              "warehouseId": %d,
              "productId": 999,
              "quantity": 1
            }
            """.formatted(warehouseId);

        mockMvc.perform(
                post("/api/inventory/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isNotFound())
        .andDo(
            document("inventory-out-404",
                responseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("message").description("재고 없음 오류 메시지"),
                    fieldWithPath("data").description("항상 null").optional()
                )
            )
        );
    }
    
    @Test
    void 재고_출고_재고부족_409_API_문서화() throws Exception {

        willThrow(new IllegalStateException("재고 부족"))
                .given(inventoryService)
                .stockOut(any());

        String body = """
            {
              "warehouseId": %d,
              "productId": 1,
              "quantity": 999
            }
            """.formatted(warehouseId);

        mockMvc.perform(
                post("/api/inventory/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(body)
        )
        .andExpect(status().isConflict())
        .andDo(
            document("inventory-out-409",
                responseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("message").description("재고 부족 오류 메시지"),
                    fieldWithPath("data").description("항상 null").optional()
                )
            )
        );
    }
    
    @Test
    void 재고_조정_이력_조회_API_문서화() throws Exception {

        InventoryTransactionResponseDto item =
                InventoryTransactionResponseDto.builder()
                        .id(1L)
                        .productId(1L)
                        .productName("상품A")
                        .warehouseId(1L)
                        .warehouseName("테스트 창고")
                        .type(InventoryTransactionType.ADJUST)
                        .quantity(-3)
                        .reason("재고 실사 차이")
                        .relatedClientId(null)
                        .occurredAt(LocalDateTime.now())
                        .build();

        Page<InventoryTransactionResponseDto> page =
                new PageImpl<>(List.of(item));

        given(inventoryService.getAdjustTransactionPage(anyLong(), any()))
                .willReturn(page);

        mockMvc.perform(
                get("/api/inventory/moves/adjust")
                        .param("warehouseId", "1")
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(
            document("inventory-adjust-history",
                relaxedResponseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("message").description("응답 메시지"),

                    fieldWithPath("data.content[].id").description("트랜잭션 ID"),
                    fieldWithPath("data.content[].productId").description("상품 ID"),
                    fieldWithPath("data.content[].productName").description("상품명"),
                    fieldWithPath("data.content[].warehouseId").description("창고 ID"),
                    fieldWithPath("data.content[].warehouseName").description("창고명"),
                    fieldWithPath("data.content[].type").description("트랜잭션 유형"),
                    fieldWithPath("data.content[].quantity").description("조정 수량"),
                    fieldWithPath("data.content[].reason").description("조정 사유"),
                    fieldWithPath("data.content[].relatedClientId").optional().description("관련 거래처 ID"),
                    fieldWithPath("data.content[].occurredAt").description("발생 일시")
                )
            )
        );
    }
    
    @Test
    void 재고_이동_이력_조회_API_문서화() throws Exception {

        InventoryMoveResponseDto item =
                InventoryMoveResponseDto.builder()
                        .transactionId(1L)
                        .productId(1L)
                        .productName("상품A")
                        .fromWarehouseId(1L)
                        .fromWarehouseName("서울 창고")
                        .toWarehouseId(2L)
                        .toWarehouseName("부산 창고")
                        .quantity(5)
                        .reason("창고 이동")
                        .relatedClientId(null)
                        .occurredAt(LocalDateTime.now())
                        .build();

        Page<InventoryMoveResponseDto> page =
                new PageImpl<>(List.of(item));

        given(inventoryService.getMoveTransactionPage(any()))
                .willReturn(page);

        mockMvc.perform(
                get("/api/inventory/moves/transfer")
                        .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(
            document("inventory-move-history",
                relaxedResponseFields(
                    fieldWithPath("success").description("요청 성공 여부"),
                    fieldWithPath("message").description("응답 메시지"),

                    fieldWithPath("data.content[].transactionId").description("트랜잭션 ID"),
                    fieldWithPath("data.content[].productId").description("상품 ID"),
                    fieldWithPath("data.content[].productName").description("상품명"),

                    fieldWithPath("data.content[].fromWarehouseId").description("출발 창고 ID"),
                    fieldWithPath("data.content[].fromWarehouseName").description("출발 창고명"),
                    fieldWithPath("data.content[].toWarehouseId").description("도착 창고 ID"),
                    fieldWithPath("data.content[].toWarehouseName").description("도착 창고명"),

                    fieldWithPath("data.content[].quantity").description("이동 수량"),
                    fieldWithPath("data.content[].reason").description("이동 사유"),
                    fieldWithPath("data.content[].relatedClientId").optional().description("관련 거래처 ID"),
                    fieldWithPath("data.content[].occurredAt").description("발생 일시")
                )
            )
        );
    }
}