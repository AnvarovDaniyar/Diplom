package com.da.productservice.controller;

import static com.da.productservice.util.RandomEntityGenerator.createMainCategory;
import static com.da.productservice.util.RandomEntityGenerator.createProductInvoiceResponse;
import static com.da.productservice.util.RandomEntityGenerator.createProductListViewStaticValues;
import static com.da.productservice.util.RandomEntityGenerator.createProductRequest;
import static com.da.productservice.util.RandomEntityGenerator.createProductWithMainCategoryAndSubCategory;
import static com.da.productservice.util.RandomEntityGenerator.createSubCategory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import com.da.productservice.dto.ProductInvoiceResponse;
import com.da.productservice.dto.ProductListView;
import com.da.productservice.dto.ProductRequest;
import com.da.productservice.entity.MainCategory;
import com.da.productservice.entity.Product;
import com.da.productservice.entity.SubCategory;
import com.da.productservice.mapper.ProductMapperImpl;
import com.da.productservice.mapper.SubCategoryMapperImpl;
import com.da.productservice.repository.MainCategoryRepository;
import com.da.productservice.repository.ProductRepository;
import com.da.productservice.repository.SubCategoryRepository;
import com.da.productservice.service.MainCategoryServiceImpl;
import com.da.productservice.service.ProductServiceImpl;
import com.da.productservice.service.SubCategoryServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import({ ProductServiceImpl.class, ProductMapperImpl.class, MainCategoryServiceImpl.class, SubCategoryServiceImpl.class, SubCategoryMapperImpl.class })
class ProductControllerTest {

  @MockBean
  private MainCategoryRepository mainCategoryRepository;

  @MockBean
  private SubCategoryRepository subCategoryRespository;

  @MockBean
  private ProductRepository productRepository;

  @Autowired
  private MockMvc mockMvc;

  private static final MediaType JSON = MediaType.APPLICATION_JSON;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String PRODUCT_ID = "$.productId";
  private static final String PRODUCT_BAR_CODE = "$.productBarCode";

  @BeforeEach
  public void setUp() {

    Optional<MainCategory> mainCategory = Optional.of(createMainCategory());

    Optional<SubCategory> subCategory = Optional.of(createSubCategory());

    Product product = createProductWithMainCategoryAndSubCategory();

    ProductInvoiceResponse productInvoiceResponse = createProductInvoiceResponse();

    List<ProductListView> listOfProducts = List.of(createProductListViewStaticValues());

    PageImpl<ProductListView> pageOfProducts = new PageImpl<>(listOfProducts);

    BDDMockito.when(mainCategoryRepository.findByMainCategoryName(anyString())).thenReturn(mainCategory);

    BDDMockito.when(subCategoryRespository.findBySubCategoryName(anyString())).thenReturn(subCategory);

    BDDMockito.when(productRepository.save(any(Product.class))).thenReturn(product);

    BDDMockito.when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

    BDDMockito.when(productRepository.updateStockByBarCode(anyInt(), anyLong())).thenReturn(1);

    BDDMockito.when(productRepository.findByProductBarCodeOrProductName(anyLong(), anyString())).thenReturn(Optional.of(product));

    BDDMockito.when(productRepository.findForInvoice(anyLong(), anyString())).thenReturn(Optional.of(productInvoiceResponse));

    BDDMockito.doNothing().when(productRepository).delete(any(Product.class));

    BDDMockito.when(productRepository.getAll(any(PageRequest.class))).thenReturn(pageOfProducts);

    BDDMockito.when(productRepository.findByMainCategoryMainCategoryId(anyLong(), any(PageRequest.class))).thenReturn(pageOfProducts);

    BDDMockito.when(subCategoryRespository.findBySubCategoryName(anyString())).thenReturn(subCategory);

    BDDMockito.when(productRepository.findBySubCategory(any(SubCategory.class))).thenReturn(listOfProducts);

    BDDMockito.when(productRepository.findByProductName(anyString())).thenReturn(Optional.of(product));

    BDDMockito.when(productRepository.findByProductNameContainingIgnoreCase(anyString(), any(PageRequest.class))).thenReturn(pageOfProducts);
  }

  @Test
  public void createProduct_Return201StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(post("/products").contentType(JSON)
                                      .content(OBJECT_MAPPER.writeValueAsString(createProductRequest()))
                                      .accept(JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(JSON))
            .andExpect(jsonPath(PRODUCT_ID, Matchers.isA(Integer.class)))
            .andExpect(jsonPath(PRODUCT_BAR_CODE, Matchers.isA(Long.class)));
  }

