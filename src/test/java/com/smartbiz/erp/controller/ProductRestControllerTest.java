package com.smartbiz.erp.controller;

import com.smartbiz.erp.entity.Category;
import com.smartbiz.erp.entity.Product;
import com.smartbiz.erp.entity.enums.ProductStatus;
import com.smartbiz.erp.repository.CategoryRepository;
import com.smartbiz.erp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@ActiveProfiles("test")
@Transactional   // ⭐ 핵심: 테스트 끝나면 자동 롤백
class ProductRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private Long productId;

    @BeforeEach
    void setUp() {
        // ❌ deleteAll() 절대 사용하지 않음 (Soft Delete 구조)

        Category category = categoryRepository.save(
                Category.builder()
                        .name("전자제품")
                        .build()
        );

        Product product = productRepository.save(
                Product.builder()
                        .name("테스트 상품")
                        .sku("TEST-001")
                        .category(category)
                        .status(ProductStatus.ACTIVE)
                        .isActive(true)
                        .build()
        );

        productId = product.getId();
    }

    @Test
    void 상품_단건_조회_API_문서화() throws Exception {

        mockMvc.perform(
                        get("/api/products/{id}", productId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(
                        document("product-get-one",
                                responseFields(
                                        fieldWithPath("success").description("요청 성공 여부"),
                                        fieldWithPath("message").description("응답 메시지"),

                                        fieldWithPath("data.id").description("상품 ID"),
                                        fieldWithPath("data.name").description("상품명"),
                                        fieldWithPath("data.sku").description("SKU"),
                                        fieldWithPath("data.barcode").optional().description("바코드"),
                                        fieldWithPath("data.costPrice").optional().description("원가"),
                                        fieldWithPath("data.unitPrice").optional().description("판매가"),
                                        fieldWithPath("data.status").description("상품 상태"),
                                        fieldWithPath("data.isActive").description("활성 여부"),
                                        fieldWithPath("data.categoryId").description("카테고리 ID"),
                                        fieldWithPath("data.categoryName").description("카테고리명")
                                )
                        )
                );
    }
}