  @Test
  public void createProduct_Return404StatusCode_WhenMainCategoryWasNotFound() throws Exception{
    BDDMockito.when(mainCategoryRepository.findByMainCategoryName(anyString())).thenReturn(Optional.empty());

    mockMvc.perform(post("/products").contentType(JSON)
                                      .content(OBJECT_MAPPER.writeValueAsString(createProductRequest())))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void createProduct_Return400StatusCode_WhenProductRequestHasInvalidFields() throws Exception{
    mockMvc.perform(post("/products").contentType(JSON)
                                      .content(OBJECT_MAPPER.writeValueAsString(new ProductRequest())))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void createProduct_Return404StatusCode_WhenSubCategoryWasNotFound() throws Exception{
    BDDMockito.when(subCategoryRespository.findBySubCategoryName(anyString())).thenReturn(Optional.empty());

    mockMvc.perform(post("/products").contentType(JSON)
                                      .content(OBJECT_MAPPER.writeValueAsString(createProductRequest())))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void updateProduct_Return200StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(put("/products/1").contentType(JSON)
                                      .content(OBJECT_MAPPER.writeValueAsString(createProductRequest()))
                                      .accept(JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(JSON))
            .andExpect(jsonPath(PRODUCT_ID, Matchers.isA(Integer.class)))
            .andExpect(jsonPath(PRODUCT_BAR_CODE, Matchers.isA(Long.class)));
  }

  @Test
  public void updateProduct_Return404StatusCode_WhenMainCategoryWasNotFound() throws Exception{
    BDDMockito.when(mainCategoryRepository.findByMainCategoryName(anyString())).thenReturn(Optional.empty());

    mockMvc.perform(put("/products/1").contentType(JSON)
                                      .content(OBJECT_MAPPER.writeValueAsString(createProductRequest())))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void updateProduct_Return404StatusCode_WhenSubCategoryWasNotFound() throws Exception{
    BDDMockito.when(subCategoryRespository.findBySubCategoryName(anyString())).thenReturn(Optional.empty());

    mockMvc.perform(put("/products/1").contentType(JSON)
                                      .content(OBJECT_MAPPER.writeValueAsString(createProductRequest())))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void updateProductStock_Return204StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(put("/products/1/stock?quantity=100"))
            .andExpect(status().isNoContent());
  }

  @Test
  public void updateProductStock_Return404StatusCode_WhenUpdateOperationsReturnsZero() throws Exception{
    BDDMockito.when(productRepository.updateStockByBarCode(anyInt(), anyLong())).thenReturn(0);

    mockMvc.perform(put("/products/1/stock?quantity=100"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void getProductResponse_Return200StatusCode_WhenSuccesful() throws Exception{
    mockMvc.perform(get("/products/responses?productBarCode=1&productName=product").accept(JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void getProductResponse_Return404StatusCode_WhenProductWasNotFound() throws Exception{
    BDDMockito.when(productRepository.findByProductBarCodeOrProductName(anyLong(), anyString())).thenReturn(Optional.empty());

    mockMvc.perform(get("/products/responses?productBarCode=1&productName=product"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void getProductForInvoice_Return200StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(get("/products/invoices?productBarCode=1&productName=product").accept(JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void getProductForInvoice_Return404StatusCode_WhenProductWasNotFound() throws Exception{
    BDDMockito.when(productRepository.findForInvoice(anyLong(), anyString())).thenReturn(Optional.empty());

    mockMvc.perform(get("/products/invoices?productBarCode=1&productName=product"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void deleteProductById_Return204StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(delete("/products/1")).andExpect(status().isNoContent());
  }

  @Test
  public void deleteProductById_Return404StatusCode_WhenProductWasNotFound() throws Exception{
    BDDMockito.when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

    mockMvc.perform(delete("/products/1")).andExpect(status().isNotFound());
  }

  @Test
  public void listAllProducts_Return200StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(get("/products?page=0&size=10").accept(JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void listAllProducts_Return404StatusCode_WhenNoProductsAreFound() throws Exception{
    BDDMockito.when(productRepository.getAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

    mockMvc.perform(get("/products?page=0&size=10"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void listProductsByMainCategoryId_Return200StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(get("/products/main-categories/1?page=0&size=10").accept(JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void listProductsByMainCategoryId_Return404StatusCode_WhenNoProductsAreFound() throws Exception{
    BDDMockito.when(productRepository.findByMainCategoryMainCategoryId(anyLong(), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

    mockMvc.perform(get("/products/main-categories/1?page=0&size=10"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void listProductsBySubCategories_Return200StatusCode_WhenSuccessful() throws Exception{
    String[] subCategories = {"SubCategory 1", "SubCategory2"};

    mockMvc.perform(get("/products/sub-categories").contentType(JSON)
                                                    .content(OBJECT_MAPPER.writeValueAsString(subCategories))
                                                    .accept(JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void listProductsBySubCategories_Return404StatusCode_WhenThatSubCategoryHasNoProducts() throws Exception{
    BDDMockito.when(productRepository.findBySubCategory(any(SubCategory.class))).thenReturn(List.of());

    String[] subCategories = {"SubCategory 1", "SubCategory2"};

    mockMvc.perform(get("/products/sub-categories").contentType(JSON)
                                                    .content(OBJECT_MAPPER.writeValueAsString(subCategories)))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void getProductByProductName_Return200StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(get("/products/names?productName=product").accept(JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void getProductByProductName_Return404StatusCode_WhenProductWasNotFound() throws Exception{
    BDDMockito.when(productRepository.findByProductName(anyString())).thenReturn(Optional.empty());

    mockMvc.perform(get("/products/names?productName=product"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void listByProductNameCoincidences_Return200StatusCode_WhenSuccessful() throws Exception{
    mockMvc.perform(get("/products/names/search?productName=product&page=0&size=10").accept(JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(JSON));
  }

  @Test
  public void listByProductNameCoincidences_Return404StatusCode_WhenNoProductsAreFound() throws Exception{
    BDDMockito.when(productRepository.findByProductNameContainingIgnoreCase(anyString(), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

    mockMvc.perform(get("/products/names/search?productName=product&page=0&size=10"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(JSON));
  }
}
